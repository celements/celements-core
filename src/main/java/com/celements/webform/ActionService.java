package com.celements.webform;

import static com.celements.model.util.References.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.VelocityManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component
public class ActionService implements IActionServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActionService.class);

  @Requirement
  IModelAccessFacade modelAccess;

  @Requirement
  private ConfigurationSource configSource;

  @Requirement
  private Execution execution;

  @Override
  public boolean executeAction(Document actionDoc, Map<String, String[]> request,
      XWikiDocument includingDoc, XWikiContext context) {
    LOGGER.info("Executing action on doc '" + actionDoc.getFullName() + "'");
    VelocityContext vcontext = getVelocityManager().getVelocityContext();
    vcontext.put("theDoc", actionDoc);
    Object vDebugBefore = vcontext.get("debug");
    final Object configScriptDebug = configSource.getProperty("celements.action.scriptDebug");
    if (configScriptDebug != null) {
      vcontext.put("debug", configScriptDebug);
    }
    Object req = vcontext.get("request");
    vcontext.put("request", getApiUsableMap(request));
    String execContent = "";
    String actionContent = "";
    XWikiDocument execAct = null;
    try {
      // TODO reimplement the celements2web:Macros.executeActions in Java
      execAct = modelAccess.getDocument(create(DocumentReference.class, "executeActions", create(
          EntityType.SPACE, "Macros", create(EntityType.WIKI, "celements2web"))));
      final Object configJavaDebug = configSource.getProperty("celements.action.javaDebug");
      vcontext.put("javaDebug", configJavaDebug);
      execContent = execAct.getContent();
      execContent = execContent.replaceAll("\\{(/?)pre\\}", "");
      actionContent = context.getWiki().getRenderingEngine().interpretText(execContent,
          includingDoc, context);
    } catch (DocumentNotExistsException notExistsExp) {
      LOGGER.error("Could not get action Macro", notExistsExp);
    }
    final Object successfulObj = vcontext.get("successful");
    final boolean successful = (successfulObj != null) && "true".equals(successfulObj.toString());
    if (!successful) {
      LOGGER.error("executeAction: Error executing action. Output: {}", vcontext.get(
          "actionScriptOutput"));
      LOGGER.error("executeAction: Rendered Action Script: {}", actionContent);
      LOGGER.error("executeAction: execAct == {}", execAct);
      LOGGER.error("executeAction: includingDoc: {}", includingDoc);
      LOGGER.error("executeAction: execContent length: {}", execContent.length());
      LOGGER.error("executeAction: execContent length: {}", actionContent.length());
      LOGGER.error("executeAction: vcontext (in variable) {}", vcontext);
      LOGGER.error("executeAction: vcontext (in context) {}",
          getVelocityManager().getVelocityContext());
    }
    vcontext.put("debug", vDebugBefore);
    vcontext.put("request", req);
    return successful;
  }

  // FIXME Hack to get mail execution to work. The script is not expecting arrays in the
  // map, since it expects a request. Multiple values with the same name get lost
  // in this "quick and dirty" fix
  private Object getApiUsableMap(Map<String, String[]> request) {
    Map<String, String> apiConform = new HashMap<>();
    for (String key : request.keySet()) {
      if ((request.get(key) != null) && (request.get(key).length > 0)) {
        apiConform.put(key, request.get(key)[0]);
      } else {
        apiConform.put(key, null);
      }
    }
    return apiConform;
  }

  VelocityManager getVelocityManager() {
    return Utils.getComponent(VelocityManager.class);
  }
}
