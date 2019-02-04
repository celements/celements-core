package com.celements.appScript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceValueProvider;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.emptycheck.service.IEmptyCheckRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.util.Util;

@Component
public class AppScriptService implements IAppScriptService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(AppScriptService.class);

  @Requirement
  IEmptyCheckRole emptyCheck;

  @Requirement
  IWebUtilsService webUtils;

  @Requirement
  Execution execution;

  @Requirement
  EntityReferenceValueProvider defaultEntityReferenceValueProvider;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public int getStartIndex(String path) {
    String actionName = getAppActionName();
    return path.indexOf("/" + actionName + "/") + actionName.length() + 2;
  }

  @Override
  public String getAppActionName() {
    return getContext().getWiki().Param(APP_SCRIPT_ACTION_NAME_CONF_PROPERTY, APP_SCRIPT_XPAGE);
  }

  @Override
  public boolean hasDocAppScript(String scriptName) {
    boolean hasDocAppScript = hasLocalAppScript(scriptName) || hasCentralAppScript(scriptName);
    LOGGER.debug("hasDocAppScript: scriptName [" + scriptName + "] hasDocAppScript ["
        + hasDocAppScript + "]");
    return hasDocAppScript;
  }

  @Override
  public boolean hasLocalAppScript(String scriptName) {
    return !"".equals(scriptName) && docAppScriptExists(getLocalAppScriptDocRef(scriptName));
  }

  private boolean docAppScriptExists(DocumentReference appScriptDocRef) {
    boolean existsAppScriptDoc = getContext().getWiki().exists(appScriptDocRef, getContext());
    boolean isNotEmptyAppScriptDoc = !emptyCheck.isEmptyRTEDocument(appScriptDocRef);
    LOGGER.debug("docAppScriptExists check [" + appScriptDocRef + "]: exists [" + existsAppScriptDoc
        + "] isNotEmpty [" + isNotEmptyAppScriptDoc + "]");
    return (existsAppScriptDoc && isNotEmptyAppScriptDoc);
  }

  @Override
  public boolean hasCentralAppScript(String scriptName) {
    return !"".equals(scriptName) && docAppScriptExists(getCentralAppScriptDocRef(scriptName));
  }

  @Override
  public DocumentReference getAppScriptDocRef(String scriptName) {
    if (hasLocalAppScript(scriptName)) {
      return getLocalAppScriptDocRef(scriptName);
    } else {
      return getCentralAppScriptDocRef(scriptName);
    }
  }

  @Override
  public DocumentReference getLocalAppScriptDocRef(String scriptName) {
    return new DocumentReference(getContext().getDatabase(), APP_SCRIPT_SPACE_NAME, scriptName);
  }

  @Override
  public DocumentReference getCentralAppScriptDocRef(String scriptName) {
    return new DocumentReference("celements2web", APP_SCRIPT_SPACE_NAME, scriptName);
  }

  @Override
  public String getAppScriptTemplatePath(String scriptName) {
    return "celAppScripts/" + scriptName + ".vm";
  }

  @Override
  public boolean isAppScriptAvailable(String scriptName) {
    try {
      String path = "/templates/" + getAppScriptTemplatePath(scriptName);
      LOGGER.debug("isAppScriptAvailable: check on [" + path + "].");
      getContext().getWiki().getResourceContentAsBytes(path);
      LOGGER.trace("isAppScriptAvailable: Successful got app script [" + scriptName + "].");
      return true;
    } catch (IOException exp) {
      LOGGER.debug("isAppScriptAvailable: Failed to get app script [" + scriptName + "].", exp);
      return false;
    }
  }

  @Override
  public String getAppScriptURL(String scriptName) {
    return getAppScriptURL(scriptName, "");
  }

  @Override
  public String getAppScriptURL(String scriptName, String queryString) {
    if (queryString == null) {
      queryString = "";
    }
    if ((queryString.length() > 0) && !queryString.startsWith("/&")) {
      queryString = "&" + queryString;
    }
    queryString = "xpage=" + IAppScriptService.APP_SCRIPT_XPAGE + "&s=" + scriptName + queryString;
    if (scriptName.split("/").length <= 2) {
      return getContext().getWiki().getURL(webUtils.resolveDocumentReference(scriptName.replaceAll(
          "/", ".")), "view", queryString, null, getContext());
    } else {
      return Util.escapeURL("/app/" + scriptName + "?" + queryString);
    }
  }

  @Override
  public boolean isAppScriptCurrentPage(String scriptName) {
    String scriptStr = getScriptNameFromURL();
    return (!"".equals(scriptStr) && (scriptStr.equals(scriptName)));
  }

  @Override
  public String getScriptNameFromDocRef(DocumentReference docRef) {
    String spaceName = docRef.getLastSpaceReference().getName();
    if (spaceName.equals(defaultEntityReferenceValueProvider.getDefaultValue(EntityType.SPACE))) {
      return docRef.getName();
    } else {
      return spaceName + "/" + docRef.getName();
    }
  }

  @Override
  public String getScriptNameFromURL() {
    String scriptStr = "";
    if (isAppScriptRequest()) {
      scriptStr = getAppScriptNameFromRequestURL();
    } else {
      LOGGER.debug("getScriptNameFromURL: no AppScriptRequest thus returning ''.");
    }
    return scriptStr;
  }

  @Override
  public boolean isAppScriptRequest() {
    // TODO exclude isOverlayRequest
    return isAppScriptXpageRequest() || isAppScriptActionRequest() || isAppScriptSpaceRequest()
        || isAppScriptOverwriteDocRef(getContext().getDoc().getDocumentReference());
  }

  @Override
  public boolean isAppScriptOverwriteDocRef(DocumentReference docRef) {
    String overwriteAppDocs = getContext().getWiki().getXWikiPreference(
        APP_SCRIPT_XWPREF_OVERW_DOCS, APP_SCRIPT_CONF_OVERW_DOCS, "-", getContext());
    List<DocumentReference> overwAppDocList = new ArrayList<>();
    if (!"-".equals(overwriteAppDocs)) {
      for (String overwAppDocFN : overwriteAppDocs.split("[, ]")) {
        try {
          DocumentReference overwAppDocRef = webUtils.resolveDocumentReference(overwAppDocFN);
          overwAppDocList.add(overwAppDocRef);
        } catch (Exception exp) {
          LOGGER.warn("Failed to parse appScript overwrite docs config part [" + overwAppDocFN
              + "] of complete config [" + overwriteAppDocs + "].");
        }
      }
    }
    return overwAppDocList.contains(docRef);
  }

  private boolean isAppScriptActionRequest() {
    Object appAction = getContext().get("appAction");
    return (appAction != null) && ((Boolean) appAction);
  }

  private boolean isAppScriptSpaceRequest() {
    return "view".equals(getContext().getAction())
        && getContext().getDoc().getDocumentReference().getSpaceReferences().contains(
            getCurrentSpaceRef());
  }

  private SpaceReference getCurrentSpaceRef() {
    return new SpaceReference(APP_SCRIPT_XPAGE, new WikiReference(getContext().getDatabase()));
  }

  private boolean isAppScriptXpageRequest() {
    String xpageStr = getContext().getRequest().getParameter("xpage");
    return APP_SCRIPT_XPAGE.equals(xpageStr) && (getAppScriptNameFromRequestURL() != null);
  }

  String getAppScriptNameFromRequestURL() {
    if (isAppScriptActionRequest() || isAppScriptSpaceRequest()) {
      String path = getContext().getRequest().getPathInfo();
      return path.substring(getStartIndex(path)).replaceAll("^/+", "");
    } else if (isAppScriptOverwriteDocRef(getContext().getDoc().getDocumentReference())) {
      String path = getContext().getRequest().getPathInfo().replaceAll("^/+", "");
      if (path.startsWith(getContext().getAction())) {
        path = path.replaceAll("^" + getContext().getAction() + "/+", "");
      }
      path = path.replaceFirst("^" + defaultEntityReferenceValueProvider.getDefaultValue(
          EntityType.SPACE) + "/", "");
      if ("".equals(path)) {
        path = defaultEntityReferenceValueProvider.getDefaultValue(EntityType.DOCUMENT);
      } else if (path.endsWith("/")) {
        path += defaultEntityReferenceValueProvider.getDefaultValue(EntityType.DOCUMENT);
      }
      return path;
    }
    return getContext().getRequest().getParameter("s");
  }

}
