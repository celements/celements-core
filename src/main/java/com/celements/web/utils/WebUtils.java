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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.inheritor.InheritorFactory;
import com.celements.navigation.Navigation;
import com.celements.navigation.TreeNode;
import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.celements.navigation.filter.ExternalUsageFilter;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.service.ITreeNodeCache;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.sajson.Builder;
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
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;

public class WebUtils implements IWebUtils {
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(WebUtils.class);

  private static IWebUtils instance;

  private static Random rand = new Random();

  private InheritorFactory injectedInheritorFactory;

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

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#queryCount()
   */
  @Deprecated
  public int queryCount() {
    return getTreeNodeCache().queryCount();
  }

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#flushMenuItemCache(com.xpn.xwiki.XWikiContext)
   */
  @Deprecated
  public void flushMenuItemCache(XWikiContext context) {
    getTreeNodeCache().flushMenuItemCache();
  }

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#getDocumentParentsList(java.lang.String, boolean, com.xpn.xwiki.XWikiContext)
   */
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

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#getActiveMenuItemPos(int, java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  public int getActiveMenuItemPos(int menuLevel, String menuPart,
      XWikiContext context) {
    List<String> parents = getDocumentParentsList(
        context.getDoc().getFullName(), true, context);
    if (parents.size() >= menuLevel) {
      return getMenuItemPos(parents.get(parents.size() - menuLevel), menuPart, context);
    }
    return -1;
  }

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#getMenuItemPos(java.lang.String, java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  public int getMenuItemPos(String fullName, String menuPart,
      XWikiContext context) {
    try {
      XWikiDocument doc = context.getWiki().getDocument(fullName, context);
      String parent = getParentFullName(fullName, context);
      int pos = -1;
      for (BaseObject menuItem : getSubMenuItemsForParent_internal(parent, doc.getSpace(),
          menuPart, context)) {
        pos = pos + 1;
        if (fullName.equals(menuItem.getName())) {
          return pos;
        }
      }
    } catch (XWikiException e) {
      LOGGER.error(e);
    }
    return -1;
  }

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#getSubMenuItemsForParent(java.lang.String, java.lang.String, java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  public List<com.xpn.xwiki.api.Object> getSubMenuItemsForParent(
      String parent, String menuSpace, String menuPart, XWikiContext context) {
    ExternalUsageFilter filter = new ExternalUsageFilter();
    filter.setMenuPart(menuPart);
    return getSubMenuItemsForParent(parent, menuSpace, filter, context);
  }

  /**
   * 
   * @deprecated use getSubNodesForParent instead
   */
  @Deprecated
  public List<BaseObject> getSubMenuItemsForParent_internal(String parent,
      String menuSpace, String menuPart, XWikiContext context) {
    InternalRightsFilter filter = new InternalRightsFilter();
    filter.setMenuPart(menuPart);
    return getSubMenuItemsForParent(parent, menuSpace, filter, context);
  }

  /**
   * @deprecated instead use TreeNodeService directly
   */
  @Deprecated
  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      String menuPart, XWikiContext context) {
    return getTreeNodeService().getSubNodesForParent(parent, menuSpace, menuPart);
  }

  /**
   * 
   * @deprecated use getSubNodesForParent instead
   */
  @Deprecated
  public <T> List<T> getSubMenuItemsForParent(String parent, String menuSpace,
      INavFilter<T> filter, XWikiContext context) {
    return getTreeNodeService().getSubMenuItemsForParent(parent, menuSpace, filter);
  }

  /**
   * @deprecated instead use TreeNodeService directly
   */
  @Deprecated
  public <T> List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      INavFilter<T> filter, XWikiContext context) {
    return getTreeNodeService().getSubNodesForParent(parent, menuSpace, filter);
  }

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#getMenuItemsForHierarchyLevel(int, java.lang.String, com.xpn.xwiki.XWikiContext)
   */
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

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#getParentForLevel(int, com.xpn.xwiki.XWikiContext)
   */
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

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#getPrevMenuItem(java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  public BaseObject getPrevMenuItem(String fullName,
      XWikiContext context) throws XWikiException {
    return getSiblingMenuItem(fullName, true, context);
  }

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#getNextMenuItem(java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  public BaseObject getNextMenuItem(String fullName,
      XWikiContext context) throws XWikiException {
    return getSiblingMenuItem(fullName, false, context);
  }

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
  
  public String getDocSectionAsJSON(String regex, String fullName, int section,
      XWikiContext context) throws XWikiException {
    Builder jsonBuilder = new Builder();
    jsonBuilder.openArray();
    
    jsonBuilder.openDictionary();
    jsonBuilder.addStringProperty("content", getDocSection(regex, fullName,
        section, context));
    int sectionNr = countSections(regex, fullName, context);
    jsonBuilder.openProperty("section");
    jsonBuilder.addNumber(new BigDecimal(getSectionNr(section, sectionNr)));
    jsonBuilder.openProperty("sectionNr");
    jsonBuilder.addNumber(new BigDecimal(sectionNr));
    jsonBuilder.closeDictionary();
    jsonBuilder.closeArray();
    return jsonBuilder.getJSON();
  }
  
  int getSectionNr(int section, int sectionNr) {
    if(section <= 0){ section = 1; }
    if(section > sectionNr){ section = sectionNr; }
    return section;
  }

  public String getDocSection(String regex, String fullName, int part,
      XWikiContext context) throws XWikiException {
    LOGGER.debug("use regex '" + regex + "' on '" + fullName
        + "' and get section " + part);
    XWikiDocument doc = context.getWiki().getDocument(fullName, context);
    String content = doc.getTranslatedDocument(context).getContent();
    LOGGER.debug("content of'" + doc.getFullName() + "' is: '" + content + "'");
    String section = null;
    if((content != null) && (!isEmptyRTEString(content))){
      part = getSectionNr(part, countSections(regex, fullName, context));
      for (String partStr : content.split(regex)) {
        if(!isEmptyRTEString(partStr)) {
          part--;
          if(part == 0) {
            section = partStr;
            break;
          }
        }
      }
    } else {
      LOGGER.debug("content ist empty");
    }
    if(section != null) {
      section = renderText(section, context);
    }
    return section;
  }

  public int countSections(String regex, String fullName, XWikiContext context
      ) throws XWikiException {
    LOGGER.debug("use regex '" + regex + "' on '" + fullName + "'");
    XWikiDocument doc = context.getWiki().getDocument(fullName, context);
    String content = doc.getTranslatedDocument(context).getContent();
    LOGGER.debug("content of'" + doc.getFullName() + "' is: '" + content + "'");
    int parts = 0;
    if((content != null) && (!isEmptyRTEString(content))){
      for (String part : content.split(regex)) {
        if(!isEmptyRTEString(part)) {
          parts++;
        }
      }
    } else {
      LOGGER.debug("content ist empty");
    }
    return parts;
  }
  
  public IPageType getPageTypeApi(String fullName, XWikiContext context)
    throws XWikiException {
    return new PageTypeApi(fullName,
        context);
  }

  public List<String> getAllowedLanguages(XWikiContext context) {
    return Arrays.asList(context.getWiki(
        ).getWebPreference("language", context).split("[ ,]"));
  }

  public Date parseDate(String date, String format) {
    try {
      return new SimpleDateFormat(format).parse(date);
    } catch (ParseException e) {
      LOGGER.fatal(e);
      return null;
    }
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  /**
   * @deprecated instead use WebUtilsService directly
   */
  @Deprecated
  public XWikiMessageTool getMessageTool(String adminLanguage, XWikiContext context) {
    return getWebUtilsService().getMessageTool(adminLanguage);
  }

  /**
   * @deprecated instead use WebUtilsService directly
   */
  @Deprecated
  public XWikiMessageTool getAdminMessageTool(XWikiContext context) {
    return getWebUtilsService().getAdminMessageTool();
  }
  
  /**
   * @deprecated instead use WebUtilsService directly
   */
  @Deprecated
  public String getAdminLanguage(XWikiContext context) {
    return getWebUtilsService().getAdminLanguage();
  }

  /**
   * @deprecated instead use WebUtilsService directly
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

  public Integer getMaxConfiguredNavigationLevel(XWikiContext context) {
    try {
      BaseCollection navConfigObj = getInheritorFactory().getConfigDocFieldInheritor(
          Navigation.NAVIGATION_CONFIG_CLASS, context.getDoc().getFullName(), context
          ).getObject("menu_element_name");
      if (navConfigObj != null) {
        XWikiDocument navConfigDoc = navConfigObj.getDocument(context);
        Vector<BaseObject> navConfigObjects = navConfigDoc.getObjects(
            Navigation.NAVIGATION_CONFIG_CLASS);
        int maxLevel = 0;
        if (navConfigObj != null) {
          for (BaseObject navObj : navConfigObjects) {
            if (navObj != null) {
              maxLevel = Math.max(maxLevel, navObj.getIntValue("to_hierarchy_level"));
            }
          }
        }
        return maxLevel;
      }
    } catch (XWikiException e) {
      LOGGER.error("unable to get configDoc.", e);
    }
    return Navigation.DEFAULT_MAX_LEVEL;
  }

  void injectInheritorFactory(InheritorFactory injectedInheritorFactory) {
    this.injectedInheritorFactory = injectedInheritorFactory;
  }

  private InheritorFactory getInheritorFactory() {
    if (injectedInheritorFactory != null) {
      return injectedInheritorFactory;
    }
    return new InheritorFactory();
  }

  @SuppressWarnings("unchecked")
  public List<Attachment> getAttachmentListSorted(Document doc,
      String comparator) throws ClassNotFoundException {
    List<Attachment> attachments = doc.getAttachmentList();
    
      try {
        Comparator<Attachment> comparatorClass = 
          (Comparator<Attachment>) Class.forName(
              "com.celements.web.comparators." + comparator).newInstance();
      Collections.sort(attachments, comparatorClass);
    } catch (InstantiationException e) {
      LOGGER.error(e);
    } catch (IllegalAccessException e) {
      LOGGER.error(e);
    } catch (ClassNotFoundException e) {
      throw e;
    }
    
    return attachments;
  }

  public List<Attachment> getAttachmentListSorted(Document doc,
      String comparator, boolean imagesOnly) {
    try {
      List<Attachment> attachments = getAttachmentListSorted(doc, comparator);
      if (imagesOnly) {
        for (Attachment att : new ArrayList<Attachment>(attachments)) {
          if (!att.isImage()) {
            attachments.remove(att);
          }
        }
      }
      return attachments;
    } catch (ClassNotFoundException exp) {
      LOGGER.error(exp);
    }
    return Collections.emptyList();
  }

  public String getAttachmentListSortedAsJSON(Document doc,
      String comparator, boolean imagesOnly) {
    SimpleDateFormat dateFormater = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    Builder jsonBuilder = new Builder();
    jsonBuilder.openArray();
    for (Attachment att : getAttachmentListSorted(doc, comparator, imagesOnly)) {
      jsonBuilder.openDictionary();
      jsonBuilder.addStringProperty("filename", att.getFilename());
      jsonBuilder.addStringProperty("version", att.getVersion());
      jsonBuilder.addStringProperty("author", att.getAuthor());
      jsonBuilder.addStringProperty("mimeType", att.getMimeType());
      jsonBuilder.addStringProperty("lastChanged",
          dateFormater.format(att.getDate()));
      jsonBuilder.addStringProperty("url",
          doc.getAttachmentURL(att.getFilename()));
      jsonBuilder.closeDictionary();
    }
    jsonBuilder.closeArray();
    return jsonBuilder.getJSON();
  }

  int coveredQuotient(int divisor, int dividend) {
    if (dividend >= 0) {
      return ( (dividend + divisor - 1) / divisor);
    } else {
      return (dividend / divisor);
    }
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
      List<Attachment> allImagesList = getAttachmentListSorted(imgDoc,
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

  /**
   * @deprecated since 2.11.6 instead use WebUtilsService directly
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

  private AttachmentURLCommand getAttachmentUrlCmd() {
    if (attachmentUrlCmd == null) {
      attachmentUrlCmd = new AttachmentURLCommand();
    }
    return attachmentUrlCmd;
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

  public String getUserNameForDocName(String authorDocName,
      XWikiContext context) throws XWikiException {
    XWikiDocument authorDoc = context.getWiki().getDocument(authorDocName, context);
    BaseObject authorObj = authorDoc.getObject("XWiki.XWikiUsers");
    if(authorObj != null) {
      return authorObj.getStringValue("last_name") + ", " + authorObj.getStringValue("first_name");
    }
    return renderText("$adminMsg.get('cel_ml_unknown_author')", context);
  }
  
  private String renderText(String velocityText, XWikiContext context) {
    return context.getWiki().getRenderingEngine().renderText(
        "{pre}" + velocityText + "{/pre}", context.getDoc(), context);
  }
  
  public String getMajorVersion(XWikiDocument doc) {
    String revision = "1";
    if(doc != null){
      revision = doc.getVersion();
      if((revision != null) && (revision.trim().length() > 0) && (revision.contains("."))) {
        revision = revision.split("\\.")[0];
      }
    }
    return revision;
  }
  
}
