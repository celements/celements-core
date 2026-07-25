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
package com.celements.rights.publication;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.model.context.ModelContext;
import com.celements.web.classcollections.DocumentDetailsClasses;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.CelDocument;
import com.xpn.xwiki.doc.CelObject;

@Component
public class PublicationService implements IPublicationServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(PublicationService.class);

  @Requirement(DocumentDetailsClasses.NAME)
  IClassCollectionRole documentDetailsClasses;

  @Requirement
  private DocumentAccessBridge documentAccessBridge;

  @Requirement
  private Execution execution;

  @Requirement
  private ModelContext modelContext;

  /**
   * @deprecated instead use {@link #modelContext}
   */
  @Deprecated
  private XWikiContext getContext() {
    return modelContext.getXWikiContext();
  }

  @Override
  public boolean isPubUnpubOverride() {
    EPubUnpub val = getPubUnpubFromContext();
    return (EPubUnpub.PUBLISHED == val) || (EPubUnpub.UNPUBLISHED == val);
  }

  @Override
  public boolean isPubOverride() {
    return EPubUnpub.PUBLISHED == getPubUnpubFromContext();
  }

  @Override
  public boolean isUnpubOverride() {
    return EPubUnpub.UNPUBLISHED == getPubUnpubFromContext();
  }

  EPubUnpub getPubUnpubFromContext() {
    EPubUnpub val = null;
    Object valObj = execution.getContext().getProperty(OVERRIDE_PUB_CHECK);
    if ((valObj != null) && (valObj instanceof EPubUnpub)) {
      val = (EPubUnpub) execution.getContext().getProperty(OVERRIDE_PUB_CHECK);
    }
    return val;
  }

  @Override
  public void overridePubUnpub(EPubUnpub value) {
    execution.getContext().setProperty(OVERRIDE_PUB_CHECK, value);
  }

  List<CelObject> getPublishObjects(CelDocument.Default doc) {
    if (doc == null) {
      return List.of();
    }
    var classRef = getPublicationClassReference();
    return doc.getXObjects().stream()
        .filter(object -> object.getClassReference().equals(classRef))
        .toList();
  }

  LocalDocumentReference getPublicationClassReference() {
    return new LocalDocumentReference(((DocumentDetailsClasses) documentDetailsClasses)
        .getDocumentPublicationClassRef(modelContext.getWikiRef().getName()));
  }

  @Override
  public boolean isRestrictedRightsAction(String accessLevel) {
    return "view".equals(accessLevel) || "comment".equals(accessLevel);
  }

  @Override
  public boolean isPublishActive() {
    DocumentReference forDocRef = null;
    if (documentAccessBridge.getCurrentDocumentReference() != null) {
      forDocRef = documentAccessBridge.getCurrentDocumentReference();
    }
    return isPublishActive(forDocRef);
  }

  @Override
  public boolean isPublishActive(DocumentReference docRef) {
    String space = null;
    if (docRef != null) {
      space = docRef.getLastSpaceReference().getName();
    }
    String isActive = getContext().getWiki().getSpacePreference("publishdate_active", space, "-1",
        getContext());
    if ("-1".equals(isActive)) {
      isActive = getContext().getWiki().getXWikiPreference("publishdate_active",
          "celements.publishdate.active", "0", getContext());
    }
    return "1".equals(isActive);
  }

  @Override
  public boolean isPublished(CelDocument.Default doc) {
    List<CelObject> objects = getPublishObjects(doc);
    if (objects.isEmpty()) {
      LOGGER.debug("no publish objects found for '{}': no limits set means always published", doc);
      return true;
    }
    return objects.stream().anyMatch(object -> isAfterStart(object) && isBeforeEnd(object));
  }

  boolean isAfterStart(CelObject object) {
    Instant publishDate = object.getDateValue(DocumentDetailsClasses.PUBLISH_DATE_FIELD);
    return (publishDate == null) || Instant.now().isAfter(publishDate);
  }

  boolean isBeforeEnd(CelObject object) {
    Instant unpublishDate = object.getDateValue(DocumentDetailsClasses.UNPUBLISH_DATE_FIELD);
    return (unpublishDate == null) || Instant.now().isBefore(unpublishDate);
  }

}
