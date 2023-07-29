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

import static java.util.function.Predicate.*;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.execution.XWikiExecutionProp;
import com.celements.init.XWikiProvider;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.util.ModelUtils;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class MultilingualMenuNameCommand {

  public static final String CELEMENTS_MENU_NAME = "Celements2.MenuName";
  public static final String MENU_NAME_CLASS_SPACE = "Celements2";
  public static final String MENU_NAME_CLASS_DOC = "MenuName";

  private final AttachmentURLCommand attCmd;

  public DocumentReference getMenuNameClassRef() {
    return new DocumentReference(getContext().getDatabase(), MENU_NAME_CLASS_SPACE,
        MENU_NAME_CLASS_DOC);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(MultilingualMenuNameCommand.class);

  public MultilingualMenuNameCommand() {
    this(new AttachmentURLCommand());
  }

  public MultilingualMenuNameCommand(AttachmentURLCommand attCmd) {
    this.attCmd = attCmd;
  }

  public String getMultilingualMenuName(String fullName, String language, XWikiContext context) {
    LOGGER.debug("getMultilingualMenuName: for '{}' and lang '{}'", fullName, language);
    DocumentReference docRef = getModelUtils().resolveRef(fullName, DocumentReference.class);
    return getMenuNameFromBaseObject(docRef, language, false);
  }

  public String getMultilingualMenuName(com.xpn.xwiki.api.Object menuItem, String language,
      XWikiContext context) {
    if (menuItem != null) {
      return getMultilingualMenuName(menuItem.getXWikiObject(), language, false, context);
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
    // TODO write tests
    String menuName = "";
    if (menuItem != null) {
      DocumentReference docRef = menuItem.getDocumentReference();
      menuName = getMenuNameFromBaseObject(docRef, language, allowEmptyMenuNames);
    }
    return menuName;
  }

  private String getMenuNameFromBaseObject(DocumentReference docRef, String language,
      boolean allowEmptyMenuNames) {
    BaseObject menuNameObj = getMenuNameBaseObject(docRef, language);
    LOGGER.debug("getMenuNameFromBaseObject: for '{}' and has object '{}'", docRef,
        menuNameObj != null);
    String menuName = "";
    if (menuNameObj != null) {
      menuName = menuNameObj.getStringValue("menu_name").trim();
    }
    if (!allowEmptyMenuNames && menuName.isEmpty()) {
      menuName = Stream.<Supplier<Optional<XWikiDocument>>>of(
          () -> getModelAccess().getDocumentOpt(docRef, language),
          () -> getModelAccess().getDocumentOpt(docRef))
          .map(Supplier::get).flatMap(Optional::stream)
          .map(XWikiDocument::getTitle)
          .filter(not(String::isBlank))
          .findFirst()
          .orElseGet(() -> getFallbackMenuName(docRef));
    }
    LOGGER.info("getMenuNameFromBaseObject: for '{}' returning '{}'", docRef, menuName);
    return menuName;
  }

  private String getFallbackMenuName(DocumentReference docRef) {
    String dictKey = "menuname_" + getModelUtils().serializeRefLocal(docRef);
    String menuNameDict = getWebUtilsService().getAdminMessageTool().get(dictKey);
    LOGGER.debug("Dictionary MenuName [{}] for [{}] and key [{}].", menuNameDict, docRef, dictKey);
    // if menuName is empty and no dictionary entry available, give back the doc name
    return !dictKey.equals(menuNameDict) ? menuNameDict : docRef.getName();
  }

  public String getMultilingualMenuNameOnly(String fullName, String language,
      boolean allowEmptyMenuNames, XWikiContext context) {
    DocumentReference docRef = getModelUtils().resolveRef(fullName, DocumentReference.class);
    return getMenuNameFromBaseObject(docRef, language, allowEmptyMenuNames);
  }

  /**
   * @param fullName
   * @param language
   * @param context
   * @return
   * @throws XWikiException
   * @deprecated since 2.14.0 use getMenuNameBaseObject instead
   */
  @Deprecated
  public BaseObject getMultilingualMenuNameOnly(String fullName, String language,
      XWikiContext context) throws XWikiException {
    return getMenuNameBaseObject(fullName, language);
  }

  /**
   * @deprecated since 6.0 use {@link #getMenuNameBaseObject(DocumentReference, String)}
   */
  @Deprecated
  public BaseObject getMenuNameBaseObject(String fullName, String language) throws XWikiException {
    return getMenuNameBaseObject(getModelUtils().resolveRef(fullName, DocumentReference.class),
        language);
  }

  public BaseObject getMenuNameBaseObject(DocumentReference docRef, String language) {
    XWikiDocument menuItemDoc = getModelAccess().getOrCreateDocument(docRef);
    BaseObject menuNameObj = null;
    if (getXWiki().isMultiLingual(getContext())
        & (menuItemDoc.getObject(CELEMENTS_MENU_NAME) != null)) {
      menuNameObj = menuItemDoc.getObject(CELEMENTS_MENU_NAME, "lang", language);
      if ((menuNameObj == null) || "".equals(menuNameObj.getStringValue("menu_name"))) {
        String spaceDefaultLanguage = getXWiki().getSpacePreference("default_language",
            menuItemDoc.getDocumentReference().getLastSpaceReference().getName(), "", getContext());
        menuNameObj = menuItemDoc.getObject(CELEMENTS_MENU_NAME, "lang", spaceDefaultLanguage,
            false);
      }
    }
    return menuNameObj;
  }

  public String addNavImageStyle(String fullName, String language, XWikiContext context)
      throws XWikiException {
    String menuItemHTML = "";
    BaseObject menuNameObj = getMenuNameBaseObject(fullName, language);
    if (menuNameObj != null) {
      String attURL = attCmd.getAttachmentURL(menuNameObj.getStringValue("image"), context);
      if ((attURL != null) && (!"".equals(attURL))) {
        menuItemHTML += " style=\"background-image:url(" + attURL + ")\"";
      }
    }
    return menuItemHTML.trim();
  }

  public String addToolTip(String fullName, String language, XWikiContext context)
      throws XWikiException {
    BaseObject menuNameObj = getMenuNameBaseObject(fullName, language);
    if (menuNameObj != null) {
      String tooltip = menuNameObj.getStringValue("tooltip");
      if ((tooltip != null) && (!"".equals(tooltip))) {
        return "title=\"" + tooltip + "\"";
      }
    }
    return "";
  }

  private XWiki getXWiki() {
    return Utils.getComponent(XWikiProvider.class).get().orElseThrow();
  }

  private XWikiContext getContext() {
    return Utils.getComponent(Execution.class).getContext()
        .get(XWikiExecutionProp.XWIKI_CONTEXT).orElseThrow();
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  private ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
