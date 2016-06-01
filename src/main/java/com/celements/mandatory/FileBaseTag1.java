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
package com.celements.mandatory;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.navigation.NavigationClasses;
import com.celements.navigation.service.ITreeNodeCache;
import com.celements.pagetype.PageTypeClasses;
import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("celements.mandatory.filebaseTag1")
public class FileBaseTag1 implements IMandatoryDocumentRole {

  private static final String _FILE_BASE_TAG_PAGE_TYPE = "FileBaseTag";

  private static Log LOGGER = LogFactory.getFactory().getInstance(FileBaseTag1.class);

  @Requirement("celements.celPageTypeClasses")
  IClassCollectionRole pageTypeClasses;

  @Requirement("celements.celNavigationClasses")
  IClassCollectionRole navigationClasses;

  @Requirement
  ITreeNodeCache treeNodeCache;

  @Requirement
  Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  private PageTypeClasses getPageTypeClasses() {
    return (PageTypeClasses) pageTypeClasses;
  }

  private NavigationClasses getNavigationClasses() {
    return (NavigationClasses) navigationClasses;
  }

  public List<String> dependsOnMandatoryDocuments() {
    return Collections.emptyList();
  }

  public void checkDocuments() throws XWikiException {
    LOGGER.trace("Start checkDocuments in FileBaseTag1 for database [" + getContext().getDatabase()
        + "].");
    if (noMainWiki() && !isSkipCelementsFileBaseTag1()) {
      LOGGER.trace("before checkFileBaseTag1 for database [" + getContext().getDatabase() + "].");
      checkFileBaseTag1();
    } else {
      LOGGER.info("skip mandatory checkFileBaseTag1 for database [" + getContext().getDatabase()
          + "], noMainWiki [" + noMainWiki() + "], skipCelementsParam ["
          + isSkipCelementsFileBaseTag1() + "].");
    }
    LOGGER.trace("end checkDocuments in FileBaseTag1 for database [" + getContext().getDatabase()
        + "].");
  }

  boolean isSkipCelementsFileBaseTag1() {
    boolean isSkip = getContext().getWiki().ParamAsLong("celements.mandatory.skipFileBaseTag1",
        0) == 1L;
    LOGGER.trace("skipFileBaseTag1 for database [" + getContext().getDatabase() + "] returning ["
        + isSkip + "].");
    return isSkip;
  }

  boolean noMainWiki() {
    String wikiName = getContext().getDatabase();
    LOGGER.trace("noMainWiki for database [" + wikiName + "].");
    return (wikiName != null) && !wikiName.equals(getContext().getMainXWiki());
  }

  void checkFileBaseTag1() throws XWikiException {
    DocumentReference fileBaseTag1Ref = getFileBaseTag1Ref(getContext().getDatabase());
    XWikiDocument fileBaseTag1Doc;
    if (!getContext().getWiki().exists(fileBaseTag1Ref, getContext())) {
      LOGGER.debug("FileBaseTag1Document is missing that we create it. ["
          + getContext().getDatabase() + "]");
      fileBaseTag1Doc = new CreateDocumentCommand().createDocument(fileBaseTag1Ref,
          _FILE_BASE_TAG_PAGE_TYPE);
    } else {
      fileBaseTag1Doc = getContext().getWiki().getDocument(fileBaseTag1Ref, getContext());
      LOGGER.trace("FileBaseTag1Document already exists. [" + getContext().getDatabase() + "]");
    }
    if (fileBaseTag1Doc != null) {
      boolean dirty = checkPageType(fileBaseTag1Doc);
      dirty |= checkMenuItem(fileBaseTag1Doc);
      dirty |= checkMenuName(fileBaseTag1Doc, "en", ".zip");
      dirty |= checkMenuName(fileBaseTag1Doc, "de", ".zip");
      if (dirty) {
        LOGGER.info("FileBaseTag1Document updated for [" + getContext().getDatabase() + "].");
        getContext().getWiki().saveDocument(fileBaseTag1Doc, "autocreate"
            + " Content_attachments.FileBaseTag1.", getContext());
        treeNodeCache.flushMenuItemCache();
      } else {
        LOGGER.debug("FileBaseTag1Document not saved. Everything uptodate. ["
            + getContext().getDatabase() + "].");
      }
    } else {
      LOGGER.trace("skip checkFileBaseTag1 because fileBaseTag1Doc is null! ["
          + getContext().getDatabase() + "]");
    }
  }

  boolean checkMenuItem(XWikiDocument fileBaseTag1Doc) throws XWikiException {
    String wikiName = getContext().getDatabase();
    DocumentReference menuItemClassRef = getNavigationClasses().getMenuItemClassRef(wikiName);
    BaseObject menuItemObj = fileBaseTag1Doc.getXObject(menuItemClassRef, false, getContext());
    if (menuItemObj == null) {
      menuItemObj = fileBaseTag1Doc.newXObject(menuItemClassRef, getContext());
      menuItemObj.set("menu_position", 2, getContext());
      LOGGER.debug("FileBaseTag1 missing fields in menu item object fixed for" + " database ["
          + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  boolean checkMenuName(XWikiDocument fileBaseTag1Doc, String lang, String menuname)
      throws XWikiException {
    String wikiName = getContext().getDatabase();
    DocumentReference menuNameClassRef = getNavigationClasses().getMenuNameClassRef(wikiName);
    BaseObject menuNameEN = fileBaseTag1Doc.getXObject(menuNameClassRef, "lang", lang, false);
    if (menuNameEN == null) {
      menuNameEN = fileBaseTag1Doc.newXObject(menuNameClassRef, getContext());
      menuNameEN.set("lang", lang, getContext());
      menuNameEN.set("menu_name", menuname, getContext());
      LOGGER.debug("FileBaseTag1 missing fields in menu name en object fixed for" + " database ["
          + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  boolean checkPageType(XWikiDocument fileBaseTag1Doc) throws XWikiException {
    DocumentReference pageTypeClassRef = getPageTypeClasses().getPageTypeClassRef(
        getContext().getDatabase());
    BaseObject pageTypeObj = fileBaseTag1Doc.getXObject(pageTypeClassRef, false, getContext());
    if (pageTypeObj == null) {
      pageTypeObj = fileBaseTag1Doc.newXObject(pageTypeClassRef, getContext());
      pageTypeObj.setStringValue("page_type", _FILE_BASE_TAG_PAGE_TYPE);
      LOGGER.debug("FileBaseTag1 missing page type object fixed for database ["
          + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  private DocumentReference getFileBaseTag1Ref(String wikiName) {
    return new DocumentReference(wikiName, "Content_attachments", "tag1");
  }

}
