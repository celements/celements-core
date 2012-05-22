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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.TreeNode;
import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.celements.navigation.filter.ExternalUsageFilter;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.service.ITreeNodeCache;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.sajson.Builder;
import com.celements.web.pagetype.IPageType;
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

  private static Random rand = new Random();

  private AttachmentURLCommand attachmentUrlCmd;

  ITreeNodeService injected_TreeNodeService;
  
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

  
  private ITreeNodeService getTreeNodeService() {
    if (injected_TreeNodeService != null) {
      return injected_TreeNodeService;
    }
    return Utils.getComponent(ITreeNodeService.class);
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

  // TODO needed?
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
   * 
   * @deprecated instead use TreeNodeService.getSubNodesForParent
   */
  @Deprecated
  public List<BaseObject> getSubMenuItemsForParent_internal(String parent,
      String menuSpace, String menuPart, XWikiContext context) {
    InternalRightsFilter filter = new InternalRightsFilter();
    filter.setMenuPart(menuPart);
    return getSubMenuItemsForParent(parent, menuSpace, filter, context);
  }

  /**
   * @deprecated instead use TreeNodeService
   */
  @Deprecated
  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      String menuPart, XWikiContext context) {
    return getTreeNodeService().getSubNodesForParent(parent, menuSpace, menuPart);
  }

  /**
   * 
   * @deprecated instead use TreeNodeService
   */
  @Deprecated
  public <T> List<T> getSubMenuItemsForParent(String parent, String menuSpace,
      INavFilter<T> filter, XWikiContext context) {
    return getTreeNodeService().getSubMenuItemsForParent(parent, menuSpace, filter);
  }

  /**
   * @deprecated instead use TreeNodeService
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
   * @deprecated instead use TreeNodeService
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
    return getSiblingMenuItem(fullName, true, context);
  }

  /**
   * @deprecated instead use TreeNodeService
   */
  @Deprecated
  public BaseObject getNextMenuItem(String fullName,
      XWikiContext context) throws XWikiException {
    return getSiblingMenuItem(fullName, false, context);
  }

  @Deprecated
  BaseObject getSiblingMenuItem(String fullName, boolean previous,
      XWikiContext context)
      throws XWikiException {
    XWikiDocument doc = context.getWiki().getDocument(fullName, context);
    BaseObject menuItem = doc.getObject("Celements2.MenuItem");
    if (menuItem != null){
      String parent;
      try {
        parent = getParentFullName(fullName, context);
        List<BaseObject> submenuItems = getSubMenuItemsForParent_internal(
            parent, doc.getSpace(), menuItem.getStringValue("part_name"), context);
        LOGGER.debug("getPrevMenuItem: " + submenuItems.size()
            + " submenuItems found for parent '" + parent + "'. "
            + Arrays.deepToString(submenuItems.toArray()));
        int pos = getMenuItemPos(fullName, menuItem.getStringValue("part_name"), context);
        if (previous && (pos > 0)) {
          return submenuItems.get(pos - 1);
        } else if (!previous && (pos < (submenuItems.size() - 1))) {
          return submenuItems.get(pos + 1);
        }
        LOGGER.info("getPrevMenuItem: no previous MenuItem found for "
            + fullName);
      } catch (XWikiException e) {
        LOGGER.error(e);
      }
    } else {
      LOGGER.debug("getPrevMenuItem: no MenuItem Object found on doc "
          + fullName);
    }
    return null;
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
        String skinDocName = context.getWiki().getWebPreference("skin", context);
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
    return getWebUtilsService().getPageTypeApi(getRef(fullName));
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
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public XWikiMessageTool getMessageTool(String adminLanguage, XWikiContext context) {
    return getWebUtilsService().getMessageTool(adminLanguage);
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public XWikiMessageTool getAdminMessageTool(XWikiContext context) {
    return getWebUtilsService().getAdminMessageTool();
  }
  
  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public String getAdminLanguage(XWikiContext context) {
    return getWebUtilsService().getAdminLanguage();
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  @Deprecated
  public String getAdminLanguage(String userFullName, XWikiContext context) {
    return getWebUtilsService().getAdminLanguage(userFullName);
  }

  public boolean hasParentSpace(XWikiContext context) {
    return (getParentSpace(context) != null)
      && !"".equals(getParentSpace(context));
  }

  public String getParentSpace(XWikiContext context) {
    return context.getWiki().getWebPreference("parent", context);
  }

  /**
   * @deprecated instead use TreeNodeService
   */
  public Integer getMaxConfiguredNavigationLevel(XWikiContext context) {
    return getTreeNodeService().getMaxConfiguredNavigationLevel();
  }
  
  /**
   * @deprecated instead use WebUtilsService
   */
  @SuppressWarnings("unchecked")
  public List<Attachment> getAttachmentListSorted(Document doc,
      String comparator) throws ClassNotFoundException {
    return this.getWebUtilsService().getAttachmentListSorted(doc, comparator);
  }

  /**
   * @deprecated instead use WebUtilsService
   */
  public String getAttachmentListSortedAsJSON(Document doc,
      String comparator, boolean imagesOnly) {
    return this.getWebUtilsService().getAttachmentListSortedAsJSON(doc, comparator,
        imagesOnly);
  }
  
  /**
   * getRandomImages computes a set of <num> randamly chosen images
   * from the given AttachmentList. It chooses the Images without dubilcats if
   * possible.
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
   * @deprecated instead use WebUtilsService directly
   */
  @Deprecated
  public boolean isAdminUser(XWikiContext context) {
    return getWebUtilsService().isAdminUser();
  }

  public Map<String, String> xwikiDoctoLinkedMap(XWikiDocument xwikiDoc,
      boolean bWithObjects, boolean bWithRendering,
      boolean bWithAttachmentContent, boolean bWithVersions,
      XWikiContext context) throws XWikiException {
    Map<String,String> docData = new LinkedHashMap<String, String>();
    docData.put("web", xwikiDoc.getSpace());
    docData.put("name", xwikiDoc.getName());
    docData.put("language", xwikiDoc.getLanguage());
    docData.put("defaultLanguage", xwikiDoc.getDefaultLanguage());
    docData.put("translation", "" + xwikiDoc.getTranslation());
    docData.put("defaultLanguage", xwikiDoc.getDefaultLanguage());
    docData.put("parent", xwikiDoc.getParent());
    docData.put("creator", xwikiDoc.getCreator());
    docData.put("author", xwikiDoc.getAuthor());
    docData.put("creator", xwikiDoc.getCreator());
    docData.put("customClass", xwikiDoc.getCustomClass());
    docData.put("contentAuthor", xwikiDoc.getContentAuthor());
    docData.put("creationDate", "" + xwikiDoc.getCreationDate().getTime());
    docData.put("date", "" + xwikiDoc.getDate().getTime());
    docData.put("contentUpdateDate", "" + xwikiDoc.getContentUpdateDate().getTime());
    docData.put("version", xwikiDoc.getVersion());
    docData.put("title", xwikiDoc.getTitle());
    docData.put("template", xwikiDoc.getTemplate());
    docData.put("getDefaultTemplate", xwikiDoc.getDefaultTemplate());
    docData.put("getValidationScript", xwikiDoc.getValidationScript());
    docData.put("comment", xwikiDoc.getComment());
    docData.put("minorEdit", String.valueOf(xwikiDoc.isMinorEdit()));
    docData.put("syntaxId", xwikiDoc.getSyntaxId());
    docData.put("menuName", new MultilingualMenuNameCommand().getMultilingualMenuName(
        xwikiDoc.getObject("Celements2.MenuItem"), xwikiDoc.getLanguage(), context));
    //docData.put("hidden", String.valueOf(xwikiDoc.isHidden()));

    /** TODO add Attachments
    for (XWikiAttachment attach : xwikiDoc.getAttachmentList()) {
        docel.add(attach.toXML(bWithAttachmentContent, bWithVersions, context));
    }**/

    if (bWithObjects) {
//        // Add Class
//        BaseClass bclass = xwikiDoc.getxWikiClass();
//        if (bclass.getFieldList().size() > 0) {
//            // If the class has fields, add class definition and field information to XML
//            docel.add(bclass.toXML(null));
//        }
//
//        // Add Objects (THEIR ORDER IS MOLDED IN STONE!)
//        for (Vector<BaseObject> objects : getxWikiObjects().values()) {
//            for (BaseObject obj : objects) {
//                if (obj != null) {
//                    BaseClass objclass = null;
//                    if (StringUtils.equals(getFullName(), obj.getClassName())) {
//                        objclass = bclass;
//                    } else {
//                        objclass = obj.getxWikiClass(context);
//                    }
//                    docel.add(obj.toXML(objclass));
//                }
//            }
//        }
      throw new NotImplementedException();
    }

    String host = context.getRequest().getHeader("host");
    // Add Content
    docData.put("content", replaceInternalWithExternalLinks(xwikiDoc.getContent(), host));
    
    if (bWithRendering) {
      try {
        docData.put("renderedcontent", replaceInternalWithExternalLinks(xwikiDoc.getRenderedContent(context), host));
      } catch (XWikiException e) {
        LOGGER.error("Exception with rendering content: " + e.getFullMessage());
      }
    }

    if (bWithVersions) {
        try {
          docData.put("versions", xwikiDoc.getDocumentArchive(context
              ).getArchive(context));
        } catch (XWikiException e) {
            LOGGER.error("Document [" + xwikiDoc.getFullName()
                + "] has malformed history");
        }
    }

    return docData;
  }

  String replaceInternalWithExternalLinks(String content, String host) {
    String result = content.replaceAll("src=\\\"(\\.\\./)*/?download/", "src=\"http://" + host + "/download/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?download/", "href=\"http://" + host + "/download/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?skin/", "href=\"http://" + host + "/skin/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?view/", "href=\"http://" + host + "/view/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?edit/", "href=\"http://" + host + "/edit/");
    return result;
  }

  public String getJSONContent(XWikiDocument cdoc, XWikiContext context) {
    Map<String, String> data;
    try {
      //    data = xwikiDoctoLinkedMap(tdoc, true, true, false,
//    false, context);
      data = xwikiDoctoLinkedMap(cdoc.getTranslatedDocument(context),
          false, true, false, false, context);
    } catch (XWikiException e) {
      LOGGER.error(e);
      data = Collections.emptyMap();
    }

    Builder jasonBuilder = new Builder();
    jasonBuilder.openDictionary();
    for (String key : data.keySet()) {
      String value = data.get(key);
      jasonBuilder.addStringProperty(key, value);
    }
    jasonBuilder.closeDictionary();
    return jasonBuilder.getJSON();
  }


  /**
   * @deprecated instead use AttachmentURLCommand directly
   */
  @Deprecated
  public String getAttachmentURL(String link, XWikiContext context) {
    return getAttachmentUrlCmd().getAttachmentURL(link, context);
  }

  /**
   * @deprecated instead use AttachmentURLCommand directly
   */
  @Deprecated
  public String getAttachmentName(String link) {
    return getAttachmentUrlCmd().getAttachmentName(link);
  }

  /**
   * @deprecated instead use AttachmentURLCommand directly
   */
  @Deprecated
  public String getPageFullName(String link) {
    return getAttachmentUrlCmd().getPageFullName(link);
  }

  /**
   * @deprecated instead use AttachmentURLCommand directly
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
   * @deprecated instead use WebUtilsService directly
   */
  @Deprecated
  public String getMajorVersion(XWikiDocument doc) {
    return getWebUtilsService().getMajorVersion(doc);
  }
  
  DocumentReference getRef(String s){
    return getWebUtilsService().resolveDocumentReference(s);
  }
  
}
