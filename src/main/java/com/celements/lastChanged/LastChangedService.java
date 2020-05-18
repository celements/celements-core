package com.celements.lastChanged;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAccessException;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * TODO write unit tests
 */
@Component
public class LastChangedService implements ILastChangedRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(LastChangedService.class);

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  QueryManager queryManager;

  @Requirement
  IModelAccessFacade modelAccess;

  final Map<WikiReference, Date> lastUpdatedWikiCache = new ConcurrentHashMap<>();
  final Map<SpaceReference, Date> lastUpdatedSpaceCache = new ConcurrentHashMap<>();

  synchronized void invalidateCacheForSpaceRef(SpaceReference spaceRef) {
    if (hasSpaceRestriction(spaceRef)) {
      lastUpdatedSpaceCache.remove(spaceRef);
      lastUpdatedWikiCache.remove(webUtilsService.getWikiRef(spaceRef));
    }
  }

  @Override
  public Date getLastUpdatedDate() {
    return getLastUpdatedDate((SpaceReference) null);
  }

  @Override
  public Date getLastUpdatedDate(List<SpaceReference> spaceRefList) {
    if (spaceRefList.isEmpty()) {
      return getLastUpdatedDate();
    } else {
      Date lastUpdatedDate = null;
      for (SpaceReference spaceRef : spaceRefList) {
        Date spaceLastUpdatedDate = getLastUpdatedDate(spaceRef);
        if ((lastUpdatedDate == null) || lastUpdatedDate.before(spaceLastUpdatedDate)) {
          lastUpdatedDate = spaceLastUpdatedDate;
        }
      }
      return lastUpdatedDate;
    }
  }

  @Override
  public Date getLastUpdatedDate(SpaceReference spaceRef) {
    Date date = null;
    if (hasSpaceRestriction(spaceRef)) {
      date = lastUpdatedSpaceCache.get(spaceRef);
    } else {
      date = lastUpdatedWikiCache.get(webUtilsService.getWikiRef());
    }
    if (date == null) {
      date = internal_getLastChangeDate(spaceRef);
    }
    return date;
  }

  Date internal_getLastChangeDate(SpaceReference spaceRef) {
    Date lastChangedDate = null;
    List<Object[]> lastChangedDocuments = getLastChangedDocuments(1, spaceRef);
    if (lastChangedDocuments.size() > 0) {
      Object[] firstRow = lastChangedDocuments.get(0);
      String lastChangedDocFN = firstRow[0].toString();
      String lastChangedDocLang = "";
      if (firstRow[1] != null) {
        lastChangedDocLang = firstRow[1].toString();
      }
      DocumentReference lastChangedDocRef = webUtilsService.resolveDocumentReference(
          lastChangedDocFN);
      XWikiDocument lastChangedDoc;
      try {
        if (Strings.isNullOrEmpty(lastChangedDocLang)) {
          lastChangedDocLang = webUtilsService.getDefaultLanguage(spaceRef);
        }
        lastChangedDoc = modelAccess.getDocument(lastChangedDocRef, lastChangedDocLang);
        lastChangedDate = lastChangedDoc.getDate();
        updateCachedDate(spaceRef, lastChangedDate, lastChangedDocRef);
      } catch (DocumentAccessException exp) {
        LOGGER.error("Failed to load last updated document '{}', '{}'.", lastChangedDocRef,
            lastChangedDocLang, exp);
      }
    } else {
      LOGGER.info("internal_getLastChangeDate: empty lastChangedDocuments list for" + " space '{}'",
          spaceRef);
    }
    LOGGER.debug("internal_getLastChangeDate: return '{}' for space '{}'", lastChangedDate,
        spaceRef);
    return lastChangedDate;
  }

  synchronized void updateCachedDate(SpaceReference spaceRef, Date lastChangedDate,
      DocumentReference lastChangedDocRef) {
    if (lastChangedDate != null) {
      if (hasSpaceRestriction(spaceRef)) {
        lastUpdatedSpaceCache.put(spaceRef, lastChangedDate);
      } else {
        lastUpdatedWikiCache.put(webUtilsService.getWikiRef(), lastChangedDate);
        lastUpdatedSpaceCache.put(lastChangedDocRef.getLastSpaceReference(), lastChangedDate);
      }
    }
  }

  @Override
  public List<Object[]> getLastChangedDocuments(int numEntries) {
    return getLastChangedDocuments(numEntries, "");
  }

  boolean hasSpaceRestriction(SpaceReference spaceRef) {
    return (spaceRef != null) && !Strings.isNullOrEmpty(spaceRef.getName());
  }

  @Override
  public List<Object[]> getLastChangedDocuments(int numEntries, SpaceReference spaceRef) {
    if (hasSpaceRestriction(spaceRef)) {
      return getLastChangedDocuments(numEntries, spaceRef.getName());
    } else {
      return getLastChangedDocuments(numEntries, "");
    }
  }

  @Override
  @Deprecated
  public List<Object[]> getLastChangedDocuments(int numEntries, String space) {
    String xwql = "select doc.fullName, doc.language from XWikiDocument doc";
    boolean hasSpaceRestriction = (!"".equals(space));
    if (hasSpaceRestriction) {
      xwql = xwql + " where doc.space = :spaceName";
    }
    xwql = xwql + " order by doc.date desc";
    Query query;
    try {
      query = queryManager.createQuery(xwql, Query.XWQL);
      if (hasSpaceRestriction) {
        query = query.bindValue("spaceName", space);
      }
      return query.setLimit(numEntries).execute();
    } catch (QueryException exp) {
      LOGGER.error("Failed to create whats-new query for space [" + space + "].", exp);
    }
    return Collections.emptyList();
  }

}
