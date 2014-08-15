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
package com.celements.web.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.inheritor.TemplatePathTransformationConfiguration;
import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.celements.rendering.RenderCommand;
import com.celements.rendering.XHTMLtoHTML5cleanup;
import com.celements.sajson.Builder;
import com.celements.web.comparators.BaseObjectComparator;
import com.celements.web.plugin.api.CelementsWebPluginApi;
import com.celements.web.plugin.cmd.EmptyCheckCommand;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;
import com.xpn.xwiki.web.XWikiRequest;

@Component
public class WebUtilsService implements IWebUtilsService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(WebUtilsService.class);

  @Requirement("default")
  EntityReferenceSerializer<String> serializer_default;
  
  @Requirement("local")
  EntityReferenceSerializer<String> serializer_local;
  
  @Requirement
  EntityReferenceResolver<String> referenceResolver;

  /**
   * Used to get the template path mapping information.
   */
  @Requirement
  private TemplatePathTransformationConfiguration tempPathConfig;

  /**
   * TODO change access to wiki-configuration "default" and check access to e.g.
   * 'languages' property
   * 
   * You get access to this configuration source by using:
   * 
   * @Requirement private ConfigurationSource configuration;
   * 
   *              Configuration properties are first looked for in the "space"
   *              source, if not found then in the "wiki" source and if not
   *              found in the "xwikiproperties" source. This is the recommended
   *              configuration source for most usages since it allows to
   *              defined configuration properties in the xwiki.propertieds file
   *              and to override them in the running wiki (globally or per
   *              space).
   *  
   *  Source: http://extensions.xwiki.org/xwiki/bin/view/Extension/Configuration+Module
   */

  @Requirement
  Execution execution;

  XWikiRenderingEngine injectedRenderingEngine;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public DocumentReference getParentForLevel(int level) {
    LOGGER.trace("getParentForLevel: start for level " + level);
    DocumentReference parent = null;
    List<DocumentReference> parentList = getDocumentParentsList(
        getContext().getDoc().getDocumentReference(), true);
    int startAtItem = parentList.size() - level + 1;
    if ((startAtItem > -1) && (startAtItem < parentList.size())) {
      parent = parentList.get(startAtItem);
    }
    LOGGER.debug("getParentForLevel: level [" + level + "] returning [" + parent + "]");
    return parent;
  }
  
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef,
      boolean includeDoc) {
    ArrayList<DocumentReference> docParents = new ArrayList<DocumentReference>();
    try {
      DocumentReference nextParent;
      if (includeDoc) {
        nextParent = docRef;
      } else {
        nextParent = getParentRef(docRef);
      }
      while ((nextParent != null)
          && getContext().getWiki().exists(nextParent, getContext())
          && !docParents.contains(nextParent)) {
        docParents.add(nextParent);
        nextParent = getParentRef(nextParent);
      }
    } catch (XWikiException e) {
      LOGGER.error("Failed to get parent reference. ", e);
    }
    return docParents;
  }
  
  private DocumentReference getParentRef(DocumentReference docRef) throws XWikiException {
    return getContext().getWiki().getDocument(docRef, getContext()).getParentReference();
  }
  
  public String getDocSectionAsJSON(String regex, DocumentReference docRef,
      int section) throws XWikiException {
    Builder jsonBuilder = new Builder();
    jsonBuilder.openArray();
    jsonBuilder.openDictionary();
    jsonBuilder.addStringProperty("content", getDocSection(regex, docRef, section));
    int sectionNr = countSections(regex, docRef);
    jsonBuilder.openProperty("section");
    jsonBuilder.addNumber(new BigDecimal(getSectionNr(section, sectionNr)));
    jsonBuilder.openProperty("sectionNr");
    jsonBuilder.addNumber(new BigDecimal(sectionNr));
    jsonBuilder.closeDictionary();
    jsonBuilder.closeArray();
    return jsonBuilder.getJSON();
  }

  public String getDocSection(String regex, DocumentReference docRef, int section
      ) throws XWikiException {
    LOGGER.debug("use regex '" + regex + "' on '" + docRef
        + "' and get section " + section);
    XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
    String content = doc.getTranslatedDocument(getContext()).getContent();
    LOGGER.debug("content of'" + docRef + "' is: '" + content + "'");
    String section_str = null;
    if((content != null) && (!isEmptyRTEString(content))){
      section = getSectionNr(section, countSections(regex, docRef));
      for (String partStr : content.split(regex)) {
        if(!isEmptyRTEString(partStr)) {
          section--;
          if(section == 0) {
            section_str = partStr;
            break;
          }
        }
      }
    } else {
      LOGGER.debug("content ist empty");
    }
    if(section_str != null) {
      section_str = renderText(section_str);
    }
    return section_str;
  }

  public int countSections(String regex, DocumentReference docRef) throws XWikiException {
    LOGGER.debug("use regex '" + regex + "' on '" + docRef + "'");
    XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
    String content = doc.getTranslatedDocument(getContext()).getContent();
    LOGGER.debug("content of'" + docRef + "' is: '" + content + "'");
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
  
  int getSectionNr(int section, int sectionNr) {
    if(section <= 0){ section = 1; }
    if(section > sectionNr){ section = sectionNr; }
    return section;
  }
  
  private String renderText(String velocityText) {
    return getContext().getWiki().getRenderingEngine().renderText(
        "{pre}" + velocityText + "{/pre}", getContext().getDoc(), getContext());
  }
  
  private boolean isEmptyRTEString(String rteContent) {
    return new EmptyCheckCommand().isEmptyRTEString(rteContent);
  }

  public List<String> getAllowedLanguages() {
    if ((getContext() != null) && (getContext().getDoc() != null)) {
      return getAllowedLanguages(getContext().getDoc().getDocumentReference(
          ).getLastSpaceReference().getName());
    }
    return Collections.emptyList();
  }

  public List<String> getAllowedLanguages(String spaceName) {
    List<String> languages = new ArrayList<String>();
    languages.addAll(Arrays.asList(getContext().getWiki(
      ).getSpacePreference("languages", spaceName, "", getContext()).split("[ ,]")));
    languages.remove("");
    if (languages.size() > 0) {
      return languages;
    }
    LOGGER.warn("Deprecated usage of Preferences field 'language'."
        + " Instead use 'languages'.");
    return Arrays.asList(getContext().getWiki(
      ).getSpacePreference("language", spaceName, "", getContext()).split("[ ,]"));
  }

  public Date parseDate(String date, String format){
    try{
      return new SimpleDateFormat(format).parse(date);
    } catch(ParseException e){
      LOGGER.fatal(e);
      return null;
    }
  }

  public XWikiMessageTool getMessageTool(String adminLanguage) {
    if(adminLanguage != null) {
      if((getContext().getLanguage() != null) && getContext().getLanguage().equals(
          adminLanguage)) {
        return getContext().getMessageTool();
      } else {
        Locale locale = new Locale(adminLanguage);
        ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources",
            locale);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("ApplicationResources");
        }
        XWikiContext adminContext = (XWikiContext) getContext().clone();
        adminContext.putAll(getContext());
        adminContext.setLanguage(adminLanguage);
        return new XWikiMessageTool(bundle, adminContext);
      }
    } else {
      return null;
    }
  }

  public XWikiMessageTool getAdminMessageTool() {
    return getMessageTool(getAdminLanguage());
  }
  
  public String getAdminLanguage() {
    return getAdminLanguage(getContext().getUser());
  }

  /**
   * @deprecated since 2.34.0 instead use getAdminLanguage(DocumentReference userRef)
   */
  @Deprecated
  public String getAdminLanguage(String userFullName) {
    return getAdminLanguage(resolveDocumentReference(userFullName));
  }

  public String getAdminLanguage(DocumentReference userRef) {
    String adminLanguage = null;
    try {
      DocumentReference xwikiUsersClassRef = new DocumentReference(
          userRef.getWikiReference().getName(), "XWiki", "XWikiUsers");
      BaseObject userObj = getContext().getWiki().getDocument(userRef, getContext()
          ).getXObject(xwikiUsersClassRef);
      if (userObj != null) {
        adminLanguage = userObj.getStringValue("admin_language");
      }
    } catch (XWikiException e) {
      LOGGER.error("failed to get UserObject for " + getContext().getUser());
    }
    if ((adminLanguage == null) || ("".equals(adminLanguage))) {
      adminLanguage = getDefaultAdminLanguage();
    }
    return adminLanguage;
  }

  public String getDefaultAdminLanguage() {
    String adminLanguage;
    adminLanguage = getContext().getWiki().getSpacePreference("admin_language",
        getContext().getLanguage(), getContext());
    if ((adminLanguage == null) || ("".equals(adminLanguage))) {
      adminLanguage = getContext().getWiki().Param("celements.admin_language");
      if ((adminLanguage == null) || ("".equals(adminLanguage))) {
        adminLanguage = "en";
      }
    }
    return adminLanguage;
  }
  
  public String getDefaultLanguage() {
    return getContext().getWiki().getSpacePreference("default_language", getContext());
  }

  public String getDefaultLanguage(String spaceName) {
    return getContext().getWiki().getSpacePreference("default_language", spaceName, "",
        getContext());
  }

  public boolean hasParentSpace() {
    return ((getParentSpace() != null) && !"".equals(getParentSpace()));
  }

  public boolean hasParentSpace(String spaceName) {
    return ((getParentSpace(spaceName) != null) && !"".equals(getParentSpace(spaceName)));
  }

  public String getParentSpace() {
    return getContext().getWiki().getSpacePreference("parent", getContext());
  }

  public String getParentSpace(String spaceName) {
    return getContext().getWiki().getSpacePreference("parent", spaceName, "",
        getContext());
  }

  public DocumentReference resolveDocumentReference(String fullName) {
    return resolveDocumentReference(fullName, null);
  }

  public DocumentReference resolveDocumentReference(String fullName, 
      WikiReference wikiRef) {
    return new DocumentReference(resolveEntityReference(fullName, EntityType.DOCUMENT, 
        wikiRef));
  }

  public SpaceReference resolveSpaceReference(String spaceName) {
    return resolveSpaceReference(spaceName, null);
  }

  public SpaceReference resolveSpaceReference(String spaceName, WikiReference wikiRef) {
    return new SpaceReference(resolveEntityReference(spaceName, EntityType.SPACE, 
        wikiRef));
  }

  private EntityReference resolveEntityReference(String name, EntityType type, 
      WikiReference wikiRef) {
    if (wikiRef == null) {
      wikiRef = new WikiReference(getContext().getDatabase());
    }
    EntityReference ref = referenceResolver.resolve(name, type, wikiRef);
    LOGGER.debug("resolveEntityReference: for [" + name + "] got reference [" + ref + "]");
    return ref;
  }

  public boolean isAdminUser() {
    try {
      if ((getContext().getXWikiUser() != null)
          && (getContext().getWiki().getRightService() != null)
          && (getContext().getDoc() != null)) {
        return (getContext().getWiki().getRightService().hasAdminRights(getContext())
          || getContext().getXWikiUser().isUserInGroup("XWiki.XWikiAdminGroup",
              getContext()));
      } else {
        return false;
      }
    } catch (XWikiException e) {
      LOGGER.error("Cannot determin if user has Admin Rights therefore guess"
        + " no (false).", e);
      return false;
    }
  }

  public boolean isSuperAdminUser() {
    String user = getContext().getUser();
    LOGGER.trace("isSuperAdminUser: user [" + user + "] db [" + getContext().getDatabase()
        + "].");
    return (isAdminUser() && (user.startsWith("xwiki:") || getContext().isMainWiki()));
  }

  public boolean isLayoutEditor() {
    String user = getContext().getUser();
    LOGGER.trace("isLayoutEditor: user [" + user + "] db [" + getContext().getDatabase()
        + "].");
    try {
      boolean isLayoutEditor = isAdvancedAdmin() || getContext().getXWikiUser(
          ).isUserInGroup("XWiki.LayoutEditorsGroup", getContext());
      LOGGER.debug("isLayoutEditor: admin [" + isAdminUser() + "] global user ["
          + user.startsWith("xwiki:") + "] returning [" + isLayoutEditor + "] db ["
          + getContext().getDatabase() + "].");
      return isLayoutEditor;
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get user document for [" + user + "].", exp);
    }
    return false;
  }

  public boolean isAdvancedAdmin() {
    String user = getContext().getUser();
    LOGGER.trace("isAdvancedAdmin: user [" + user + "] db [" + getContext().getDatabase()
        + "].");
    try {
      XWikiDocument userDoc = getContext().getWiki().getDocument(resolveDocumentReference(
          user), getContext());
      BaseObject userObj = userDoc.getXObject(resolveDocumentReference(
          "XWiki.XWikiUsers"));
      boolean isAdvancedAdmin = isAdminUser() && (user.startsWith("xwiki:")
          || ((userObj != null) && "Advanced".equals(userObj.getStringValue("usertype"
              ))));
      LOGGER.debug("isAdvancedAdmin: admin [" + isAdminUser() + "] global user ["
          + user.startsWith("xwiki:") + "] usertype [" + ((userObj != null
          ) ? userObj.getStringValue("usertype") : "null") + "] returning ["
          + isAdvancedAdmin + "] db [" + getContext().getDatabase() + "].");
      return isAdvancedAdmin;
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get user document for [" + user + "].", exp);
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public List<Attachment> getAttachmentListSortedSpace(String spaceName,
      String comparator, boolean imagesOnly, int start, int nb
      ) throws ClassNotFoundException {
    List<Attachment> attachments = new ArrayList<Attachment>();
    try {
      for(String docName : getContext().getWiki().getSpaceDocsName(spaceName, getContext())) {
        DocumentReference docRef = new DocumentReference(getContext().getDatabase(), 
            spaceName, docName);
        XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
        attachments.addAll(new Document(doc, getContext()).getAttachmentList());
      }
    } catch (XWikiException xwe) {
      LOGGER.error("Could not get all documents in " + spaceName, xwe);
    }
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
    if (imagesOnly) {
      for (Attachment att : new ArrayList<Attachment>(attachments)) {
        if (!att.isImage()) {
          attachments.remove(att);
        }
      }
    }
    return reduceListToSize(attachments, start, nb);
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
  
  List<Attachment> reduceListToSize(List<Attachment> attachments, int start, int nb) {
    List<Attachment> countedAtts = new ArrayList<Attachment>();
    if((start <= 0) && ((nb <= 0) || (nb >= attachments.size()))) {
      countedAtts = attachments;
    } else if(start < attachments.size()) {
      countedAtts = attachments.subList(Math.max(0, start), Math.min(Math.max(0, start) 
          + Math.max(0, nb), attachments.size()));
    }
    return countedAtts;
  }

  public List<Attachment> getAttachmentListSorted(Document doc, String comparator, 
      boolean imagesOnly) {
    return getAttachmentListSorted(doc, comparator, imagesOnly, 0, 0);
  }

  public List<Attachment> getAttachmentListSorted(Document doc, String comparator, 
      boolean imagesOnly, int start, int nb) {
    try {
      List<Attachment> attachments = getAttachmentListSorted(doc, comparator);
      if (imagesOnly) {
        for (Attachment att : new ArrayList<Attachment>(attachments)) {
          if (!att.isImage()) {
            attachments.remove(att);
          }
        }
      }
      return reduceListToSize(attachments, start, nb);
    } catch (ClassNotFoundException exp) {
      LOGGER.error(exp);
    }
    return Collections.emptyList();
  }

  public String getAttachmentListSortedAsJSON(Document doc, String comparator,
      boolean imagesOnly) {
    return getAttachmentListSortedAsJSON(doc, comparator, imagesOnly, 0, 0);
  }

  public String getAttachmentListSortedAsJSON(Document doc,
      String comparator, boolean imagesOnly, int start, int nb) {
    SimpleDateFormat dateFormater = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    Builder jsonBuilder = new Builder();
    jsonBuilder.openArray();
    for (Attachment att : getAttachmentListSorted(doc, comparator, imagesOnly, start, nb)
        ) {
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

  Map<String, String> xwikiDoctoLinkedMap(XWikiDocument xwikiDoc,
      boolean bWithObjects, boolean bWithRendering,
      boolean bWithAttachmentContent, boolean bWithVersions) throws XWikiException {
    Map<String,String> docData = new LinkedHashMap<String, String>();
    DocumentReference docRef = xwikiDoc.getDocumentReference();
    docData.put("web", docRef .getLastSpaceReference().getName());
    docData.put("name", docRef.getName());
    docData.put("language", xwikiDoc.getLanguage());
    docData.put("defaultLanguage", xwikiDoc.getDefaultLanguage());
    docData.put("translation", "" + xwikiDoc.getTranslation());
    docData.put("defaultLanguage", xwikiDoc.getDefaultLanguage());
    docData.put("parent", serializer_default.serialize(xwikiDoc.getParentReference()));
    String parentsListStr = "";
    String parentsListMNStr = "";
    MultilingualMenuNameCommand menuNameCmd = new MultilingualMenuNameCommand();
    for(DocumentReference parentDocRef : getDocumentParentsList(docRef, false)) {
      String parentDocFN = serializer_default.serialize(parentDocRef);
      parentsListMNStr += menuNameCmd.getMultilingualMenuName(parentDocFN, getContext(
          ).getLanguage(), getContext()) + ",";
      parentsListStr += parentDocFN + ",";
    }
    docData.put("parentslist", parentsListStr.replaceAll(",*$", ""));
    docData.put("parentslistmenuname", parentsListMNStr.replaceAll(",*$", ""));
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
    docData.put("template", serializer_local.serialize(
        xwikiDoc.getTemplateDocumentReference()));
    docData.put("getDefaultTemplate", xwikiDoc.getDefaultTemplate());
    docData.put("getValidationScript", xwikiDoc.getValidationScript());
    docData.put("comment", xwikiDoc.getComment());
    docData.put("minorEdit", String.valueOf(xwikiDoc.isMinorEdit()));
    docData.put("syntaxId", xwikiDoc.getSyntax().toIdString());
    docData.put("menuName", menuNameCmd.getMultilingualMenuName(
        serializer_default.serialize(xwikiDoc.getDocumentReference()), getContext(
            ).getLanguage(), getContext()));
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

    String host = getContext().getRequest().getHeader("host");
    // Add Content
    docData.put("content", replaceInternalWithExternalLinks(xwikiDoc.getContent(), host));
    
    if (bWithRendering) {
      try {
        docData.put("renderedcontent", replaceInternalWithExternalLinks(
            xwikiDoc.getRenderedContent(getContext()), host));
      } catch (XWikiException exp) {
        LOGGER.error("Exception with rendering content: ", exp);
      }
      try {
        docData.put("celrenderedcontent", replaceInternalWithExternalLinks(
            getCelementsRenderCmd().renderCelementsDocument(xwikiDoc.getDocumentReference(
                ), getContext().getLanguage(), "view"), host));
      } catch (XWikiException exp) {
        LOGGER.error("Exception with rendering content: ", exp);
      }
    }

    if (bWithVersions) {
        try {
          docData.put("versions", xwikiDoc.getDocumentArchive(getContext()
              ).getArchive(getContext()));
        } catch (XWikiException exp) {
            LOGGER.error("Document [" + docRef.getName()
                + "] has malformed history", exp);
        }
    }

    return docData;
  }

  private RenderCommand getCelementsRenderCmd() {
    RenderCommand renderCommand = new RenderCommand();
    renderCommand.setDefaultPageType("RichText");
    return renderCommand;
  }

  String replaceInternalWithExternalLinks(String content, String host) {
    String result = content.replaceAll("src=\\\"(\\.\\./)*/?download/", "src=\"http://" 
  + host + "/download/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?download/", "href=\"http://" 
  + host + "/download/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?skin/", "href=\"http://" 
  + host + "/skin/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?view/", "href=\"http://" 
  + host + "/view/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?edit/", "href=\"http://" 
  + host + "/edit/");
    return result;
  }
  
  public String getJSONContent(DocumentReference docRef) {
    try {
      return getJSONContent(getContext().getWiki().getDocument(docRef, getContext()));
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get document [" + docRef + "] for JSON.", exp);
    }
    return "{}";
  }

  public String getJSONContent(XWikiDocument cdoc) {
    Map<String, String> data;
    try {
      data = xwikiDoctoLinkedMap(cdoc.getTranslatedDocument(getContext()), false, true,
          false, false);
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

  public String getUserNameForDocRef(DocumentReference authDocRef) throws XWikiException {
    XWikiDocument authDoc = getContext().getWiki().getDocument(authDocRef, getContext());
    BaseObject authObj = authDoc.getXObject(getRef("XWiki","XWikiUsers"));
    if(authObj!=null){
      return authObj.getStringValue("last_name") + ", " 
          + authObj.getStringValue("first_name");
    } else{
      return getAdminMessageTool().get("cel_ml_unknown_author");
    }
  }
  
  public String getMajorVersion(XWikiDocument doc) {
    String revision = "1";
    if(doc!=null){
      revision = doc.getVersion();
      if(revision!=null
          && revision.trim().length()>0
          && revision.contains(".")){
        revision = revision.split("\\.")[0];
      }
    }
    return revision;
  }
  
  private DocumentReference getRef(String spaceName, String pageName){
    return new DocumentReference(getContext().getDatabase(), spaceName, pageName);
  }

  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField, boolean asc) {
    return getObjectsOrdered(doc, classRef, orderField, asc, null, false);
  }

  /**
   * Get a list of Objects for a Document sorted by one or two fields.
   * 
   * @param doc The Document where the Objects are attached.
   * @param classRef The reference to the class of the Objects to return
   * @param orderField1 Field to order the objects by. First priority.
   * @param asc1 Order first priority ascending or descending.
   * @param orderField2 Field to order the objects by. Second priority.
   * @param asc2 Order second priority ascending or descending.
   * @return List of objects ordered as specified
   */
  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField1, boolean asc1, String orderField2, boolean asc2) {
    List<BaseObject> resultList = new ArrayList<BaseObject>();
    if(doc != null) {
      List<BaseObject> allObjects = doc.getXObjects(classRef);
      if(allObjects != null) {
        for (BaseObject obj : allObjects) {
          if(obj != null) {
            resultList.add(obj);
          }
        }
      }
      Collections.sort(resultList, new BaseObjectComparator(orderField1, asc1, 
          orderField2, asc2));
    }
    return resultList;
  }

  public String[] splitStringByLength(String inStr, int maxLength) {
    int numFullStr = (inStr.length() - 1) / maxLength;
    String[] splitedStr = new String[1 + numFullStr];
    for(int i = 0 ; i < numFullStr ; i ++) {
      int startIndex = i * maxLength;
      splitedStr[i] = inStr.substring(startIndex, startIndex + maxLength);
    }
    int lastPiece = splitedStr.length - 1;
    splitedStr[lastPiece] = inStr.substring(lastPiece * maxLength,
        inStr.length());
    return splitedStr;
  }

  public WikiReference getWikiRef(DocumentReference docRef) {
    return (WikiReference) docRef.getLastSpaceReference().getParent();
  }

  public DocumentReference getWikiTemplateDocRef() {
    if (getContext().getRequest() != null) {
      String templateFN = getContext().getRequest().get("template");
      if ((templateFN != null) && !"".equals(templateFN.trim())) {
        DocumentReference templateDocRef = resolveDocumentReference(templateFN);
        if (getContext().getWiki().exists(templateDocRef, getContext())) {
          return templateDocRef;
        }
      }
    }
    return null;
  }

  public XWikiDocument getWikiTemplateDoc() {
    DocumentReference templateDocRef = getWikiTemplateDocRef();
    if (templateDocRef != null) {
      try {
        return getContext().getWiki().getDocument(templateDocRef, getContext());
      } catch (XWikiException exp) {
        LOGGER.error("Exception while getting template doc '" + templateDocRef + "'",
            exp);
      }
    }
    return null;
  }

  public EntityReferenceSerializer<String> getRefDefaultSerializer() {
    return serializer_default;
  }

  public EntityReferenceSerializer<String> getRefLocalSerializer() {
    return serializer_local;
  }
  
  public Map<String, String[]> getRequestParameterMap() {
    XWikiRequest request = getContext().getRequest();
    if (request != null) {
      Map<?, ?> requestMap = request.getParameterMap();
      Map<String, String[]> convertedMap = new HashMap<String, String[]>();
      for (Object keyObj : requestMap.keySet()) {
        String key = keyObj.toString();
        String[] value = getValueAsStringArray(requestMap.get(keyObj));
        convertedMap.put(key, value);
      }
      return convertedMap;
    } else {
      return null;
    }
  }

  private String[] getValueAsStringArray(Object value) {
    if (value instanceof String) {
      return new String[] { value.toString() };
    } else if (value instanceof String[]) {
      return (String[]) value;
    } else {
      throw new IllegalArgumentException("Invalid requestMap value type");
    }
  }

  public String getInheritedTemplatedPath(DocumentReference localTemplateRef) {
    if (localTemplateRef != null) {
      String templatePath = getRefDefaultSerializer().serialize(localTemplateRef);
      if (!getContext().getWiki().exists(localTemplateRef, getContext())) {
        if (!"celements2web".equals(localTemplateRef.getLastSpaceReference().getParent(
          ).getName())
          && getContext().getWiki().exists(getCentralTemplateRef(localTemplateRef),
              getContext())) {
          templatePath = "celements2web:" + templatePath;
        } else {
          templatePath = ":" + templatePath.replaceAll("celements2web:", "");
        }
      }
      return templatePath.replaceAll(getContext().getDatabase() + ":", "");
    }
    return null;
  }

  private DocumentReference getCentralTemplateRef(DocumentReference localTemplateRef) {
    DocumentReference centralTemplateRef = new DocumentReference("celements2web",
        localTemplateRef.getLastSpaceReference().getName(), localTemplateRef.getName());
    return centralTemplateRef;
  }

  public void deleteDocument(XWikiDocument doc, boolean totrash) throws XWikiException {
    /** deleteDocument in XWiki does NOT set context and store database to doc database
     * Thus deleting the doc fails if it is not in the current context database. Hence we
     * need to fix the context database before deleting.
     */
    String dbBefore = getContext().getDatabase();
    try {
      getContext().setDatabase(doc.getDocumentReference().getLastSpaceReference().getParent(
          ).getName());
      LOGGER.debug("deleteDocument: doc [" + getRefDefaultSerializer().serialize(
          doc.getDocumentReference()) + "," + doc.getLanguage() + "] totrash [" + totrash
          + "] dbBefore [" + dbBefore + "] db now [" + getContext().getDatabase() + "].");
      getContext().getWiki().deleteDocument(doc, totrash, getContext());
    } finally {
      getContext().setDatabase(dbBefore);
    }
  }

  public void deleteAllDocuments(XWikiDocument doc, boolean totrash
      ) throws XWikiException {
    // Delete all documents
    for (String lang : doc.getTranslationList(getContext())) {
      XWikiDocument tdoc = doc.getTranslatedDocument(lang, getContext());
      deleteDocument(tdoc, totrash);
    }

    deleteDocument(doc, totrash);
  }

  public String getTemplatePathOnDisk(String renderTemplatePath) {
    return getTemplatePathOnDisk(renderTemplatePath, null);
  }

  public String getTemplatePathOnDisk(String renderTemplatePath, String lang) {
    for (Map.Entry<Object, Object> entry : tempPathConfig.getMappings().entrySet()) {
      String pathName = (String) entry.getKey();
      if (renderTemplatePath.startsWith(":" + pathName)) {
        String newRenderTemplatePath = renderTemplatePath.replaceAll("^:(" + pathName
            + "\\.)?", "/templates/" + ((String) entry.getValue()) + "/")
            + getTemplatePathLangSuffix(lang) + ".vm";
        LOGGER.debug("getTemplatePathOnDisk: for [" + renderTemplatePath + "] and lang ["
            + lang + "] returning [" + newRenderTemplatePath + "].");
        return newRenderTemplatePath;
      }
    }
    return renderTemplatePath;
  }

  private String getTemplatePathLangSuffix(String lang) {
    if (lang != null) {
      return "_" + lang;
    }
    return "";
  }

  public String renderInheritableDocument(DocumentReference docRef, String lang
      ) throws XWikiException {
    return renderInheritableDocument(docRef, lang, null);
  }

  public String renderInheritableDocument(DocumentReference docRef, String lang,
      String defLang) throws XWikiException {
    RenderCommand renderCommand = new RenderCommand();
    if (this.injectedRenderingEngine != null) {
      renderCommand.setRenderingEngine(this.injectedRenderingEngine);
    }
    String templatePath = getInheritedTemplatedPath(docRef);
    LOGGER.debug("renderInheritableDocument: call renderTemplatePath for ["
        + templatePath + "] and lang [" + lang + "] and defLang [" + defLang + "].");
    return renderCommand.renderTemplatePath(templatePath, lang, defLang);
  }

  public boolean existsInheritableDocument(DocumentReference docRef, String lang) {
    return existsInheritableDocument(docRef, lang, null);
  }

  public boolean existsInheritableDocument(DocumentReference docRef, String lang,
      String defLang) {
    String templatePath = getInheritedTemplatedPath(docRef);
    LOGGER.debug("existsInheritableDocument: check content for templatePath ["
        + templatePath + "] and lang [" + lang + "] and defLang [" + defLang + "].");
    if (templatePath.startsWith(":")) {
      return !StringUtils.isEmpty(getTranslatedDiscTemplateContent(templatePath, lang,
          defLang));
    } else {
      //Template must exist otherwise getInheritedTemplatedPath would have fallen back
      //on disk template path.
      return true;
    }
  }

  private PageLayoutCommand getPageLayoutCmd() {
    if (!getContext().containsKey(CelementsWebPluginApi.CELEMENTS_PAGE_LAYOUT_COMMAND)) {
      getContext().put(CelementsWebPluginApi.CELEMENTS_PAGE_LAYOUT_COMMAND, 
          new PageLayoutCommand());
    }
    return (PageLayoutCommand) getContext().get(
        CelementsWebPluginApi.CELEMENTS_PAGE_LAYOUT_COMMAND);
  }

  @Deprecated
  public String cleanupXHTMLtoHTML5(String xhtml) {
    return cleanupXHTMLtoHTML5(xhtml, getContext().getDoc().getDocumentReference());
  }

  @Deprecated
  public String cleanupXHTMLtoHTML5(String xhtml, DocumentReference docRef) {
    return cleanupXHTMLtoHTML5(xhtml, getPageLayoutCmd().getPageLayoutForDoc(docRef));
  }

  @Deprecated
  public String cleanupXHTMLtoHTML5(String xhtml, SpaceReference layoutRef) {
    BaseObject layoutObj = getPageLayoutCmd().getLayoutPropertyObj(layoutRef);
    if((layoutObj != null) && "HTML 5".equals(layoutObj.getStringValue("doctype"))) {
      XHTMLtoHTML5cleanup html5Cleaner = Utils.getComponent(XHTMLtoHTML5cleanup.class);
      return html5Cleaner.cleanAll(xhtml);
    }
    return xhtml;
  }

  public String getTranslatedDiscTemplateContent(String renderTemplatePath, String lang,
      String defLang) {
    String templateContent;
    List<String> langList = new ArrayList<String>();
    if (lang != null) {
      langList.add(lang);
    }
    if ((defLang != null) && !defLang.equals(lang)) {
      langList.add(defLang);
    }
    templateContent = "";
    for(String theLang : langList) {
      String templatePath = getTemplatePathOnDisk(renderTemplatePath, theLang);
      try {
        templateContent = getContext().getWiki().getResourceContent(templatePath);
      } catch (FileNotFoundException fnfExp) {
        LOGGER.trace("FileNotFound [" + templatePath + "].");
        templateContent = "";
      } catch (IOException exp) {
        LOGGER.debug("Exception while parsing template [" + templatePath + "].", exp);
        templateContent = "";
      }
    }
    if ("".equals(templateContent)) {
      String templatePathDef = getTemplatePathOnDisk(renderTemplatePath);
      try {
        templateContent = getContext().getWiki().getResourceContent(templatePathDef);
      } catch (FileNotFoundException fnfExp) {
        LOGGER.trace("FileNotFound [" + templatePathDef + "].");
        return "";
      } catch (IOException exp) {
        LOGGER.debug("Exception while parsing template [" + templatePathDef + "].",
            exp);
        return "";
      }
    }
    return templateContent;
  }

}
