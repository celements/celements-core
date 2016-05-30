package com.celements.webform;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.DocFormCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("docform")
public class DocFormScriptService implements ScriptService {

  private static Logger LOGGER = LoggerFactory.getLogger(DocFormScriptService.class);

  private static final String _DOC_FORM_COMMAND_OBJECT = "com.celements.DocFormCommand";

  @Requirement
  private Execution execution;

  @Requirement
  IWebUtilsService webUtilsService;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file ATTENTION: use only for preview and NOT for save!
   * Removed objects will not be saved correctly using this method. To save use
   * updateAndSaveDocFromRequest() instead.
   */
  public Set<Document> updateDocFromMap(DocumentReference docRef, Map<String, ?> map)
      throws XWikiException {
    Collection<XWikiDocument> xdocs = getDocFormCommand().updateDocFromMap(docRef,
        getDocFormCommand().prepareMapForDocUpdate(map), getContext());
    Set<Document> docs = new HashSet<Document>();
    for (XWikiDocument xdoc : xdocs) {
      docs.add(xdoc.newDocument(getContext()));
    }
    return docs;
  }

  public Map<String, Set<DocumentReference>> updateAndSaveDocFromMap(DocumentReference docRef,
      Map<String, ?> map) {
    Collection<XWikiDocument> xdocs = Collections.<XWikiDocument>emptyList();
    try {
      xdocs = getDocFormCommand().updateDocFromMap(docRef,
          getDocFormCommand().prepareMapForDocUpdate(map), getContext());
      return checkRightsAndSaveXWikiDocCollection(xdocs);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception in getDocFormCommand().updateDocFromMap()", xwe);
    }
    return Collections.emptyMap();
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  public Set<Document> updateDocFromRequest() throws XWikiException {
    return updateDocFromRequest(null);
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file ATTENTION: use only for preview and NOT for save!
   * Removed objects will not be saved correctly using this method. To save use
   * updateAndSaveDocFromRequest() instead.
   */
  @SuppressWarnings("unchecked")
  public Set<Document> updateDocFromRequest(DocumentReference docRef) throws XWikiException {
    Set<Document> docs = new HashSet<Document>();
    Collection<XWikiDocument> xdocs = getDocFormCommand().updateDocFromMap(docRef,
        getContext().getRequest().getParameterMap(), getContext());
    for (XWikiDocument xdoc : xdocs) {
      docs.add(xdoc.newDocument(getContext()));
    }
    return docs;
  }

  public Map<String, Set<DocumentReference>> updateAndSaveDocFromRequest() {
    return updateAndSaveDocFromRequest(null);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Set<DocumentReference>> updateAndSaveDocFromRequest(DocumentReference docRef) {
    Collection<XWikiDocument> xdocs = Collections.<XWikiDocument>emptyList();
    try {
      xdocs = getDocFormCommand().updateDocFromMap(docRef,
          getContext().getRequest().getParameterMap(), getContext());
      return checkRightsAndSaveXWikiDocCollection(xdocs);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception in getDocFormCommand().updateDocFromMap()", xwe);
    }
    return Collections.emptyMap();
  }

  private DocFormCommand getDocFormCommand() {
    if (getContext().get(_DOC_FORM_COMMAND_OBJECT) == null) {
      getContext().put(_DOC_FORM_COMMAND_OBJECT, new DocFormCommand());
    }
    return (DocFormCommand) getContext().get(_DOC_FORM_COMMAND_OBJECT);
  }

  Map<String, Set<DocumentReference>> checkRightsAndSaveXWikiDocCollection(
      Collection<XWikiDocument> xdocs) throws XWikiException {
    boolean hasEditOnAllDocs = hasEditOnAllDocs(xdocs);
    Map<String, Set<DocumentReference>> docs;
    if (hasEditOnAllDocs) {
      docs = getDocFormCommand().saveXWikiDocCollection(xdocs);
    } else {
      Set<DocumentReference> saveFailed = new HashSet<DocumentReference>();
      for (XWikiDocument xdoc : xdocs) {
        saveFailed.add(xdoc.getDocumentReference());
      }
      docs = new HashMap<String, Set<DocumentReference>>();
      docs.put("successful", new HashSet<DocumentReference>());
      docs.put("failed", saveFailed);
    }
    return docs;
  }

  boolean hasEditOnAllDocs(Collection<XWikiDocument> xdocs) throws XWikiException {
    boolean hasEditOnAllDocs = true;
    for (XWikiDocument xdoc : xdocs) {
      if (getDocFormCommand().notNewOrCreateAllowed(xdoc)) {
        hasEditOnAllDocs &= getContext().getWiki().getRightService().hasAccessLevel("edit",
            getContext().getUser(), webUtilsService.serializeRef(xdoc.getDocumentReference()),
            getContext());
      }
    }
    return hasEditOnAllDocs;
  }
}
