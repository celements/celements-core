package com.celements.web.service;

import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.internal.reference.DefaultStringEntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.api.PageLayoutApi;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component("layout")
public class LayoutScriptService  implements ScriptService {

  private static Logger _LOGGER  = LoggerFactory.getLogger(LayoutScriptService.class);
  
  private static final String CELEMENTS_PAGE_LAYOUT_COMMAND =
      "com.celements.web.PageLayoutCommand";
  
  @Requirement
  private Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public String renderPageLayout() {
    return getPageLayoutCmd().renderPageLayout();
  }
  
  public String renderPageLayout(SpaceReference spaceRef) {
    return getPageLayoutCmd().renderPageLayout(spaceRef);
  }
  
  public Map<String,String> getActivePageLayouts() {
    return getPageLayoutCmd().getActivePageLyouts();
  }
  
  public Map<String,String> getAllPageLayouts() {
    return getPageLayoutCmd().getAllPageLayouts();
  }
  
  public String createNewLayout(String layoutSpaceName) {
    return getPageLayoutCmd().createNew(getWebUtilsService().resolveSpaceReference(
        layoutSpaceName));
  }
  
  public boolean deleteLayout(String layoutSpaceName) {
    SpaceReference layoutSpaceRef = getWebUtilsService().resolveSpaceReference(
        layoutSpaceName);
    String layoutPropDocName = getEntitySerializer().serialize(getPageLayoutCmd(
        ).standardPropDocRef(layoutSpaceRef));
    try {
      if (getContext().getWiki().getRightService().hasAccessLevel("delete", getContext(
          ).getUser(), layoutPropDocName, getContext())) {
        return getPageLayoutCmd().deleteLayout(layoutSpaceRef);
      } else {
        _LOGGER.warn("NO delete rights on [" + layoutPropDocName
            + "] for user [" + getContext().getUser() + "].");
      }
    } catch (XWikiException exp) {
      _LOGGER.error("Failed to check delete rights on [" + layoutSpaceName + "] for user ["
          + getContext().getUser() + "].");
    }
    return false;
  }
  
  public PageLayoutApi getPageLayoutApiForName(String layoutSpaceName) {
    return new PageLayoutApi(getWebUtilsService().resolveSpaceReference(layoutSpaceName),
        getContext());
  }
  
  public String getPageLayoutForDoc(DocumentReference docRef) {
    SpaceReference pageLayoutForDoc = getPageLayoutCmd().getPageLayoutForDoc(docRef);
    if (pageLayoutForDoc != null) {
      return pageLayoutForDoc.getName();
    }
    return "";
  }
  
  public boolean layoutExists(SpaceReference layoutSpaceRef) {
    return getPageLayoutCmd().layoutExists(layoutSpaceRef);
  }
  
  public boolean useXWikiLoginLayout() {
    return "1".equals(getContext().getWiki().getSpacePreference("xwikiLoginLayout",
        "celements.xwikiLoginLayout", "1", getContext()));
  }
  
  public boolean layoutEditorAvailable() {
    return getPageLayoutCmd().layoutEditorAvailable();
  }
  
  public String renderCelementsDocumentWithLayout(DocumentReference docRef,
      SpaceReference layoutSpaceRef) {
    XWikiDocument oldContextDoc = getContext().getDoc();
    _LOGGER.debug("renderCelementsDocumentWithLayout for docRef [" + docRef
        + "] and layoutSpaceRef [" + layoutSpaceRef + "] overwrite oldContextDoc ["
        + oldContextDoc.getDocumentReference() + "].");
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    try {
      XWikiDocument newContextDoc = getContext().getWiki().getDocument(docRef, 
          getContext());
      getContext().setDoc(newContextDoc);
      vcontext.put("doc", newContextDoc.newDocument(getContext()));
      return getPageLayoutCmd().renderPageLayout(layoutSpaceRef);
    } catch (XWikiException exp) {
      _LOGGER.error("Failed to get docRef document to renderCelementsDocumentWithLayout.",
          exp);
    } finally {
      getContext().setDoc(oldContextDoc);
      vcontext.put("doc", oldContextDoc.newDocument(getContext()));
    }
    return "";
  }
  
  public SpaceReference getCurrentRenderingLayout() {
    return getPageLayoutCmd().getCurrentRenderingLayout();
  }
  
  /**
   * TODO: Probably use TreeNodeService directly
   */
  private PageLayoutCommand getPageLayoutCmd() {
    if (!getContext().containsKey(CELEMENTS_PAGE_LAYOUT_COMMAND)) {
      getContext().put(CELEMENTS_PAGE_LAYOUT_COMMAND, new PageLayoutCommand());
    }
    return (PageLayoutCommand) getContext().get(CELEMENTS_PAGE_LAYOUT_COMMAND);
  }
  
  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }
  
  private DefaultStringEntityReferenceSerializer getEntitySerializer() {
    return ((DefaultStringEntityReferenceSerializer)Utils.getComponent(
        EntityReferenceSerializer.class));
  }
}
