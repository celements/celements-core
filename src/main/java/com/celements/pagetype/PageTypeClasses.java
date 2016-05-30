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
package com.celements.pagetype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.AbstractClassCollection;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.celPageTypeClasses")
public class PageTypeClasses extends AbstractClassCollection {

  private static Log LOGGER = LogFactory.getFactory().getInstance(PageTypeClasses.class);

  /**
   * @deprecated instead use constants from IPageTypeClassConfig
   */
  @Deprecated
  public static final String PAGE_TYPE_PROPERTIES_CLASS_SPACE = "Celements2";
  @Deprecated
  public static final String PAGE_TYPE_PROPERTIES_CLASS_DOC = "PageTypeProperties";
  @Deprecated
  public static final String PAGE_TYPE_PROPERTIES_CLASS = PAGE_TYPE_PROPERTIES_CLASS_SPACE + "."
      + PAGE_TYPE_PROPERTIES_CLASS_DOC;

  @Deprecated
  public static final String PAGE_TYPE_CLASS_SPACE = "Celements2";
  @Deprecated
  public static final String PAGE_TYPE_CLASS_DOC = "PageType";
  @Deprecated
  public static final String PAGE_TYPE_CLASS = PAGE_TYPE_CLASS_SPACE + "." + PAGE_TYPE_CLASS_DOC;
  @Deprecated
  public static final String PAGE_TYPE_FIELD = "page_type";

  @Requirement
  private IPageTypeClassConfig pageTypeClassConfig;

  @Requirement
  private IWebUtilsService webUtilsService;

  /**
   * @deprecated instead use getPageTypePropertiesClassRef(WikiReference) in
   *             IPageTypeClassConfig
   */
  @Deprecated
  public DocumentReference getPageTypePropertiesClassRef(String wikiName) {
    return new DocumentReference(wikiName, PAGE_TYPE_PROPERTIES_CLASS_SPACE,
        PAGE_TYPE_PROPERTIES_CLASS_DOC);
  }

  /**
   * @deprecated instead use getPageTypeClassRef(WikiReference) in IPageTypeClassConfig
   */
  @Deprecated
  public DocumentReference getPageTypeClassRef(String wikiName) {
    return new DocumentReference(wikiName, PAGE_TYPE_CLASS_SPACE, PAGE_TYPE_CLASS_DOC);
  }

  @Override
  protected void initClasses() throws XWikiException {
    LOGGER.debug("entering initClasses for database: " + getContext().getDatabase());
    getPageTypePropertiesClass();
    getPageTypeClass();
  }

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

  public String getConfigName() {
    return "celPageTypeClasses";
  }

  private BaseClass getPageTypePropertiesClass() throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = getContext().getWiki();
    boolean needsUpdate = false;

    DocumentReference pageTypePropertiesClassRef = pageTypeClassConfig.getPageTypePropertiesClassRef(
        webUtilsService.getWikiRef());
    try {
      doc = xwiki.getDocument(pageTypePropertiesClassRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get [" + pageTypePropertiesClassRef + "] class document", exp);
      doc = new XWikiDocument(pageTypePropertiesClassRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(pageTypePropertiesClassRef);
    needsUpdate |= bclass.addTextField(IPageTypeClassConfig.PAGETYPE_PROP_TYPE_NAME,
        "Type Pretty Name", 30);
    needsUpdate |= bclass.addTextField(IPageTypeClassConfig.PAGETYPE_PROP_CATEGORY, "Category", 30);
    needsUpdate |= bclass.addTextField(IPageTypeClassConfig.PAGETYPE_PROP_PAGE_EDIT,
        "Type Edit Template", 30);
    needsUpdate |= bclass.addTextField(IPageTypeClassConfig.PAGETYPE_PROP_PAGE_VIEW,
        "Type View Template", 30);
    needsUpdate |= bclass.addBooleanField(IPageTypeClassConfig.PAGETYPE_PROP_VISIBLE, "Visible",
        "yesno");
    needsUpdate |= bclass.addBooleanField(IPageTypeClassConfig.PAGETYPE_PROP_SHOW_FRAME,
        "Show Frame", "yesno");
    needsUpdate |= bclass.addBooleanField(IPageTypeClassConfig.PAGETYPE_PROP_LOAD_RICHTEXT,
        "Load Richtext Editor", "yesno");
    needsUpdate |= bclass.addNumberField(IPageTypeClassConfig.PAGETYPE_PROP_RTE_WIDTH,
        "Richtext Editor Width", 30, "integer");
    needsUpdate |= bclass.addNumberField(IPageTypeClassConfig.PAGETYPE_PROP_RTE_HEIGHT,
        "Richtext Editor Height", 30, "integer");
    needsUpdate |= bclass.addBooleanField(IPageTypeClassConfig.PAGETYPE_PROP_HASPAGETITLE,
        "Has Page Title", "yesno");
    needsUpdate |= bclass.addBooleanField(IPageTypeClassConfig.PAGETYPE_PROP_IS_UNCONNECTED_PARENT,
        "Is Unconnected Parent", "yesno");

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  private BaseClass getPageTypeClass() throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = getContext().getWiki();
    boolean needsUpdate = false;

    DocumentReference pageTypeClassRef = pageTypeClassConfig.getPageTypeClassRef(
        webUtilsService.getWikiRef());
    try {
      doc = xwiki.getDocument(pageTypeClassRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get [" + pageTypeClassRef + "] class document", exp);
      doc = new XWikiDocument(pageTypeClassRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(pageTypeClassRef);
    needsUpdate |= bclass.addTextField(PAGE_TYPE_FIELD, "Page Type", 30);
    needsUpdate |= bclass.addTextField("page_layout", "Page Layout", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

}
