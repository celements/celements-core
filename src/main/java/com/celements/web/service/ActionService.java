package com.celements.web.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class ActionService implements IActionServiceRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      ActionService.class);
  
  @Requirement
  private IWebUtilsService webUtilsService;
  
  @Requirement
  private Execution execution;
  
  public boolean executeAction(Document actionDoc, Map<String, String[]> request, 
      XWikiDocument includingDoc, XWikiContext context) {
    LOGGER.info("Executing action on doc '" + actionDoc.getFullName() + "'");
    VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
    vcontext.put("theDoc", actionDoc);
    Boolean debug = (Boolean)vcontext.get("debug");
    vcontext.put("debug", true);
    Boolean hasedit = (Boolean)vcontext.get("hasedit");
    vcontext.put("hasedit", true);
    Object req = vcontext.get("request");
    vcontext.put("request", getApiUsableMap(request));
    XWikiDocument execAct = null;
    try {
      execAct = context.getWiki().getDocument(webUtilsService.resolveDocumentReference(
              "celements2web:Macros.executeActions"), context);
    } catch (XWikiException e) {
      LOGGER.error("Could not get action Macro", e);
    }
    String actionContent = "";
    if(execAct != null) {
      String execContent = execAct.getContent();
      execContent = execContent.replaceAll("\\{(/?)pre\\}", "");
      actionContent = context.getWiki().getRenderingEngine().interpretText(
          execContent, includingDoc, context);
    }
    Object successfulObj = vcontext.get("successful");
    boolean successful = (successfulObj != null)
                          && "true".equals(successfulObj.toString());
    if(!successful) {
      LOGGER.error("Error executing action. Output:" + vcontext.get("actionScriptOutput"));
      LOGGER.error("Rendered Action Script: " + actionContent);
    }
    vcontext.put("debug", debug);
    vcontext.put("hasedit", hasedit);
    vcontext.put("request", req);
    return successful;
  }
  
  //FIXME Hack to get mail execution to work. The script is not expecting arrays in the
  //      map, since it expects a request. Multiple values with the same name get lost 
  //      in this "quick and dirty" fix
  private Object getApiUsableMap(Map<String, String[]> request) {
    Map<String, String> apiConform = new HashMap<String, String>();
    for (String key : request.keySet()) {
      if((request.get(key) != null) && (request.get(key).length > 0)) {
        apiConform.put(key, request.get(key)[0]);
      } else {
        apiConform.put(key, null);
      }
    }
    return apiConform;
  }
}
