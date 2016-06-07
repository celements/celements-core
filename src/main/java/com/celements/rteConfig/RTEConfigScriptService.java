package com.celements.rteConfig;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.emptycheck.internal.IDefaultEmptyDocStrategyRole;
import com.xpn.xwiki.XWikiException;

@Component("rteconfig")
public class RTEConfigScriptService implements ScriptService {

  @Requirement
  IDefaultEmptyDocStrategyRole defaultEmptyDocStrategyRole;

  private static Logger _LOGGER = LoggerFactory.getLogger(RTEConfigScriptService.class);

  public String getRTEConfigField(String name) {
    try {
      return new RTEConfig().getRTEConfigField(name);
    } catch (XWikiException exp) {
      _LOGGER.error("getRTEConfigField for name [" + name + "] failed.", exp);
    }
    return "";
  }

  public boolean isEmptyRTEString(String rteContent) {
    return defaultEmptyDocStrategyRole.isEmptyRTEString(rteContent);
  }

  public List<DocumentReference> getRTEConfigsList() {
    return new RTEConfig().getRTEConfigsList();
  }
}
