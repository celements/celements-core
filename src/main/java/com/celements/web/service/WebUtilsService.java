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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.WikiReference;

import com.celements.sajson.Builder;
import com.celements.web.plugin.cmd.EmptyCheckCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.XWikiMessageTool;

@Component
public class WebUtilsService implements IWebUtilsService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(WebUtilsService.class);

  @Requirement
  EntityReferenceResolver<String> referenceResolver;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public DocumentReference getParentForLevel(int level) throws IndexOutOfBoundsException {
    DocumentReference parent = null;
    if(level!=1){
      List<DocumentReference> parentList = getDocumentParentsList(
          getContext().getDoc().getDocumentReference(), true);
      int startAtItem = parentList.size()-level+1;
      parent = parentList.get(startAtItem);
    }
    return parent;
  }
  
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef,
      boolean includeDoc) {
    ArrayList<DocumentReference> docParents = new ArrayList<DocumentReference>();
    try {
      DocumentReference nextParent;
      if(includeDoc){
        nextParent = docRef;
      } else {
        nextParent = getParentRef(docRef);
      }
      while (nextParent!=null
          && (getContext().getWiki().exists(nextParent, getContext()))
          && !docParents.contains(nextParent)) {
        docParents.add(nextParent);
        nextParent = getParentRef(nextParent);
      }
    } catch (XWikiException e) {
      LOGGER.error(e);
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

  public String getAdminLanguage(String userFullName) {
    String adminLanguage = null;
    try {
      DocumentReference userDocRef = resolveDocumentReference(userFullName);
      DocumentReference xwikiUsersClassRef = new DocumentReference(
          getContext().getDatabase(), "XWiki", "XWikiUsers");
      xwikiUsersClassRef.setWikiReference(userDocRef.getWikiReference());
      BaseObject userObj = getContext().getWiki().getDocument(userDocRef, getContext()
          ).getXObject(xwikiUsersClassRef);
      if (userObj != null) {
        adminLanguage = userObj.getStringValue("admin_language");
      }
    } catch (XWikiException e) {
      LOGGER.error("failed to get UserObject for " + getContext().getUser());
    }
    if ((adminLanguage == null) || ("".equals(adminLanguage))) {
      adminLanguage = getContext().getWiki().getWebPreference("admin_language",
          getContext().getLanguage(), getContext());
    }
    return adminLanguage;
  }

  public DocumentReference resolveDocumentReference(String fullName) {
    DocumentReference eventRef = new DocumentReference(referenceResolver.resolve(
        fullName, EntityType.DOCUMENT));
    if (!fullName.contains(":")) {
      eventRef.setWikiReference(new WikiReference(getContext().getDatabase()));
    }
    LOGGER.debug("getDocRefFromFullName: for [" + fullName + "] got reference ["
        + eventRef + "].");
    return eventRef;
  }

  public String getDefaultLanguage() {
    return getContext().getWiki().getWebPreference("default_language", getContext());
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

}
