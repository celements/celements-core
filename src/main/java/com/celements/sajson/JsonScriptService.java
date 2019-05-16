package com.celements.sajson;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;

@Component("json")
public class JsonScriptService implements ScriptService {

  public JsonBuilder newBuilder() {
    return new JsonBuilder();
  }

}
