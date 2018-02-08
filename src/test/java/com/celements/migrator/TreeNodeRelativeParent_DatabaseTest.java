package com.celements.migrator;

import static org.easymock.EasyMock.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.celements.migrations.celSubSystem.ICelementsMigrator;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiHibernateBaseStore.HibernateCallback;
import com.xpn.xwiki.store.XWikiHibernateStore;

public class TreeNodeRelativeParent_DatabaseTest
    extends AbstractBridgedComponentTestCase {

  private TreeNodeRelativeParent_Database migrator;
  private XWiki xwiki;
  private XWikiContext context;

  @Before
  public void setUp_TreeNodeRelativeParent_DatabaseTest() throws Exception {
    migrator = (TreeNodeRelativeParent_Database) getComponentManager().lookup(
        ICelementsMigrator.class, "TreeNodeRelativeParent_Database");
    xwiki = getWikiMock();
    context = getContext();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMigrateXWikiMigrationManagerInterfaceXWikiContext() throws Exception {
    SubSystemHibernateMigrationManager manager = createMockAndAddToDefault(
        SubSystemHibernateMigrationManager.class);
    XWikiHibernateStore hibStoreMock = createMockAndAddToDefault(
        XWikiHibernateStore.class);
    List<String> resultList = Arrays.asList("MySpace.MyDoc");
    expect(hibStoreMock.executeRead(same(context), eq(true), isA(HibernateCallback.class)
        )).andReturn(resultList);
    expect(xwiki.getHibernateStore()).andReturn(hibStoreMock).once();
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDoc");
    XWikiDocument xwikiDoc = new XWikiDocument(docRef);
    EntityReference entityReference = new EntityReference("MyParent", EntityType.DOCUMENT,
        new EntityReference("MySpace", EntityType.SPACE, new EntityReference(
            context.getDatabase(), EntityType.WIKI)));
    xwikiDoc.setParentReference(entityReference);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(xwikiDoc);
    xwiki.saveDocument(same(xwikiDoc), eq("TreeNodeRelativeParent_Database Migration"),
        same(context));
    expectLastCall().once();
    replayDefault();
    migrator.migrate(manager, context);
    verifyDefault();
  }

}
