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
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
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

  /**
   * @deprecated since 2.63.0
   * @deprecated instead use IDocumentParentsListerRole.getDocumentParentsList(
   *             DocumentReference docRef, boolean includeDoc)
   */
  @Deprecated
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef,
      boolean includeDoc);

  public String getDocSectionAsJSON(String regex, DocumentReference docRef, int section)
      throws XWikiException;

  public String getDocSection(String regex, DocumentReference docRef, int section)
      throws XWikiException;

  public int countSections(String regex, DocumentReference docRef) throws XWikiException;

  public List<String> getAllowedLanguages();

  public List<String> getAllowedLanguages(String spaceName);

  public List<String> getAllowedLanguages(SpaceReference spaceRef);

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

  /**
   * @deprecated instead use {@link ModelContext#getDefaultLanguage()}
   */
  @Deprecated
  public String getDefaultLanguage();

  /**
   * @deprecated instead use {@link ModelContext#getDefaultLanguage(EntityReference)}
   */
  @Deprecated
  public String getDefaultLanguage(String spaceName);

  /**
   * @deprecated instead use {@link ModelContext#getDefaultLanguage(EntityReference)}
   */
  @Deprecated
  public String getDefaultLanguage(SpaceReference spaceRef);

  public boolean hasParentSpace();

  public boolean hasParentSpace(String spaceName);

  public String getParentSpace();

  public String getParentSpace(String spaceName);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  public DocumentReference resolveDocumentReference(String fullName);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  public DocumentReference resolveDocumentReference(String fullName, WikiReference wikiRef);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  public EntityReference resolveRelativeEntityReference(String name, EntityType type);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  public SpaceReference resolveSpaceReference(String spaceName);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  public SpaceReference resolveSpaceReference(String spaceName, WikiReference wikiRef);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  public WikiReference resolveWikiReference(@Nullable String wikiName);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  public AttachmentReference resolveAttachmentReference(String fullName);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  public AttachmentReference resolveAttachmentReference(String fullName, WikiReference wikiRef);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  public EntityReference resolveEntityReference(String name, EntityType type);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  public EntityReference resolveEntityReference(String name, EntityType type,
      WikiReference wikiRef);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  @NotNull
  public <T extends EntityReference> T resolveReference(@NotNull String name,
      @NotNull Class<T> token);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  @NotNull
  public <T extends EntityReference> T resolveReference(@NotNull String name,
      @NotNull Class<T> token, @Nullable EntityReference baseRef);

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
   * @deprecated instead use {@link #getAttachmentListSorted(XWikiDocument, Comparator, boolean)}
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

  /**
   * @deprecated instead use {@link ModelContext#getWiki()}
   */
  @Deprecated
  public WikiReference getWikiRef();

  /**
   * @deprecated instead use {@link ModelUtils#extractRef(EntityReference, EntityReference, Class)}
   */
  @Deprecated
  public WikiReference getWikiRef(XWikiDocument doc);

  /**
   * @deprecated instead use {@link ModelUtils#extractRef(EntityReference, EntityReference, Class)}
   */
  @Deprecated
  public WikiReference getWikiRef(DocumentReference docRef);

  /**
   * @deprecated instead use {@link ModelUtils#extractRef(EntityReference, EntityReference, Class)}
   */
  @Deprecated
  public WikiReference getWikiRef(EntityReference ref);

  public DocumentReference getWikiTemplateDocRef();

  public XWikiDocument getWikiTemplateDoc();

  /**
   * @deprecated instead use {@link ModelUtils#serializeRef(EntityReference)}
   */
  @Deprecated
  public EntityReferenceSerializer<String> getRefDefaultSerializer();

  /**
   * @deprecated instead use {@link ModelUtils#serializeRefLocal(EntityReference)}
   */
  @Deprecated
  public EntityReferenceSerializer<String> getRefLocalSerializer();

  /**
   * @deprecated instead use {@link ModelUtils#serializeRef(EntityReference)}
   */
  @Deprecated
  public String serializeRef(EntityReference entityRef);

  /**
   * @deprecated instead use {@link ModelUtils#serializeRef(EntityReference)} or
   *             {@link ModelUtils#serializeRefLocal(EntityReference)}
   */
  @Deprecated
  public String serializeRef(EntityReference entityRef, boolean local);

  public Map<String, String[]> getRequestParameterMap();

  public String getInheritedTemplatedPath(DocumentReference localTemplateRef);

  /**
   * @deprecated instead use
   *             {@link IModelAccessFacade#deleteDocumentWithoutTranslations(XWikiDocument, boolean)}
   */
  @Deprecated
  public void deleteDocument(XWikiDocument doc, boolean totrash) throws XWikiException;

  /**
   * @deprecated instead use {@link IModelAccessFacade#deleteDocument(XWikiDocument, boolean)}
   */
  @Deprecated
  public void deleteAllDocuments(XWikiDocument doc, boolean totrash) throws XWikiException;

  public String getTemplatePathOnDisk(String renderTemplatePath);

  public String getTemplatePathOnDisk(String renderTemplatePath, String lang);

  public String renderInheritableDocument(DocumentReference docRef, String lang)
      throws XWikiException;

  public String renderInheritableDocument(DocumentReference docRef, String lang, String defLang)
      throws XWikiException;

  public boolean isLayoutEditor();

  @Deprecated
  public String cleanupXHTMLtoHTML5(String xhtml);

  @Deprecated
  public String cleanupXHTMLtoHTML5(String xhtml, DocumentReference doc);

  @Deprecated
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
   * @deprecated instead use {@link ModelUtils#identifyClassFromName(String)}
   */
  @Deprecated
  public EntityType resolveEntityTypeForFullName(String fullName);

  /**
   * @deprecated instead use {@link ModelUtils#identifyClassFromName(String)}
   */
  @Deprecated
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
   * @deprecated instead use {@link ModelUtils#adjustRef(EntityReference, Class, EntityReference)}
   */
  @Deprecated
  public DocumentReference checkWikiRef(DocumentReference docRef);

  /**
   * @deprecated instead use {@link ModelUtils#adjustRef(EntityReference, Class, EntityReference)}
   */
  @Deprecated
  public DocumentReference checkWikiRef(DocumentReference docRef, XWikiDocument toDoc);

  /**
   * @deprecated instead use {@link ModelUtils#adjustRef(EntityReference, Class, EntityReference)}
   */
  @Deprecated
  public DocumentReference checkWikiRef(DocumentReference docRef, EntityReference toRef);

  /**
   * @deprecated instead use {@link ModelUtils#adjustRef(EntityReference, Class, EntityReference)}
   */
  @Deprecated
  public DocumentReference setWikiReference(DocumentReference docRef, String wikiName);

  /**
   * @deprecated instead use {@link ModelUtils#adjustRef(EntityReference, Class, EntityReference)}
   */
  @Deprecated
  public DocumentReference setWikiReference(DocumentReference docRef, WikiReference wikiRef);

  public void setUser(DocumentReference userReference, boolean main);

}
