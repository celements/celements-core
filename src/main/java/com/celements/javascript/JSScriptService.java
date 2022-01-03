package com.celements.javascript;

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

  public String addExtJSfileOnce(String jsFile) {
    return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile);
  }

  public String addExtJSfileOnce(String jsFile, String action) {
    return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile, action);
  }

  public String addExtJSfileOnceDefer(String jsFile) {
    return getExtJavaScriptFileCmd().addExtJSfileOnceDefer(jsFile);
  }

  public String addExtJSfileOnceDefer(String jsFile, String action) {
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
   */
  public String addExtJSfileOnce(String jsFile, String action, String params) {
    return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile, action, params);
  }

  public String addLazyExtJSfile(String jsFile) {
    return getExtJavaScriptFileCmd().addLazyExtJSfile(jsFile);
  }

  public String addLazyExtJSfile(String jsFile, String action) {
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
   */
  public String addLazyExtJSfile(String jsFile, String action, String params) {
    return getExtJavaScriptFileCmd().addLazyExtJSfile(jsFile, action, params);
  }

  private ExternalJavaScriptFilesCommand getExtJavaScriptFileCmd() {
    if (getContext().get(JAVA_SCRIPT_FILES_COMMAND_KEY) == null) {
      getContext().put(JAVA_SCRIPT_FILES_COMMAND_KEY, new ExternalJavaScriptFilesCommand(
          getContext()));
    }
    return (ExternalJavaScriptFilesCommand) getContext().get(JAVA_SCRIPT_FILES_COMMAND_KEY);
  }
}
