package com.celements.web.service;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.ExternalJavaScriptFilesCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("javascript")
public class JSScriptService implements ScriptService {
  
  public static final String JAVA_SCRIPT_FILES_COMMAND_KEY =
      "com.celements.web.ExternalJavaScriptFilesCommand";

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public String getAllExternalJavaScriptFiles() throws XWikiException {
    return getExtJavaScriptFileCmd().getAllExternalJavaScriptFiles();
  }
  
  public String addExtJSfileOnce(String jsFile) {
    return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile);
  }

  public String addExtJSfileOnce(String jsFile, String action) {
    return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile, action);
  }
  
  private ExternalJavaScriptFilesCommand getExtJavaScriptFileCmd() {
    if (getContext().get(JAVA_SCRIPT_FILES_COMMAND_KEY) == null) {
      getContext().put(JAVA_SCRIPT_FILES_COMMAND_KEY, new ExternalJavaScriptFilesCommand(
          getContext()));
    }
    return (ExternalJavaScriptFilesCommand) getContext().get(
        JAVA_SCRIPT_FILES_COMMAND_KEY);
  }
}
