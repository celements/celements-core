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

import com.celements.auth.user.User;
import com.celements.auth.user.UserService;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.web.CelConstant;
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

  Date DATE_LOW = new Date(-62135773200000L);

  /**
   * {@value #DATE_HIGH} has the value [Fri Dec 31 23:59:00 CET 9999]
   */
  Date DATE_HIGH = new Date(253402297140000L);

  /**
   * Returns level of hierarchy with level=1 returning root which is null, else
   * corresponding DocumentReference or throws IndexOutOfBoundsException
   *
   * @param level
   * @return DocumentReference of level
   * @throws IndexOutOfBoundsException
   *           - if level above root or below lowest
   */
  DocumentReference getParentForLevel(int level) throws IndexOutOfBoundsException;

  /**
   * @deprecated since 2.63.0
   * @deprecated instead use IDocumentParentsListerRole.getDocumentParentsList(
   *             DocumentReference docRef, boolean includeDoc)
   */
  @Deprecated
  List<DocumentReference> getDocumentParentsList(DocumentReference docRef,
      boolean includeDoc);

  String getDocSectionAsJSON(String regex, DocumentReference docRef, int section)
      throws XWikiException;

  String getDocSection(String regex, DocumentReference docRef, int section)
      throws XWikiException;

  int countSections(String regex, DocumentReference docRef) throws XWikiException;

  List<String> getAllowedLanguages();

  List<String> getAllowedLanguages(String spaceName);

  List<String> getAllowedLanguages(SpaceReference spaceRef);

  Date parseDate(String date, String format);

  XWikiMessageTool getMessageTool(String adminLanguage);

  XWikiMessageTool getAdminMessageTool();

  @NotNull
  String getDefaultAdminLanguage();

  @NotNull
  String getAdminLanguage();

  /**
   * @deprecated since 2.34.0 instead use getAdminLanguage(User user)
   */
  @Deprecated
  String getAdminLanguage(String userFullName);

  @NotNull
  String getAdminLanguage(@Nullable DocumentReference userDocRef);

  @NotNull
  String getAdminLanguage(@Nullable User user);

  /**
   * @deprecated instead use {@link ModelContext#getDefaultLanguage()}
   */
  @Deprecated
  String getDefaultLanguage();

  /**
   * @deprecated instead use {@link ModelContext#getDefaultLanguage(EntityReference)}
   */
  @Deprecated
  String getDefaultLanguage(String spaceName);

  /**
   * @deprecated instead use {@link ModelContext#getDefaultLanguage(EntityReference)}
   */
  @Deprecated
  String getDefaultLanguage(SpaceReference spaceRef);

  boolean hasParentSpace();

  boolean hasParentSpace(String spaceName);

  String getParentSpace();

  String getParentSpace(String spaceName);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  DocumentReference resolveDocumentReference(String fullName);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  DocumentReference resolveDocumentReference(String fullName, WikiReference wikiRef);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  EntityReference resolveRelativeEntityReference(String name, EntityType type);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  SpaceReference resolveSpaceReference(String spaceName);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  SpaceReference resolveSpaceReference(String spaceName, WikiReference wikiRef);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  WikiReference resolveWikiReference(@Nullable String wikiName);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  AttachmentReference resolveAttachmentReference(String fullName);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  AttachmentReference resolveAttachmentReference(String fullName, WikiReference wikiRef);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  EntityReference resolveEntityReference(String name, EntityType type);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  EntityReference resolveEntityReference(String name, EntityType type,
      WikiReference wikiRef);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  @NotNull
  <T extends EntityReference> T resolveReference(@NotNull String name,
      @NotNull Class<T> token);

  /**
   * @deprecated instead use {@link ModelUtils#resolveRef(String, Class, EntityReference)}
   */
  @Deprecated
  @NotNull
  <T extends EntityReference> T resolveReference(@NotNull String name,
      @NotNull Class<T> token, @Nullable EntityReference baseRef);

  /**
   * @deprecated instead use {@link IRightsAccessFacadeRole#isAdmin()}
   */
  @Deprecated
  boolean isAdminUser();

  /**
   * @deprecated instead use {@link IRightsAccessFacadeRole#isAdvancedAdmin()}
   */
  @Deprecated
  boolean isAdvancedAdmin();

  /**
   * @deprecated instead use {@link IRightsAccessFacadeRole#isSuperAdmin()}
   */
  @Deprecated
  boolean isSuperAdminUser();

  /**
   * @deprecated instead use {@link IRightsAccessFacadeRole#isLayoutEditor()}
   */
  @Deprecated
  boolean isLayoutEditor();

  /**
   * instead use IRightsAccessFacadeRole.hasAccessLevel(EntityReference, EAccessLevel)
   */
  @Deprecated
  boolean hasAccessLevel(EntityReference ref, EAccessLevel level);

  /**
   * use IRightsAccessFacadeRole.hasAccessLevel(EntityReference, EAccessLevel, XWikiUser)
   * instead
   */
  @Deprecated
  boolean hasAccessLevel(EntityReference ref, EAccessLevel level, XWikiUser user);

  /**
   * CAUTION: this method returns attachments which start with the attRef.name if no exact
   * match exists.
   *
   * @deprecated instead use IAttachmentServiceRole.getAttachmentNameEqual or
   *             IAttachmentServiceRole.getAttachmentsNameMatch
   */
  @Deprecated
  XWikiAttachment getAttachment(AttachmentReference attRef) throws XWikiException;

  Attachment getAttachmentApi(AttachmentReference attRef) throws XWikiException;

  /**
   * @deprecated instead use {@link #getAttachmentListSorted(XWikiDocument, Comparator)}
   */
  @Deprecated
  List<Attachment> getAttachmentListSorted(Document doc, String comparator)
      throws ClassNotFoundException;

  List<XWikiAttachment> getAttachmentListSorted(XWikiDocument doc,
      Comparator<XWikiAttachment> comparator);

  /**
   * @deprecated instead use {@link #getAttachmentListSorted(XWikiDocument, Comparator, boolean)}
   */
  @Deprecated
  List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly);

  List<XWikiAttachment> getAttachmentListSorted(XWikiDocument doc,
      Comparator<XWikiAttachment> comparator, boolean imagesOnly);

  /**
   * @deprecated instead use
   *             {@link #getAttachmentListSorted(XWikiDocument, Comparator, boolean, int, int)}
   */
  @Deprecated
  List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly, int start, int nb);

  List<XWikiAttachment> getAttachmentListSorted(XWikiDocument doc,
      Comparator<XWikiAttachment> comparator, boolean imagesOnly, int start, int nb);

  // TODO change signature requirement to XWikiDocument instead of document and mark
  // the old version as deprecated
  List<Attachment> getAttachmentListSortedSpace(String spaceName, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException;

  // TODO change signature requirement to XWikiDocument instead of document and mark
  // the old version as deprecated
  List<Attachment> getAttachmentListForTagSorted(Document doc, String tagName,
      String comparator, boolean imagesOnly, int start, int nb);

  // TODO change signature requirement to XWikiDocument instead of document and mark
  // the old version as deprecated
  List<Attachment> getAttachmentListForTagSortedSpace(String spaceName, String tagName,
      String comparator, boolean imagesOnly, int start, int nb) throws ClassNotFoundException;

  // TODO change signature requirement to XWikiDocument instead of document and mark
  // the old version as deprecated
  String getAttachmentListSortedAsJSON(Document doc, String comparator, boolean imagesOnly);

  // TODO change signature requirement to XWikiDocument instead of document and mark
  // the old version as deprecated
  String getAttachmentListSortedAsJSON(Document doc, String comparator, boolean imagesOnly,
      int start, int nb);

  /**
   * @since 5.8 instead use {@link XWikiObjectFetcher}
   */
  @Deprecated
  List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField, boolean asc);

  /**
   * @since 5.8 instead use {@link XWikiObjectFetcher}
   */
  @Deprecated
  List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField1, boolean asc1, String orderField2, boolean asc2);

  String[] splitStringByLength(String inStr, int maxLength);

  String getJSONContent(XWikiDocument cdoc);

  String getJSONContent(DocumentReference docRef);

  /**
   * @deprecated instead use {@link UserService#getUser(DocumentReference)} with
   *             {@link User#getPrettyName()}
   */
  @Deprecated
  String getUserNameForDocRef(DocumentReference userDocRef) throws XWikiException;

  String getMajorVersion(XWikiDocument doc);

  /**
   * @deprecated instead use {@link ModelContext#getWiki()}
   */
  @Deprecated
  WikiReference getWikiRef();

  /**
   * @deprecated instead use {@link ModelUtils#extractRef(EntityReference, EntityReference, Class)}
   */
  @Deprecated
  WikiReference getWikiRef(XWikiDocument doc);

  /**
   * @deprecated instead use {@link ModelUtils#extractRef(EntityReference, EntityReference, Class)}
   */
  @Deprecated
  WikiReference getWikiRef(DocumentReference docRef);

  /**
   * @deprecated instead use {@link ModelUtils#extractRef(EntityReference, EntityReference, Class)}
   */
  @Deprecated
  WikiReference getWikiRef(EntityReference ref);

  DocumentReference getWikiTemplateDocRef();

  XWikiDocument getWikiTemplateDoc();

  /**
   * @deprecated instead use {@link ModelUtils#serializeRef(EntityReference)}
   */
  @Deprecated
  EntityReferenceSerializer<String> getRefDefaultSerializer();

  /**
   * @deprecated instead use {@link ModelUtils#serializeRefLocal(EntityReference)}
   */
  @Deprecated
  EntityReferenceSerializer<String> getRefLocalSerializer();

  /**
   * @deprecated instead use {@link ModelUtils#serializeRef(EntityReference)}
   */
  @Deprecated
  String serializeRef(EntityReference entityRef);

  /**
   * @deprecated instead use {@link ModelUtils#serializeRef(EntityReference)} or
   *             {@link ModelUtils#serializeRefLocal(EntityReference)}
   */
  @Deprecated
  String serializeRef(EntityReference entityRef, boolean local);

  Map<String, String[]> getRequestParameterMap();

  String getInheritedTemplatedPath(DocumentReference localTemplateRef);

  /**
   * @deprecated instead use
   *             {@link IModelAccessFacade#deleteDocumentWithoutTranslations(XWikiDocument, boolean)}
   */
  @Deprecated
  void deleteDocument(XWikiDocument doc, boolean totrash) throws XWikiException;

  /**
   * @deprecated instead use {@link IModelAccessFacade#deleteDocument(XWikiDocument, boolean)}
   */
  @Deprecated
  void deleteAllDocuments(XWikiDocument doc, boolean totrash) throws XWikiException;

  String getTemplatePathOnDisk(String renderTemplatePath);

  String getTemplatePathOnDisk(String renderTemplatePath, String lang);

  String renderInheritableDocument(DocumentReference docRef, String lang)
      throws XWikiException;

  String renderInheritableDocument(DocumentReference docRef, String lang, String defLang)
      throws XWikiException;

  @Deprecated
  String cleanupXHTMLtoHTML5(String xhtml);

  @Deprecated
  String cleanupXHTMLtoHTML5(String xhtml, DocumentReference doc);

  @Deprecated
  String cleanupXHTMLtoHTML5(String xhtml, SpaceReference layoutRef);

  List<Attachment> getAttachmentsForDocs(List<String> docsFN);

  String getTranslatedDiscTemplateContent(String renderTemplatePath, String lang,
      String defLang);

  boolean existsInheritableDocument(@NotNull DocumentReference docRef);

  boolean existsInheritableDocument(@NotNull DocumentReference docRef,
      @Nullable String lang);

  boolean existsInheritableDocument(@NotNull DocumentReference docRef, @Nullable String lang,
      @Nullable String defLang);

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
  void sendCheckJobMail(String jobMailName, String fromAddr, String toAddr,
      List<String> params);

  /**
   * @deprecated instead use {@link CelConstant#CENTRAL_WIKI}
   */
  @Deprecated
  WikiReference getCentralWikiRef();

  /**
   * @deprecated instead use {@link ModelUtils#identifyClassFromName(String)}
   */
  @Deprecated
  EntityType resolveEntityTypeForFullName(String fullName);

  /**
   * @deprecated instead use {@link ModelUtils#identifyClassFromName(String)}
   */
  @Deprecated
  EntityType resolveEntityTypeForFullName(String fullName, EntityType defaultNameType);

  /**
   * only used as an adapter for unstable 2
   */
  <T> T lookup(Class<T> role) throws ComponentLookupException;

  /**
   * only used as an adapter for unstable 2
   */
  <T> T lookup(Class<T> role, String roleHint) throws ComponentLookupException;

  /**
   * only used as an adapter for unstable 2
   */
  <T> List<T> lookupList(Class<T> role) throws ComponentLookupException;

  /**
   * only used as an adapter for unstable 2
   */
  <T> Map<String, T> lookupMap(Class<T> role) throws ComponentLookupException;

  /**
   * @deprecated instead use {@link ModelUtils#adjustRef(EntityReference, Class, EntityReference)}
   */
  @Deprecated
  DocumentReference checkWikiRef(DocumentReference docRef);

  /**
   * @deprecated instead use {@link ModelUtils#adjustRef(EntityReference, Class, EntityReference)}
   */
  @Deprecated
  DocumentReference checkWikiRef(DocumentReference docRef, XWikiDocument toDoc);

  /**
   * @deprecated instead use {@link ModelUtils#adjustRef(EntityReference, Class, EntityReference)}
   */
  @Deprecated
  DocumentReference checkWikiRef(DocumentReference docRef, EntityReference toRef);

  /**
   * @deprecated instead use {@link ModelUtils#adjustRef(EntityReference, Class, EntityReference)}
   */
  @Deprecated
  DocumentReference setWikiReference(DocumentReference docRef, String wikiName);

  /**
   * @deprecated instead use {@link ModelUtils#adjustRef(EntityReference, Class, EntityReference)}
   */
  @Deprecated
  DocumentReference setWikiReference(DocumentReference docRef, WikiReference wikiRef);

  /**
   * @deprecated instead use {@link ModelContext#setUser}
   */
  @Deprecated
  void setUser(DocumentReference userReference, boolean main);

}
