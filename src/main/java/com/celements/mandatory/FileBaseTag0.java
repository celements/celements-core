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

@Component("celements.mandatory.filebaseTag0")
public class FileBaseTag0 implements IMandatoryDocumentRole {

  private static final String _FILE_BASE_TAG_PAGE_TYPE = "FileBaseTag";

  private static Log LOGGER = LogFactory.getFactory().getInstance(FileBaseTag0.class);

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

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return Collections.emptyList();
  }

  @Override
  public void checkDocuments() throws XWikiException {
    LOGGER.trace("Start checkDocuments in FileBaseTag0 for database [" + getContext().getDatabase()
        + "].");
    if (noMainWiki() && !isSkipCelementsFileBaseTag0()) {
      LOGGER.trace("before checkFileBaseTag0 for database [" + getContext().getDatabase() + "].");
      checkFileBaseTag0();
    } else {
      LOGGER.info("skip mandatory checkFileBaseTag0 for database [" + getContext().getDatabase()
          + "], noMainWiki [" + noMainWiki() + "], skipCelementsParam ["
          + isSkipCelementsFileBaseTag0() + "].");
    }
    LOGGER.trace("end checkDocuments in FileBaseTag0 for database [" + getContext().getDatabase()
        + "].");
  }

  boolean isSkipCelementsFileBaseTag0() {
    boolean isSkip = getContext().getWiki().ParamAsLong("celements.mandatory.skipFileBaseTag0",
        0) == 1L;
    LOGGER.trace("skipFileBaseTag0 for database [" + getContext().getDatabase() + "] returning ["
        + isSkip + "].");
    return isSkip;
  }

  boolean noMainWiki() {
    String wikiName = getContext().getDatabase();
    LOGGER.trace("noMainWiki for database [" + wikiName + "].");
    return (wikiName != null) && !wikiName.equals(getContext().getMainXWiki());
  }

  void checkFileBaseTag0() throws XWikiException {
    DocumentReference fileBaseTag0Ref = getFileBaseTag0Ref(getContext().getDatabase());
    XWikiDocument fileBaseTag0Doc;
    if (!getContext().getWiki().exists(fileBaseTag0Ref, getContext())) {
      LOGGER.debug("FileBaseTag0Document is missing that we create it. ["
          + getContext().getDatabase() + "]");
      fileBaseTag0Doc = new CreateDocumentCommand().createDocument(fileBaseTag0Ref,
          _FILE_BASE_TAG_PAGE_TYPE);
    } else {
      fileBaseTag0Doc = getContext().getWiki().getDocument(fileBaseTag0Ref, getContext());
      LOGGER.trace("FileBaseTag0Document already exists. [" + getContext().getDatabase() + "]");
    }
    if (fileBaseTag0Doc != null) {
      boolean dirty = checkPageType(fileBaseTag0Doc);
      dirty |= checkMenuItem(fileBaseTag0Doc);
      dirty |= checkMenuName(fileBaseTag0Doc, "en", "Photos");
      dirty |= checkMenuName(fileBaseTag0Doc, "de", "Fotos");
      if (dirty) {
        LOGGER.info("FileBaseTag0Document updated for [" + getContext().getDatabase() + "].");
        getContext().getWiki().saveDocument(fileBaseTag0Doc, "autocreate"
            + " Content_attachments.FileBaseTag0.", getContext());
        treeNodeCache.flushMenuItemCache();
      } else {
        LOGGER.debug("FileBaseTag0Document not saved. Everything uptodate. ["
            + getContext().getDatabase() + "].");
      }
    } else {
      LOGGER.trace("skip checkFileBaseTag0 because fileBaseTag0Doc is null! ["
          + getContext().getDatabase() + "]");
    }
  }

  boolean checkMenuItem(XWikiDocument fileBaseTag0Doc) throws XWikiException {
    String wikiName = getContext().getDatabase();
    DocumentReference menuItemClassRef = getNavigationClasses().getMenuItemClassRef(wikiName);
    BaseObject menuItemObj = fileBaseTag0Doc.getXObject(menuItemClassRef, false, getContext());
    if (menuItemObj == null) {
      menuItemObj = fileBaseTag0Doc.newXObject(menuItemClassRef, getContext());
      menuItemObj.set("menu_position", 1, getContext());
      LOGGER.debug("FileBaseTag0 missing fields in menu item object fixed for" + " database ["
          + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  boolean checkMenuName(XWikiDocument fileBaseTag0Doc, String lang, String menuname)
      throws XWikiException {
    String wikiName = getContext().getDatabase();
    DocumentReference menuNameClassRef = getNavigationClasses().getMenuNameClassRef(wikiName);
    BaseObject menuNameEN = fileBaseTag0Doc.getXObject(menuNameClassRef, "lang", lang, false);
    if (menuNameEN == null) {
      menuNameEN = fileBaseTag0Doc.newXObject(menuNameClassRef, getContext());
      menuNameEN.set("lang", lang, getContext());
      menuNameEN.set("menu_name", menuname, getContext());
      LOGGER.debug("FileBaseTag0 missing fields in menu name en object fixed for" + " database ["
          + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  boolean checkPageType(XWikiDocument fileBaseTag0Doc) throws XWikiException {
    DocumentReference pageTypeClassRef = getPageTypeClasses().getPageTypeClassRef(
        getContext().getDatabase());
    BaseObject pageTypeObj = fileBaseTag0Doc.getXObject(pageTypeClassRef, false, getContext());
    if (pageTypeObj == null) {
      pageTypeObj = fileBaseTag0Doc.newXObject(pageTypeClassRef, getContext());
      pageTypeObj.setStringValue("page_type", _FILE_BASE_TAG_PAGE_TYPE);
      LOGGER.debug("FileBaseTag0 missing page type object fixed for database ["
          + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  private DocumentReference getFileBaseTag0Ref(String wikiName) {
    return new DocumentReference(wikiName, "Content_attachments", "tag0");
  }

}
