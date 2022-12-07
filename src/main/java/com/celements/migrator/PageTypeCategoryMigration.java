package com.celements.migrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAccessException;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.classes.PageTypePropertiesClass;
import com.celements.query.IQueryExecutionServiceRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.migration.XWikiDBVersion;

@Component(PageTypeCategoryMigration.NAME)
public class PageTypeCategoryMigration extends AbstractCelementsHibernateMigrator {

  static final Logger LOGGER = LoggerFactory.getLogger(PageTypeCategoryMigration.class);

  public static final String NAME = "PageTypeCategoryMigration";

  static final ClassField<String> FIELD = PageTypePropertiesClass.PAGETYPE_PROP_CATEGORY;

  @Requirement
  private QueryManager queryManager;

  @Requirement
  private IQueryExecutionServiceRole queryExecutor;

  @Requirement
  private IPageTypeCategoryRole ptDefaultCategory;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return "sets default page type category for empty ones";
  }

  /**
   * getVersion is using days since 1.1.2010 until the day of committing this migration
   * http://www.wolframalpha.com/input/?i=days+since+01.01.2010
   */
  @Override
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(3421);
  }

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context)
      throws XWikiException {
    String database = context.getDatabase();
    LOGGER.info("[{}] migrate empty page type categories", database);
    try {
      Query query = queryManager.createQuery(getXwql(), Query.XWQL);
      for (DocumentReference docRef : queryExecutor.executeAndGetDocRefs(query)) {
        setCategoryIfAbsent(docRef);
      }
    } catch (Exception exc) {
      LOGGER.error("[{}] failed migrating empty page type categories", database, exc);
      throw new XWikiException(0, 0, "migration failed", exc);
    }
  }

  String getXwql() {
    return "from doc.object(" + FIELD.getClassReference().serialize() + ") prop";
  }

  private void setCategoryIfAbsent(DocumentReference docRef) throws DocumentAccessException {
    XWikiDocument doc = modelAccess.getDocument(docRef);
    XWikiObjectEditor editor = XWikiObjectEditor.on(doc).filterAbsent(FIELD);
    if (editor.editField(FIELD).first(ptDefaultCategory.getTypeName())) {
      modelAccess.saveDocument(doc, getName());
      LOGGER.info("migrated [{}]", docRef);
    } else {
      LOGGER.debug("skipped [{}]", docRef);
    }
  }

}
