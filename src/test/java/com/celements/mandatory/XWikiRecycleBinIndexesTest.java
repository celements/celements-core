package com.celements.mandatory;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.query.IQueryExecutionServiceRole;
import com.xpn.xwiki.web.Utils;

public class XWikiRecycleBinIndexesTest extends AbstractComponentTest {

  private XWikiRecycleBinIndexes xWikiRecycleBinIndexes;

  private IQueryExecutionServiceRole queryExecServiceMock;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    xWikiRecycleBinIndexes = (XWikiRecycleBinIndexes) Utils.getComponent(
        IMandatoryDocumentRole.class, XWikiRecycleBinIndexes.NAME);
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
    expect(queryExecServiceMock.existsConstraint("prefix_db", "xwikirecyclebin",
        "dateIDX")).andReturn(true).once();
    expect(queryExecServiceMock.executeWriteSQL(sql)).andReturn(0).once();

    replayDefault();
    xWikiRecycleBinIndexes.checkDocuments();
    verifyDefault();
  }

  @Test
  public void testCheckDocuments_noContraintExists() throws Exception {
    expect(queryExecServiceMock.existsConstraint("prefix_db", "xwikirecyclebin",
        "dateIDX")).andReturn(false).once();

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
