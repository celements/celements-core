package com.celements.nextfreedoc;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryExecutor;
import org.xwiki.query.QueryManager;
import org.xwiki.query.internal.DefaultQuery;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class NextFreeDocServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private NextFreeDocService nextFreeDocService;
  
  private QueryManager queryManagerMock;
  private QueryExecutor queryExecutorMock;

  @Before
  public void setUp_NextFreeDocNameCommandTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    nextFreeDocService = (NextFreeDocService) Utils.getComponent(INextFreeDocRole.class);
    queryManagerMock = createMockAndAddToDefault(QueryManager.class);
    nextFreeDocService.injectQueryManager(queryManagerMock);
    queryExecutorMock = createMockAndAddToDefault(QueryExecutor.class);
  }

  @Test
  public void testGetNextTitledPageDocRef() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference(
        context.getDatabase()));
    String title = "asdf";
    nextFreeDocService.injectNum(spaceRef, title, 5);
    
    DocumentReference docRef1 = new DocumentReference(title + 5, spaceRef);
    expect(xwiki.exists(eq(docRef1), same(context))).andReturn(true).once();
    
    DocumentReference docRef2 = new DocumentReference(title + 6, spaceRef);
    expect(xwiki.exists(eq(docRef2), same(context))).andReturn(false).once();
    expect(xwiki.getDocument(eq(docRef2), same(context))).andThrow(new XWikiException()
        ).once();
    
    DocumentReference docRef3 = new DocumentReference(title + 7, spaceRef);
    expect(xwiki.exists(eq(docRef3), same(context))).andReturn(false).once();
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(docRef3), same(context))).andReturn(docMock).once();
    expect(docMock.getLock(same(context))).andReturn(null);
    
    replayDefault();
    DocumentReference ret = nextFreeDocService.getNextTitledPageDocRef(spaceRef, title);
    verifyDefault();
    
    assertEquals(docRef3, ret);
  }

  @Test
  public void testGetNextTitledPageDocRef_nullSpace() throws Exception {
    SpaceReference spaceRef = null;
    String title = "asdf";
    
    replayDefault();
    try {
      nextFreeDocService.getNextTitledPageDocRef(spaceRef, title);
      fail();
    } catch (IllegalArgumentException exp) {
      //expected
    }
    verifyDefault();
  }

  @Test
  public void testGetNextTitledPageDocRef_nullTitle() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference(
        context.getDatabase()));
    String title = "";
    
    replayDefault();
    try {
      nextFreeDocService.getNextTitledPageDocRef(spaceRef, title);
      fail();
    } catch (IllegalArgumentException exp) {
      //expected
    }
    verifyDefault();
  }

  @Test
  public void testGetNextUntitledPageDocRef() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference(
        context.getDatabase()));
    nextFreeDocService.injectNum(spaceRef, INextFreeDocRole.UNTITLED_NAME, 5);
    
    DocumentReference docRef1 = new DocumentReference(INextFreeDocRole.UNTITLED_NAME + 5, 
        spaceRef);
    expect(xwiki.exists(eq(docRef1), same(context))).andReturn(true).once();
    
    DocumentReference docRef2 = new DocumentReference(INextFreeDocRole.UNTITLED_NAME + 6, 
        spaceRef);
    expect(xwiki.exists(eq(docRef2), same(context))).andReturn(false).once();
    expect(xwiki.getDocument(eq(docRef2), same(context))).andThrow(new XWikiException()
        ).once();
    
    DocumentReference docRef3 = new DocumentReference(INextFreeDocRole.UNTITLED_NAME + 7, 
        spaceRef);
    expect(xwiki.exists(eq(docRef3), same(context))).andReturn(false).once();
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(docRef3), same(context))).andReturn(docMock).once();
    expect(docMock.getLock(same(context))).andReturn(null);
    
    replayDefault();
    DocumentReference ret = nextFreeDocService.getNextUntitledPageDocRef(spaceRef);
    verifyDefault();
    
    assertEquals(docRef3, ret);
  }

  @Test
  public void testGetNextUntitledPageDocRef_nullSpace() throws Exception {
    SpaceReference spaceRef = null;
    
    replayDefault();
    try {
      nextFreeDocService.getNextUntitledPageDocRef(spaceRef);
      fail();
    } catch (IllegalArgumentException exp) {
      //expected
    }
    verifyDefault();
  }
  
  @Test
  public void testGetHighestNum_fromCache() {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("myWiki"));
    String name = "asdf";
    DocumentReference baseDocRef = new DocumentReference(name, spaceRef);
    nextFreeDocService.injectNum(spaceRef, name, 5);
    
    replayDefault();
    long ret = nextFreeDocService.getHighestNum(baseDocRef);
    verifyDefault();
    
    assertEquals(5, ret);
  }
  
  @Test
  public void testGetHighestNum() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("myWiki"));
    String name = "asdf";
    DocumentReference baseDocRef = new DocumentReference(name, spaceRef);
    Query query = new DefaultQuery("statement", null, queryExecutorMock);
    expect(queryManagerMock.createQuery(eq(nextFreeDocService.getHighestNumHQL()), eq(
        "hql"))).andReturn(query).once();
    expect(queryExecutorMock.execute(same(query))).andReturn(Arrays.<Object>asList(
        name + "NoDigit", name + 5, name + 4)).once();
    
    replayDefault();
    long ret = nextFreeDocService.getHighestNum(baseDocRef);
    verifyDefault();
    
    assertEquals(6, ret);
    assertEquals(0, query.getOffset());
    assertEquals(8, query.getLimit());
    assertEquals(spaceRef.getParent().getName(), query.getWiki());
    assertEquals(2, query.getNamedParameters().size());
    assertEquals(spaceRef.getName(), query.getNamedParameters().get("space"));
    assertEquals(name + "%", query.getNamedParameters().get("name"));
  }
  
  @Test
  public void testGetHighestNum_emptyResult() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("myWiki"));
    String name = "asdf";
    DocumentReference baseDocRef = new DocumentReference(name, spaceRef);
    Query query = new DefaultQuery("statement", null, queryExecutorMock);
    expect(queryManagerMock.createQuery(eq(nextFreeDocService.getHighestNumHQL()), eq(
        "hql"))).andReturn(query).once();
    expect(queryExecutorMock.execute(same(query))).andReturn(Collections.emptyList()).once();
    
    replayDefault();
    long ret = nextFreeDocService.getHighestNum(baseDocRef);
    verifyDefault();
    
    assertEquals(1, ret);
    assertEquals(0, query.getOffset());
    assertEquals(8, query.getLimit());
    assertEquals(spaceRef.getParent().getName(), query.getWiki());
    assertEquals(2, query.getNamedParameters().size());
    assertEquals(spaceRef.getName(), query.getNamedParameters().get("space"));
    assertEquals(name + "%", query.getNamedParameters().get("name"));
  }
  
  @Test
  public void testGetHighestNum_QueryException_createQuery() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("myWiki"));
    String name = "asdf";
    DocumentReference baseDocRef = new DocumentReference(name, spaceRef);
    expect(queryManagerMock.createQuery(eq(nextFreeDocService.getHighestNumHQL()), eq(
        "hql"))).andThrow(new QueryException("", null, null)).once();
    
    replayDefault();
    long ret = nextFreeDocService.getHighestNum(baseDocRef);
    verifyDefault();
    
    assertEquals(1, ret);
  }
  
  @Test
  public void testGetHighestNum_QueryException_execute() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("myWiki"));
    String name = "asdf";
    DocumentReference baseDocRef = new DocumentReference(name, spaceRef);
    Query query = new DefaultQuery("statement", null, queryExecutorMock);
    expect(queryManagerMock.createQuery(eq(nextFreeDocService.getHighestNumHQL()), eq(
        "hql"))).andReturn(query).once();
    expect(queryExecutorMock.execute(same(query))).andThrow(new QueryException("", query, 
        null)).once();
    
    replayDefault();
    long ret = nextFreeDocService.getHighestNum(baseDocRef);
    verifyDefault();
    
    assertEquals(1, ret);
    assertEquals(0, query.getOffset());
    assertEquals(8, query.getLimit());
    assertEquals(spaceRef.getParent().getName(), query.getWiki());
    assertEquals(2, query.getNamedParameters().size());
    assertEquals(spaceRef.getName(), query.getNamedParameters().get("space"));
    assertEquals(name + "%", query.getNamedParameters().get("name"));
  }
  
  @Test
  public void testGetHighestNum_multiQuery() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("myWiki"));
    String name = "asdf";
    DocumentReference baseDocRef = new DocumentReference(name, spaceRef);
    Query query1 = new DefaultQuery("statement", null, queryExecutorMock);
    expect(queryManagerMock.createQuery(eq(nextFreeDocService.getHighestNumHQL()), eq(
        "hql"))).andReturn(query1).once();
    List<Object> noDigit = Arrays.<Object>asList(name + "NoDigit", name + "Muh");
    expect(queryExecutorMock.execute(same(query1))).andReturn(noDigit).once();
    Query query2 = new DefaultQuery("statement", null, queryExecutorMock);
    expect(queryManagerMock.createQuery(eq(nextFreeDocService.getHighestNumHQL()), eq(
        "hql"))).andReturn(query2).once();
    noDigit = new ArrayList<Object>(noDigit);
    noDigit.add(name); // to make sure offset depends on return size
    expect(queryExecutorMock.execute(same(query2))).andReturn(noDigit).once();
    Query query3 = new DefaultQuery("statement", null, queryExecutorMock);
    expect(queryManagerMock.createQuery(eq(nextFreeDocService.getHighestNumHQL()), eq(
        "hql"))).andReturn(query3).once();
    expect(queryExecutorMock.execute(same(query3))).andReturn(Arrays.<Object>asList(
        name + "5")).once();
    
    replayDefault();
    long ret = nextFreeDocService.getHighestNum(baseDocRef);
    verifyDefault();
    
    assertEquals(6, ret);
    assertEquals(0, query1.getOffset());
    assertEquals(8, query1.getLimit());
    assertEquals(spaceRef.getParent().getName(), query1.getWiki());
    assertEquals(2, query1.getNamedParameters().size());
    assertEquals(spaceRef.getName(), query1.getNamedParameters().get("space"));
    assertEquals(name + "%", query1.getNamedParameters().get("name"));
    assertEquals(2, query2.getOffset());
    assertEquals(16, query2.getLimit());
    assertEquals(spaceRef.getParent().getName(), query2.getWiki());
    assertEquals(2, query2.getNamedParameters().size());
    assertEquals(spaceRef.getName(), query2.getNamedParameters().get("space"));
    assertEquals(name + "%", query2.getNamedParameters().get("name"));
    assertEquals(5, query3.getOffset());
    assertEquals(32, query3.getLimit());
    assertEquals(spaceRef.getParent().getName(), query3.getWiki());
    assertEquals(2, query3.getNamedParameters().size());
    assertEquals(spaceRef.getName(), query3.getNamedParameters().get("space"));
    assertEquals(name + "%", query3.getNamedParameters().get("name"));
  }
  
  @Test
  public void testGetNumFromName() {
    String prefix = "asdf";
    List<Object> results = Arrays.<Object>asList(prefix + "NoDigit", prefix + "1234", 
        prefix + "6");
    
    assertEquals(new Long(1235), nextFreeDocService.extractNumFromResults(prefix, 
        results));
  }
  
  @Test
  public void testGetNumFromName_noDigit() {
    String prefix = "asdf";
    List<Object> results = Arrays.<Object>asList(prefix + "NoDigit", prefix 
        + "StillNoDigit", "notStartingWithPrefix");

    assertNull(nextFreeDocService.extractNumFromResults(prefix, results));
  }
  
  @Test
  public void testGetNumFromName_emptyList() {
    String prefix = "asdf";
    List<Object> results = Collections.emptyList();

    assertNull(nextFreeDocService.extractNumFromResults(prefix, results));
  }
  
  @Test
  public void testGetHighestNumHQL() {
    assertEquals("SELECT doc.name FROM XWikiDocument doc WHERE doc.space=:space "
        + "AND doc.name LIKE :name ORDER BY LENGTH(doc.name) DESC, doc.name DESC", 
        nextFreeDocService.getHighestNumHQL());
  }

}
