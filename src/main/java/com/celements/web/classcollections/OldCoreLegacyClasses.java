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
package com.celements.web.classcollections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.oldCoreLegacyClasses")
public class OldCoreLegacyClasses extends AbstractClassCollection {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      OldCoreLegacyClasses.class);

  public static final String XWIKI_SKINS_CLASS_DOC = "XWikiSkins";
  public static final String XWIKI_SKINS_CLASS_SPACE = "XWiki";
  public static final String XWIKI_SKINS_CLASS = XWIKI_SKINS_CLASS_SPACE + "."
        + XWIKI_SKINS_CLASS_DOC;

  public static final String TOOLS_BANNER_CLASS_DOC = "Banner";
  public static final String TOOLS_BANNER_CLASS_SPACE = "Tools";
  public static final String TOOLS_BANNER_CLASS = TOOLS_BANNER_CLASS_SPACE + "."
        + TOOLS_BANNER_CLASS_DOC;

  public static final String TOOLS_FLASH_BANNER_CLASS_DOC = "FlashBanner";
  public static final String TOOLS_FLASH_BANNER_CLASS_SPACE = "Tools";
  public static final String TOOLS_FLASH_BANNER_CLASS = TOOLS_FLASH_BANNER_CLASS_SPACE
      + "." + TOOLS_FLASH_BANNER_CLASS_DOC;

  public static final String MEDIALIB_CONFIG_CLASS_DOC = "MediaLibConfigClass";
  public static final String MEDIALIB_CONFIG_CLASS_SPACE = "Classes";
  public static final String MEDIALIB_CONFIG_CLASS = MEDIALIB_CONFIG_CLASS_SPACE + "."
      + MEDIALIB_CONFIG_CLASS_DOC;

  public static final String DOCLIB_CONFIG_CLASS_DOC = "DocLibConfigClass";
  public static final String DOCLIB_CONFIG_CLASS_SPACE = "Classes";
  public static final String DOCLIB_CONFIG_CLASS = DOCLIB_CONFIG_CLASS_SPACE + "."
      + DOCLIB_CONFIG_CLASS_DOC;

  public static final String FORM_FIELD_CLASS_DOC = "FormFieldClass";
  public static final String FORM_FIELD_CLASS_SPACE = "Celements2";
  public static final String FORM_FIELD_CLASS = FORM_FIELD_CLASS_SPACE + "."
      + FORM_FIELD_CLASS_DOC;

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

  public String getConfigName() {
    return "oldCoreLegacyClasses";
  }

  @Override
  protected void initClasses() throws XWikiException {
    getExtended_XWikiSkinsClass();
    getFormFieldClass();
    getBannerClass();
    getFlashBannerClass();
    getMediaLibConfigClass();
    getDocLibConfigClass();
  }

  public DocumentReference getXWikiSkinsClassRef(String wikiName) {
    return new DocumentReference(wikiName, XWIKI_SKINS_CLASS_SPACE,
        XWIKI_SKINS_CLASS_DOC);
  }

  private BaseClass getExtended_XWikiSkinsClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getXWikiSkinsClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + XWIKI_SKINS_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("menu_elements", "Available Menu Elements", 30);
    needsUpdate |= bclass.addTextField("skin_config_class_name",
        "Skin Config Class Name", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getFormFieldClassRef(String wikiName) {
    return new DocumentReference(wikiName, FORM_FIELD_CLASS_SPACE,
        FORM_FIELD_CLASS_DOC);
  }

  private BaseClass getFormFieldClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getFormFieldClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + FORM_FIELD_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("fieldname", "fieldname", 30);
    needsUpdate |= addBooleanField(bclass, "isRequired", "is Required", "yesno", 0);
    needsUpdate |= addBooleanField(bclass, "ruleSnippet", "Rule velocity Snippet",
        "yesno", 0);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getBannerClassRef(String wikiName) {
    return new DocumentReference(wikiName, TOOLS_BANNER_CLASS_SPACE,
        TOOLS_BANNER_CLASS_DOC);
  }

  private BaseClass getBannerClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getBannerClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + TOOLS_BANNER_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("filename", "filename", 30);
    needsUpdate |= bclass.addTextField("id", "id", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getFlashBannerClassRef(String wikiName) {
    return new DocumentReference(wikiName, TOOLS_FLASH_BANNER_CLASS_SPACE,
        TOOLS_FLASH_BANNER_CLASS_DOC);
  }

  private BaseClass getFlashBannerClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getFlashBannerClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + TOOLS_FLASH_BANNER_CLASS + " class document. ",
          exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("filename", "filename", 30);
    needsUpdate |= bclass.addNumberField("height_old", "height_old", 30, "integer");
    needsUpdate |= bclass.addNumberField("width", "width", 30, "integer");
    needsUpdate |= bclass.addNumberField("height", "height", 30, "integer");
    needsUpdate |= bclass.addTextField("id", "id", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getMediaLibConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, MEDIALIB_CONFIG_CLASS_SPACE,
        MEDIALIB_CONFIG_CLASS_DOC);
  }

  private BaseClass getMediaLibConfigClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getMediaLibConfigClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + MEDIALIB_CONFIG_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("configname", "Config Name", 30);
    needsUpdate |= bclass.addTextField("columnconfig", "Column Config", 30);
    needsUpdate |= bclass.addStaticListField("accesslvl", "Access Level", 1, false,
        "view|edit|delete", "select");
    needsUpdate |= bclass.addTextAreaField("hql", "HQL", 80, 7);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getDocLibConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, DOCLIB_CONFIG_CLASS_SPACE,
        DOCLIB_CONFIG_CLASS_DOC);
  }

  private BaseClass getDocLibConfigClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getDocLibConfigClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + DOCLIB_CONFIG_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("browser_doc", "Browser document full name", 30);
    needsUpdate |= bclass.addTextField("content_doc",
        "Content default document (optional)", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

}
