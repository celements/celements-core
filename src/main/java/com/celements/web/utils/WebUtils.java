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
package com.celements.web.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.inheritor.InheritorFactory;
import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.ExternalUsageFilter;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.service.ITreeNodeCache;
import com.celements.navigation.service.TreeNodeService;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.web.pagetype.IPageType;
import com.celements.web.pagetype.PageTypeApi;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.celements.web.plugin.cmd.EmptyCheckCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;

public class WebUtils implements IWebUtils {
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(WebUtils.class);

  private static IWebUtils instance;

  private AttachmentURLCommand attachmentUrlCmd;

  private InheritorFactory injectedInheritorFactory;
  private ITreeNodeService injectedTreeNodeService;
  
  /**
   * FOR TEST ONLY!!!
   * Please use getInstance instead.
   */
  WebUtils() {}

  public static IWebUtils getInstance() {
    if (instance == null) {
      instance = new WebUtils();
    }
    return instance;
  }

  void injectInheritorFactory(InheritorFactory injectedInheritorFactory) {
    this.injectedInheritorFactory = injectedInheritorFactory;
  }
  
  InheritorFactory getInheritorFactory() {
    if (injectedInheritorFactory != null) {
      return injectedInheritorFactory;
    }
    return new InheritorFactory();
  }
  
  void injectTreeNodeService(ITreeNodeService injectedTreeNodeService) {
    this.injectedTreeNodeService = injectedTreeNodeService;
  }
  
  private ITreeNodeService getTreeNodeService() {
    if (injectedTreeNodeService != null) {
      return injectedTreeNodeService;
    }
    ITreeNodeService ret = Utils.getComponent(ITreeNodeService.class);
    if(injectedInheritorFactory!=null){
      ((TreeNodeService)ret).injectInheritorFactory(injectedInheritorFactory);
    }
    return ret;
  }

  private ITreeNodeCache getTreeNodeCache() {
    return Utils.getComponent(ITreeNodeCache.class);
  }
  
  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }
  
  private AttachmentURLCommand getAttachmentUrlCmd() {
    if (attachmentUrlCmd == null) {
      attachmentUrlCmd = new AttachmentURLCommand();
    }
    return attachmentUrlCmd;
  }

  /**
   * @deprecated instead use TreeNodeCache
   */
  @Deprecated
  public int queryCount() {
    return getTreeNodeCache().queryCount();
  }

  /**
   * @deprecated instead use TreeNodeCache
   */
  @Deprecated
  public void flushMenuItemCache(XWikiContext context) {
    getTreeNodeCache().flushMenuItemCache();
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public List<String> getDocumentParentsList(String fullName,
      boolean includeDoc, XWikiContext context) {
    ArrayList<String> docParents = new ArrayList<String>();
    try {
      String nextParent;
      if (includeDoc) {
        nextParent = fullName;
      } else {
        nextParent = getParentFullName(fullName, context);
      }
      while (!"".equals(nextParent)
          && (context.getWiki().exists(nextParent, context))
          && !docParents.contains(nextParent)) {
        docParents.add(nextParent);
        nextParent = getParentFullName(nextParent, context);
      }
    } catch (XWikiException e) {
      LOGGER.error(e);
    }
    return docParents;
  }

  @Deprecated
  private String getParentFullName(String fullName, XWikiContext context
      ) throws XWikiException {
    return context.getWiki().getDocument(fullName,
      context).getParent();
  }

  /**
   * @deprecated instead use EmptyCheckCommand
   */
  @Deprecated
  public boolean isEmptyRTEDocument(XWikiDocument localdoc) {
    return isEmptyRTEString(localdoc.getContent());
  }

  @Deprecated
  public boolean isEmptyRTEString(String rteContent) {
    return new EmptyCheckCommand().isEmptyRTEString(rteContent);
  }

  /**
   * @deprecated instead use TreeNodeService
   */
  @Deprecated
  public int getActiveMenuItemPos(int menuLevel, String menuPart,
      XWikiContext context) {
    return getTreeNodeService().getActiveMenuItemPos(menuLevel, menuPart);
  }

  /**
   * @deprecated instead use TreeNodeService
   */
  @Deprecated
  public int getMenuItemPos(String fullName, String menuPart,
      XWikiContext context) {
    return getTreeNodeService().getMenuItemPos(getRef(fullName), menuPart);
  }

  /**
   * 
   * @deprecated instead use TreeNodeService.getSubNodesForParent
   */
  @Deprecated
  public List<com.xpn.xwiki.api.Object> getSubMenuItemsForParent(
      String parent, String menuSpace, String menuPart, XWikiContext context) {
    ExternalUsageFilter filter = new ExternalUsageFilter();
    filter.setMenuPart(menuPart);
    return getSubMenuItemsForParent(parent, menuSpace, filter, context);
  }

  /**
   * @deprecated since 2.14.0 use getSubNodesForParent instead
   */
  @Deprecated
  public List<BaseObject> getSubMenuItemsForParent_internal(String parent,
      String menuSpace, String menuPart, XWikiContext context) {
    InternalRightsFilter filter = new InternalRightsFilter();
    filter.setMenuPart(menuPart);
    return getSubMenuItemsForParent(parent, menuSpace, filter, context);
  }

  /**
   * @deprecated since 2.14.0 instead use TreeNodeService directly
   */
  @Deprecated
  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      String menuPart, XWikiContext context) {
    return getTreeNodeService().getSubNodesForParent(parent, menuSpace, menuPart);
  }

  /**
   * @deprecated since 2.14.0 use getSubNodesForParent instead
   */
  @Deprecated
  public <T> List<T> getSubMenuItemsForParent(String parent, String menuSpace,
      INavFilter<T> filter, XWikiContext context) {
    return getTreeNodeService().getSubMenuItemsForParent(parent, menuSpace, filter);
  }

  /**
   * @deprecated since 2.14.0 instead use TreeNodeService directly
   */
  @Deprecated
  public <T> List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      INavFilter<T> filter, XWikiContext context) {
    return getTreeNodeService().getSubNodesForParent(parent, menuSpace, filter);
  }

  /**
   * @deprecated instead use TreeNodeService
   */
  @Deprecated
  public List<com.xpn.xwiki.api.Object> getMenuItemsForHierarchyLevel(
      int menuLevel, String menuPart, XWikiContext context) {
    String parent = getParentForLevel(menuLevel, context);
    if (parent != null) {
      List<com.xpn.xwiki.api.Object> submenuItems = getSubMenuItemsForParent(parent, "",
          menuPart, context);
      LOGGER.debug("submenuItems for parent: " + parent + " ; "
          + submenuItems);
      return submenuItems;
    }
    LOGGER.debug("parent is null");
    return new ArrayList<com.xpn.xwiki.api.Object>();
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public String getParentForLevel(int menuLevel, XWikiContext context) {
    String parent = null;
    if (menuLevel == 1) {
      parent = ""; // mainMenu
    } else {
      List<String> parentList = getDocumentParentsList(
          context.getDoc().getFullName(), true, context);
      int startAtItem = parentList.size() - menuLevel + 1;
      if(startAtItem >= 0) {
        parent = parentList.get(startAtItem);
      }
    }
    return parent;
  }

  /**
   * @deprecated instead use TreeNodeService
   */
  @Deprecated
  public BaseObject getPrevMenuItem(String fullName,
      XWikiContext context) throws XWikiException {
    DocumentReference docRef = getTreeNodeService().getPrevMenuItem(getRef(fullName)
        ).getDocumentReference();
    return context.getWiki().getDocument(docRef, context)
        .getXObject(getRef("Celements2.MenuItem"));
  }

  /**
   * @deprecated instead use TreeNodeService
   */
  @Deprecated
  public BaseObject getNextMenuItem(String fullName,
      XWikiContext context) throws XWikiException {
    DocumentReference docRef = getTreeNodeService().getNextMenuItem(getRef(fullName)
        ).getDocumentReference();
    return context.getWiki().getDocument(docRef, context)
        .getXObject(getRef("Celements2.MenuItem"));
  }

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#getConfigDocByInheritance(com.xpn.xwiki.doc.XWikiDocument, java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  @Deprecated
  public XWikiDocument getConfigDocByInheritance(XWikiDocument doc, String className, XWikiContext context) throws XWikiException {
    XWikiDocument preferenceDoc = context.getWiki().getDocument(doc.getSpace()
        + ".WebPreferences", context);
    if(preferenceDoc.getObject(className, false, context) == null){
      preferenceDoc = context.getWiki().getDocument("XWiki.XWikiPreferences", context);
      if(preferenceDoc.getObject(className, false, context) == null){
        String skinDocName = context.getWiki().getSpacePreference("skin", context);
        if((skinDocName != null)
            && (context.getWiki().exists(skinDocName, context))){
          preferenceDoc = context.getWiki().getDocument(skinDocName, context);
        }
      }
    }
    return preferenceDoc;
  }
  
  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public String getDocSectionAsJSON(String regex, String fullName, int section,
      XWikiContext context) throws XWikiException {
    return getWebUtilsService().getDocSectionAsJSON(regex, getRef(fullName), section);
  }
  
  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public String getDocSection(String regex, String fullName, int section,
      XWikiContext context) throws XWikiException {
    return getWebUtilsService().getDocSection(regex, getRef(fullName), section);
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public int countSections(String regex, String fullName, XWikiContext context
      ) throws XWikiException {
    return getWebUtilsService().countSections(regex, getRef(fullName));
  }
  
  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public IPageType getPageTypeApi(String fullName, XWikiContext context)
    throws XWikiException {
    return new PageTypeApi(fullName, context);
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public List<String> getAllowedLanguages(XWikiContext context) {
    return getWebUtilsService().getAllowedLanguages();
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public Date parseDate(String date, String format) {
    return getWebUtilsService().parseDate(date, format);
  }

  /**
   * @deprecated since 2.14.0 instead use WebUtilsService directly
   */
  @Deprecated
  public XWikiMessageTool getMessageTool(String adminLanguage, XWikiContext context) {
    return getWebUtilsService().getMessageTool(adminLanguage);
  }

  /**
   * @deprecated since 2.14.0 instead use WebUtilsService directly
   */
  @Deprecated
  public XWikiMessageTool getAdminMessageTool(XWikiContext context) {
    return getWebUtilsService().getAdminMessageTool();
  }
  
  /**
   * @deprecated since 2.14.0 instead use WebUtilsService directly
   */
  @Deprecated
  public String getAdminLanguage(XWikiContext context) {
    return getWebUtilsService().getAdminLanguage();
  }

  /**
   * @deprecated since 2.14.0 instead use WebUtilsService directly
   */
  @Deprecated
  public String getAdminLanguage(String userFullName, XWikiContext context) {
    return getWebUtilsService().getAdminLanguage(userFullName);
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public boolean hasParentSpace(XWikiContext context) {
    return getWebUtilsService().hasParentSpace();
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public String getParentSpace(XWikiContext context) {
    return getWebUtilsService().getParentSpace();
  }

  /**
   * @deprecated instead use TreeNodeService
   */
  @Deprecated
  public Integer getMaxConfiguredNavigationLevel(XWikiContext context) {
    return getTreeNodeService().getMaxConfiguredNavigationLevel();
  }
  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator
      ) throws ClassNotFoundException {
    return this.getWebUtilsService().getAttachmentListSorted(doc, comparator);
  }

  /**
   * @deprecated instead use WebUtilsService directly
   */
  @Deprecated
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException {
    return this.getWebUtilsService().getAttachmentListSorted(doc, comparator, imagesOnly,
        start, nb);
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public String getAttachmentListSortedAsJSON(Document doc,
      String comparator, boolean imagesOnly) {
    return this.getWebUtilsService().getAttachmentListSortedAsJSON(doc, comparator,
        imagesOnly);
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public String getAttachmentListSortedAsJSON(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) {
    return this.getWebUtilsService().getAttachmentListSortedAsJSON(doc, comparator,
        imagesOnly, start, nb);
  }

  /**
   * @deprecated instead use ImageService
   */
  public List<Attachment> getRandomImages(String fullName, int num,
      XWikiContext context) {
    try {
      Document imgDoc = context.getWiki().getDocument(fullName,
          context).newDocument(context);
      List<Attachment> allImagesList = getWebUtilsService().getAttachmentListSorted(imgDoc,
          "AttachmentAscendingNameComparator", true);
      if (allImagesList.size() > 0) {
        List<Attachment> preSetImgList = prepareMaxCoverSet(num, allImagesList);
        List<Attachment> imgList = new ArrayList<Attachment>(num);
        Random rand = new Random();
        for (int i=1; i<=num ; i++) {
          int nextimg = rand.nextInt(preSetImgList.size());
          imgList.add(preSetImgList.remove(nextimg));
        }
        return imgList;
      }
    } catch (XWikiException e) {
      LOGGER.error(e);
    }
    return Collections.emptyList();
  }

  <T> List<T> prepareMaxCoverSet(int num, List<T> allImagesList) {
    List<T> preSetImgList = new Vector<T>(num);
    preSetImgList.addAll(allImagesList);
    for(int i=2; i <= coveredQuotient(allImagesList.size(), num); i++) {
      preSetImgList.addAll(allImagesList);
    }
    return preSetImgList;
  }
  
  int coveredQuotient(int divisor, int dividend) {
    if (dividend >= 0) {
      return ( (dividend + divisor - 1) / divisor);
    } else {
      return (dividend / divisor);
    }
  }

  /**
   * @deprecated since 2.11.6 instead use WebUtilsService directly
   */
  @Deprecated
  public boolean isAdminUser(XWikiContext context) {
    return getWebUtilsService().isAdminUser();
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public Map<String, String> xwikiDoctoLinkedMap(XWikiDocument xwikiDoc,
      boolean bWithObjects, boolean bWithRendering,
      boolean bWithAttachmentContent, boolean bWithVersions,
      XWikiContext context) throws XWikiException {
    return getWebUtilsService().xwikiDocToLinkedMap(xwikiDoc.getDocumentReference(),
        bWithObjects, bWithRendering, bWithAttachmentContent, bWithVersions);
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public String getJSONContent(XWikiDocument cdoc, XWikiContext context) {
    return getWebUtilsService().getJSONContent(cdoc.getDocumentReference());
  }


  /**
   * @deprecated since 2.14.0 instead use AttachmentURLCommand directly
   */
  @Deprecated
  public String getAttachmentURL(String link, XWikiContext context) {
    return getAttachmentUrlCmd().getAttachmentURL(link, context);
  }

  /**
   * @deprecated since 2.14.0 instead use AttachmentURLCommand directly
   */
  @Deprecated
  public String getAttachmentName(String link) {
    return getAttachmentUrlCmd().getAttachmentName(link);
  }

  /**
   * @deprecated since 2.14.0 instead use AttachmentURLCommand directly
   */
  @Deprecated
  public String getPageFullName(String link) {
    return getAttachmentUrlCmd().getPageFullName(link);
  }

  /**
   * @deprecated since 2.14.0 instead use AttachmentURLCommand directly
   */
  @Deprecated
  public boolean isAttachmentLink(String link) {
    return getAttachmentUrlCmd().isAttachmentLink(link);
  }

  /**
   * @deprecated instead use WebUtilsService.getUserNameForDocRef
   */
  @Deprecated
  public String getUserNameForDocName(String authorDocName,
      XWikiContext context) throws XWikiException {
    return getWebUtilsService().getUserNameForDocRef(getRef(authorDocName));
  }
  
  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public String getMajorVersion(XWikiDocument doc) {
    return getWebUtilsService().getMajorVersion(doc);
  }
  
  DocumentReference getRef(String s){
    return getWebUtilsService().resolveDocumentReference(s);
  }
  
}
