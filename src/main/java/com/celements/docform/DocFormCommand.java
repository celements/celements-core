/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.docform;

import static com.celements.model.util.References.*;
import static com.google.common.base.Predicates.*;
import static com.google.common.collect.ImmutableMap.*;
import static java.util.stream.Collectors.*;
import static org.xwiki.component.descriptor.ComponentInstantiationStrategy.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.copydoc.ICopyDocumentRole;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.XDocumentFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.util.ModelUtils;
import com.celements.web.classes.oldcore.XWikiDocumentClass;
import com.celements.web.plugin.cmd.AddTranslationCommand;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * DocFormCommand handles validation of a request with document/object fields and ensures
 * that they are correctly prepared for save. IMPORTANT: use exactly ONE instance of this
 * class for each XWikiContext (each request).
 *
 * @author Fabian Pichler <fabian.pichler@synventis.com>
 * @author Marc Sladek <marc@sladek.me>
 */
@Component
@NotThreadSafe
@InstantiationStrategy(PER_LOOKUP)
public class DocFormCommand implements IDocForm {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocFormCommand.class);

  @Requirement
  private ICopyDocumentRole copyDocService;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  @Requirement(XWikiDocumentClass.CLASS_DEF_HINT)
  private ClassDefinition xDocClassDef;

  @Requirement(XDocumentFieldAccessor.NAME)
  private FieldAccessor<XWikiDocument> xDocFieldAccessor;

  private final Map<ResponseState, Set<DocumentReference>> responseMap;
  private final Map<Integer, Integer> changedObjects;

  private Optional<DocumentReference> defaultDocRef;
  private boolean isCreateAllowed;

  public DocFormCommand() {
    responseMap = new EnumMap<>(ResponseState.class);
    Stream.of(ResponseState.values())
        .forEach(state -> responseMap.put(state, new HashSet<>()));
    changedObjects = new HashMap<>();
  }

  @Override
  public DocFormCommand initialize(DocumentReference defaultDocRef, boolean isCreateAllowed) {
    this.defaultDocRef = Optional.ofNullable(defaultDocRef)
        .map(ref -> cloneRef(ref, DocumentReference.class));
    this.isCreateAllowed = isCreateAllowed;
    return this;
  }

  Map<Integer, Integer> getChangedObjects() {
    return changedObjects;
  }

  @Override
  public void updateDocs(List<DocFormRequestParam> requestParams) {
    Map<DocumentReference, List<DocFormRequestParam>> requestParamsByDoc;
    requestParamsByDoc = requestParams.stream().sorted()
        .collect(groupingBy(DocFormRequestParam::getDocRef));
    defaultDocRef.ifPresent(docRef -> requestParamsByDoc.putIfAbsent(docRef, ImmutableList.of()));
    requestParamsByDoc.forEach(this::updateDoc);
  }

  private void updateDoc(DocumentReference docRef, List<DocFormRequestParam> requestParams) {
    // TODO [CELDEV-900] aquire lock on docRef
    XWikiDocument xdoc = modelAccess.getOrCreateDocument(docRef);
    XWikiDocument tdoc = getTranslatedDoc(xdoc);
    Set<XWikiDocument> changedDocs = new LinkedHashSet<>();
    if (docRef.equals(defaultDocRef.orElse(null)) && updateDocFromTemplateIfNew(xdoc)) {
      // apply template for request document
      changedDocs.add(xdoc);
    }
    requestParams.stream()
        .filter(param -> param.getDocRef().equals(docRef))
        .map(param -> updateDocFromParam(xdoc, tdoc, param))
        .filter(Objects::nonNull)
        .forEach(changedDocs::add);
    changedDocs.stream()
        .forEach(this::trySaveDoc);
    // TODO [CELDEV-900] release lock on docRef
  }

  private boolean updateDocFromTemplateIfNew(XWikiDocument doc) {
    String template = context.getRequestParameter("template").or("");
    if (doc.isNew() && !template.isEmpty()) {
      LOGGER.debug("updateFromTemplate: updating doc '{}' with template '{}'", doc, template);
      try {
        DocumentReference templRef = modelUtils.resolveRef(template, DocumentReference.class);
        doc.readFromTemplate(templRef, context.getXWikiContext());
        return true;
      } catch (IllegalArgumentException iae) {
        LOGGER.warn("updateDocFromTemplate: failed for doc [{}] from template [{}]",
            doc.getDocumentReference(), template, iae);
      } catch (XWikiException xwe) {
        if (xwe.getCode() == XWikiException.ERROR_XWIKI_APP_DOCUMENT_NOT_EMPTY) {
          context.getXWikiContext().put("exception", xwe);
        }
        LOGGER.error("updateDocFromTemplate: failed for doc [{}] from template [{}]",
            doc.getDocumentReference(), template, xwe);
      }
    }
    return false;
  }

  XWikiDocument updateDocFromParam(XWikiDocument xdoc, XWikiDocument tdoc,
      DocFormRequestParam param) {
    LOGGER.debug("updateDoc: param [{}]", param);
    DocFormRequestKey.Type type = param.getKey().getType();
    switch (type) {
      case DOC_FIELD:
        return setDocField(tdoc, param);
      case OBJ_FIELD:
        return setObjField(xdoc, param);
      case OBJ_REMOVE:
        return removeObj(xdoc, param.getKey());
      default:
        throw new IllegalArgumentException(type.name());
    }
  }

  private XWikiDocument setDocField(XWikiDocument tdoc, DocFormRequestParam param) {
    DocFormRequestKey key = param.getKey();
    if (xDocClassDef.getField(key.getFieldName(), String.class).toJavaUtil()
        .map(field -> xDocFieldAccessor.setValue(tdoc, field, param.getValuesAsString()))
        .orElse(false)) {
      LOGGER.info("setDocField: set doc field [{}]", param);
      return tdoc;
    }
    return null;
  }

  private XWikiDocument setObjField(XWikiDocument xdoc, DocFormRequestParam param) {
    DocFormRequestKey key = param.getKey();
    int actualObjNb = getChangedObjects().getOrDefault(key.getObjHash(), key.getObjNb());
    XWikiObjectEditor editor = XWikiObjectEditor.on(xdoc)
        .filter(key.getClassRef())
        .filter(actualObjNb);
    if ((key.getObjNb() >= 0) && !editor.fetch().exists()) {
      // XXX [CELDEV-901] disable object creation from non-negative numbers
      LOGGER.warn("CELDEV-901: created obj from non-negative number [{}]", key);
    }
    BaseObject obj = editor.createFirstIfNotExists();
    getChangedObjects().put(key.getObjHash(), obj.getNumber());
    if (modelAccess.setProperty(obj, key.getFieldName(), param.getValueSingleOrList())) {
      LOGGER.debug("setObjField: set value for [{}]", param);
      return xdoc;
    }
    return null;
  }

  private XWikiDocument removeObj(XWikiDocument xdoc, DocFormRequestKey key) {
    XWikiObjectEditor editor = XWikiObjectEditor.on(xdoc)
        .filter(key.getClassRef())
        .filter(key.getObjNb());
    if (!editor.delete().isEmpty()) {
      LOGGER.debug("removeObj: for [{}]", key);
      return xdoc;
    }
    return null;
  }

  private void trySaveDoc(XWikiDocument doc) {
    ResponseState state;
    if (!copyDocService.check(doc, doc.getOriginalDocument())) {
      state = ResponseState.unchanged;
    } else if (doc.isNew() && !isCreateAllowed) {
      state = ResponseState.failed;
    } else {
      try {
        modelAccess.saveDocument(doc, "updateAndSaveDocFormRequest");
        state = ResponseState.successful;
      } catch (DocumentSaveException dse) {
        LOGGER.error("failed saving [{}]", doc.getDocumentReference(), dse);
        state = ResponseState.failed;
      }
    }
    responseMap.get(state).add(doc.getDocumentReference());
  }

  @Override
  public Map<ResponseState, Set<DocumentReference>> getResponseMap(
      List<DocFormRequestParam> requestParams) {
    Stream.concat(defaultDocRef.map(Stream::of).orElseGet(Stream::empty),
        requestParams.stream().map(DocFormRequestParam::getDocRef))
        .filter(not(responseMap.get(ResponseState.successful)::contains))
        .filter(not(responseMap.get(ResponseState.failed)::contains))
        .forEach(responseMap.get(ResponseState.unchanged)::add);
    return responseMap.entrySet().stream().collect(toImmutableMap(Entry::getKey,
        entry -> ImmutableSet.copyOf(entry.getValue())));
  }

  private XWikiDocument getTranslatedDoc(XWikiDocument xdoc) {
    String lang = context.getLanguage().orElse("");
    try {
      return getAddTranslationCommand().getTranslatedDoc(xdoc, lang);
    } catch (XWikiException xwe) {
      LOGGER.warn("getTranslatedDoc: failed for {}", xdoc.getDocumentReference(), xwe);
      return xdoc;
    }
  }

  /**
   * intended for test injects only
   */
  AddTranslationCommand addTranslationCmd;

  private AddTranslationCommand getAddTranslationCommand() {
    return Optional.ofNullable(addTranslationCmd)
        .orElseGet(AddTranslationCommand::new);
  }

}
