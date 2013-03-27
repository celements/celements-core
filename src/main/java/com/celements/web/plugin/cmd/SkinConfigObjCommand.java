package com.celements.web.plugin.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.inheritor.FieldInheritor;
import com.celements.inheritor.InheritorFactory;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class SkinConfigObjCommand {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      SkinConfigObjCommand.class);

  IWebUtilsService injected_webService;

  public FieldInheritor getSkinConfigFieldInheritor(String fallbackClassName) {
    XWikiDocument doc = getContext().getDoc();
    try {
      String className = getSkinConfigClassNameFromSkinDoc(fallbackClassName);
      return new InheritorFactory().getConfigDocFieldInheritor(
          className, getWebService().getRefLocalSerializer().serialize(
              doc.getDocumentReference()), getContext());
    } catch(XWikiException exp){
      LOGGER.error("Failed to get skin-config object.", exp);
    }
    return null;
  }

  public BaseObject getSkinConfigObj() {
    return getSkinConfigObj("");
  }

  public BaseObject getSkinConfigObj(String fallbackClassName) {
    XWikiDocument doc = getContext().getDoc();
    try {
      String className = getSkinConfigClassNameFromSkinDoc(fallbackClassName);
      if ((className != null) && !"".equals(className)) {
        BaseObject configObj = WebUtils.getInstance().getConfigDocByInheritance(doc,
            className, getContext()).getObject(className);
        return configObj;
      }
    } catch(XWikiException e){
      LOGGER.error(e);
    }
    return null;
  }

  private String getSkinConfigClassNameFromSkinDoc(String fallbackClassName)
      throws XWikiException {
    XWiki xwiki = getContext().getWiki();
    XWikiDocument skinDoc = xwiki.getDocument(getWebService().resolveDocumentReference(
        xwiki.getSpacePreference("skin", getContext())), getContext());
    BaseObject skinObj = skinDoc.getXObject(getSkinsClassRef());
    String className = fallbackClassName;
    if (skinObj != null) {
      String skinConfigClassName = skinObj.getStringValue("skin_config_class_name");
      if ((skinConfigClassName != null) && !"".equals(skinConfigClassName)) {
        className = skinConfigClassName;
      }
    }
    return className;
  }

  public DocumentReference getSkinsClassRef() {
    return getWebService().resolveDocumentReference("XWiki.XWikiSkins");
  }

  private IWebUtilsService getWebService() {
    if (injected_webService != null) {
      return injected_webService;
    }
    return Utils.getComponent(IWebUtilsService.class);
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext)getExecution().getContext().getProperty("xwikicontext");
  }

}
