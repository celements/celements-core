package com.celements.migrator;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.celements.auth.user.UserPageType;
import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAccessException;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.query.IQueryExecutionServiceRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.migration.XWikiDBVersion;

@Component
public class UserPageTypeMigration extends AbstractCelementsHibernateMigrator {

  static final Logger LOGGER = LoggerFactory.getLogger(UserPageTypeMigration.class);

  public static final String NAME = "UserPageTypeMigration";

  private final QueryManager queryManager;

  private final IQueryExecutionServiceRole queryExecutor;

  private final IPageTypeCategoryRole ptDefaultCategory;

  private final IModelAccessFacade modelAccess;

  @Inject
  public UserPageTypeMigration(QueryManager queryManager, IQueryExecutionServiceRole queryExecutor,
      IPageTypeCategoryRole ptDefaultCategory, IModelAccessFacade modelAccess) {
    super();
    this.queryManager = queryManager;
    this.queryExecutor = queryExecutor;
    this.ptDefaultCategory = ptDefaultCategory;
    this.modelAccess = modelAccess;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return "sets PageType for all existing Users to UserPageType";
  }

  /**
   * getVersion is using days since 1.1.2010 until the day of committing this migration
   * https://www.wolframalpha.com/input/?i=days+since+01.01.2010
   */
  @Override
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(4800);
  }

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context)
      throws XWikiException {
    String database = context.getDatabase();
    LOGGER.info("[{}] migrate existing users and add UserPageType", database);
    try {
      Query query = queryManager.createQuery(getXwql(), Query.XWQL);
      for (DocumentReference docRef : queryExecutor.executeAndGetDocRefs(query)) {
        setPageTypeIfAbsent(docRef);
      }
    } catch (Exception exc) {
      LOGGER.error("[{}] Migration failed: adding UserPageType to existing users", database, exc);
      throw new XWikiException(0, 0, "migration failed", exc);
    }
  }

  String getXwql() {
    return "from doc.object(XWiki.XWikiUsers) usr";
  }

  private void setPageTypeIfAbsent(DocumentReference docRef) throws DocumentAccessException {
    XWikiDocument doc = modelAccess.getDocument(docRef);
    XWikiObjectEditor editor = XWikiObjectEditor.on(doc).filter(PageTypeClass.CLASS_REF);
    editor.createFirstIfNotExists();
    if (editor.editField(PageTypeClass.FIELD_PAGE_TYPE).first(UserPageType.PAGETYPE_NAME)) {
      modelAccess.saveDocument(doc, getName());
      LOGGER.info("migrated [{}]", docRef);
    } else {
      LOGGER.debug("skipped [{}]", docRef);
    }
  }

}
