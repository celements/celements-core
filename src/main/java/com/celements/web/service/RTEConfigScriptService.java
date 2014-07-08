package com.celements.web.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.emptycheck.internal.IDefaultEmptyDocStrategyRole;
import com.celements.rteConfig.RTEConfig;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

@Component("rteconfig")
public class RTEConfigScriptService implements ScriptService{

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      RTEConfigScriptService.class);
  
  public String getRTEConfigField(String name) {
    try {
      return new RTEConfig().getRTEConfigField(name);
    } catch (XWikiException exp) {
      LOGGER.error("getRTEConfigField for name [" + name + "] failed.", exp);
    }
    return "";
  }
  
  public boolean isEmptyRTEString(String rteContent) {
    return getDefaultEmptyCheckService().isEmptyRTEString(rteContent);
  }
  
  public List<DocumentReference> getRTEConfigsList() {
    return new RTEConfig().getRTEConfigsList();
  }
  
  private IDefaultEmptyDocStrategyRole getDefaultEmptyCheckService() {
    return Utils.getComponent(IDefaultEmptyDocStrategyRole.class);
  }
}
