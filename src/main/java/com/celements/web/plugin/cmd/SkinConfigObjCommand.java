/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.plugin.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(SkinConfigObjCommand.class);

  IWebUtilsService injected_webService;

  public FieldInheritor getSkinConfigFieldInheritor(String fallbackClassName) {
    XWikiDocument doc = getContext().getDoc();
    try {
      String className = getSkinConfigClassNameFromSkinDoc(fallbackClassName);
      return new InheritorFactory().getConfigDocFieldInheritor(className,
          getWebService().getRefLocalSerializer().serialize(doc.getDocumentReference()),
          getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get skin-config object.", exp);
    }
    return null;
  }

  public BaseObject getSkinConfigObj() {
    return getSkinConfigObj("");
  }

  public BaseObject getSkinConfigObj(String fallbackClassName) {
    XWikiDocument doc = getContext().getDoc();
    LOGGER.trace("getSkinConfigObj: start with fallbackClassName [" + fallbackClassName
        + "] for context doc [" + doc.getDocumentReference() + "].");
    try {
      String className = getSkinConfigClassNameFromSkinDoc(fallbackClassName);
      if ((className != null) && !"".equals(className)) {
        BaseObject configObj = WebUtils.getInstance().getConfigDocByInheritance(doc, className,
            getContext()).getObject(className);
        return configObj;
      }
    } catch (XWikiException e) {
      LOGGER.error("failed", e);
    }
    return null;
  }

  private String getSkinConfigClassNameFromSkinDoc(String fallbackClassName) throws XWikiException {
    XWiki xwiki = getContext().getWiki();
    XWikiDocument skinDoc = xwiki.getDocument(getWebService().resolveDocumentReference(
        xwiki.getSpacePreference("skin", getContext())), getContext());
    BaseObject skinObj = skinDoc.getXObject(getSkinsClassRef(
        skinDoc.getDocumentReference().getLastSpaceReference().getParent().getName()));
    LOGGER.debug("getSkinConfigClassNameFromSkinDoc: skinObj [" + skinObj + "] found for"
        + " skinDoc [" + skinDoc.getDocumentReference() + "].");
    String className = fallbackClassName;
    if (skinObj != null) {
      String skinConfigClassName = skinObj.getStringValue("skin_config_class_name");
      if ((skinConfigClassName != null) && !"".equals(skinConfigClassName)) {
        className = skinConfigClassName;
      }
    }
    LOGGER.info("getSkinConfigClassNameFromSkinDoc returning className [" + className + "].");
    return className;
  }

  public DocumentReference getSkinsClassRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiSkins");
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
    return (XWikiContext) getExecution().getContext().getProperty("xwikicontext");
  }

}
