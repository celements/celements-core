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

import java.util.Date;
import java.util.List;

import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.INavFilter;
import com.celements.pagetype.IPageType;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.XWikiMessageTool;

public interface IWebUtils {

  /**
   * @deprecated since 2.17.0 instead use TreeNodeCache
   */
  @Deprecated
  public int queryCount();

  /**
   * @deprecated since 2.17.0 instead use TreeNodeCache
   */
  @Deprecated
  public void flushMenuItemCache(XWikiContext context);

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService
   */
  @Deprecated
  public List<String> getDocumentParentsList(String fullName,
      boolean includeDoc, XWikiContext context);

  /**
   * @deprecated since 2.14.0 use isEmptyRTEDocument in EmptyCheckCommand class instead.
   */
  @Deprecated
  public boolean isEmptyRTEDocument(XWikiDocument localdoc);
  
  /**
   * @deprecated since 2.14.0 use isEmptyRTEString in EmptyCheckCommand class instead.
   */
  @Deprecated
  public boolean isEmptyRTEString(String rteContent);

  /**
   * @deprecated since 2.17.0 instead use TreeNodeService
   */
  @Deprecated
  public int getActiveMenuItemPos(int menuLevel, String menuPart,
      XWikiContext context);

  /**
   * @deprecated since 2.17.0 instead use TreeNodeService
   */
  @Deprecated
  public int getMenuItemPos(String fullName, String menuPart,
      XWikiContext context);

  /**
   * get all submenu items of given parent document (by fullname).
   * 
   * @param parent
   * @param menuSpace (default: $doc.space)
   * @param menuPart 
   * @return (array of menuitems)
   * 
   * @deprecated since 2.17.0 instead use TreeNodeService.getSubNodesForParent
   */
  @Deprecated
  public List<Object> getSubMenuItemsForParent(
      String parent, String menuSpace, String menuPart, XWikiContext context);

  
  /**
   * @deprecated since 2.17.0 instead use TreeNodeService
   * 
   * getMenuItemsForHierarchyLevel
   * get all submenu items of given parent document (by fullname).
   * 
   * @param menuLevel
   * @param menuPart 
   * @return (array of menuitems)
   */
  @Deprecated
  public List<Object> getMenuItemsForHierarchyLevel(
      int menuLevel, String menuPart, XWikiContext context);

  /**
   * @deprecated since 2.17.0 instead use TreeNodeService
   */
  @Deprecated
  public String getParentForLevel(int menuLevel, XWikiContext context);

  /**
   * @deprecated since 2.14.0 use getSubNodesForParent instead
   */
  @Deprecated
  public List<BaseObject> getSubMenuItemsForParent_internal(
      String parent, String menuSpace, String menuPart, XWikiContext context);

  /**
   * getSubNodesForParent
   * get all subnodes of a given parent document (by fullname).
   * 
   * @param parent
   * @param menuSpace (default: $doc.space)
   * @param menuPart 
   * @return (array of tree nodes)
   * 
   * @deprecated since 2.14.0 instead use TreeNodeService directly
   */
  @Deprecated
  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      String menuPart, XWikiContext context);

  /**
   * 
   * @deprecated since 2.14.0 use getSubNodesForParent instead
   */
  @Deprecated
  public <T> List<T> getSubMenuItemsForParent(String parent, String menuSpace,
      INavFilter<T> filter, XWikiContext context);

  /**
   * 
   * @param <T>
   * @param parent
   * @param menuSpace
   * @param filter
   * @param context
   * @return
   * 
   * @deprecated since 2.14.0 instead use TreeNodeService directly
   */
  @Deprecated
  public <T> List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      INavFilter<T> filter, XWikiContext context);


  /**
   * @deprecated since 2.17.0 instead use TreeNodeService
   */
  @Deprecated
  public BaseObject getPrevMenuItem(String fullName,
      XWikiContext context) throws XWikiException;
  
  /**
   * @deprecated since 2.17.0 instead use TreeNodeService
   */
  @Deprecated
  public BaseObject getNextMenuItem(String fullName,
      XWikiContext context) throws XWikiException;

  /**
   * @deprecated since 2.17.0 instead use InheritorFactory().getConfigDocFieldInheritor
   */
  @Deprecated
  public XWikiDocument getConfigDocByInheritance(XWikiDocument doc,
      String className, XWikiContext context) throws XWikiException;
  
  /**
   * @deprecated since 2.17.0 instead use WebUtilsService
   */
  @Deprecated
  public String getDocSectionAsJSON(String regex, String fullName, int part, 
      XWikiContext context) throws XWikiException;

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService
   */
  @Deprecated
  public int countSections(String regex, String fullName, XWikiContext context)
      throws XWikiException;

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService
   */
  @Deprecated
  public IPageType getPageTypeApi(String fullName, XWikiContext context
      ) throws XWikiException;

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService
   */
  @Deprecated
  public List<String> getAllowedLanguages(XWikiContext context);

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService
   */
  @Deprecated
  public Date parseDate(String date, String format);

  /**
   * @deprecated since 2.14.0 instead use WebUtilsService directly
   */
  @Deprecated
  public XWikiMessageTool getMessageTool(String adminLanguage,
      XWikiContext context);

  /**
   * @deprecated since 2.14.0 instead use WebUtilsService directly
   */
  @Deprecated
  public XWikiMessageTool getAdminMessageTool(XWikiContext context);

  /**
   * @deprecated since 2.14.0 instead use WebUtilsService directly
   */
  @Deprecated
  public String getAdminLanguage(XWikiContext context);
  
  /**
   * @deprecated since 2.14.0 instead use WebUtilsService directly
   */
  @Deprecated
  public String getAdminLanguage(String userFullName, XWikiContext context);
  
  /**
   * @deprecated since 2.17.0 instead use WebUtilsService directly
   */
  @Deprecated
  public boolean hasParentSpace(XWikiContext context);

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService directly
   */
  @Deprecated
  public String getParentSpace(XWikiContext context);

  /**
   * @deprecated since 2.17.0 instead use TreeNodeService directly
   */
  @Deprecated
  public Integer getMaxConfiguredNavigationLevel(XWikiContext context);

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService directly
   */
  @Deprecated
  public List<Attachment> getAttachmentListSorted(Document doc,
      String comparator) throws ClassNotFoundException;

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService directly
   */
  @Deprecated
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException;

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService directly
   */
  @Deprecated
  public String getAttachmentListSortedAsJSON(Document doc, String comparator,
      boolean imagesOnly);
  
  /**
   * @deprecated since 2.17.0 instead use WebUtilsService directly
   */
  @Deprecated
  public String getAttachmentListSortedAsJSON(Document doc, String comparator,
      boolean imagesOnly, int start, int nb);

  /**
   * @deprecated since 2.17.0 instead use ImageService directly
   */
  @Deprecated
  public List<Attachment> getRandomImages(String fullName, int num,
      XWikiContext context);

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService directly
   */
  @Deprecated
  public boolean isAdminUser(XWikiContext context);

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService directly
   */
  @Deprecated
  public String getJSONContent(XWikiDocument document, XWikiContext context);

  /**
   * @deprecated since 2.14.0 instead use AttachmentURLCommand directly
   */
  @Deprecated
  public String getAttachmentURL(String link, XWikiContext context);

  /**
   * @deprecated since 2.17.0 instead use WebUtilsService.getUserNameForDocRef
   */
  @Deprecated
  public String getUserNameForDocName(String authorDocName,
      XWikiContext context) throws XWikiException;
  
  /**
   * @deprecated instead use AttachmentURLCommand directly
   */
  @Deprecated
  public String getMajorVersion(XWikiDocument doc);

  /**
   * @deprecated since 2.14.0 instead use AttachmentURLCommand directly
   */
  @Deprecated
  public String getAttachmentName(String link);

  /**
   * @deprecated since 2.14.0 instead use AttachmentURLCommand directly
   */
  @Deprecated
  public String getPageFullName(String link);

  /**
   * @deprecated since 2.14.0 instead use AttachmentURLCommand directly
   */
  @Deprecated
  public boolean isAttachmentLink(String link);

}