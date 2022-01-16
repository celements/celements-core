package com.celements.javascript;

import javax.annotation.Nullable;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.ExternalJavaScriptFilesCommand;
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
    return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile);
  }

  /**
   * @deprecated since 5.4 instead use {@link includeExtJsFile(ExtJsFileParameter)}
   */
  @Deprecated
  public String addExtJSfileOnce(@Nullable String jsFile, @Nullable String action) {
    return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile, action);
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
    return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile, action, params);
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

  /**
   * @deprecated since 5.4 instead use {@link includeExtJsFile(ExtJsFileParameter)}
   */
  @Deprecated
  public String addLazyExtJSfile(@Nullable String jsFile) {
    return getExtJavaScriptFileCmd().addLazyExtJSfile(jsFile);
  }

  /**
   * @deprecated since 5.4 instead use {@link includeExtJsFile(ExtJsFileParameter)}
   */
  @Deprecated
  public String addLazyExtJSfile(@Nullable String jsFile, @Nullable String action) {
    return getExtJavaScriptFileCmd().addLazyExtJSfile(jsFile, action);
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
    return getExtJavaScriptFileCmd().addLazyExtJSfile(jsFile, action, params);
  }

  private ExternalJavaScriptFilesCommand getExtJavaScriptFileCmd() {
    if (getContext().get(JAVA_SCRIPT_FILES_COMMAND_KEY) == null) {
      getContext().put(JAVA_SCRIPT_FILES_COMMAND_KEY, new ExternalJavaScriptFilesCommand());
    }
    return (ExternalJavaScriptFilesCommand) getContext().get(JAVA_SCRIPT_FILES_COMMAND_KEY);
  }
}
