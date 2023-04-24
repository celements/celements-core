package com.celements.webform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;

@Component("action")
public class ActionScriptService implements ScriptService {

  @Requirement
  private IActionServiceRole actionService;

  @Requirement
  private Execution execution;

  public boolean executeAction(Document actionDoc) {
    return actionService.executeAction(actionDoc, getContext().getRequest().getParameterMap(),
        getContext().getDoc(), getContext());
  }

  public boolean executeAction(Document actionDoc, Map<String, List<Object>> fakeRequestMap) {
    Map<String, String[]> requestMap = new HashMap<>();
    for (String key : fakeRequestMap.keySet()) {
      if (fakeRequestMap.get(key) != null) {
        ArrayList<String> stringArray = new ArrayList<>(fakeRequestMap.get(key).size());
        for (Object value : fakeRequestMap.get(key)) {
          stringArray.add(value.toString());
        }
        requestMap.put(key, stringArray.toArray(new String[0]));
      }
    }
    return actionService.executeAction(actionDoc, requestMap, getContext().getDoc(), getContext());
  }

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }
}
