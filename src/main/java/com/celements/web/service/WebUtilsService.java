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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.emptycheck.internal.IDefaultEmptyDocStrategyRole;
import com.celements.inheritor.TemplatePathTransformationConfiguration;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.celements.pagelayout.LayoutScriptService;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.parents.IDocumentParentsListerRole;
import com.celements.rendering.RenderCommand;
import com.celements.rendering.XHTMLtoHTML5cleanup;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.sajson.Builder;
import com.celements.web.comparators.BaseObjectComparator;
import com.celements.web.plugin.cmd.CelSendMail;
import com.celements.web.plugin.cmd.EmptyCheckCommand;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.plugin.cmd.PlainTextCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;
import com.xpn.xwiki.web.XWikiRequest;

@Component
public class WebUtilsService implements IWebUtilsService {

  private static final WikiReference CENTRAL_WIKI_REF = new WikiReference("celements2web");

  private static Logger _LOGGER = LoggerFactory.getLogger(WebUtilsService.class);

  @Requirement
  ComponentManager componentManager;

  @Requirement("default")
  EntityReferenceSerializer<String> serializer_default;

  @Requirement("local")
  EntityReferenceSerializer<String> serializer_local;

  @Requirement
  EntityReferenceResolver<String> refResolver;

  @Requirement("relative")
  EntityReferenceResolver<String> refResolver_relative;

  /**
   * Used to get the template path mapping information.
   */
  @Requirement
  private TemplatePathTransformationConfiguration tempPathConfig;

  /**
   * TODO change access to wiki-configuration "default" and check access to e.g.
   * 'languages' property You get access to this configuration source by using:
   *
   * @Requirement private ConfigurationSource configuration; Configuration properties are
   *              first looked for in the "space" source, if not found then in the "wiki"
   *              source and if not found in the "xwikiproperties" source. This is the
   *              recommended configuration source for most usages since it allows to
   *              defined configuration properties in the xwiki.propertieds file and to
   *              override them in the running wiki (globally or per space). Source:
   *              http://extensions.xwiki.org/xwiki/bin/view/Extension/Configuration+
   *              Module
   */

  @Requirement
  IDefaultEmptyDocStrategyRole emptyChecker;

  @Requirement
  ConfigurationSource defaultConfigSrc;

  /*
   * not loaded as requirement due to cyclic dependency
   */
  IDocumentParentsListerRole docParentsLister;

  @Requirement
  Execution execution;

  XWikiRenderingEngine injectedRenderingEngine;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public DocumentReference getParentForLevel(int level) {
    _LOGGER.trace("getParentForLevel: start for level " + level);
    DocumentReference parent = null;
    List<DocumentReference> parentList = getDocumentParentsList(getContext().getDoc()
        .getDocumentReference(), true);
    int startAtItem = (parentList.size() - level) + 1;
    if ((startAtItem > -1) && (startAtItem < parentList.size())) {
      parent = parentList.get(startAtItem);
    }
    _LOGGER.debug("getParentForLevel: level [" + level + "] returning [" + parent + "]");
    return parent;
  }

  /**
   * @deprecated since 2.63.0
   * @deprecated instead use IDocumentParentsListerRole.getDocumentParentsList(
   *             DocumentReference docRef, boolean includeDoc)
   */
  @Override
  @Deprecated
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef,
      boolean includeDoc) {
    return getDocumentParentsLister().getDocumentParentsList(docRef, includeDoc);
  }

  @Override
  public String getDocSectionAsJSON(String regex, DocumentReference docRef, int section)
      throws XWikiException {
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

  @Override
  public String getDocSection(String regex, DocumentReference docRef, int section)
      throws XWikiException {
    _LOGGER.debug("use regex '" + regex + "' on '" + docRef + "' and get section " + section);
    XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
    String content = doc.getTranslatedDocument(getContext()).getContent();
    _LOGGER.debug("content of'" + docRef + "' is: '" + content + "'");
    String section_str = null;
    if ((content != null) && (!emptyChecker.isEmptyRTEString(content))) {
      section = getSectionNr(section, countSections(regex, docRef));
      for (String partStr : content.split(regex)) {
        if (!emptyChecker.isEmptyRTEString(partStr)) {
          section--;
          if (section == 0) {
            section_str = partStr;
            break;
          }
        }
      }
    } else {
      _LOGGER.debug("content ist empty");
    }
    if (section_str != null) {
      section_str = renderText(section_str);
    }
    return section_str;
  }

  @Override
  public int countSections(String regex, DocumentReference docRef) throws XWikiException {
    _LOGGER.debug("use regex '" + regex + "' on '" + docRef + "'");
    XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
    String content = doc.getTranslatedDocument(getContext()).getContent();
    _LOGGER.debug("content of'" + docRef + "' is: '" + content + "'");
    int parts = 0;
    if ((content != null) && (!emptyChecker.isEmptyRTEString(content))) {
      for (String part : content.split(regex)) {
        if (!emptyChecker.isEmptyRTEString(part)) {
          parts++;
        }
      }
    } else {
      _LOGGER.debug("content ist empty");
    }
    return parts;
  }

  int getSectionNr(int section, int sectionNr) {
    if (section <= 0) {
      section = 1;
    }
    if (section > sectionNr) {
      section = sectionNr;
    }
    return section;
  }

  private String renderText(String velocityText) {
    return getContext().getWiki().getRenderingEngine().renderText("{pre}" + velocityText + "{/pre}",
        getContext().getDoc(), getContext());
  }

  private boolean isEmptyRTEString(String rteContent) {
    return new EmptyCheckCommand().isEmptyRTEString(rteContent);
  }

  @Override
  public List<String> getAllowedLanguages() {
    if ((getContext() != null) && (getContext().getDoc() != null)) {
      return getAllowedLanguages(getContext().getDoc().getDocumentReference()
          .getLastSpaceReference().getName());
    }
    return Collections.emptyList();
  }

  @Override
  public List<String> getAllowedLanguages(String spaceName) {
    List<String> languages = new ArrayList<String>();
    String spaceLanguages = getContext().getWiki().getSpacePreference("languages", spaceName, "",
        getContext());
    languages.addAll(Arrays.asList(spaceLanguages.split("[ ,]")));
    languages.remove("");
    if (languages.size() > 0) {
      _LOGGER.debug("getAllowedLanguages: returning [" + spaceLanguages + "] for space ["
          + spaceName + "]");
      return languages;
    }
    _LOGGER.warn("Deprecated usage of Preferences field 'language'." + " Instead use 'languages'.");
    return Arrays.asList(getContext().getWiki().getSpacePreference("language", spaceName, "",
        getContext()).split("[ ,]"));
  }

  @Override
  public Date parseDate(String date, String format) {
    try {
      return new SimpleDateFormat(format).parse(date);
    } catch (ParseException exp) {
      _LOGGER.error("parseDate failed.", exp);
      return null;
    }
  }

  @Override
  public XWikiMessageTool getMessageTool(String adminLanguage) {
    if (adminLanguage != null) {
      if ((getContext().getLanguage() != null) && getContext().getLanguage().equals(
          adminLanguage)) {
        return getContext().getMessageTool();
      } else {
        Locale locale = new Locale(adminLanguage);
        ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources", locale);
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

  @Override
  public XWikiMessageTool getAdminMessageTool() {
    return getMessageTool(getAdminLanguage());
  }

  @Override
  public String getAdminLanguage() {
    return getAdminLanguage(getContext().getUser());
  }

  /**
   * @deprecated since 2.34.0 instead use getAdminLanguage(DocumentReference userRef)
   */
  @Deprecated
  @Override
  public String getAdminLanguage(String userFullName) {
    return getAdminLanguage(resolveDocumentReference(userFullName));
  }

  @Override
  public String getAdminLanguage(DocumentReference userRef) {
    String adminLanguage = null;
    try {
      DocumentReference xwikiUsersClassRef = new DocumentReference(userRef.getWikiReference()
          .getName(), "XWiki", "XWikiUsers");
      BaseObject userObj = getContext().getWiki().getDocument(userRef, getContext()).getXObject(
          xwikiUsersClassRef);
      if (userObj != null) {
        adminLanguage = userObj.getStringValue("admin_language");
      }
    } catch (XWikiException e) {
      _LOGGER.error("failed to get UserObject for " + getContext().getUser());
    }
    if ((adminLanguage == null) || ("".equals(adminLanguage))) {
      adminLanguage = getDefaultAdminLanguage();
    }
    return adminLanguage;
  }

  @Override
  public String getDefaultAdminLanguage() {
    String adminLanguage;
    adminLanguage = getContext().getWiki().getSpacePreference("admin_language", getContext()
        .getLanguage(), getContext());
    if ((adminLanguage == null) || ("".equals(adminLanguage))) {
      adminLanguage = getContext().getWiki().Param("celements.admin_language");
      if ((adminLanguage == null) || ("".equals(adminLanguage))) {
        adminLanguage = "en";
      }
    }
    return adminLanguage;
  }

  @Override
  public String getDefaultLanguage() {
    return getDefaultLanguage((SpaceReference) null);
  }

  @Deprecated
  @Override
  public String getDefaultLanguage(String spaceName) {
    SpaceReference spaceRef = null;
    if (StringUtils.isNotBlank(spaceName)) {
      spaceRef = resolveSpaceReference(spaceName);
    }
    return getDefaultLanguage(spaceRef);
  }

  @Override
  public String getDefaultLanguage(SpaceReference spaceRef) {
    String defaultLang = "";
    String dbbackup = getContext().getDatabase();
    XWikiDocument docBackup = getContext().getDoc();
    try {
      if (spaceRef != null) {
        DocumentReference docRef = new DocumentReference("WebPreferences", spaceRef);
        if (getContext().getWiki().exists(docRef, getContext())) {
          getContext().setDatabase(spaceRef.getParent().getName());
          getContext().setDoc(getContext().getWiki().getDocument(docRef, getContext()));
        }
      }
      // IMPORTANT: in unstable-2.0 defaultLanguage may never be empty
      defaultLang = defaultConfigSrc.getProperty("default_language", "en");
    } catch (XWikiException xwe) {
      _LOGGER.error("failed getting WebPreferences for space '{}'", spaceRef, xwe);
    } finally {
      getContext().setDatabase(dbbackup);
      getContext().setDoc(docBackup);
    }
    _LOGGER.trace("getDefaultLanguage: for currentDoc '{}' and spaceRef '{}' got lang" + " '{}'",
        getContext().getDoc(), spaceRef, defaultLang);
    return defaultLang;
  }

  @Override
  public boolean hasParentSpace() {
    return ((getParentSpace() != null) && !"".equals(getParentSpace()));
  }

  @Override
  public boolean hasParentSpace(String spaceName) {
    return ((getParentSpace(spaceName) != null) && !"".equals(getParentSpace(spaceName)));
  }

  @Override
  public String getParentSpace() {
    return getContext().getWiki().getSpacePreference("parent", getContext());
  }

  @Override
  public String getParentSpace(String spaceName) {
    return getContext().getWiki().getSpacePreference("parent", spaceName, "", getContext());
  }

  @Override
  public DocumentReference resolveDocumentReference(String fullName) {
    return resolveDocumentReference(fullName, null);
  }

  @Override
  public DocumentReference resolveDocumentReference(String fullName, WikiReference wikiRef) {
    return new DocumentReference(resolveEntityReference(fullName, EntityType.DOCUMENT, wikiRef));
  }

  @Override
  public SpaceReference resolveSpaceReference(String spaceName) {
    return resolveSpaceReference(spaceName, null);
  }

  @Override
  public SpaceReference resolveSpaceReference(String spaceName, WikiReference wikiRef) {
    return new SpaceReference(resolveEntityReference(spaceName, EntityType.SPACE, wikiRef));
  }

  @Override
  public AttachmentReference resolveAttachmentReference(String fullName) {
    return resolveAttachmentReference(fullName, null);
  }

  @Override
  public AttachmentReference resolveAttachmentReference(String fullName, WikiReference wikiRef) {
    return new AttachmentReference(resolveEntityReference(fullName, EntityType.ATTACHMENT,
        wikiRef));
  }

  @Override
  public EntityReference resolveEntityReference(String name, EntityType type) {
    return resolveEntityReference(name, type, null);
  }

  @Override
  public EntityReference resolveEntityReference(String name, EntityType type,
      WikiReference wikiRef) {
    if (wikiRef == null) {
      wikiRef = new WikiReference(getContext().getDatabase());
    }
    EntityReference ref = refResolver.resolve(name, type, wikiRef);
    _LOGGER.debug("resolveEntityReference: for [" + name + "] got reference [" + ref + "]");
    return ref;
  }

  @Override
  public EntityReference resolveRelativeEntityReference(String name, EntityType type) {
    EntityReference ref = refResolver_relative.resolve(name, type);
    _LOGGER.debug("resolveRelativeEntityReference: for [" + name + "] got reference [" + ref + "]");
    return ref;
  }

  @Override
  public boolean isAdminUser() {
    try {
      if ((getContext().getXWikiUser() != null) && (getContext().getWiki()
          .getRightService() != null) && (getContext().getDoc() != null)) {
        return (getContext().getWiki().getRightService().hasAdminRights(getContext())
            || getContext().getXWikiUser().isUserInGroup("XWiki.XWikiAdminGroup", getContext()));
      } else {
        return false;
      }
    } catch (XWikiException e) {
      _LOGGER.error("Cannot determin if user has Admin Rights therefore guess" + " no (false).", e);
      return false;
    }
  }

  @Override
  public boolean isSuperAdminUser() {
    String user = getContext().getUser();
    _LOGGER.trace("isSuperAdminUser: user [" + user + "] db [" + getContext().getDatabase() + "].");
    return (isAdminUser() && (user.startsWith("xwiki:") || getContext().isMainWiki()));
  }

  @Override
  public boolean isLayoutEditor() {
    String user = getContext().getUser();
    _LOGGER.trace("isLayoutEditor: user [" + user + "] db [" + getContext().getDatabase() + "].");
    try {
      boolean isLayoutEditor = isAdvancedAdmin() || getContext().getXWikiUser().isUserInGroup(
          "XWiki.LayoutEditorsGroup", getContext());
      _LOGGER.debug("isLayoutEditor: admin [" + isAdminUser() + "] global user [" + user.startsWith(
          "xwiki:") + "] returning [" + isLayoutEditor + "] db [" + getContext().getDatabase()
          + "].");
      return isLayoutEditor;
    } catch (XWikiException exp) {
      _LOGGER.error("Failed to get user document for [" + user + "].", exp);
    }
    return false;
  }

  @Override
  public boolean isAdvancedAdmin() {
    String user = getContext().getUser();
    _LOGGER.trace("isAdvancedAdmin: user [" + user + "] db [" + getContext().getDatabase() + "].");
    try {
      XWikiDocument userDoc = getContext().getWiki().getDocument(resolveDocumentReference(user),
          getContext());
      BaseObject userObj = userDoc.getXObject(resolveDocumentReference("XWiki.XWikiUsers"));
      boolean isAdvancedAdmin = isAdminUser() && (user.startsWith("xwiki:") || ((userObj != null)
          && "Advanced".equals(userObj.getStringValue("usertype"))));
      _LOGGER.debug("isAdvancedAdmin: admin [" + isAdminUser() + "] global user [" + user
          .startsWith("xwiki:") + "] usertype [" + ((userObj != null) ? userObj.getStringValue(
              "usertype") : "null") + "] returning [" + isAdvancedAdmin + "] db [" + getContext()
                  .getDatabase() + "].");
      return isAdvancedAdmin;
    } catch (XWikiException exp) {
      _LOGGER.error("Failed to get user document for [" + user + "].", exp);
    }
    return false;
  }

  /**
   * CAUTION: cyclic dependency with IRightsAccessFacadeRole
   */
  @Deprecated
  private IRightsAccessFacadeRole getRightsAccess() {
    return Utils.getComponent(IRightsAccessFacadeRole.class);
  }

  @Override
  @Deprecated
  public boolean hasAccessLevel(EntityReference ref, EAccessLevel level) {
    return getRightsAccess().hasAccessLevel(ref, level);
  }

  @Override
  @Deprecated
  public boolean hasAccessLevel(EntityReference ref, EAccessLevel level, XWikiUser user) {
    return getRightsAccess().hasAccessLevel(ref, level, user);
  }

  @Override
  @Deprecated
  public XWikiAttachment getAttachment(AttachmentReference attRef) throws XWikiException {
    XWikiDocument attDoc = getContext().getWiki().getDocument(attRef.getDocumentReference(),
        getContext());
    return attDoc.getAttachment(attRef.getName());
  }

  @Override
  public Attachment getAttachmentApi(AttachmentReference attRef) throws XWikiException {
    XWikiAttachment att = getAttachment(attRef);
    if (att != null) {
      XWikiDocument attDoc = getContext().getWiki().getDocument(attRef.getDocumentReference(),
          getContext());
      return new Attachment(attDoc.newDocument(getContext()), att, getContext());
    }
    return null;
  }

  @Deprecated
  @SuppressWarnings("unchecked")
  @Override
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator)
      throws ClassNotFoundException {
    List<Attachment> attachments = doc.getAttachmentList();

    try {
      Comparator<Attachment> comparatorClass = (Comparator<Attachment>) Class.forName(
          "com.celements.web.comparators." + comparator).newInstance();
      Collections.sort(attachments, comparatorClass);
    } catch (InstantiationException e) {
      _LOGGER.error("getAttachmentListSorted failed.", e);
    } catch (IllegalAccessException e) {
      _LOGGER.error("getAttachmentListSorted failed.", e);
    } catch (ClassNotFoundException e) {
      throw e;
    }

    return attachments;
  }

  @Override
  public List<XWikiAttachment> getAttachmentListSorted(XWikiDocument doc,
      Comparator<XWikiAttachment> comparator) {
    return getAttachmentListSorted(doc, comparator, false);
  }

  @Deprecated
  @Override
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly) {
    return getAttachmentListSorted(doc, comparator, imagesOnly, 0, 0);
  }

  @Override
  public List<XWikiAttachment> getAttachmentListSorted(XWikiDocument doc,
      Comparator<XWikiAttachment> comparator, boolean imagesOnly) {
    return getAttachmentListSorted(doc, comparator, imagesOnly, 0, 0);
  }

  @Deprecated
  @Override
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) {
    return getAttachmentListForTagSorted(doc, null, comparator, imagesOnly, start, nb);
  }

  @Override
  public List<XWikiAttachment> getAttachmentListSorted(XWikiDocument doc,
      Comparator<XWikiAttachment> comparator, boolean imagesOnly, int start, int nb) {
    List<XWikiAttachment> atts = new ArrayList<XWikiAttachment>(doc.getAttachmentList());
    if (comparator != null) {
      Collections.sort(atts, comparator);
    }
    if (imagesOnly) {
      filterAttachmentsByImage(atts);
    }
    return Collections.unmodifiableList(reduceListToSize(atts, start, nb));
  }

  private void filterAttachmentsByImage(List<XWikiAttachment> atts) {
    Iterator<XWikiAttachment> iter = atts.iterator();
    while (iter.hasNext()) {
      if (!iter.next().isImage(getContext())) {
        iter.remove();
      }
    }
  }

  @Override
  public List<Attachment> getAttachmentListSortedSpace(String spaceName, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException {
    return getAttachmentListForTagSortedSpace(spaceName, null, comparator, imagesOnly, start, nb);
  }

  @Override
  public List<Attachment> getAttachmentListForTagSorted(Document doc, String tagName,
      String comparator, boolean imagesOnly, int start, int nb) {
    try {
      List<Attachment> attachments = getAttachmentListSorted(doc, comparator);
      if (imagesOnly) {
        for (Attachment att : new ArrayList<Attachment>(attachments)) {
          if (!att.isImage()) {
            attachments.remove(att);
          }
        }
      }
      return reduceListToSize(filterAttachmentsByTag(attachments, tagName), start, nb);
    } catch (ClassNotFoundException exp) {
      _LOGGER.error("getAttachmentListSorted failed.", exp);
    }
    return Collections.emptyList();
  }

  @Override
  public List<Attachment> getAttachmentListForTagSortedSpace(String spaceName, String tagName,
      String comparator, boolean imagesOnly, int start, int nb) throws ClassNotFoundException {
    List<Attachment> attachments = new ArrayList<Attachment>();
    try {
      for (String docName : getContext().getWiki().getSpaceDocsName(spaceName, getContext())) {
        DocumentReference docRef = new DocumentReference(getContext().getDatabase(), spaceName,
            docName);
        XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
        attachments.addAll(new Document(doc, getContext()).getAttachmentList());
      }
    } catch (XWikiException xwe) {
      _LOGGER.error("Could not get all documents in " + spaceName, xwe);
    }
    try {
      Comparator<Attachment> comparatorClass = (Comparator<Attachment>) Class.forName(
          "com.celements.web.comparators." + comparator).newInstance();
      Collections.sort(attachments, comparatorClass);
    } catch (InstantiationException e) {
      _LOGGER.error("getAttachmentListSortedSpace failed.", e);
    } catch (IllegalAccessException e) {
      _LOGGER.error("getAttachmentListSortedSpace failed.", e);
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
    return reduceListToSize(filterAttachmentsByTag(attachments, tagName), start, nb);
  }

  List<Attachment> filterAttachmentsByTag(List<Attachment> attachments, String tagName) {
    if ((tagName != null) && getContext().getWiki().exists(resolveDocumentReference(tagName),
        getContext())) {
      XWikiDocument filterDoc = null;
      try {
        filterDoc = getContext().getWiki().getDocument(resolveDocumentReference(tagName),
            getContext());
      } catch (XWikiException xwe) {
        _LOGGER.error("Exception getting tag document '" + tagName + "'", xwe);
      }
      DocumentReference tagClassRef = new DocumentReference(getContext().getDatabase(), "Classes",
          "FilebaseTag");
      if ((filterDoc != null) && (filterDoc.getXObjectSize(tagClassRef) > 0)) {
        List<Attachment> filteredAttachments = new ArrayList<Attachment>();
        for (Attachment attachment : attachments) {
          String attFN = attachment.getDocument().getFullName() + "/" + attachment.getFilename();
          if (null != filterDoc.getXObject(tagClassRef, "attachment", attFN, false)) {
            filteredAttachments.add(attachment);
          }
        }
        return filteredAttachments;
      }
    }
    return attachments;
  }

  @Override
  public String getAttachmentListSortedAsJSON(Document doc, String comparator, boolean imagesOnly) {
    return getAttachmentListSortedAsJSON(doc, comparator, imagesOnly, 0, 0);
  }

  @Override
  public String getAttachmentListSortedAsJSON(Document doc, String comparator, boolean imagesOnly,
      int start, int nb) {
    SimpleDateFormat dateFormater = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    Builder jsonBuilder = new Builder();
    jsonBuilder.openArray();
    for (Attachment att : getAttachmentListSorted(doc, comparator, imagesOnly, start, nb)) {
      jsonBuilder.openDictionary();
      jsonBuilder.addStringProperty("filename", att.getFilename());
      jsonBuilder.addStringProperty("version", att.getVersion());
      jsonBuilder.addStringProperty("author", att.getAuthor());
      jsonBuilder.addStringProperty("mimeType", att.getMimeType());
      jsonBuilder.addStringProperty("lastChanged", dateFormater.format(att.getDate()));
      jsonBuilder.addStringProperty("url", doc.getAttachmentURL(att.getFilename()));
      jsonBuilder.closeDictionary();
    }
    jsonBuilder.closeArray();
    return jsonBuilder.getJSON();
  }

  <T> List<T> reduceListToSize(List<T> list, int start, int nb) {
    List<T> countedAtts = new ArrayList<T>();
    if ((start <= 0) && ((nb <= 0) || (nb >= list.size()))) {
      countedAtts = list;
    } else if (start < list.size()) {
      countedAtts = list.subList(Math.max(0, start), Math.min(Math.max(0, start) + Math.max(0, nb),
          list.size()));
    }
    return countedAtts;
  }

  Map<String, String> xwikiDoctoLinkedMap(XWikiDocument xwikiDoc, boolean bWithObjects,
      boolean bWithRendering, boolean bWithAttachmentContent, boolean bWithVersions)
          throws XWikiException {
    Map<String, String> docData = new LinkedHashMap<String, String>();
    DocumentReference docRef = xwikiDoc.getDocumentReference();
    docData.put("web", docRef.getLastSpaceReference().getName());
    docData.put("name", docRef.getName());
    docData.put("language", xwikiDoc.getLanguage());
    docData.put("defaultLanguage", xwikiDoc.getDefaultLanguage());
    docData.put("translation", "" + xwikiDoc.getTranslation());
    docData.put("defaultLanguage", xwikiDoc.getDefaultLanguage());
    List<DocumentReference> documentParentsList = getDocumentParentsList(docRef, false);
    String docParentStr = "";
    if (!documentParentsList.isEmpty()) {
      DocumentReference docParentRef = documentParentsList.get(0);
      docParentStr = serializer_default.serialize(docParentRef);
    }
    docData.put("parent", docParentStr);
    String parentsListStr = "";
    String parentsListMNStr = "";
    MultilingualMenuNameCommand menuNameCmd = new MultilingualMenuNameCommand();
    for (DocumentReference parentDocRef : documentParentsList) {
      String parentDocFN = serializer_default.serialize(parentDocRef);
      parentsListMNStr += menuNameCmd.getMultilingualMenuName(parentDocFN, getContext()
          .getLanguage(), getContext()) + ",";
      parentsListStr += parentDocFN + ",";
    }
    docData.put("parentslist", parentsListStr.replaceAll(",*$", ""));
    docData.put("parentslistmenuname", parentsListMNStr.replaceAll(",*$", ""));
    PageTypeReference pageTypeRef = getPageTypeResolver().getPageTypeRefForDocWithDefault(xwikiDoc);
    if (pageTypeRef != null) {
      docData.put("pagetype", pageTypeRef.getConfigName());
    }
    docData.put("author", xwikiDoc.getAuthor());
    docData.put("creator", xwikiDoc.getCreator());
    docData.put("customClass", xwikiDoc.getCustomClass());
    docData.put("contentAuthor", xwikiDoc.getContentAuthor());
    docData.put("creationDate", "" + xwikiDoc.getCreationDate().getTime());
    docData.put("date", "" + xwikiDoc.getDate().getTime());
    docData.put("contentUpdateDate", "" + xwikiDoc.getContentUpdateDate().getTime());
    docData.put("version", xwikiDoc.getVersion());
    docData.put("title", xwikiDoc.getTitle());
    docData.put("template", serializer_local.serialize(xwikiDoc.getTemplateDocumentReference()));
    docData.put("getDefaultTemplate", xwikiDoc.getDefaultTemplate());
    docData.put("getValidationScript", xwikiDoc.getValidationScript());
    docData.put("comment", xwikiDoc.getComment());
    docData.put("minorEdit", String.valueOf(xwikiDoc.isMinorEdit()));
    docData.put("syntaxId", xwikiDoc.getSyntax().toIdString());
    docData.put("menuName", menuNameCmd.getMultilingualMenuName(serializer_default.serialize(
        xwikiDoc.getDocumentReference()), getContext().getLanguage(), getContext()));
        // docData.put("hidden", String.valueOf(xwikiDoc.isHidden()));

    /**
     * TODO add Attachments for (XWikiAttachment attach : xwikiDoc.getAttachmentList()) {
     * docel.add(attach.toXML(bWithAttachmentContent, bWithVersions, context)); }
     **/

    if (bWithObjects) {
      // // Add Class
      // BaseClass bclass = xwikiDoc.getxWikiClass();
      // if (bclass.getFieldList().size() > 0) {
      // // If the class has fields, add class definition and field information to XML
      // docel.add(bclass.toXML(null));
      // }
      //
      // // Add Objects (THEIR ORDER IS MOLDED IN STONE!)
      // for (Vector<BaseObject> objects : getxWikiObjects().values()) {
      // for (BaseObject obj : objects) {
      // if (obj != null) {
      // BaseClass objclass = null;
      // if (StringUtils.equals(getFullName(), obj.getClassName())) {
      // objclass = bclass;
      // } else {
      // objclass = obj.getxWikiClass(context);
      // }
      // docel.add(obj.toXML(objclass));
      // }
      // }
      // }
      throw new NotImplementedException();
    }

    String host = getContext().getRequest().getHeader("host");
    // Add Content
    docData.put("content", replaceInternalWithExternalLinks(xwikiDoc.getContent(), host));

    if (bWithRendering) {
      try {
        docData.put("renderedcontent", replaceInternalWithExternalLinks(xwikiDoc.getRenderedContent(
            getContext()), host));
      } catch (XWikiException exp) {
        _LOGGER.error("Exception with rendering content: ", exp);
      }
      try {
        docData.put("celrenderedcontent", replaceInternalWithExternalLinks(getCelementsRenderCmd()
            .renderCelementsDocument(xwikiDoc.getDocumentReference(), getContext().getLanguage(),
                "view"), host));
      } catch (XWikiException exp) {
        _LOGGER.error("Exception with rendering content: ", exp);
      }
    }

    if (bWithVersions) {
      try {
        docData.put("versions", xwikiDoc.getDocumentArchive(getContext()).getArchive(getContext()));
      } catch (XWikiException exp) {
        _LOGGER.error("Document [" + docRef.getName() + "] has malformed history", exp);
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
    String result = content.replaceAll("src=\\\"(\\.\\./)*/?download/", "src=\"http://" + host
        + "/download/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?download/", "href=\"http://" + host
        + "/download/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?skin/", "href=\"http://" + host + "/skin/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?view/", "href=\"http://" + host + "/view/");
    result = result.replaceAll("href=\\\"(\\.\\./)*/?edit/", "href=\"http://" + host + "/edit/");
    return result;
  }

  @Override
  public String getJSONContent(DocumentReference docRef) {
    try {
      return getJSONContent(getContext().getWiki().getDocument(docRef, getContext()));
    } catch (XWikiException exp) {
      _LOGGER.error("Failed to get document [" + docRef + "] for JSON.", exp);
    }
    return "{}";
  }

  @Override
  public String getJSONContent(XWikiDocument cdoc) {
    Map<String, String> data;
    try {
      data = xwikiDoctoLinkedMap(cdoc.getTranslatedDocument(getContext()), false, true, false,
          false);
    } catch (XWikiException e) {
      _LOGGER.error("getJSONContent failed.", e);
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

  @Override
  public String getUserNameForDocRef(DocumentReference authDocRef) throws XWikiException {
    XWikiDocument authDoc = getContext().getWiki().getDocument(authDocRef, getContext());
    BaseObject authObj = authDoc.getXObject(getRef("XWiki", "XWikiUsers"));
    if (authObj != null) {
      return authObj.getStringValue("last_name") + ", " + authObj.getStringValue("first_name");
    } else {
      return getAdminMessageTool().get("cel_ml_unknown_author");
    }
  }

  @Override
  public String getMajorVersion(XWikiDocument doc) {
    String revision = "1";
    if (doc != null) {
      revision = doc.getVersion();
      if ((revision != null) && (revision.trim().length() > 0) && revision.contains(".")) {
        revision = revision.split("\\.")[0];
      }
    }
    return revision;
  }

  private DocumentReference getRef(String spaceName, String pageName) {
    return new DocumentReference(getContext().getDatabase(), spaceName, pageName);
  }

  @Override
  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField, boolean asc) {
    return getObjectsOrdered(doc, classRef, orderField, asc, null, false);
  }

  /**
   * Get a list of Objects for a Document sorted by one or two fields.
   *
   * @param doc
   *          The Document where the Objects are attached.
   * @param classRef
   *          The reference to the class of the Objects to return
   * @param orderField1
   *          Field to order the objects by. First priority.
   * @param asc1
   *          Order first priority ascending or descending.
   * @param orderField2
   *          Field to order the objects by. Second priority.
   * @param asc2
   *          Order second priority ascending or descending.
   * @return List of objects ordered as specified
   */
  @Override
  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField1, boolean asc1, String orderField2, boolean asc2) {
    List<BaseObject> resultList = new ArrayList<BaseObject>();
    if (doc != null) {
      List<BaseObject> allObjects = doc.getXObjects(classRef);
      if (allObjects != null) {
        for (BaseObject obj : allObjects) {
          if (obj != null) {
            resultList.add(obj);
          }
        }
      }
      Collections.sort(resultList, new BaseObjectComparator(orderField1, asc1, orderField2, asc2));
    }
    return resultList;
  }

  @Override
  public String[] splitStringByLength(String inStr, int maxLength) {
    int numFullStr = (inStr.length() - 1) / maxLength;
    String[] splitedStr = new String[1 + numFullStr];
    for (int i = 0; i < numFullStr; i++) {
      int startIndex = i * maxLength;
      splitedStr[i] = inStr.substring(startIndex, startIndex + maxLength);
    }
    int lastPiece = splitedStr.length - 1;
    splitedStr[lastPiece] = inStr.substring(lastPiece * maxLength, inStr.length());
    return splitedStr;
  }

  @Override
  public WikiReference getWikiRef() {
    return getWikiRef((EntityReference) null);
  }

  @Override
  public WikiReference getWikiRef(XWikiDocument doc) {
    EntityReference ref = ((doc != null) ? doc.getDocumentReference() : null);
    return getWikiRef(ref);
  }

  @Override
  public WikiReference getWikiRef(DocumentReference ref) {
    return getWikiRef((EntityReference) ref);
  }

  @Override
  public WikiReference getWikiRef(EntityReference ref) {
    WikiReference ret = null;
    if (ref != null) {
      ret = new WikiReference(ref.extractReference(EntityType.WIKI));
    } else {
      ret = new WikiReference(getContext().getDatabase());
    }
    return ret;
  }

  @Override
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

  @Override
  public XWikiDocument getWikiTemplateDoc() {
    DocumentReference templateDocRef = getWikiTemplateDocRef();
    if (templateDocRef != null) {
      try {
        return getContext().getWiki().getDocument(templateDocRef, getContext());
      } catch (XWikiException exp) {
        _LOGGER.error("Exception while getting template doc '" + templateDocRef + "'", exp);
      }
    }
    return null;
  }

  @Override
  public EntityReferenceSerializer<String> getRefDefaultSerializer() {
    return serializer_default;
  }

  @Override
  public EntityReferenceSerializer<String> getRefLocalSerializer() {
    return serializer_local;
  }

  @Override
  public String serializeRef(EntityReference entityRef) {
    return getRefDefaultSerializer().serialize(entityRef);
  }

  @Override
  public String serializeRef(EntityReference entityRef, boolean local) {
    if (local) {
      return getRefLocalSerializer().serialize(entityRef);
    } else {
      return getRefDefaultSerializer().serialize(entityRef);
    }
  }

  @Override
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

  @Override
  public String getInheritedTemplatedPath(DocumentReference localTemplateRef) {
    if (localTemplateRef != null) {
      String templatePath = getRefDefaultSerializer().serialize(localTemplateRef);
      if (!getContext().getWiki().exists(localTemplateRef, getContext())) {
        if (!"celements2web".equals(localTemplateRef.getLastSpaceReference().getParent().getName())
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
    DocumentReference centralTemplateRef = new DocumentReference("celements2web", localTemplateRef
        .getLastSpaceReference().getName(), localTemplateRef.getName());
    return centralTemplateRef;
  }

  /**
   * @deprecated instead use
   *             {@link IModelAccessFacade# deleteDocumentWithoutTranslations(XWikiDocument, boolean)}
   */
  @Deprecated
  @Override
  public void deleteDocument(XWikiDocument doc, boolean totrash) throws XWikiException {
    try {
      getModelAccess().deleteDocumentWithoutTranslations(doc, totrash);
    } catch (DocumentDeleteException exc) {
      throw (XWikiException) exc.getCause();
    }
  }

  /**
   * @deprecated instead use
   *             {@link IModelAccessFacade#deleteDocument(XWikiDocument, boolean)}
   */
  @Deprecated
  @Override
  public void deleteAllDocuments(XWikiDocument doc, boolean totrash) throws XWikiException {
    try {
      getModelAccess().deleteDocument(doc, totrash);
    } catch (DocumentDeleteException exc) {
      throw (XWikiException) exc.getCause();
    }
  }

  private IModelAccessFacade getModelAccess() {
    // not as requirement due to cyclic dependency
    return Utils.getComponent(IModelAccessFacade.class);
  }

  @Override
  public String getTemplatePathOnDisk(String renderTemplatePath) {
    return getTemplatePathOnDisk(renderTemplatePath, null);
  }

  @Override
  public String getTemplatePathOnDisk(String renderTemplatePath, String lang) {
    for (Map.Entry<Object, Object> entry : tempPathConfig.getMappings().entrySet()) {
      String pathName = (String) entry.getKey();
      if (renderTemplatePath.startsWith(":" + pathName)) {
        String newRenderTemplatePath = renderTemplatePath.replaceAll("^:(" + pathName + "\\.)?",
            "/templates/" + ((String) entry.getValue()) + "/") + getTemplatePathLangSuffix(lang)
            + ".vm";
        _LOGGER.debug("getTemplatePathOnDisk: for [" + renderTemplatePath + "] and lang [" + lang
            + "] returning [" + newRenderTemplatePath + "].");
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

  @Override
  public String renderInheritableDocument(DocumentReference docRef, String lang)
      throws XWikiException {
    return renderInheritableDocument(docRef, lang, null);
  }

  @Override
  public String renderInheritableDocument(DocumentReference docRef, String lang, String defLang)
      throws XWikiException {
    RenderCommand renderCommand = new RenderCommand();
    if (this.injectedRenderingEngine != null) {
      renderCommand.setRenderingEngine(this.injectedRenderingEngine);
    }
    String templatePath = getInheritedTemplatedPath(docRef);
    _LOGGER.debug("renderInheritableDocument: call renderTemplatePath for [" + templatePath
        + "] and lang [" + lang + "] and defLang [" + defLang + "].");
    return renderCommand.renderTemplatePath(templatePath, lang, defLang);
  }

  @Override
  public boolean existsInheritableDocument(DocumentReference docRef, String lang) {
    return existsInheritableDocument(docRef, lang, null);
  }

  @Override
  public boolean existsInheritableDocument(DocumentReference docRef, String lang, String defLang) {
    String templatePath = getInheritedTemplatedPath(docRef);
    _LOGGER.debug("existsInheritableDocument: check content for templatePath [" + templatePath
        + "] and lang [" + lang + "] and defLang [" + defLang + "].");
    if (templatePath.startsWith(":")) {
      return !StringUtils.isEmpty(getTranslatedDiscTemplateContent(templatePath, lang, defLang));
    } else {
      // Template must exist otherwise getInheritedTemplatedPath would have fallen back
      // on disk template path.
      return true;
    }
  }

  private PageLayoutCommand getPageLayoutCmd() {
    if (!getContext().containsKey(LayoutScriptService.CELEMENTS_PAGE_LAYOUT_COMMAND)) {
      getContext().put(LayoutScriptService.CELEMENTS_PAGE_LAYOUT_COMMAND, new PageLayoutCommand());
    }
    return (PageLayoutCommand) getContext().get(LayoutScriptService.CELEMENTS_PAGE_LAYOUT_COMMAND);
  }

  @Deprecated
  @Override
  public String cleanupXHTMLtoHTML5(String xhtml) {
    return cleanupXHTMLtoHTML5(xhtml, getContext().getDoc().getDocumentReference());
  }

  @Deprecated
  @Override
  public String cleanupXHTMLtoHTML5(String xhtml, DocumentReference docRef) {
    return cleanupXHTMLtoHTML5(xhtml, getPageLayoutCmd().getPageLayoutForDoc(docRef));
  }

  @Deprecated
  @Override
  public String cleanupXHTMLtoHTML5(String xhtml, SpaceReference layoutRef) {
    BaseObject layoutObj = getPageLayoutCmd().getLayoutPropertyObj(layoutRef);
    if ((layoutObj != null) && "HTML 5".equals(layoutObj.getStringValue("doctype"))) {
      XHTMLtoHTML5cleanup html5Cleaner = Utils.getComponent(XHTMLtoHTML5cleanup.class);
      return html5Cleaner.cleanAll(xhtml);
    }
    return xhtml;
  }

  @Override
  public List<Attachment> getAttachmentsForDocs(List<String> docsFN) {
    List<Attachment> attachments = new ArrayList<Attachment>();
    for (String docFN : docsFN) {
      try {
        _LOGGER.info("getAttachmentsForDocs: processing doc " + docFN);
        for (XWikiAttachment xwikiAttachment : getContext().getWiki().getDocument(
            resolveDocumentReference(docFN), getContext()).getAttachmentList()) {
          _LOGGER.info("getAttachmentsForDocs: adding attachment " + xwikiAttachment.getFilename()
              + " to list.");
          attachments.add(new Attachment(getContext().getWiki().getDocument(
              resolveDocumentReference(docFN), getContext()).newDocument(getContext()),
              xwikiAttachment, getContext()));
        }
      } catch (XWikiException exp) {
        _LOGGER.error("", exp);
      }
    }
    return attachments;
  }

  @Override
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
    for (String theLang : langList) {
      String templatePath = getTemplatePathOnDisk(renderTemplatePath, theLang);
      try {
        templateContent = getContext().getWiki().getResourceContent(templatePath);
      } catch (FileNotFoundException fnfExp) {
        _LOGGER.trace("FileNotFound [" + templatePath + "].");
        templateContent = "";
      } catch (IOException exp) {
        _LOGGER.debug("Exception while parsing template [" + templatePath + "].", exp);
        templateContent = "";
      }
    }
    if ("".equals(templateContent)) {
      String templatePathDef = getTemplatePathOnDisk(renderTemplatePath);
      try {
        templateContent = getContext().getWiki().getResourceContent(templatePathDef);
      } catch (FileNotFoundException fnfExp) {
        _LOGGER.trace("FileNotFound [" + templatePathDef + "].");
        return "";
      } catch (IOException exp) {
        _LOGGER.debug("Exception while parsing template [" + templatePathDef + "].", exp);
        return "";
      }
    }
    return templateContent;
  }

  private IPageTypeResolverRole getPageTypeResolver() {
    return Utils.getComponent(IPageTypeResolverRole.class);
  }

  @Override
  public void sendCheckJobMail(String jobMailName, String fromAddr, String toAddr,
      List<String> params) {
    _LOGGER.info("sendCheckJobMail started for jobMailName [" + jobMailName + "] fromAdr ["
        + fromAddr + "], toAddr [" + toAddr + "].");
    DocumentReference emailTemplateDocRef = new DocumentReference(getContext().getDatabase(),
        "Mails", jobMailName);
    String lang = "de"; // TODO add multilingual email addresslist
    try {
      String htmlContent = renderInheritableDocument(emailTemplateDocRef, lang);
      if (!emptyChecker.isEmptyRTEString(htmlContent)) {
        CelSendMail sender = new CelSendMail();
        sender.setFrom(fromAddr);
        sender.setReplyTo(fromAddr);
        sender.setTo(toAddr);
        sender.setSubject(getMessageTool(lang).get("job_mail_subject_" + jobMailName, params));
        sender.setHtmlContent(htmlContent, false);
        String textContent = new PlainTextCommand().convertToPlainText(htmlContent);
        sender.setTextContent(textContent);
        int successfulSend = sender.sendMail();
        _LOGGER.debug("sendCheckJobMail ended for [" + toAddr + "] email send [" + successfulSend
            + "].");
      } else {
        _LOGGER.warn("No Email content found for [" + jobMailName + "] [" + emailTemplateDocRef
            + "].");
      }
    } catch (XWikiException exp) {
      _LOGGER.error("Failed to render email template document [" + emailTemplateDocRef + "].", exp);
    }
  }

  @Override
  public WikiReference getCentralWikiRef() {
    return CENTRAL_WIKI_REF;
  }

  @Override
  public EntityType resolveEntityTypeForFullName(String fullName) {
    return resolveEntityTypeForFullName(fullName, null);
  }

  @Override
  public EntityType resolveEntityTypeForFullName(String fullName, EntityType defaultNameType) {
    EntityType ret = null;
    if (StringUtils.isNotBlank(fullName)) {
      if (fullName.matches(REGEX_WORD)) {
        ret = defaultNameType != null ? defaultNameType : EntityType.WIKI;
      } else if (fullName.matches(REGEX_SPACE)) {
        ret = EntityType.SPACE;
      } else if (fullName.matches(REGEX_DOC)) {
        ret = EntityType.DOCUMENT;
      } else if (fullName.matches(REGEX_ATT)) {
        ret = EntityType.ATTACHMENT;
      }
    }
    _LOGGER.debug("resolveEntityTypeForFullName: got '" + ret + "' for fullName '" + fullName
        + "' and default '" + defaultNameType + "'");
    return ret;
  }

  @Override
  public <T> T lookup(Class<T> role) throws ComponentLookupException {
    return componentManager.lookup(role);
  }

  @Override
  public <T> T lookup(Class<T> role, String roleHint) throws ComponentLookupException {
    return componentManager.lookup(role, roleHint);
  }

  @Override
  public <T> List<T> lookupList(Class<T> role) throws ComponentLookupException {
    return componentManager.lookupList(role);
  }

  @Override
  public <T> Map<String, T> lookupMap(Class<T> role) throws ComponentLookupException {
    return componentManager.lookupMap(role);
  }

  @Override
  public DocumentReference checkWikiRef(DocumentReference docRef) {
    return checkWikiRef(docRef, (DocumentReference) null);
  }

  @Override
  public DocumentReference checkWikiRef(DocumentReference docRef, XWikiDocument toDoc) {
    return checkWikiRef(docRef, toDoc.getDocumentReference());
  }

  @Override
  public DocumentReference checkWikiRef(DocumentReference docRef, EntityReference toRef) {
    WikiReference wikiRef = getWikiRef(toRef);
    if (!docRef.getWikiReference().equals(wikiRef)) {
      docRef = new DocumentReference(docRef.getName(), new SpaceReference(docRef
          .getLastSpaceReference().getName(), wikiRef));
    }
    return docRef;
  }

  private IDocumentParentsListerRole getDocumentParentsLister() {
    if (docParentsLister == null) {
      docParentsLister = Utils.getComponent(IDocumentParentsListerRole.class);
    }
    return docParentsLister;
  }

  @Override
  public void setUser(DocumentReference userReference, boolean main) {
    getContext().setUser("XWiki." + userReference.getName(), main);
  }

  @Override
  public DocumentReference setWikiReference(DocumentReference docRef, String wikiName) {
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    return new DocumentReference(wikiName, docRef.getLastSpaceReference().getName(), docRef
        .getName());
  }

  @Override
  public DocumentReference setWikiReference(DocumentReference docRef, WikiReference wikiRef) {
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    SpaceReference spaceRef = new SpaceReference(docRef.getLastSpaceReference().getName(), wikiRef);
    return new DocumentReference(docRef.getName(), spaceRef);
  }

}
