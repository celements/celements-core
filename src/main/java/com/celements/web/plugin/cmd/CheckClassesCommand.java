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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.observation.EventListener;

import com.celements.common.classes.CompositorComponent;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class CheckClassesCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CheckClassesCommand.class);

  public static final String CLASS_PANEL_CONFIG_CLASS = "Class.PanelConfigClass";

  public static final String MEDIALIB_CONFIG_CLASS_SPACE = "Classes";

  public static final String MEDIALIB_CONFIG_CLASS_NAME = "MediaLibConfigClass";

  public static final String MEDIALIB_CONFIG_CLASS = MEDIALIB_CONFIG_CLASS_SPACE + "."
      + MEDIALIB_CONFIG_CLASS_NAME;

  public void checkClasses(XWikiContext context) {
    try {
      getExtended_XWikiPreferencesClass(context);
      getExtended_XWikiSkinsClass(context);
      getXWikiUsersClass(context);
      getFormMailClass(context);
      getBannerClass(context);
      getFlashBannerClass(context);
      getUserCSSClass(context);
      getImportClass(context);
      getPhotoAlbumClass(context);
      getFilebaseTagClass(context);
      getFormActionClass(context);
      getFormConfigClass(context);
      getActionTypeClass(context);
      getFormStorageClass(context);
      getReceiverEMailClass(context);
      getJavaScriptExternalFilesClass(context);
      getContextMenuItemClass(context);
      getPanelConfigClass(context);
      getRTEConfigTypeClass(context);
      getRTEConfigTypePropertiesClass(context);
      getMediaLibConfigClass(context);
      getDocLibConfigClass(context);
      getTagValueClass(context);
      getTokenClass(context);
      getOverlayConfigClass(context);
    } catch (XWikiException e) {
      mLogger.fatal(e);
    }
    
    CompositorComponent compComponent = (CompositorComponent) Utils.getComponent(
        EventListener.class, "CompositerComponent");
    if(compComponent != null) {
      compComponent.checkAllClassCollections();
    }
  }

  protected BaseClass getContextMenuItemClass(XWikiContext context
      ) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Celements2.ContextMenuItemClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements2");
      doc.setName("ContextMenuItemClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Celements2.ContextMenuItemClass");
    needsUpdate |= bclass.addTextAreaField("cmi_link", "CMItem Link (velocity code)", 80,
        20);
    needsUpdate |= bclass.addTextField("cmi_text", "CMItem Link Name", 30);
    needsUpdate |= bclass.addTextField("cmi_icon", "optional CMItem icon", 30);
    needsUpdate |= bclass
        .addTextField("cmi_shortcut", "optional shortcut definition", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getPanelConfigClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument(CLASS_PANEL_CONFIG_CLASS, context);
    } catch (XWikiException e) {
      mLogger.error(e);
      String[] classNameParts = CLASS_PANEL_CONFIG_CLASS.split("\\.");
      doc = new XWikiDocument();
      doc.setSpace(classNameParts[0]);
      doc.setName(classNameParts[1]);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName(CLASS_PANEL_CONFIG_CLASS);
    needsUpdate |= bclass.addStaticListField("config_name", "Panel Config Type", 1,
        false, "leftPanels|rightPanels", "select");
    needsUpdate |= bclass.addBooleanField("show_panels", "Display the panel column",
        "yesno");
    needsUpdate |= bclass.addTextField("panels", "Panels displayed", 60);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getExtended_XWikiPreferencesClass(XWikiContext context)
      throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("XWiki.XWikiPreferences", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("XWiki");
      doc.setName("XWikiPreferences");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("XWiki.XWikiPreferences");

    needsUpdate |= bclass.addTextField("cel_centralfilebase", "Central Filebase", 30);
    needsUpdate |= bclass.addStaticListField("celements_version",
        "Celements Version Mode", 1, false, "---|celements2|celements3", "select");
    needsUpdate |= bclass.addTextField("cel_edittabs_config", "Tab Config", 30);
    needsUpdate |= bclass.addTextField("cellogin", "cellogin", 30);
    needsUpdate |= bclass.addTextField("admin_language", "Admin Language", 30);
    // plugins property works only for the main wiki.
    // needsUpdate |= bclass.addTextField("plugins" , "Additional Plugins", 30);
    needsUpdate |= bclass.addTextField("activated_classcollections",
        "Activated Class Collections", 30);

    // Ensure all needed Sendmail Plugin config fields are available
    needsUpdate |= bclass.addTextField("admin_email", "Admin eMail", 30);
    needsUpdate |= bclass.addTextField("smtp_server", "SMTP Server", 30);
    needsUpdate |= bclass.addTextField("smtp_port", "SMTP Port", 5);
    needsUpdate |= bclass.addTextField("smtp_server_username",
        "Server username (optional)", 30);
    needsUpdate |= bclass.addTextField("smtp_server_password",
        "Server password (optional)", 30);
    needsUpdate |= bclass.addTextAreaField("javamail_extra_props",
        "Additional JavaMail properties", 60, 6);
    needsUpdate |= bclass.addBooleanField("use_navigation_images",
        "Use Images for Navigation", "yesno");

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getExtended_XWikiSkinsClass(XWikiContext context)
      throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("XWiki.XWikiSkins", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("XWiki");
      doc.setName("XWikiSkins");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("XWiki.XWikiSkins");
    needsUpdate |= bclass.addTextField("menu_elements", "Available Menu Elements", 30);
    needsUpdate |= bclass.addTextField("skin_config_class_name",
        "Skin Config Class Name", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getXWikiUsersClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("XWiki.XWikiUsers", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("XWiki");
      doc.setName("XWikiUsers");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("XWiki.XWikiUsers");
    needsUpdate |= bclass
        .addBooleanField("force_pwd_change", "force_pwd_change", "yesno");
    needsUpdate |= bclass.addTextField("admin_language", "User Edit-Interface Language",
        4);

    mLogger.debug("checking XWikiUsers Class for " + context.getDatabase()
        + " update needed: " + needsUpdate);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getFormMailClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Celements2.FormMailClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements2");
      doc.setName("FormMailClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Celements2.FormMailClass");
    needsUpdate |= bclass.addTextField("name", "name", 30);
    needsUpdate |= bclass.addTextField("emailFrom", "emailFrom", 30);
    needsUpdate |= bclass.addTextField("emailFields", "emailFields", 30);
    needsUpdate |= bclass.addTextField("userEmailFields", "userEmailFields", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getPhotoAlbumClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("XWiki.PhotoAlbumClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("XWiki");
      doc.setName("PhotoAlbumClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("XWiki.PhotoAlbumClass");
    needsUpdate |= bclass.addTextField("title", "title", 30);
    needsUpdate |= bclass.addTextAreaField("description", "description", 30, 15);
    needsUpdate |= bclass.addNumberField("height", "humbnail Height", 30, "integer");
    needsUpdate |= bclass.addNumberField("thumbWidth", "Thumbnail Width", 30, "integer");
    needsUpdate |= bclass.addNumberField("height2", "Photo Height", 30, "integer");
    needsUpdate |= bclass.addNumberField("photoWidth", "Photo Width", 30, "integer");
    needsUpdate |= bclass.addTextField("id", "id", 30);
    needsUpdate |= bclass.addBooleanField("hasOverview", "hasOverview", "yesno");
    needsUpdate |= bclass.addStaticListField("theme", "theme",
        "grey|black|red|green|blue|gold|orange");

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getFilebaseTagClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Classes.FilebaseTag", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("FilebaseTag");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Classes.FilebaseTag");
    needsUpdate |= bclass.addTextField("attachment", "attachment", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getImportClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Classes.ImportClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("ImportClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Classes.ImportClass");
    needsUpdate |= bclass.addTextField("filebaseTagDocName", "filebaseTagDocName", 30);
    needsUpdate |= bclass.addTextField("preimport_link", "preimport_link", 30);
    needsUpdate |= bclass.addTextField("allowed_file_extentions",
        "allowed_file_extentions", 30);
    needsUpdate |= bclass.addTextAreaField("js_preimport_function",
        "js_preimport_function (optional)", 80, 15);
    needsUpdate |= bclass.addTextField("encoding", "encoding (optional)", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getBannerClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Tools.Banner", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Tools");
      doc.setName("Banner");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Tools.Banner");
    needsUpdate |= bclass.addTextField("filename", "filename", 30);
    needsUpdate |= bclass.addTextField("id", "id", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getFlashBannerClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Tools.FlashBanner", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Tools");
      doc.setName("FlashBanner");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Tools.FlashBanner");
    needsUpdate |= bclass.addTextField("filename", "filename", 30);
    needsUpdate |= bclass.addNumberField("height_old", "height_old", 30, "integer");
    needsUpdate |= bclass.addNumberField("width", "width", 30, "integer");
    needsUpdate |= bclass.addNumberField("height", "height", 30, "integer");
    needsUpdate |= bclass.addTextField("id", "id", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getUserCSSClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Skins.UserCSS", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Skins");
      doc.setName("UserCSS");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Skins.UserCSS");
    needsUpdate |= bclass.addTextField("cssname", "CSS Filename", 30);
    needsUpdate |= bclass.addBooleanField("is_rte_content", "is_rte_content", "yesno");
    needsUpdate |= bclass.addStaticListField("media", "Media",
        "all|aural|braille|embossed|handheld|print|projection|screen|tty|tv");

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getFormActionClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Celements2.FormActionClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements2");
      doc.setName("FormActionClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Celements2.FormActionClass");
    needsUpdate |= bclass.addTextField("doc_fullName", "doc_fullName", 30);
    needsUpdate |= bclass.addTextAreaField("completeRuleSnippet",
        "is Form complete Rule", 80, 15);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getFormConfigClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Celements2.FormConfigClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements2");
      doc.setName("FormConfigClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Celements2.FormConfigClass");
    needsUpdate |= bclass.addTextField("successfulpage", "successfulpage", 30);
    needsUpdate |= bclass.addTextField("failedpage", "failedpage", 30);
    needsUpdate |= bclass.addTextField("excludeFromIsFilledCheck", "Exclude fields from"
        + " 'isFilled' check. (separator: ',')", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getActionTypeClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Celements2.ActionTypeClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements2");
      doc.setName("ActionTypeClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Celements2.ActionTypeClass");
    needsUpdate |= bclass.addTextField("action_type", "Action Type", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getFormStorageClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Celements2.FormStorageClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements2");
      doc.setName("FormStorageClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Celements2.FormStorageClass");
    needsUpdate |= bclass.addTextField("storageLocation", "Storage Location", 30);
    needsUpdate |= bclass.addTextField("storageClassname", "Storage Classname", 30);
    needsUpdate |= bclass.addTextAreaField("storeMapping", "Store Mapping", 80, 15);
    needsUpdate |= bclass.addTextAreaField("exportMapping", "exportMapping", 80, 15);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getReceiverEMailClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Celements2.ReceiverEMail", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements2");
      doc.setName("ReceiverEMail");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Celements2.ReceiverEMail");
    needsUpdate |= bclass.addTextField("email", "email", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getJavaScriptExternalFilesClass(XWikiContext context)
      throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("JavaScript.ExternalFiles", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("JavaScript");
      doc.setName("ExternalFiles");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("JavaScript.ExternalFiles");
    needsUpdate |= bclass.addTextField("filepath", "filepath", 50);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getRTEConfigTypeClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Classes.RTEConfigTypeClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("RTEConfigTypeClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Classes.RTEConfigTypeClass");
    needsUpdate |= bclass.addTextField("rteconfig", "RichTextEditor Config Document", 30);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getRTEConfigTypePropertiesClass(XWikiContext context)
      throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Classes.RTEConfigTypePropertiesClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("RTEConfigTypePropertiesClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Classes.RTEConfigTypePropertiesClass");
    needsUpdate |= bclass.addTextField("styles", "RichTextEditor Styles", 30);
    needsUpdate |= bclass
        .addTextField("plugins", "RichTextEditor Additional Plugins", 30);
    needsUpdate |= bclass.addTextField("row_1", "RichTextEditor Layout Row 1", 30);
    needsUpdate |= bclass.addTextField("row_2", "RichTextEditor Layout Row 2", 30);
    needsUpdate |= bclass.addTextField("row_3", "RichTextEditor Layout Row 3", 30);
    needsUpdate |= bclass
        .addTextField("blockformats", "RichTextEditor Block Formats", 30);
    needsUpdate |= bclass.addTextAreaField("valid_elements",
        "RichTextEditor valid elements config", 80, 15);
    needsUpdate |= bclass.addTextAreaField("invalid_elements",
        "RichTextEditor invalid elements config", 80, 15);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getMediaLibConfigClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument(MEDIALIB_CONFIG_CLASS, context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace(MEDIALIB_CONFIG_CLASS_SPACE);
      doc.setName(MEDIALIB_CONFIG_CLASS_NAME);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName(MEDIALIB_CONFIG_CLASS);
    needsUpdate |= bclass.addTextField("configname", "Config Name", 30);
    needsUpdate |= bclass.addTextField("columnconfig", "Column Config", 30);
    needsUpdate |= bclass.addStaticListField("accesslvl", "Access Level", 1, false,
        "view|edit|delete", "select");
    needsUpdate |= bclass.addTextAreaField("hql", "HQL", 80, 7);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getDocLibConfigClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Classes.DocLibConfigClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("DocLibConfigClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Classes.DocLibConfigClass");
    needsUpdate |= bclass.addTextField("browser_doc", "Browser document full name", 30);
    needsUpdate |= bclass.addTextField("content_doc",
        "Content default document (optional)", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getTagValueClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Classes.KeyValueClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("KeyValueClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Classes.KeyValueClass");
    needsUpdate |= bclass.addTextField("key", "Key", 30);
    needsUpdate |= bclass.addTextAreaField("value", "Value", 80, 15);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getTokenClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Classes.TokenClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("TokenClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Classes.TokenClass");
    needsUpdate |= bclass.addPasswordField("tokenvalue", "Token value", 30);
    needsUpdate |= bclass.addDateField("validuntil", "Valid until");

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private BaseClass getOverlayConfigClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Classes.OverlayConfigClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("OverlayConfigClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setName("Classes.OverlayConfigClass");

    needsUpdate |= bclass.addBooleanField("loadAdminMenuBar", "Load admin menu bar",
        "yesno");
    needsUpdate |= bclass.addBooleanField("showOverlayLanguageBar",
        "Show overlay language bar", "yesno");
    needsUpdate |= bclass.addBooleanField("showCloseOverlayButton",
        "Show close overlay button", "yesno");
    needsUpdate |= bclass.addTextField("documentToLoadInOverlay",
        "Document to load in overlay", 30);
    needsUpdate |= bclass.addTextAreaField("overlayCSS", "Overlay CSS", 80, 3);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  private void setContentAndSaveClassDocument(XWikiDocument doc, boolean needsUpdate,
      XWikiContext context) throws XWikiException {
    String content = doc.getContent();
    if ((content == null) || (content.equals(""))) {
      needsUpdate = true;
      doc.setContent(" ");
    }

    if (needsUpdate) {
      context.getWiki().saveDocument(doc, context);
    }
  }

}
