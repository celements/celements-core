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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.rights.access.EAccessLevel;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.XWikiMessageTool;

@ComponentRole
public interface IWebUtilsService {

  public static final Date DATE_LOW = new Date(-62135773200000L);

  /**
   * {@value #DATE_HIGH} has the value [Fri Dec 31 23:59:00 CET 9999]
   */
  public static final Date DATE_HIGH = new Date(253402297140000L);

  public static final String REGEX_WORD = "[a-zA-Z0-9]*";
  public static final String REGEX_SPACE = "(" + REGEX_WORD + "\\:)?" + REGEX_WORD;
  public static final String REGEX_DOC = REGEX_SPACE + "\\." + REGEX_WORD;
  public static final String REGEX_ATT = REGEX_DOC + "\\@.*";

  /**
   * Returns level of hierarchy with level=1 returning root which is null, else
   * corresponding DocumentReference or throws IndexOutOfBoundsException
   *
   * @param level
   * @return DocumentReference of level
   * @throws IndexOutOfBoundsException
   *           - if level above root or below lowest
   */
  public DocumentReference getParentForLevel(int level) throws IndexOutOfBoundsException;

  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef,
      boolean includeDoc);

  public String getDocSectionAsJSON(String regex, DocumentReference docRef, int section)
      throws XWikiException;

  public String getDocSection(String regex, DocumentReference docRef, int section)
      throws XWikiException;

  public int countSections(String regex, DocumentReference docRef) throws XWikiException;

  public List<String> getAllowedLanguages();

  public Date parseDate(String date, String format);

  public XWikiMessageTool getMessageTool(String adminLanguage);

  public XWikiMessageTool getAdminMessageTool();

  public String getDefaultAdminLanguage();

  public String getAdminLanguage();

  /**
   * @deprecated since 2.34.0 instead use getAdminLanguage(DocumentReference userRef)
   */
  @Deprecated
  public String getAdminLanguage(String userFullName);

  public String getAdminLanguage(DocumentReference userRef);

  public String getDefaultLanguage();

  /**
   * @deprecated instead use {@link #getDefaultLanguage(SpaceReference)}
   */
  @Deprecated
  public String getDefaultLanguage(String spaceName);

  public String getDefaultLanguage(SpaceReference spaceRef);

  public boolean hasParentSpace();

  public boolean hasParentSpace(String spaceName);

  public String getParentSpace();

  public String getParentSpace(String spaceName);

  public DocumentReference resolveDocumentReference(String fullName);

  public DocumentReference resolveDocumentReference(String fullName, WikiReference wikiRef);

  public EntityReference resolveRelativeEntityReference(String name, EntityType type);

  public SpaceReference resolveSpaceReference(String spaceName);

  public SpaceReference resolveSpaceReference(String spaceName, WikiReference wikiRef);

  public WikiReference resolveWikiReference(@Nullable String wikiName);

  public AttachmentReference resolveAttachmentReference(String fullName);

  public AttachmentReference resolveAttachmentReference(String fullName, WikiReference wikiRef);

  public EntityReference resolveEntityReference(String name, EntityType type);

  public EntityReference resolveEntityReference(String name, EntityType type,
      WikiReference wikiRef);

  public boolean isAdminUser();

  public boolean isAdvancedAdmin();

  public boolean isSuperAdminUser();

  /**
   * instead use IRightsAccessFacadeRole.hasAccessLevel(EntityReference, EAccessLevel)
   */
  @Deprecated
  public boolean hasAccessLevel(EntityReference ref, EAccessLevel level);

  /**
   * use IRightsAccessFacadeRole.hasAccessLevel(EntityReference, EAccessLevel, XWikiUser)
   * instead
   */
  @Deprecated
  public boolean hasAccessLevel(EntityReference ref, EAccessLevel level, XWikiUser user);

  /**
   * CAUTION: this method returns attachments which start with the attRef.name if no exact
   * match exists.
   *
   * @deprecated instead use IAttachmentServiceRole.getAttachmentNameEqual or
   *             IAttachmentServiceRole.getAttachmentsNameMatch
   */
  @Deprecated
  public XWikiAttachment getAttachment(AttachmentReference attRef) throws XWikiException;

  public Attachment getAttachmentApi(AttachmentReference attRef) throws XWikiException;

  /**
   * @deprecated instead use {@link #getAttachmentListSorted(XWikiDocument, Comparator)}
   */
  @Deprecated
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator)
      throws ClassNotFoundException;

  public List<XWikiAttachment> getAttachmentListSorted(XWikiDocument doc,
      Comparator<XWikiAttachment> comparator);

  /**
   * @deprecated instead use
   *             {@link #getAttachmentListSorted(XWikiDocument, Comparator, boolean)}
   */
  @Deprecated
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly);

  public List<XWikiAttachment> getAttachmentListSorted(XWikiDocument doc,
      Comparator<XWikiAttachment> comparator, boolean imagesOnly);

  /**
   * @deprecated instead use
   *             {@link #getAttachmentListSorted(XWikiDocument, Comparator, boolean, int, int)}
   */
  @Deprecated
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly, int start, int nb);

  public List<XWikiAttachment> getAttachmentListSorted(XWikiDocument doc,
      Comparator<XWikiAttachment> comparator, boolean imagesOnly, int start, int nb);

  // TODO change signature requirement to XWikiDocument instead of document and mark
  // the old version as deprecated
  public List<Attachment> getAttachmentListSortedSpace(String spaceName, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException;

  // TODO change signature requirement to XWikiDocument instead of document and mark
  // the old version as deprecated
  public List<Attachment> getAttachmentListForTagSorted(Document doc, String tagName,
      String comparator, boolean imagesOnly, int start, int nb);

  // TODO change signature requirement to XWikiDocument instead of document and mark
  // the old version as deprecated
  public List<Attachment> getAttachmentListForTagSortedSpace(String spaceName, String tagName,
      String comparator, boolean imagesOnly, int start, int nb) throws ClassNotFoundException;

  // TODO change signature requirement to XWikiDocument instead of document and mark
  // the old version as deprecated
  public String getAttachmentListSortedAsJSON(Document doc, String comparator, boolean imagesOnly);

  // TODO change signature requirement to XWikiDocument instead of document and mark
  // the old version as deprecated
  public String getAttachmentListSortedAsJSON(Document doc, String comparator, boolean imagesOnly,
      int start, int nb);

  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField, boolean asc);

  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField1, boolean asc1, String orderField2, boolean asc2);

  public String[] splitStringByLength(String inStr, int maxLength);

  public String getJSONContent(XWikiDocument cdoc);

  public String getJSONContent(DocumentReference docRef);

  public String getUserNameForDocRef(DocumentReference authDocRef) throws XWikiException;

  public String getMajorVersion(XWikiDocument doc);

  public WikiReference getWikiRef();

  public WikiReference getWikiRef(XWikiDocument doc);

  public WikiReference getWikiRef(DocumentReference docRef);

  public WikiReference getWikiRef(EntityReference ref);

  public List<String> getAllowedLanguages(String spaceName);

  public DocumentReference getWikiTemplateDocRef();

  public XWikiDocument getWikiTemplateDoc();

  public EntityReferenceSerializer<String> getRefDefaultSerializer();

  public EntityReferenceSerializer<String> getRefLocalSerializer();

  public String serializeRef(EntityReference entityRef);

  public String serializeRef(EntityReference entityRef, boolean local);

  public Map<String, String[]> getRequestParameterMap();

  public String getInheritedTemplatedPath(DocumentReference localTemplateRef);

  public void deleteDocument(XWikiDocument doc, boolean totrash) throws XWikiException;

  public void deleteAllDocuments(XWikiDocument doc, boolean totrash) throws XWikiException;

  public String getTemplatePathOnDisk(String renderTemplatePath);

  public String getTemplatePathOnDisk(String renderTemplatePath, String lang);

  public String renderInheritableDocument(DocumentReference docRef, String lang)
      throws XWikiException;

  public String renderInheritableDocument(DocumentReference docRef, String lang, String defLang)
      throws XWikiException;

  public boolean isLayoutEditor();

  public String cleanupXHTMLtoHTML5(String xhtml);

  public String cleanupXHTMLtoHTML5(String xhtml, DocumentReference doc);

  public String cleanupXHTMLtoHTML5(String xhtml, SpaceReference layoutRef);

  public List<Attachment> getAttachmentsForDocs(List<String> docsFN);

  public String getTranslatedDiscTemplateContent(String renderTemplatePath, String lang,
      String defLang);

  public boolean existsInheritableDocument(DocumentReference docRef, String lang);

  public boolean existsInheritableDocument(DocumentReference docRef, String lang, String defLang);

  /**
   * used to send an email if result of <param>jobMailName</param> is not empty
   *
   * @param jobMailName
   *          inheritable Mails document name
   * @param fromAddr
   *          sender address
   * @param toAddr
   *          recipients
   * @param params
   *          list of strings passed through to dictionary subject resolving
   */
  public void sendCheckJobMail(String jobMailName, String fromAddr, String toAddr,
      List<String> params);

  public WikiReference getCentralWikiRef();

  /**
   * resolves the {@link EntityType} for the given fullName.<br>
   * <br>
   * Simple names will return {@link EntityType#WIKI}.
   *
   * @param fullName
   * @return
   */
  public EntityType resolveEntityTypeForFullName(String fullName);

  /**
   * resolves the {@link EntityType} for the given fullName.
   *
   * @param fullName
   * @param defaultNameType
   *          EntityType used if given fullName is just a simple name
   * @return
   */
  public EntityType resolveEntityTypeForFullName(String fullName, EntityType defaultNameType);

  /**
   * only used as an adapter for unstable 2
   */
  public <T> T lookup(Class<T> role) throws ComponentLookupException;

  /**
   * only used as an adapter for unstable 2
   */
  public <T> T lookup(Class<T> role, String roleHint) throws ComponentLookupException;

  /**
   * only used as an adapter for unstable 2
   */
  public <T> List<T> lookupList(Class<T> role) throws ComponentLookupException;

  /**
   * only used as an adapter for unstable 2
   */
  public <T> Map<String, T> lookupMap(Class<T> role) throws ComponentLookupException;

  /**
   * checks and corrects the WikiReference on docRef compared to current wiki.
   *
   * @param docRef
   * @return
   */
  public DocumentReference checkWikiRef(DocumentReference docRef);

  /**
   * checks and corrects the WikiReference on docRef compared to toDoc.
   *
   * @param docRef
   * @param toDoc
   * @return
   */
  public DocumentReference checkWikiRef(DocumentReference docRef, XWikiDocument toDoc);

  /**
   * checks and corrects the WikiReference on docRef compared to toDoc.
   *
   * @param docRef
   * @param toRef
   * @return
   */
  public DocumentReference checkWikiRef(DocumentReference docRef, EntityReference toRef);

  public void setUser(DocumentReference userReference, boolean main);

  public DocumentReference setWikiReference(DocumentReference docRef, String wikiName);

  public DocumentReference setWikiReference(DocumentReference docRef, WikiReference wikiRef);

}
