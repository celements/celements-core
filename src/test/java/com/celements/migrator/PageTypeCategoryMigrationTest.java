package com.celements.migrator;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.migrator.PageTypeCategoryMigration.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;

import com.celements.common.test.AbstractComponentTest;
import com.celements.configuration.CelementsFromWikiConfigurationSource;
import com.celements.migrations.celSubSystem.ICelementsMigrator;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.pagetype.classes.PageTypePropertiesClass;
import com.celements.query.IQueryExecutionServiceRole;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class PageTypeCategoryMigrationTest extends AbstractComponentTest {

  PageTypeCategoryMigration migration;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(ConfigurationSource.class, CelementsFromWikiConfigurationSource.NAME,
        getConfigurationSource());
    registerComponentMocks(IQueryExecutionServiceRole.class, IModelAccessFacade.class);
    migration = (PageTypeCategoryMigration) Utils.getComponent(ICelementsMigrator.class,
        PageTypeCategoryMigration.NAME);
    expectClass(getClassDef(), new WikiReference("xwikidb"));
  }

  private static ClassDefinition getClassDef() {
    return Utils.getComponent(ClassDefinition.class, PageTypePropertiesClass.CLASS_DEF_HINT);
  }

  private static BaseClass expectClass(ClassDefinition classDef, WikiReference wikiRef)
      throws XWikiException {
    BaseClass bClass = createBaseClassMock(classDef.getDocRef(wikiRef));
    for (ClassField<?> field : classDef.getFields()) {
      expect(bClass.get(field.getName())).andReturn(field.getXField()).anyTimes();
    }
    return bClass;
  }

  @Test
  public void test_getName() {
    assertEquals(PageTypeCategoryMigration.NAME, migration.getName());
  }

  @Test
  public void test_getXwql() {
    assertEquals("from doc.object(Celements2.PageTypeProperties) prop", migration.getXwql());
  }

  @Test
  public void test_migrate() throws Exception {
    XWikiDocument doc1 = expectPtDoc(new DocumentReference("xwikidb", "PageTypes", "pt1"), null);
    XWikiDocument doc2 = expectPtDoc(new DocumentReference("xwikidb", "PageTypes", "pt2"), "asdf");
    XWikiDocument doc3 = expectPtDoc(new DocumentReference("xwikidb", "PageTypes", "pt3"), "  ");
    expect(getMock(IQueryExecutionServiceRole.class).executeAndGetDocRefs(anyObject(Query.class)))
        .andReturn(Arrays.asList(doc1.getDocumentReference(), doc2.getDocumentReference(),
            doc3.getDocumentReference()));
    getMock(IModelAccessFacade.class).saveDocument(same(doc1), eq(migration.getName()));
    getMock(IModelAccessFacade.class).saveDocument(same(doc3), eq(migration.getName()));

    replayDefault();
    migration.migrate(null, getContext());
    verifyDefault();

    assertEquals("migration should set null", "pageType", doc1.getStringValue(FIELD.getName()));
    assertEquals("migration shouldn't overwrite", "asdf", doc2.getStringValue(FIELD.getName()));
    assertEquals("migration should set blank", "pageType", doc3.getStringValue(FIELD.getName()));
  }

  private XWikiDocument expectPtDoc(DocumentReference docRef, String ptCategory)
      throws DocumentNotExistsException {
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setNew(false);
    BaseObject xObj = new BaseObject();
    xObj.setDocumentReference(docRef);
    xObj.setXClassReference(getClassDef().getClassReference());
    xObj.setStringValue(FIELD.getName(), ptCategory);
    doc.addXObject(xObj);
    expect(getMock(IModelAccessFacade.class).getDocument(docRef)).andReturn(doc);
    return doc;
  }
}
