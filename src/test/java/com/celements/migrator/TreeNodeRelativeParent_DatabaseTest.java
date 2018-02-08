package com.celements.migrator;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.celements.migrations.celSubSystem.ICelementsMigrator;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public class TreeNodeRelativeParent_DatabaseTest extends AbstractComponentTest {

  private TreeNodeRelativeParent_Database migrator;
  private XWiki xwiki;
  private XWikiContext context;
  private QueryManager queryManagerMock;

  @Before
  public void prepareTest() throws Exception {
    queryManagerMock = registerComponentMock(QueryManager.class);
    migrator = (TreeNodeRelativeParent_Database) getComponentManager().lookup(
        ICelementsMigrator.class, "TreeNodeRelativeParent_Database");
    xwiki = getWikiMock();
    context = getContext();
  }

  @Test
  public void testMigrateXWikiMigrationManagerInterfaceXWikiContext() throws Exception {
    SubSystemHibernateMigrationManager manager = createMockAndAddToDefault(
        SubSystemHibernateMigrationManager.class);
    Query queryMock = createMockAndAddToDefault(Query.class);
    expect(queryManagerMock.createQuery(anyObject(String.class), eq(Query.XWQL))).andReturn(
        queryMock);
    expect(queryMock.bindValue("buggyParent", "xwikidb:%")).andReturn(queryMock);
    List<String> resultList = Arrays.asList("MySpace.MyDoc");
    expect(queryMock.<String>execute()).andReturn(resultList);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace", "MyDoc");
    XWikiDocument xwikiDoc = new XWikiDocument(docRef);
    EntityReference entityReference = new EntityReference("MyParent", EntityType.DOCUMENT,
        new EntityReference("MySpace", EntityType.SPACE, new EntityReference(context.getDatabase(),
            EntityType.WIKI)));
    xwikiDoc.setParentReference(entityReference);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(xwikiDoc);
    xwiki.saveDocument(same(xwikiDoc), eq("TreeNodeRelativeParent_Database Migration"), same(
        context));
    expectLastCall().once();
    replayDefault();
    migrator.migrate(manager, context);
    verifyDefault();
  }

}
