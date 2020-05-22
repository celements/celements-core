package com.celements.docform;

import static com.google.common.collect.ImmutableMap.*;
import static org.apache.commons.lang.BooleanUtils.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.model.util.ReferenceSerializationMode;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

@Component("docform")
public class DocFormScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocFormScriptService.class);

  private static final String DOC_FORM_COMMAND_CTX_KEY = "com.celements.DocFormCommand";

  @Requirement
  private Execution execution;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  private XWikiContext getContext() {
    return context.getXWikiContext();
  }

  public Map<String, Set<DocumentReference>> updateAndSaveDocFromMap(
      DocumentReference docRef, Map<String, ?> map) {
    try {
      docRef = Optional.ofNullable(docRef).orElseGet(() -> context.getCurrentDocRef().get());
      map = Optional.ofNullable(map).orElseGet(Collections::emptyMap);
      DocFormRequestKeyParser parser = new DocFormRequestKeyParser(docRef);
      List<DocFormRequestParam> requestParams = parser.parseParameterMap(map);
      IDocForm docForm = getDocFormCommand(docRef);
      if (hasEditOnAllDocs(requestParams)) {
        docForm.updateDocs(requestParams);
      }
      return docForm.getResponseMap(requestParams)
          .entrySet().stream()
          .collect(toImmutableMap(entry -> entry.getKey().name(), Entry::getValue));
    } catch (Exception exc) {
      LOGGER.error("updateAndSaveDocFromMap: failed for map [{}]", map, exc);
      return ImmutableMap.of();
    }
  }

  public Map<String, Set<DocumentReference>> updateAndSaveDocFromRequest() {
    return updateAndSaveDocFromRequest(null);
  }

  public Map<String, Set<DocumentReference>> updateAndSaveDocFromRequest(DocumentReference docRef) {
    return updateAndSaveDocFromMap(docRef, getRequestParameterMap());
  }

  @SuppressWarnings("unchecked")
  private <T> Map<String, T> getRequestParameterMap() {
    return context.getRequest().transform(XWikiRequest::getParameterMap)
        .or(Collections.emptyMap());
  }

  boolean hasEditOnAllDocs(List<DocFormRequestParam> requestParams) {
    return requestParams.stream()
        .map(DocFormRequestParam::getDocRef)
        .distinct()
        .filter(docRef -> modelAccess.exists(docRef) || isCreateAllowed())
        .allMatch(docRef -> rightsAccess.hasAccessLevel(docRef, EAccessLevel.EDIT));
  }

  public boolean isCreateAllowed() {
    return toBoolean(context.getRequestParameter("createIfNotExists").or(""));
  }

  private IDocForm getDocFormCommand(DocumentReference docRef) {
    return (IDocForm) getContext().computeIfAbsent(DOC_FORM_COMMAND_CTX_KEY + "_" +
        modelUtils.serializeRef(docRef, ReferenceSerializationMode.GLOBAL),
        key -> Utils.getComponent(IDocForm.class)
            .initialize(docRef, isCreateAllowed()));
  }
}
