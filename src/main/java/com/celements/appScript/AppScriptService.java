package com.celements.appScript;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceValueProvider;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.emptycheck.service.IEmptyCheckRole;
import com.celements.init.XWikiProvider;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.web.service.UrlService;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiConfigSource;
import com.xpn.xwiki.XWikiConstant;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.util.Util;

@Component
public class AppScriptService implements IAppScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppScriptService.class);

  @Inject
  private IEmptyCheckRole emptyCheck;

  @Inject
  private Execution execution;

  @Inject
  private EntityReferenceValueProvider defaultEntityReferenceValueProvider;

  @Inject
  private XWikiProvider wikiProvider;

  @Inject
  private IModelAccessFacade modelAccess;

  @Inject
  private ModelUtils modelUtils;

  @Inject
  private ModelContext mContext;

  @Inject
  private UrlService urlService;

  @Inject
  @Named(XWikiConfigSource.NAME)
  private ConfigurationSource xwikiConfigSource;

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
    return xwikiConfigSource.getProperty(APP_SCRIPT_ACTION_NAME_CONF_PROPERTY, APP_SCRIPT_XPAGE);
  }

  @Override
  public boolean hasDocAppScript(@Nullable String scriptName) {
    boolean hasDocAppScript = hasLocalAppScript(scriptName) || hasCentralAppScript(scriptName);
    LOGGER.debug("hasDocAppScript: scriptName [{} hasDocAppScript [{}]", scriptName,
        hasDocAppScript);
    return hasDocAppScript;
  }

  @Override
  public boolean hasLocalAppScript(@Nullable String scriptName) {
    return !Strings.isNullOrEmpty(scriptName)
        && docAppScriptExists(getLocalAppScriptDocRef(scriptName).orElse(null));
  }

  @Override
  public boolean hasCentralAppScript(@Nullable String scriptName) {
    return !Strings.isNullOrEmpty(scriptName)
        && docAppScriptExists(getCentralAppScriptDocRef(scriptName).orElse(null));
  }

  @Override
  public boolean hasLocalAppRecursiveScript(@Nullable String scriptName) {
    return !Strings.isNullOrEmpty(scriptName)
        && docAppScriptExists(getLocalAppRecursiveScriptDocRef(scriptName).orElse(null));
  }

  @Override
  public boolean hasCentralAppRecursiveScript(@Nullable String scriptName) {
    return !Strings.isNullOrEmpty(scriptName)
        && docAppScriptExists(getCentralAppRecursiveScriptDocRef(scriptName).orElse(null));
  }

  private boolean docAppScriptExists(@Nullable DocumentReference appScriptDocRef) {
    boolean existsAppScriptDoc = modelAccess.exists(appScriptDocRef);
    boolean isNotEmptyAppScriptDoc = !emptyCheck.isEmptyRTEDocument(appScriptDocRef);
    LOGGER.debug("docAppScriptExists check [{}]: exists [{}] isNotEmpty [{}]", appScriptDocRef,
        existsAppScriptDoc, isNotEmptyAppScriptDoc);
    return (existsAppScriptDoc && isNotEmptyAppScriptDoc);
  }

  @Override
  public Optional<DocumentReference> getAppScriptDocRef(String scriptName) {
    if (hasLocalAppScript(scriptName)) {
      return getLocalAppScriptDocRef(scriptName);
    } else {
      return getCentralAppScriptDocRef(scriptName);
    }
  }

  @Override
  public Optional<DocumentReference> getAppRecursiveScriptDocRef(String scriptName) {
    return findAppScriptRecursivly(scriptName,
        sn -> hasLocalAppRecursiveScript(sn)
            || hasCentralAppRecursiveScript(sn),
        sn -> sn.lastIndexOf("/") > 0).flatMap(scriptNameFound -> {
          if (hasLocalAppRecursiveScript(scriptNameFound)) {
            return getLocalAppRecursiveScriptDocRef(scriptNameFound);
          } else if (hasCentralAppRecursiveScript(scriptNameFound)) {
            return getCentralAppRecursiveScriptDocRef(scriptNameFound);
          }
          return Optional.empty();
        });
  }

  @Override
  public Optional<DocumentReference> getLocalAppScriptDocRef(String scriptName) {
    return RefBuilder.from(mContext.getWikiRef()).space(APP_SCRIPT_SPACE_NAME).doc(scriptName)
        .buildOpt(DocumentReference.class);
  }

  @Override
  public Optional<DocumentReference> getCentralAppScriptDocRef(String scriptName) {
    return RefBuilder.from(XWikiConstant.CENTRAL_WIKI).space(APP_SCRIPT_SPACE_NAME).doc(scriptName)
        .buildOpt(DocumentReference.class);
  }

  @Override
  public Optional<DocumentReference> getLocalAppRecursiveScriptDocRef(String scriptName) {
    return RefBuilder.from(mContext.getWikiRef()).space(APP_RECURSIVE_SCRIPT_SPACE_NAME)
        .doc(scriptName).buildOpt(DocumentReference.class);
  }

  @Override
  public Optional<DocumentReference> getCentralAppRecursiveScriptDocRef(String scriptName) {
    return RefBuilder.from(XWikiConstant.CENTRAL_WIKI).space(APP_RECURSIVE_SCRIPT_SPACE_NAME)
        .doc(scriptName).buildOpt(DocumentReference.class);
  }

  @Override
  public String getAppScriptTemplatePath(String scriptName) {
    return "celAppScripts/" + scriptName + ".vm";
  }

  @Override
  public boolean isAppScriptAvailable(String scriptName) {
    try {
      String path = "/templates/" + getAppScriptTemplatePath(scriptName);
      LOGGER.debug("isAppScriptAvailable: check on [{}].", path);
      wikiProvider.await(Duration.ofSeconds(60)).getResourceContentAsBytes(path);
      LOGGER.trace("isAppScriptAvailable: Successful got app script [{}].", scriptName);
      return true;
    } catch (IOException | ExecutionException exp) {
      LOGGER.debug("isAppScriptAvailable: Failed to get app script [{}].", scriptName, exp);
      return false;
    }
  }

  @Override
  public Optional<String> getAppRecursiveScript(String scriptName) {
    return findAppScriptRecursivly(scriptName,
        sn -> isAppScriptAvailable(sn + "++"),
        sn -> sn.lastIndexOf("/") > 0)
            .map(sNT -> sNT + "++")
            .filter(sNT -> !Strings.isNullOrEmpty(sNT) && isAppScriptAvailable(sNT));
  }

  private Optional<String> findAppScriptRecursivly(String scriptName,
      Predicate<String> hasFound, Predicate<String> hasMore) {
    String scriptNameTest = scriptName;
    do {
      scriptNameTest = reduceOneDirectory(scriptNameTest);
    } while (!hasFound.test(scriptNameTest) && hasMore.test(scriptNameTest));
    if (hasFound.test(scriptNameTest)) {
      return Optional.of(scriptNameTest);
    }
    return Optional.empty();
  }

  private String reduceOneDirectory(String scriptNameTest) {
    if (!Strings.isNullOrEmpty(scriptNameTest) && (scriptNameTest.lastIndexOf("/") > -1)) {
      return Strings.emptyToNull(scriptNameTest.substring(0, scriptNameTest.lastIndexOf("/")));
    } else {
      return null;
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
      return urlService.getURL(
          modelUtils.resolveRef(scriptName.replace("/", "."), DocumentReference.class),
          "view", queryString);
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
        || isAppScriptOverwriteDocRef(mContext.getDocRef().orElse(null));
  }

  @Override
  public boolean isAppScriptOverwriteDocRef(DocumentReference docRef) {
    String overwriteAppDocs = wikiProvider.get()
        .map(wiki -> wiki.getXWikiPreference(APP_SCRIPT_XWPREF_OVERW_DOCS,
            APP_SCRIPT_CONF_OVERW_DOCS, "-", getContext()))
        .orElse("-");
    List<DocumentReference> overwAppDocList = new ArrayList<>();
    if (!"-".equals(overwriteAppDocs)) {
      for (String overwAppDocFN : overwriteAppDocs.split("[, ]")) {
        try {
          DocumentReference overwAppDocRef = modelUtils.resolveRef(overwAppDocFN,
              DocumentReference.class);
          overwAppDocList.add(overwAppDocRef);
        } catch (Exception exp) {
          LOGGER.warn("Failed to parse appScript overwrite docs config part [{}] of complete"
              + " config [{}].", overwAppDocFN, overwriteAppDocs);
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
        && mContext.getDocRef().orElseThrow().getSpaceReferences().contains(
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
    } else if (isAppScriptOverwriteDocRef(mContext.getDocRef().orElse(null))) {
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
