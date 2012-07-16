package com.celements.appScript;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.web.plugin.cmd.EmptyCheckCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;

@Component
public class AppScriptService implements IAppScriptService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(AppScriptService.class);

  private EmptyCheckCommand emptyCheckCmd = new EmptyCheckCommand();

  @Requirement
  IWebUtilsService webUtils;

  @Requirement
  Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public int getStartIndex(String path) {
    String actionName = getAppActionName();
    return path.indexOf("/" + actionName + "/") + actionName.length() + 2;
  }

  public String getAppActionName() {
    return getContext().getWiki().Param(APP_SCRIPT_ACTION_NAME_CONF_PROPERTY,
        APP_SCRIPT_XPAGE);
  }

  public boolean hasDocAppScript(String scriptName) {
    LOGGER.debug("hasDocAppScript: scriptName [" + scriptName + "]");
    return hasLocalAppScript(scriptName) || hasCentralAppScript(scriptName);
  }

  public boolean hasLocalAppScript(String scriptName) {
    return !"".equals(scriptName) && docAppScriptExists(getLocalAppScriptDocRef(
        scriptName));
  }

  private boolean docAppScriptExists(DocumentReference appScriptDocRef) {
    return (getContext().getWiki().exists(appScriptDocRef, getContext())
        && !emptyCheckCmd.isEmptyRTEDocument(appScriptDocRef));
  }

  public boolean hasCentralAppScript(String scriptName) {
    return !"".equals(scriptName) && docAppScriptExists(getCentralAppScriptDocRef(
        scriptName));
  }

  public DocumentReference getAppScriptDocRef(String scriptName) {
    if (hasLocalAppScript(scriptName)) {
      return getLocalAppScriptDocRef(scriptName);
    } else {
      return getCentralAppScriptDocRef(scriptName);
    }
  }

  public DocumentReference getLocalAppScriptDocRef(String scriptName) {
    return new DocumentReference(getContext().getDatabase(), APP_SCRIPT_SPACE_NAME,
        scriptName);
  }

  public DocumentReference getCentralAppScriptDocRef(String scriptName) {
    return new DocumentReference("celements2web", APP_SCRIPT_SPACE_NAME, scriptName);
  }

  public String getAppScriptTemplatePath(String scriptName) {
    return "celAppScripts/" + scriptName + ".vm";
  }

  public boolean isAppScriptAvailable(String scriptName) {
    try {
      String path = "/templates/" + getAppScriptTemplatePath(scriptName);
      LOGGER.debug("isAppScriptAvailable: check on [" + path + "].");
      getContext().getWiki().getResourceContentAsBytes(path);
      LOGGER.trace("isAppScriptAvailable: Successful got app script [" + scriptName
          + "].");
      return true;
    } catch (IOException exp) {
      LOGGER.debug("isAppScriptAvailable: Failed to get app script [" + scriptName + "].",
          exp);
      return false;
    }
  }

  public String getAppScriptURL(String scriptName) {
    return getAppScriptURL(scriptName, "");
  }

  public String getAppScriptURL(String scriptName, String queryString) {
    if (queryString == null) {
      queryString = "";
    }
    if (!"".equals(queryString)) {
      queryString = "&" + queryString;
    }
    return getContext().getDoc().getURL("view", "xpage="
        + IAppScriptService.APP_SCRIPT_XPAGE + "&s=" + scriptName + queryString,
        getContext());
  }

  public boolean isAppScriptCurrentPage(String scriptName) {
    String scriptStr = getScriptNameFromURL();
    return (!"".equals(scriptStr) && (scriptStr.equals(scriptName)));
  }

  public String getScriptNameFromURL() {
    String scriptStr = "";
    if (isAppScriptRequest()) {
      scriptStr = getAppScriptNameFromRequestURL();
    } else {
      LOGGER.debug("getScriptNameFromURL: no AppScriptRequest thus returning ''.");
    }
    return scriptStr;
  }

  public boolean isAppScriptRequest() {
    return isAppScriptXpageRequest() || isAppScriptActionRequest()
        || isAppScriptSpaceRequest() || isAppScriptOverwriteDocRequest();
  }

  private boolean isAppScriptOverwriteDocRequest() {
    String overwriteAppDocs = getContext().getWiki().getXWikiPreference(
        APP_SCRIPT_XWPREF_OVERW_DOCS, APP_SCRIPT_CONF_OVERW_DOCS, "-", getContext());
    List<DocumentReference> overwAppDocList = new Vector<DocumentReference>();
    if (!"-".equals(overwriteAppDocs)) {
      for (String overwAppDocFN : overwriteAppDocs.split("[, ]")) {
        try {
          DocumentReference overwAppDocRef = webUtils.resolveDocumentReference(
              overwAppDocFN);
          overwAppDocList.add(overwAppDocRef);
        } catch (Exception exp) {
          LOGGER.warn("Failed to parse appScript overwrite docs config part ["
              + overwAppDocFN+ "] of complete config [" + overwriteAppDocs + "].");
        }
      }
    }
    return overwAppDocList.contains(getContext().getDoc().getDocumentReference());
  }

  private boolean isAppScriptActionRequest() {
    Object appAction = getContext().get("appAction");
    return (appAction != null) && ((Boolean)appAction);
  }

  private boolean isAppScriptSpaceRequest() {
    return "view".equals(getContext().getAction()) && getContext().getDoc(
        ).getDocumentReference().getSpaceReferences().contains(getCurrentSpaceRef());
  }

  private SpaceReference getCurrentSpaceRef() {
    return new SpaceReference(APP_SCRIPT_XPAGE, new WikiReference(getContext(
        ).getDatabase()));
  }

  private boolean isAppScriptXpageRequest() {
    String xpageStr = getContext().getRequest().getParameter("xpage");
    return APP_SCRIPT_XPAGE.equals(xpageStr)
        && (getAppScriptNameFromRequestURL() != null);
  }

  String getAppScriptNameFromRequestURL() {
    if (isAppScriptActionRequest() || isAppScriptSpaceRequest()) {
      String path = getContext().getRequest().getPathInfo();
      return path.substring(getStartIndex(path)).replaceAll("^/+", "");
    } else if (isAppScriptOverwriteDocRequest()) {
      return getContext().getRequest().getPathInfo().replaceAll("^/+", "");
    }
    return getContext().getRequest().getParameter("s");
  }

}
