package com.celements.nextfreedoc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAlreadyExistsException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.util.References;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Singleton
@Service
public class NextFreeDocService implements INextFreeDocRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(NextFreeDocService.class);

  // TODO refactor to org.xwiki.cache.CacheManager
  private final Map<DocumentReference, Long> numCache = new HashMap<>();

  private final QueryManager queryManager;

  private final IModelAccessFacade modelAccess;

  private final Execution execution;

  @Inject
  public NextFreeDocService(QueryManager queryManager, IModelAccessFacade modelAccess,
      Execution execution) {
    super();
    this.queryManager = queryManager;
    this.modelAccess = modelAccess;
    this.execution = execution;
  }

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public DocumentReference getNextTitledPageDocRef(SpaceReference spaceRef, String title) {
    DocumentReference baseDocRef = new DocumentReference(title, spaceRef);
    return createDocRef(baseDocRef, getNextTitledPageNum(baseDocRef));
  }

  @Override
  public DocumentReference getNextUntitledPageDocRef(SpaceReference spaceRef) {
    return getNextTitledPageDocRef(spaceRef, UNTITLED_NAME);
  }

  private synchronized long getNextTitledPageNum(DocumentReference baseDocRef) {
    long num = getHighestNum(baseDocRef);
    while (!isAvailableDocRef(createDocRef(baseDocRef, num))) {
      num += 1;
    }
    numCache.put(baseDocRef, num + 1);
    return num;
  }

  private boolean isAvailableDocRef(DocumentReference newDocRef) {
    try {
      XWikiDocument doc = modelAccess.createDocument(newDocRef);
      if (doc.getLock(getContext()) == null) {
        // TODO use (not yet existing) ModelLockService.aquireLock(doc) for cluster concurrency
        // safety
        doc.setLock(getContext().getUser(), getContext());
        return true;
      }
    } catch (DocumentAlreadyExistsException exp) {
      LOGGER.trace("New docRef already exists '{}'", newDocRef, exp);
    } catch (XWikiException | DocumentLoadException exp) {
      LOGGER.error("Failed to check new docRef '{}'", newDocRef, exp);
    }
    return false;
  }

  private DocumentReference createDocRef(DocumentReference baseDocRef, long num) {
    String name = new StringBuilder(baseDocRef.getName()).append(num).toString();
    SpaceReference parent = References.extractRef(baseDocRef, SpaceReference.class).get();
    return References.create(DocumentReference.class, name, parent);
  }

  /**
   * NOTE: only use in synchronized context due to numCache
   *
   * @param baseDocRef
   * @return
   */
  long getHighestNum(DocumentReference baseDocRef) {
    Long num = numCache.get(baseDocRef);
    try {
      int offset = 0, limit = 8;
      List<Object> results;
      while ((num == null) && ((results = getHighestNumQuery(baseDocRef, offset,
          limit).execute()).size() > 0)) {
        num = extractNumFromResults(baseDocRef.getName(), results);
        offset += results.size();
        limit *= 2;
      }
    } catch (QueryException queryExc) {
      LOGGER.error("Error executing query '{}'", getHighestNumHQL(), queryExc);
    }
    if (num == null) {
      num = 1L;
    }
    LOGGER.debug("getHighestNum: for baseDocRef '{}' got '{}'", baseDocRef, num);
    return num;
  }

  private Query getHighestNumQuery(DocumentReference baseDocRef, int offset, int limit)
      throws QueryException {
    Query query = queryManager.createQuery(getHighestNumHQL(), Query.HQL);
    query.setOffset(offset);
    query.setLimit(limit);
    query.setWiki(baseDocRef.getWikiReference().getName());
    query.bindValue("space", baseDocRef.getLastSpaceReference().getName());
    query.bindValue("name", baseDocRef.getName() + "%");
    return query;
  }

  String getHighestNumHQL() {
    return "SELECT doc.name FROM XWikiDocument doc WHERE doc.space=:space "
        + "AND doc.name LIKE :name ORDER BY LENGTH(doc.name) DESC, doc.name DESC";
  }

  Long extractNumFromResults(String prefix, List<Object> results) {
    Long num = null;
    for (Object name : results) {
      try {
        if (name.toString().startsWith(prefix)) {
          String numStr = name.toString().substring(prefix.length());
          num = Long.parseLong(numStr) + 1;
          break;
        } else {
          LOGGER.warn("extractNumFromResults: given name '{}' does not start with expected"
              + " prefix '{}'", name, prefix);
        }
      } catch (NumberFormatException nfExc) {
        LOGGER.debug("extractNumFromResults: unable to parse digit in '{}'", name);
      }
    }
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("extractNumFromResults: got '{}' from docs: {}", num, results);
    }
    return num;
  }

  /**
   * USE FOR TEST PURPOSES ONLY
   */
  public void injectNum(SpaceReference spaceRef, String title, long num) {
    DocumentReference baseDocRef = new DocumentReference(title, spaceRef);
    numCache.put(baseDocRef, num);
  }

  @Override
  public @NotNull DocumentReference getNextRandomPageDocRef(@NotNull SpaceReference spaceRef,
      int lengthOfRandomAlphanumeric, String prefix) {
    Preconditions.checkNotNull(spaceRef, "SpaceReference cannot be null.");
    Preconditions.checkArgument(lengthOfRandomAlphanumeric > 3,
        "Parameter int lengthOfRandomAlphanumeric has to be > 3");
    prefix = Strings.nullToEmpty(prefix);
    DocumentReference docRef = null;
    do {
      String newPageName = prefix
          + RandomStringUtils.randomAlphanumeric(lengthOfRandomAlphanumeric);
      docRef = new DocumentReference(newPageName, spaceRef);
    } while (modelAccess.exists(docRef));
    return docRef;
  }

}
