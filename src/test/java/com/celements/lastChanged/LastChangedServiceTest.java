package com.celements.lastChanged;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.model.access.IModelAccessFacade;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class LastChangedServiceTest extends AbstractBridgedComponentTestCase {

  private LastChangedService lastChangedServ;
  private XWikiContext context;
  private QueryManager queryManagerMock;
  private IModelAccessFacade modelAccessMock;
  private IWebUtilsService webUtilsMock;
  private String mySpaceName;
  private SpaceReference spaceRef;

  @Before
  public void setUp_AbstractBridgedComponentTestCase() throws Exception {
    lastChangedServ = (LastChangedService) Utils.getComponent(ILastChangedRole.class);
    context = getContext();
    queryManagerMock = createMockAndAddToDefault(QueryManager.class);
    lastChangedServ.queryManager = queryManagerMock;
    modelAccessMock = createMockAndAddToDefault(IModelAccessFacade.class);
    lastChangedServ.modelAccess = modelAccessMock;
    webUtilsMock = createMockAndAddToDefault(IWebUtilsService.class);
    lastChangedServ.webUtilsService = webUtilsMock;
    mySpaceName = "mySpace";
    spaceRef = new SpaceReference(mySpaceName, new WikiReference(context.getDatabase()));
    expect(webUtilsMock.getDefaultLanguage(eq(spaceRef))).andReturn("de").anyTimes();
  }

  @Test
  public void testInternal_getLastChangeDate_emptyDocumentsList() throws Exception {
    Query mockQuery = createMockAndAddToDefault(Query.class);
    expect(queryManagerMock.createQuery(isA(String.class), eq("xwql"))).andReturn(mockQuery).once();
    expect(mockQuery.bindValue(eq("spaceName"), eq(mySpaceName))).andReturn(mockQuery).once();
    expect(mockQuery.setLimit(eq(1))).andReturn(mockQuery).once();
    expect(mockQuery.execute()).andReturn(Collections.emptyList()).once();
    replayDefault();
    Date lastUpdated = lastChangedServ.internal_getLastChangeDate(spaceRef);
    verifyDefault();
    assertNull(lastUpdated);
    assertTrue(lastChangedServ.lastUpdatedWikiCache.isEmpty());
    assertTrue(lastChangedServ.lastUpdatedSpaceCache.isEmpty());
  }

  @Test
  public void testInternal_getLastChangeDate_emptyLangResponse() throws Exception {
    Query mockQuery = createMockAndAddToDefault(Query.class);
    expect(queryManagerMock.createQuery(isA(String.class), eq("xwql"))).andReturn(mockQuery).once();
    expect(mockQuery.bindValue(eq("spaceName"), eq(mySpaceName))).andReturn(mockQuery).once();
    expect(mockQuery.setLimit(eq(1))).andReturn(mockQuery).once();
    DocumentReference docRef = new DocumentReference("myDoc", spaceRef);
    String firstDocFN = mySpaceName + ".myDoc";
    expect(webUtilsMock.resolveDocumentReference(eq(firstDocFN))).andReturn(docRef).anyTimes();
    Object[] firstRow = new Object[] { firstDocFN, "" };
    List<Object[]> testResult = Arrays.<Object[]>asList(firstRow);
    expect(mockQuery.<Object[]>execute()).andReturn(testResult).once();
    expect(modelAccessMock.exists(eq(docRef))).andReturn(true).anyTimes();
    XWikiDocument doc = new XWikiDocument(docRef);
    Date expectedUpdated = new Date();
    doc.setDate(expectedUpdated);
    expect(modelAccessMock.getDocument(eq(docRef), eq("de"))).andReturn(doc).once();
    replayDefault();
    Date lastUpdated = lastChangedServ.internal_getLastChangeDate(spaceRef);
    verifyDefault();
    assertEquals(expectedUpdated, lastUpdated);
  }

  @Test
  public void testInternal_getLastChangeDate_nullLangResponse() throws Exception {
    String mySpaceName = "mySpace";
    SpaceReference spaceRef = new SpaceReference(mySpaceName, new WikiReference(
        context.getDatabase()));
    Query mockQuery = createMockAndAddToDefault(Query.class);
    expect(queryManagerMock.createQuery(isA(String.class), eq("xwql"))).andReturn(mockQuery).once();
    expect(mockQuery.bindValue(eq("spaceName"), eq(mySpaceName))).andReturn(mockQuery).once();
    expect(mockQuery.setLimit(eq(1))).andReturn(mockQuery).once();
    DocumentReference docRef = new DocumentReference("myDoc", spaceRef);
    String firstDocFN = mySpaceName + ".myDoc";
    expect(webUtilsMock.resolveDocumentReference(eq(firstDocFN))).andReturn(docRef).anyTimes();
    Object[] firstRow = new Object[] { firstDocFN, null };
    List<Object[]> testResult = Arrays.<Object[]>asList(firstRow);
    expect(mockQuery.<Object[]>execute()).andReturn(testResult).once();
    XWikiDocument doc = new XWikiDocument(docRef);
    Date expectedUpdated = new Date();
    doc.setDate(expectedUpdated);
    expect(modelAccessMock.getDocument(eq(docRef), eq("de"))).andReturn(doc).once();
    replayDefault();
    Date lastUpdated = lastChangedServ.internal_getLastChangeDate(spaceRef);
    verifyDefault();
    assertEquals(expectedUpdated, lastUpdated);
  }

}
