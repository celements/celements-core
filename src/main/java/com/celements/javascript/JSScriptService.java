package com.celements.javascript;

import javax.annotation.Nullable;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.ExternalJavaScriptFilesCommand;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;

@Component("javascript")
public class JSScriptService implements ScriptService {

  public static final String JAVA_SCRIPT_FILES_COMMAND_KEY = "com.celements.web.ExternalJavaScriptFilesCommand";

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public String getAllExternalJavaScriptFiles() {
    return getExtJavaScriptFileCmd().getAllExternalJavaScriptFiles();
  }

  /**
   * @deprecated since 5.4 instead use {@link includeExtJsFile(ExtJsFileParameter)}
   */
  @Deprecated
  public String addExtJSfileOnce(@Nullable String jsFile) {
    if (!Strings.isNullOrEmpty(jsFile)) {
      return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile);
    }
    return "<!-- addExtJSfileOnce(String) called with null or empty jsFile -->";
  }

  /**
   * @deprecated since 5.4 instead use {@link includeExtJsFile(ExtJsFileParameter)}
   */
  @Deprecated
  public String addExtJSfileOnce(@Nullable String jsFile, @Nullable String action) {
    if (!Strings.isNullOrEmpty(jsFile)) {
      return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile, action);
    }
    return "<!-- addExtJSfileOnce(String, String) called with null or empty jsFile -->";
  }

  /**
   * addExtJSfileOnce
   *
   * @param jsFile
   * @param action
   *          use empty string for default action
   * @param params
   * @return
   * @deprecated since 5.4 instead use {@link includeExtJsFile(ExtJsFileParameter)}
   */
  @Deprecated
  public String addExtJSfileOnce(@Nullable String jsFile, @Nullable String action,
      @Nullable String params) {
    if (!Strings.isNullOrEmpty(jsFile)) {
      return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile, action, params);
    }
    return "<!-- addExtJSfileOnce(String, String, String) called with null or empty jsFile -->";
  }

  public ExtJsFileParameter.Builder createExtJSParam() {
    return new ExtJsFileParameter.Builder();
  }

  public String includeExtJsFile(@Nullable ExtJsFileParameter.Builder extJsFileParams) {
    if (extJsFileParams != null) {
      return getExtJavaScriptFileCmd().includeExtJsFile(extJsFileParams.build());
    }
    return "<!-- includeExtJsFile(ExtJsFileParameter.Builder) called with null  -->";
  }

  public String includeExtJsFile(@Nullable ExtJsFileParameter extJsFileParams) {
    if (extJsFileParams != null) {
      return getExtJavaScriptFileCmd().includeExtJsFile(extJsFileParams);
    }
    return "<!-- includeExtJsFile(ExtJsFileParameter) called with null  -->";
  }

  public JsLoadMode getJsLoadMode(String loadMode) {
    return JsLoadMode.valueOf(loadMode);
  }

  /**
   * @deprecated since 5.4 instead use {@link includeExtJsFile(ExtJsFileParameter)}
   */
  @Deprecated
  public String addLazyExtJSfile(@Nullable String jsFile) {
    if (!Strings.isNullOrEmpty(jsFile)) {
      return getExtJavaScriptFileCmd().addLazyExtJSfile(jsFile);
    }
    return "<!-- addLazyExtJSfile(String) called with null or empty jsFile -->";
  }

  /**
   * @deprecated since 5.4 instead use {@link includeExtJsFile(ExtJsFileParameter)}
   */
  @Deprecated
  public String addLazyExtJSfile(@Nullable String jsFile, @Nullable String action) {
    if (!Strings.isNullOrEmpty(jsFile)) {
      return getExtJavaScriptFileCmd().addLazyExtJSfile(jsFile, action);
    }
    return "<!-- addLazyExtJSfile(String, String) called with null or empty jsFile -->";
  }

  /**
   * addLazyExtJSfile
   *
   * @param jsFile
   * @param action
   *          use empty string for default action
   * @param params
   * @return
   * @deprecated since 5.4 instead use {@link includeExtJsFile(ExtJsFileParameter)}
   */
  @Deprecated
  public String addLazyExtJSfile(@Nullable String jsFile, @Nullable String action,
      @Nullable String params) {
    if (!Strings.isNullOrEmpty(jsFile)) {
      return getExtJavaScriptFileCmd().addLazyExtJSfile(jsFile, action, params);
    }
    return "<!-- addLazyExtJSfile(String, String, String) called with null or empty jsFile -->";
  }

  private ExternalJavaScriptFilesCommand getExtJavaScriptFileCmd() {
    if (getContext().get(JAVA_SCRIPT_FILES_COMMAND_KEY) == null) {
      getContext().put(JAVA_SCRIPT_FILES_COMMAND_KEY, new ExternalJavaScriptFilesCommand());
    }
    return (ExternalJavaScriptFilesCommand) getContext().get(JAVA_SCRIPT_FILES_COMMAND_KEY);
  }
}
