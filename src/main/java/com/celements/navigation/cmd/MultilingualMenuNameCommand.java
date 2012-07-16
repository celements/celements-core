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
package com.celements.navigation.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class MultilingualMenuNameCommand {

  public static final String CELEMENTS_MENU_NAME = "Celements2.MenuName";
  public static final String MENU_NAME_CLASS_SPACE = "Celements2";
  public static final String MENU_NAME_CLASS_DOC = "MenuName";

  public DocumentReference getMenuNameClassRef() {
    return new DocumentReference(getContext().getDatabase(), MENU_NAME_CLASS_SPACE,
        MENU_NAME_CLASS_DOC);
  }

  private static Log mLogger = LogFactory.getFactory().getInstance(
      MultilingualMenuNameCommand.class);

  private IWebUtils webUtils = WebUtils.getInstance();

  public MultilingualMenuNameCommand() {
  }

  public String getMultilingualMenuName(String fullName, String language,
      XWikiContext context) {
    try {
      return getMenuNameFromBaseObject(fullName, getMultilingualMenuNameOnly(fullName,
          language, context), false, context);
    } catch (XWikiException exp) {
      mLogger.error(exp);
      return "";
    }
  }

  public String getMultilingualMenuName(com.xpn.xwiki.api.Object menuItem,
      String language, XWikiContext context) {
    if (menuItem != null) {
      return getMultilingualMenuName(menuItem.getXWikiObject(), language, false,
        context);
    } else {
      return "";
    }
  }

  public String getMultilingualMenuName(BaseObject menuItem, String language,
      XWikiContext context) {
    return getMultilingualMenuName(menuItem, language, false, context);
  }

  public String getMultilingualMenuName(BaseObject menuItem, String language,
      boolean allowEmptyMenuNames, XWikiContext context) {
    //TODO write tests
    String menuName = "";
    if (menuItem != null) {
      String docFullName = menuItem.getName();
      try {
        menuName = getMenuNameFromBaseObject(docFullName, getMultilingualMenuNameOnly(
            docFullName, language, context), allowEmptyMenuNames, context);
      } catch (XWikiException e) {
        mLogger.error(e);
      }
    }
    return menuName;
  }

  private String getMenuNameFromBaseObject(String fullName, BaseObject menuNameObj,
      boolean allowEmptyMenuNames, XWikiContext context) throws XWikiException {
    String menuName = "";
    if (menuNameObj != null) {
      menuName = menuNameObj.getStringValue("menu_name");
    }
    // if menuName is empty give back the DocURLname
    if ((!allowEmptyMenuNames) && "".equals(menuName)) {
      menuName = fullName.substring(fullName.indexOf('.') + 1);
    }
    return menuName;
  }

  public String getMultilingualMenuNameOnly(String fullName, String language,
      boolean allowEmptyMenuNames, XWikiContext context) {
    try {
      return getMenuNameFromBaseObject(fullName, getMultilingualMenuNameOnly(fullName,
          language, context), allowEmptyMenuNames, context);
    } catch (XWikiException exp) {
      mLogger.error("Failed to get MenuName for [" + fullName + "].", exp);
      if (allowEmptyMenuNames) {
        return "";
      } else {
        return fullName.split("\\.")[1];
      }
    }
  }

  /**
   * 
   * @param fullName
   * @param language
   * @param context
   * @return
   * @throws XWikiException
   * 
   * @deprecated since 2.14.0 use getMenuNameBaseObject instead
   */
  @Deprecated
  public BaseObject getMultilingualMenuNameOnly(String fullName, String language,
      XWikiContext context) throws XWikiException {
    BaseObject menuNameObj = getMenuNameBaseObject(fullName, language, context);
    return menuNameObj;
  }

  public BaseObject getMenuNameBaseObject(String fullName, String language,
      XWikiContext context) throws XWikiException {
    XWikiDocument menuItemDoc = context.getWiki().getDocument(fullName, context);
    BaseObject menuNameObj = null;
    if (context.getWiki().isMultiLingual(context) && (menuItemDoc != null)
        && (menuItemDoc.getObject(CELEMENTS_MENU_NAME) != null)) {
      menuNameObj = menuItemDoc.getObject(CELEMENTS_MENU_NAME, "lang", language);
      if ((menuNameObj == null) || "".equals(menuNameObj.getStringValue("menu_name"))) {
        menuNameObj = menuItemDoc.getObject(CELEMENTS_MENU_NAME, "lang", context
            .getWiki().getSpacePreference("default_language", context), false);
      }
    }
    return menuNameObj;
  }

  public String addNavImageStyle(String fullName, String language, XWikiContext context
      ) throws XWikiException {
    String menuItemHTML = "";
    BaseObject menuNameObj = getMenuNameBaseObject(fullName, language, context);
    if (menuNameObj != null) {
      String attURL = webUtils.getAttachmentURL(menuNameObj.getStringValue(
          "image"), context);
      if ((attURL != null) && (!"".equals(attURL))) {
        menuItemHTML += " style=\"background-image:url(" + attURL + ")\"";
      }
    }
    return menuItemHTML.trim();
  }

  public String addToolTip(String fullName, String language, XWikiContext context
      ) throws XWikiException {
    BaseObject menuNameObj = getMenuNameBaseObject(fullName, language, context);
    if (menuNameObj != null) {
      String tooltip = menuNameObj.getStringValue("tooltip");
      if ((tooltip != null) && (!"".equals(tooltip))) {
        return "title=\"" + tooltip + "\"";
      }
    }
    return "";
  }

  void inject_webUtils(IWebUtils webUtils) {
    this.webUtils = webUtils;
  }

  private XWikiContext getContext() {
    return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

}
