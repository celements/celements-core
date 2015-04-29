package com.celements.web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.inject.Singleton;
import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.pagetype.PageType;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.cmd.PageTypeCommand;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.web.plugin.cmd.CheckClassesCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.util.Util;
import com.xpn.xwiki.web.XWikiRequest;

/**
 * TODO implement VelocityContextInitializer role
 *      --> maybe XWiki overwrites later some vcontext variables ($language, $doc, $tdoc)
 * @author fabian
 *
 */

@Component
@Singleton
public class PrepareVelocityContextService implements IPrepareVelocityContext {

  private static Logger _LOGGER  = LoggerFactory.getLogger(
      PrepareVelocityContextService.class);

  private String _CEL_PREPARE_VELOCITY_COUNTER = "celPrepareVelocityCounter";
  private String _CEL_PREPARE_VELOCITY_TOTALTIME = "celPrepareVelocityTotelTime";
  
  @Requirement
  Execution execution;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  IPageTypeResolverRole pageTypeResolver;

  @Requirement
  IPageTypeRole pageTypeService;

  private XWikiContext getContext() {
    return (XWikiContext)getExecContext().getProperty("xwikicontext");
  }

  private ExecutionContext getExecContext() {
    return execution.getContext();
  }

  @Override
  public void prepareVelocityContext(VelocityContext vcontext) {
    Integer count = 0;
    if (getExecContext().getProperty(_CEL_PREPARE_VELOCITY_COUNTER) != null) {
      count = (Integer) getExecContext().getProperty(_CEL_PREPARE_VELOCITY_COUNTER);
    }
    count = count + 1;
    long startMillis = System.currentTimeMillis();
    _LOGGER.debug("prepareVelocityContext [" + count + "]: with vcontext ["
        + vcontext + "].");
    fixLanguagePreference(vcontext);
    _LOGGER.trace("prepareVelocityContext [" + count + "]: after fixLanguagePreference.");
    fixTdocForInvalidLanguage(vcontext);
    _LOGGER.trace("prepareVelocityContext [" + count
        + "]: after fixTdocForInvalidLanguage.");
    initCelementsVelocity(vcontext);
    _LOGGER.trace("prepareVelocityContext [" + count + "]: after initCelementsVelocity.");
    initPanelsVelocity(vcontext);
    _LOGGER.trace("prepareVelocityContext [" + count + "]: after initCelementsVelocity.");
    getExecContext().setProperty(_CEL_PREPARE_VELOCITY_COUNTER, count);
    if (_LOGGER.isInfoEnabled()) {
      long endMillis = System.currentTimeMillis();
      long timeUsed = endMillis - startMillis;
      Long totalTimeUsed = 0L;
      if (getExecContext().getProperty(_CEL_PREPARE_VELOCITY_TOTALTIME) != null) {
        totalTimeUsed = (Long) getExecContext().getProperty(
            _CEL_PREPARE_VELOCITY_TOTALTIME);
      }
      totalTimeUsed += timeUsed;
      getExecContext().setProperty(_CEL_PREPARE_VELOCITY_TOTALTIME, totalTimeUsed);
      _LOGGER.info("prepareVelocityContext [" + count + "]: with vcontext ["
          + vcontext + "] finished in [" + timeUsed + "], total time [" + totalTimeUsed
          + "].");
    }
  }

  /**
   * @param context
   * 
   * @deprecated instead use prepareVelocityContext(VelocityContext)
   */
  @Override
  @Deprecated
  public void prepareVelocityContext(XWikiContext context) {
    if (context != null) {
      prepareVelocityContext((VelocityContext) context.get("vcontext"));
    }
  }

  void fixLanguagePreference(VelocityContext vcontext) {
    String langPref = getLanguagePreference(getContext());
    getContext().setLanguage(langPref);
    if (vcontext != null) {
      vcontext.put("language", langPref);
    }
  }

  void initPanelsVelocity(VelocityContext vcontext) {
    if (vcontext != null) {
      if (!vcontext.containsKey("rightPanels")) {
        _LOGGER.debug("setting rightPanels in vcontext: " + getRightPanels());
        vcontext.put("rightPanels", getRightPanels());
      }
      if (!vcontext.containsKey("leftPanels")) {
        _LOGGER.debug("setting leftPanels in vcontext: " + getLeftPanels());
        vcontext.put("leftPanels", getLeftPanels());
      }
      if (!vcontext.containsKey("showRightPanels")) {
        vcontext.put("showRightPanels", Integer.toString(showRightPanels()));
      }
      if (!vcontext.containsKey("showLeftPanels")) {
        vcontext.put("showLeftPanels", Integer.toString(showLeftPanels()));
      }
      _LOGGER.debug("leftPanels [" + vcontext.get("leftPanels")
          + "] and rightPanels [" + vcontext.get("rightPanels")
          + "] after initPanelsVelocity.");
    }
  }

  void initCelementsVelocity(VelocityContext vcontext) {
    if ((vcontext != null) && (getContext().getWiki() != null)) {
      if (!vcontext.containsKey(getVelocityName())) {
        vcontext.put(getVelocityName(), getContext().getWiki().getPluginApi(
            getVelocityName(), getContext()));
      }
      if (!vcontext.containsKey("default_language")) {
        vcontext.put("default_language", webUtilsService.getDefaultLanguage());
      }
      if (!vcontext.containsKey("langs")) {
        vcontext.put("langs", webUtilsService.getAllowedLanguages());
      }
      if (!vcontext.containsKey("hasedit")) {
        try {
          if (getContext().getDoc() != null) {
            vcontext.put("hasedit", getContext().getWiki(
                ).getRightService().hasAccessLevel("edit", getContext().getUser(),
                    getContext().getDoc().getFullName(), getContext()));
          } else {
            vcontext.put("hasedit", new Boolean(false));
          }
        } catch (XWikiException exp) {
          _LOGGER.error("Failed to check edit Access Rights on "
              + getContext().getDoc().getDocumentReference(), exp);
          vcontext.put("hasedit", new Boolean(false));
        }
      }
      if (!vcontext.containsKey("skin_doc")) {
        try {
          String skinDocName = getContext().getWiki().getSkin(getContext());
          Document skinDoc = getContext().getWiki().getDocument(
              webUtilsService.resolveDocumentReference(skinDocName), getContext()
              ).newDocument(getContext());
          vcontext.put("skin_doc", skinDoc);
        } catch (XWikiException e) {
          _LOGGER.error("Failed to get skin_doc");
        }
      }
      if (!vcontext.containsKey("isAdmin")) {
        vcontext.put("isAdmin", webUtilsService.isAdminUser());
      }
      if (!vcontext.containsKey("isSuperAdmin")) {
        vcontext.put("isSuperAdmin", webUtilsService.isSuperAdminUser());
      }
      if (!vcontext.containsKey("admin_language")) {
        vcontext.put("admin_language", webUtilsService.getAdminLanguage());
        _LOGGER.debug("added admin_language to vcontext: "
            + webUtilsService.getAdminLanguage());
      }
      if (!vcontext.containsKey("adminMsg")) {
        vcontext.put("adminMsg", webUtilsService.getAdminMessageTool());
      }
      if (!vcontext.containsKey("celements2_skin")) {
        vcontext.put("celements2_skin", getCelementsSkinDoc(getContext()));
      }
      try {
        if (!vcontext.containsKey("celements2_baseurl")
            && (getContext().getURLFactory() != null)) {
          XWikiDocument celementsSkinXWikiDoc = getCelementsSkinXWikiDoc(getContext());
          String celements2_baseurl = celementsSkinXWikiDoc.getURL("view", getContext());
          if (celements2_baseurl.indexOf("/",8) > 0) {
            vcontext.put("celements2_baseurl", celements2_baseurl.substring(0,
                celements2_baseurl.indexOf("/",8)));
          }
        }
      } catch (XWikiException exp) {
        _LOGGER.error("failed to get CelementsSkin XWikiDocument.", exp);
      }
      if (!vcontext.containsKey("page_type")) {
        PageTypeReference pageTypeRef = pageTypeResolver.getPageTypeRefForCurrentDoc();
        if (pageTypeRef != null) {
          vcontext.put("page_type", pageTypeRef.getConfigName());
        }
      }
      if (!vcontext.containsKey("user")) {
        vcontext.put("user", getContext().getUser());
      }
      if (!vcontext.containsKey("isContentEditor")) {
        vcontext.put("isContentEditor", getContext().getWiki().getUser(
            getContext().getUser(), getContext()).isUserInGroup(
                "XWiki.ContentEditorsGroup"));
      }
      if (!vcontext.containsKey("q")) {
        vcontext.put("q", "'");
      }
      if (!vcontext.containsKey("Q")) {
        vcontext.put("Q", "\"");
      }
      if (!vcontext.containsKey("tinyMCE_width")) {
        vcontext.put("tinyMCE_width", getRTEwidth(getContext()));
      }
    }
  }

  String getRTEwidth(XWikiContext context) {
    int tinyMCEwidth = -1;
    String tinyMCEwidthStr = "";
    if (getCelementsSkinDoc(context) != null) {
      BaseObject pageTypeObj = new PageTypeCommand().getPageTypeObj(context.getDoc(),
          context).getPageTypeProperties(context);
      if (pageTypeObj != null) {
        tinyMCEwidth = pageTypeObj.getIntValue("rte_width", -1);
        tinyMCEwidthStr = Integer.toString(tinyMCEwidth);
      }
      if (tinyMCEwidth < 0) {
        tinyMCEwidthStr = context.getWiki().getSpacePreference("editbox_width", context);
        if ((tinyMCEwidthStr != null) && !"".equals(tinyMCEwidthStr)) {
          tinyMCEwidth = Integer.parseInt(tinyMCEwidthStr);
        }
      }
    }
    if (tinyMCEwidth < 0) {
      tinyMCEwidth = 453;
      tinyMCEwidthStr = Integer.toString(tinyMCEwidth);
    }
    return tinyMCEwidthStr;
  }

  private Document getCelementsSkinDoc(XWikiContext context) {
    Document skinDoc = null;
    try {
      skinDoc = getCelementsSkinXWikiDoc(context).newDocument(context);
    } catch (XWikiException exp) {
      _LOGGER.error("Failed to load celements2_skin"
          + " (celements2web:XWiki.Celements2Skin) ", exp);
    }
    return skinDoc;
  }

  private XWikiDocument getCelementsSkinXWikiDoc(XWikiContext context
      ) throws XWikiException {
    return context.getWiki(
        ).getDocument(new DocumentReference("celements2web", "XWiki", "Celements2Skin"),
            context);
  }

  @Override
  public int showRightPanels() {
    if (showRightPanelsBoolean(getContext()) && !getRightPanels().isEmpty()) {
      return 1;
    } else {
      return 0;
    }
  }

  private boolean showRightPanelsBoolean(XWikiContext context) {
    return showPanelByConfigName(context, "showRightPanels");
  }

  @Override
  public int showLeftPanels() {
    if (showLeftPanelsBoolean(getContext()) && !getLeftPanels().isEmpty()) {
      return 1;
    } else {
      return 0;
    }
  }

  private boolean showLeftPanelsBoolean(XWikiContext context) {
    return showPanelByConfigName(context, "showLeftPanels");
  }

  @Override
  public List<String> getRightPanels() {
    if (showRightPanelsBoolean(getContext())) {
      return Arrays.asList(getPanelString(getContext(), "rightPanels").split(","));
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public List<String> getLeftPanels() {
    if (showLeftPanelsBoolean(getContext())) {
      return Arrays.asList(getPanelString(getContext(), "leftPanels").split(","));
    } else {
      return Collections.emptyList();
    }
  }

  boolean showPanelByConfigName(XWikiContext context, String configName) {
    if (isPageShowPanelOverwrite(configName, context.getDoc())) {
      return (1 == getPagePanelObj(configName, context.getDoc()
          ).getIntValue("show_panels"));
    } else if ((getPageTypeDoc(context) != null)
        && isPageShowPanelOverwrite(configName, getPageTypeDoc(context))) {
      boolean showPanels = (1 == getPagePanelObj(configName, getPageTypeDoc(context)
          ).getIntValue("show_panels"));
      _LOGGER.debug("using pagetype for panels " + configName + " -> "+ showPanels);
      return showPanels;
    } else if (isSpaceOverwrite(context)) {
      boolean showPanels = "1".equals(context.getWiki().getSpacePreference(configName,
          getSpaceOverwrite(context), "0", context));
      _LOGGER.debug("using spaceover webPrefs for panels " + configName
          + "," + getSpaceOverwrite(context) +" -> "+ showPanels);
      return showPanels;
    } else if (isGlobalPref(context)) {
      boolean showPanels = ("1".equals(context.getWiki().getXWikiPreference(configName,
          context)));
      _LOGGER.debug("using globalPref for panels " + configName + " -> "+ showPanels);
      return showPanels;
    } else if (context.getWiki() != null) {
      boolean showPanels = ("1".equals(context.getWiki().getSpacePreference(configName,
          context)));
      _LOGGER.debug("using webPrefs for panels " + configName + " -> "+ showPanels);
      return showPanels;
    }
    return false;
  }

  XWikiDocument getPageTypeDoc(XWikiContext context) {
    if(context.getDoc() != null) {
      PageTypeReference pTRefForCurrDoc = pageTypeResolver.getPageTypeRefForCurrentDoc();
      if (pTRefForCurrDoc != null) {
        try {
          DocumentReference pageTypeDocRef = new DocumentReference(context.getDatabase(),
              "PageTypes", pTRefForCurrDoc.getConfigName());
          XWikiDocument pageTypeDoc = new PageType(pageTypeDocRef).getTemplateDocument(
              getContext());
          _LOGGER.debug("getPageTypeDoc: pageTypeDoc=" + pageTypeDoc);
          return pageTypeDoc;
        } catch (XWikiException exp) {
          _LOGGER.error("Failed to getPageTypeDoc.", exp);
        }
      } else {
        _LOGGER.info("no pageType reference for current document found.");
      }
    }
    return null;
  }

  private BaseObject getPagePanelObj(String configName, XWikiDocument theDoc) {
    if (theDoc != null) {
      return theDoc.getObject(CheckClassesCommand.CLASS_PANEL_CONFIG_CLASS, "config_name",
        getPanelType(configName), false);
    } else {
      return null;
    }
  }

  private boolean isPageShowPanelOverwrite(String configName,
      XWikiDocument theDoc) {
    try {
      return ((getPagePanelObj(configName, theDoc) != null)
         && (((BaseProperty)getPagePanelObj(configName, theDoc
             ).get("show_panels")).getValue() != null));
    } catch (XWikiException exp) {
      _LOGGER.error("", exp);
      return false;
    }
  }

  private String getPanelType(String configName) {
    if ("showLeftPanels".equals(configName)) {
      return "leftPanels";
    } else if ("showRightPanels".equals(configName)) {
      return "rightPanels";
    } else {
      return configName;
    }
  }

  private boolean isSpaceOverwrite(XWikiContext context) {
    return ((getSpaceOverwrite(context) != null)
        && !"".equals(getSpaceOverwrite(context)));
  }

  private String getPanelString(XWikiContext context, String configName) {
    String panelsString = "";
    if(isGlobalPref(context)) {
      panelsString = context.getWiki().getXWikiPreference(configName, context);
    } else if(isPagePanelsOverwrite(configName, context.getDoc())) {
      panelsString = getPagePanelObj(configName, context.getDoc()
          ).getStringValue("panels");
    } else if((getPageTypeDoc(context) != null)
        && isPagePanelsOverwrite(configName, getPageTypeDoc(context))) {
      panelsString = getPagePanelObj(configName, getPageTypeDoc(context)
          ).getStringValue("panels");
    } else if(isSpaceOverwrite(context)) {
      panelsString = context.getWiki().getSpacePreference(configName,
           getSpaceOverwrite(context), "", context);
    } else {
      panelsString = context.getWiki().getUserPreference(configName, context);
      _LOGGER.debug("else with panels in userPreferences: " + panelsString);
      if("".equals(panelsString)) {
         panelsString = context.getWiki().getSpacePreference(configName, context);
         _LOGGER.debug("else with panels in webPreferences: " + panelsString);
      }
    }
    _LOGGER.debug("panels for config " + configName + " ; " + panelsString);
    return panelsString;
  }

  private boolean isPagePanelsOverwrite(String configName,
      XWikiDocument theDoc) {
    return ((getPagePanelObj(configName, theDoc) != null)
        && (!"".equals(getPagePanelObj(configName, theDoc
            ).getStringValue("panels"))));
  }

  private String getSpaceOverwrite(XWikiContext context) {
    if(context.getRequest() != null) {
      return context.getRequest().get("space");
    }
    return "";
  }

  private boolean isGlobalPref(XWikiContext context) {
    if ((context.getDoc() != null) && (context.getRequest() != null)) {
      return new DocumentReference(context.getDatabase(), "XWiki", "XWikiPreferences"
          ).equals(context.getDoc().getDocumentReference())
          || "globaladmin".equals(context.getRequest().get("editor"));
    }
    return false;
  }

  @Override
  public String getVelocityName() {
    return "celementsweb";
  }

  /**
   * First try to find the current language in use from the XWiki context. If
   * none is used and if the wiki is not multilingual use the default language
   * defined in the XWiki preferences. If the wiki is multilingual try to get
   * the language passed in the request. If none was passed try to get it from a
   * cookie. If no language cookie exists then use the user default language and
   * barring that use the browser's "Accept-Language" header sent in HTTP
   * request. If none is defined use the default language.
   * 
   * @return the language to use
   * 
   * introduced in 2.18.0 because of a bug in xwiki 2.7.2 it is fixed
   * in unstable-xwiki4 YET celements allows default_language and language change
   * between spaces. XWiki does not!
   */
  String getLanguagePreference(XWikiContext context) {
    _LOGGER.debug("getLanguagePreference: start " + context.getLanguage());
    String language = context.getLanguage();

    _LOGGER.debug("getLanguagePreference: isMultiLingual ["
        + context.getWiki().isMultiLingual(context) + "] defaultLanguage ["
        + webUtilsService.getDefaultLanguage() + "].");
    // If the wiki is non multilingual then the language is the default
    // language.
    if (!context.getWiki().isMultiLingual(context)) {
      return webUtilsService.getDefaultLanguage();
    }

    if (context.getRequest() != null) {
      try {
        language = getLanguageInRequestParam();
        if (isValidLanguage(language)) {
          _LOGGER.debug("getLanguagePreference: found parameter language " + language);
          return language;
        }
      } catch (Exception e) {
        _LOGGER.error("Failed to get the language paramter from the request.", e);
      }
      // As no language parameter was passed in the request, try to get the
      // language to use from a cookie.
      try {
        language = getLanguageFromCookie();
        if (isValidLanguage(language)) {
          _LOGGER.debug("getLanguagePreference: found cookie language " + language);
          return language;
        }
      } catch (Exception e) {
        _LOGGER.error("Failed to get the language from the cookie.", e);
      }
    } else {
      _LOGGER.info("getLanguagePreference: skip language parameter in request and"
          + " language cookie check, because request is null.");
    }

    _LOGGER.trace("getLanguagePreference: Next from the default user preference.");

    // Next from the default user preference
    try {
      language = getLanguageFromUserPreferences();
      if (isValidLanguage(language)) {
        _LOGGER.debug("getLanguagePreference: found userpref language " + language);
        return language;
      }
    } catch (XWikiException e) {
      _LOGGER.error("Failed to get the default language from the user preferences.", e);
    }

    _LOGGER.trace("getLanguagePreference: Next from preferDefault? ");

    if (isConsiderBrowserAcceptLanguages()) {
      _LOGGER.trace("getLanguagePreference: Then from the navigator language setting.");
      // Then from the navigator language setting
      if (context.getRequest() != null) {
        language = getLanguageFromAcceptedHeaderLanguages();
        if (isValidLanguage(language)) {
          _LOGGER.debug("getLanguagePreference: found accepted language " + language);
          return language;
        }
      } else {
        _LOGGER.info("getLanguagePreference: skip accept-language in request,"
            + " because request is null.");
      }
    } else {
      _LOGGER.debug("getLanguagePreference: found preferDefault language " + language);
    }

    // Finally, use the default language from the global preferences.
    _LOGGER.debug("getLanguagePreference: use default language " + language);
    return webUtilsService.getDefaultLanguage();
  }

  private boolean isValidLanguage(String language) {
    boolean isValidLanguage = (language != null) && !"".equals(language)
              && webUtilsService.getAllowedLanguages().contains(language);
    return isValidLanguage;
  }

  private String getLanguageFromAcceptedHeaderLanguages() {
    String language = "";
    XWiki xwiki = getContext().getWiki();
    String acceptHeader = getContext().getRequest().getHeader("Accept-Language");
    // If the client didn't specify some languages, skip this phase
    if ((acceptHeader != null) && (!acceptHeader.equals(""))) {
      List<String> acceptedLanguages = getAcceptedLanguages(getContext().getRequest());
      _LOGGER.debug("getLanguageFromAcceptedHeaderLanguages: getAcceptedLanguages "
          + acceptedLanguages);
      _LOGGER.debug("getLanguageFromAcceptedHeaderLanguages: forceSupported "
          + xwiki.Param("xwiki.language.forceSupported", "0"));
      // We can force one of the configured languages to be accepted
      if (xwiki.Param("xwiki.language.forceSupported", "0").equals("1")) {
        List<String> available = webUtilsService.getAllowedLanguages();
        _LOGGER.debug("getLanguageFromAcceptedHeaderLanguages: forceSupported lang "
            + " languages [" + Arrays.deepToString(available.toArray(new String[] {}))
            + "].");
        // Filter only configured languages
        acceptedLanguages.retainAll(available);
      }
      _LOGGER.debug("getLanguageFromAcceptedHeaderLanguages: acceptedLanguages after "
          + acceptedLanguages);
      if (acceptedLanguages.size() > 0) {
        // Use the "most-preferred" language, as requested by the client.
        language = acceptedLanguages.get(0);
      }
      // If none of the languages requested by the client is acceptable, skip
      // to next
      // phase (use default language).
    }
    return language;
  }

  private boolean isConsiderBrowserAcceptLanguages() {
    XWiki xwiki = getContext().getWiki();
    return !xwiki.Param("xwiki.language.preferDefault", "0").equals("1")
        && !xwiki.getSpacePreference("preferDefaultLanguage", "0", getContext()).equals(
            "1");
  }

  private String getLanguageFromUserPreferences() throws XWikiException {
    String language = null;
    String userFN = getContext().getUser();
    XWikiDocument userdoc = null;
    userdoc = getContext().getWiki().getDocument(webUtilsService.resolveDocumentReference(
        userFN), getContext());
    if (userdoc != null) {
      language = Util.normalizeLanguage(userdoc.getStringValue("XWiki.XWikiUsers",
          "default_language"));
    }
    return language;
  }

  private String getLanguageFromCookie() {
    // First we get the language from the cookie
    // !!! getUserPreferenceFromCookie throws NPE if request is NULL !!!
    return Util.normalizeLanguage(getContext().getWiki().getUserPreferenceFromCookie(
        "language", getContext()));
  }

  /**
   * As the wiki is multilingual try to find the language to use from the request by
   * looking for a language parameter. If the language value is "default" use the
   * default language from the XWiki preferences settings. Otherwise set a cookie to
   * remember the language in use.
   *    
   * @return
   */
  private String getLanguageInRequestParam() {
    String language = Util.normalizeLanguage(getContext().getRequest().getParameter(
        "language"));
    if ((language != null) && (!language.equals(""))) {
      Object cookieAddedBefore = getExecContext().getProperty(
          ADD_LANGUAGE_COOKIE_DONE);
      if ((cookieAddedBefore == null) || !(Boolean)cookieAddedBefore) {
        if (isInvalidLanguageOrDefault(language)) {
          // forgetting language cookie
          Cookie cookie = new Cookie("language", "");
          cookie.setMaxAge(0);
          cookie.setPath("/");
          getContext().getResponse().addCookie(cookie);
          language = webUtilsService.getDefaultLanguage();
        } else {
          // setting language cookie
          Cookie cookie = new Cookie("language", language);
          cookie.setMaxAge(60 * 60 * 24 * 365 * 10);
          cookie.setPath("/");
          getContext().getResponse().addCookie(cookie);
        }
        getExecContext().setProperty(ADD_LANGUAGE_COOKIE_DONE, new Boolean(true));
      }
    }
    return language;
  }

  boolean isInvalidLanguageOrDefault(String language) {
    return language.equals("default")
          || (isSuppressInvalid() && !webUtilsService.getAllowedLanguages().contains(
              language));
  }

  private boolean isSuppressInvalid() {
    return getContext().getWiki().getXWikiPreference(CEL_SUPPRESS_INVALID_LANG,
        CFG_LANGUAGE_SUPPRESS_INVALID, "0", getContext()).equals("1");
  }

  /**
   * Construct a list of language codes (ISO 639-1) from the Accept-Languages
   * header. This method filters out some bugs in different browsers or
   * containers, like returning '*' as a language (Jetty) or using '_' as a
   * language--country delimiter (some versions of Opera).
   * 
   * @param request
   *          The client request.
   * @return A list of language codes, in the client preference order; might be
   *         empty if the header is not well formed.
   */
  @SuppressWarnings("unchecked")
  private List<String> getAcceptedLanguages(XWikiRequest request) {
    List<String> result = new ArrayList<String>();
    Enumeration<Locale> e = request.getLocales();
    while (e.hasMoreElements()) {
      String language = e.nextElement().getLanguage().toLowerCase();
      _LOGGER.debug("getAcceptedLanguages: found language " + language);
      // All language codes should have 2 letters.
      if (StringUtils.isAlpha(language)) {
        result.add(language);
      }
    }
    return result;
  }

  void fixTdocForInvalidLanguage(VelocityContext vcontext) {
    //FIXME should be executed only once per vcontext right?
    XWikiDocument doc = getContext().getDoc();
    if ((doc != null) && (vcontext != null)) {
      Document vTdocBefore = (Document) vcontext.get("tdoc");
      if (isTdocLanguageWrong(vTdocBefore)) {
        try {
          XWikiDocument tdoc = doc.getTranslatedDocument(getContext());
          try {
              String rev = (String) getContext().get("rev");
              if (StringUtils.isNotEmpty(rev)) {
                  tdoc = getContext().getWiki().getDocument(tdoc, rev, getContext());
              }
          } catch (Exception ex) {
            _LOGGER.debug("Invalid version, just use the most recent one.", ex);
          }
          getContext().put("tdoc", tdoc);
          vcontext.put("tdoc", tdoc.newDocument(getContext()));
        } catch (XWikiException exp) {
          _LOGGER.error("Faild to get translated document for ["
              + getContext().getDoc().getDocumentReference() + "].", exp);
        }
      } else {
        _LOGGER.debug("skip fixTdocForInvalidLanguage because vTdoc launguage is"
            + " correct.");
      }
    } else {
      _LOGGER.debug("skip fixTdocForInvalidLanguage doc [" + doc + "] vcontext ["
          + vcontext + "].");
    }
  }

  boolean isTdocLanguageWrong(Document vTdocBefore) {
    if (vTdocBefore == null) return true;
    String vTdocLang = vTdocBefore.getLanguage();
    if ("".equals(vTdocLang)) {
      vTdocLang = vTdocBefore.getDefaultLanguage();
    }
    return (!getContext().getLanguage().equals(vTdocLang));
  }

}
