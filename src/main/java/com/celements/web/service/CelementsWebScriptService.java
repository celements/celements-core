package com.celements.web.service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.script.service.ScriptService;

import com.celements.navigation.cmd.DeleteMenuItemCommand;
import com.celements.sajson.Builder;
import com.celements.web.plugin.api.CelementsWebPluginApi;
import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.celements.web.plugin.cmd.PlainTextCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("celementsweb")
public class CelementsWebScriptService implements ScriptService {

  private static final String APP_SCRIPT_XPAGE = "app";

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelementsWebPluginApi.class);

  @Requirement
  Execution execution;

  @Requirement("local")
  EntityReferenceSerializer<String> modelSerializer;

  @Requirement
  QueryManager queryManager;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
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
    return getContext().getDoc().getURL("view", "xpage=" + APP_SCRIPT_XPAGE + "&s="
        + scriptName + queryString, getContext());
  }

  public boolean isAppScriptCurrentPage(String scriptName) {
    String scriptStr = getScriptNameFromURL();
    return (!"".equals(scriptStr) && (scriptStr.equals(scriptName)));
  }

  public String getScriptNameFromURL() {
    String scriptStr = "";
    if (isAppScriptRequest()) {
      scriptStr = getAppScriptNameFromRequestURL();
    }
    return scriptStr;
  }

  public boolean isAppScriptRequest() {
    String xpageStr = getContext().getRequest().getParameter("xpage");
    return APP_SCRIPT_XPAGE.equals(xpageStr)
        && (getAppScriptNameFromRequestURL() != null);
  }

  private String getAppScriptNameFromRequestURL() {
    return getContext().getRequest().getParameter("s");
  }

  public String getCurrentPageURL(String queryString) {
    if(isAppScriptRequest()) {
      return getAppScriptURL(getScriptNameFromURL(), queryString);
    } else {
      return "?" + queryString;
    }
  }

  public String convertToPlainText(String htmlContent) {
    LOGGER.trace("convertToPlainText called on celementsweb script service for ["
        + htmlContent + "].");
    return new PlainTextCommand().convertToPlainText(htmlContent);
  }

  public Builder getNewJSONBuilder() {
    return new Builder();
  }

  public boolean deleteMenuItem(DocumentReference docRef) {
    String docFN = modelSerializer.serialize(docRef);
    try {
      if (getContext().getWiki().getRightService().hasAccessLevel("edit",
          getContext().getUser(), docFN, getContext())) {
        return new DeleteMenuItemCommand().deleteMenuItem(docRef);
      }
    } catch (XWikiException exp) {
      LOGGER.error("Failed to check 'edit' access rights for user ["
          + getContext().getUser() + "] on document [" + docFN + "]");
    }
    return false;
  }

  public List<String[]> getLastChangedDocuments(int numEntries) {
    return getLastChangedDocuments(numEntries, "");
  }

  //TODO write unit tests
  public List<String[]> getLastChangedDocuments(int numEntries, String space) {
    String xwql = "select doc.fullName, doc.language from XWikiDocument doc";
    boolean hasSpaceRestriction = (!"".equals(space));
    if (hasSpaceRestriction) {
      xwql = xwql + " where doc.space = :spaceName";
    }
    xwql = xwql + " order by doc.date desc";
    Query query;
    try {
      query = queryManager.createQuery(xwql, Query.XWQL);
      if (hasSpaceRestriction) {
        query = query.bindValue("spaceName", space);
      }
      return query.setLimit(numEntries).execute();
    } catch (QueryException exp) {
      LOGGER.error("Failed to create whats-new query for space [" + space + "].", exp);
    }
    return Collections.emptyList();
  }

  public String getHumanReadableSize(int bytes, boolean si) {
    return getHumanReadableSize(bytes, si, getContext().getLanguage());
  }

  public String getHumanReadableSize(int bytes, boolean si, String language) {
    return getHumanReadableSize(bytes, si, getLocal(language));
  }

  public String getHumanReadableSize(int bytes, boolean si, Locale locale) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
    NumberFormat decimalFormat = DecimalFormat.getInstance(locale);
    decimalFormat.setMaximumFractionDigits(1);
    decimalFormat.setMinimumFractionDigits(1);
    return String.format("%s %sB", decimalFormat.format(bytes / Math.pow(unit, exp)), pre);
  }

  public Locale getLocal(String language) {
    return new Locale(language);
  }

  public Locale getLocal(String language, String country) {
    return new Locale(language, country);
  }

  public Document createDocument(DocumentReference newDocRef) {
    return createDocument(newDocRef, null);
  }

  public Document createDocument(DocumentReference newDocRef, String pageType) {
    XWikiDocument theNewDoc = new CreateDocumentCommand().createDocument(newDocRef,
        pageType);
    if (theNewDoc != null) {
      return theNewDoc.newDocument(getContext());
    }
    return null;
  }

}
