package com.celements.mandatory;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.query.IQueryExecutionServiceRole;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.web.Utils;

public class XWikiRecycleBinIndexesTest extends AbstractComponentTest {

  private XWikiRecycleBinIndexes xWikiRecycleBinIndexes;

  private IQueryExecutionServiceRole queryExecServiceMock;

  private XWiki xwiki;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    xWikiRecycleBinIndexes = (XWikiRecycleBinIndexes) Utils.getComponent(
        IMandatoryDocumentRole.class, XWikiRecycleBinIndexes.NAME);
    xwiki = getWikiMock();
    queryExecServiceMock = createMockAndAddToDefault(IQueryExecutionServiceRole.class);
    xWikiRecycleBinIndexes.queryExecService = queryExecServiceMock;
  }

  @Test
  public void testDependsOnMandatoryDocuments() throws Exception {
    assertEquals(0, xWikiRecycleBinIndexes.dependsOnMandatoryDocuments().size());
  }

  @Test
  public void testCheckDocuments() throws Exception {
    String sql = xWikiRecycleBinIndexes.getSQLIndexToRecycleBin();
    String prefixParam = "xwiki.db.prefix";
    expect(xwiki.Param(eq(prefixParam))).andReturn("prefix_").atLeastOnce();
    expect(queryExecServiceMock.existsIndex("prefix_xwikidb", "xwikirecyclebin",
        "dateIDX")).andReturn(false).atLeastOnce();
    expect(queryExecServiceMock.executeWriteSQL(sql)).andReturn(0).once();

    replayDefault();
    xWikiRecycleBinIndexes.checkDocuments();
    verifyDefault();
  }

  @Test
  public void testCheckDocuments_noContraintExists() throws Exception {
    String prefixParam = "xwiki.db.prefix";
    expect(xwiki.Param(eq(prefixParam))).andReturn("prefix_").atLeastOnce();
    expect(queryExecServiceMock.existsIndex("prefix_xwikidb", "xwikirecyclebin",
        "dateIDX")).andReturn(true).atLeastOnce();

    replayDefault();
    xWikiRecycleBinIndexes.checkDocuments();
    verifyDefault();
  }

  @Test
  public void testGetSQLXWikiDocument() throws Exception {
    String expSQL = "alter table xwikirecyclebin add index `dateIDX` (XDD_DATE, XDD_ID);";
    assertEquals(expSQL, xWikiRecycleBinIndexes.getSQLIndexToRecycleBin());
  }
}
