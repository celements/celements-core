package com.celements.nextfreedoc;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.easymock.Capture;
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

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAlreadyExistsException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.doc.XWikiLock;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

public class NextFreeDocServiceTest extends AbstractComponentTest {

  private XWikiContext context;
  private NextFreeDocService nextFreeDocService;

  private QueryExecutor queryExecutorMock;
  private XWikiStoreInterface storeMock;

  @Before
  public void prepareTest() throws Exception {
    context = getContext();
    registerComponentMocks(IModelAccessFacade.class, QueryManager.class);
    nextFreeDocService = (NextFreeDocService) Utils.getComponent(INextFreeDocRole.class);
    queryExecutorMock = createDefaultMock(QueryExecutor.class);
    storeMock = createDefaultMock(XWikiStoreInterface.class);
    expect(getWikiMock().getStore()).andReturn(storeMock).anyTimes();
  }

  @Test
  public void test_getNextTitledPageDocRef() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference(
        context.getDatabase()));
    String title = "asdf";
    nextFreeDocService.injectNum(spaceRef, title, 5);

    DocumentReference docRef1 = new DocumentReference(title + 5, spaceRef);
    expect(getMock(IModelAccessFacade.class).createDocument(docRef1))
        .andThrow(new DocumentAlreadyExistsException(docRef1));
    DocumentReference docRef2 = new DocumentReference(title + 6, spaceRef);
    XWikiDocument doc2 = new XWikiDocument(docRef2);
    expect(getMock(IModelAccessFacade.class).createDocument(docRef2)).andReturn(doc2);
    expect(storeMock.loadLock(eq(doc2.getId()), same(context), eq(true))).andReturn(null).once();
    storeMock.saveLock(anyObject(XWikiLock.class), same(context), eq(true));
    expectLastCall().once();

    replayDefault();
    DocumentReference ret = nextFreeDocService.getNextTitledPageDocRef(spaceRef, title);
    verifyDefault();

    assertEquals(docRef2, ret);
  }

  @Test
  public void test_getNextTitledPageDocRef_nullSpace() throws Exception {
    SpaceReference spaceRef = null;
    String title = "asdf";

    replayDefault();
    try {
      nextFreeDocService.getNextTitledPageDocRef(spaceRef, title);
      fail();
    } catch (IllegalArgumentException exp) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void test_getNextTitledPageDocRef_nullTitle() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference(
        context.getDatabase()));
    String title = "";

    replayDefault();
    try {
      nextFreeDocService.getNextTitledPageDocRef(spaceRef, title);
      fail();
    } catch (IllegalArgumentException exp) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void test_getNextUntitledPageDocRef() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference(
        context.getDatabase()));
    nextFreeDocService.injectNum(spaceRef, INextFreeDocRole.UNTITLED_NAME, 5);

    DocumentReference docRef1 = new DocumentReference(INextFreeDocRole.UNTITLED_NAME + 5, spaceRef);
    expect(getMock(IModelAccessFacade.class).createDocument(docRef1))
        .andThrow(new DocumentAlreadyExistsException(docRef1));

    DocumentReference docRef2 = new DocumentReference(INextFreeDocRole.UNTITLED_NAME + 6, spaceRef);
    XWikiDocument doc2 = new XWikiDocument(docRef2);
    expect(getMock(IModelAccessFacade.class).createDocument(docRef2)).andReturn(doc2);
    expect(storeMock.loadLock(eq(doc2.getId()), same(context), eq(true))).andReturn(null).once();
    storeMock.saveLock(anyObject(XWikiLock.class), same(context), eq(true));
    expectLastCall().once();

    replayDefault();
    DocumentReference ret = nextFreeDocService.getNextUntitledPageDocRef(spaceRef);
    verifyDefault();

    assertEquals(docRef2, ret);
  }

  @Test
  public void test_getNextUntitledPageDocRef_nullSpace() throws Exception {
    SpaceReference spaceRef = null;

    replayDefault();
    try {
      nextFreeDocService.getNextUntitledPageDocRef(spaceRef);
      fail();
    } catch (IllegalArgumentException exp) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void test_getHighestNum_fromCache() {
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
  public void test_getHighestNum() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("myWiki"));
    String name = "asdf";
    DocumentReference baseDocRef = new DocumentReference(name, spaceRef);
    Query query = new DefaultQuery("statement", null, queryExecutorMock);
    expect(getMock(QueryManager.class).createQuery(eq(nextFreeDocService.getHighestNumHQL()),
        eq("hql"))).andReturn(query).once();
    expect(queryExecutorMock.execute(same(query))).andReturn(Arrays.<Object>asList(name + "NoDigit",
        name + 5, name + 4)).once();

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
  public void test_getHighestNum_emptyResult() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("myWiki"));
    String name = "asdf";
    DocumentReference baseDocRef = new DocumentReference(name, spaceRef);
    Query query = new DefaultQuery("statement", null, queryExecutorMock);
    expect(getMock(QueryManager.class).createQuery(eq(nextFreeDocService.getHighestNumHQL()),
        eq("hql"))).andReturn(query).once();
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
  public void test_getHighestNum_QueryException_createQuery() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("myWiki"));
    String name = "asdf";
    DocumentReference baseDocRef = new DocumentReference(name, spaceRef);
    expect(getMock(QueryManager.class).createQuery(eq(nextFreeDocService.getHighestNumHQL()),
        eq("hql"))).andThrow(new QueryException("", null, null)).once();

    replayDefault();
    long ret = nextFreeDocService.getHighestNum(baseDocRef);
    verifyDefault();

    assertEquals(1, ret);
  }

  @Test
  public void test_getHighestNum_QueryException_execute() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("myWiki"));
    String name = "asdf";
    DocumentReference baseDocRef = new DocumentReference(name, spaceRef);
    Query query = new DefaultQuery("statement", null, queryExecutorMock);
    expect(getMock(QueryManager.class).createQuery(eq(nextFreeDocService.getHighestNumHQL()),
        eq("hql"))).andReturn(query).once();
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
  public void test_getHighestNum_multiQuery() throws Exception {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("myWiki"));
    String name = "asdf";
    DocumentReference baseDocRef = new DocumentReference(name, spaceRef);
    Query query1 = new DefaultQuery("statement", null, queryExecutorMock);
    expect(getMock(QueryManager.class).createQuery(eq(nextFreeDocService.getHighestNumHQL()),
        eq("hql"))).andReturn(query1).once();
    List<Object> noDigit = Arrays.<Object>asList(name + "NoDigit", name + "Muh");
    expect(queryExecutorMock.execute(same(query1))).andReturn(noDigit).once();
    Query query2 = new DefaultQuery("statement", null, queryExecutorMock);
    expect(getMock(QueryManager.class).createQuery(eq(nextFreeDocService.getHighestNumHQL()),
        eq("hql"))).andReturn(query2).once();
    noDigit = new ArrayList<>(noDigit);
    noDigit.add(name); // to make sure offset depends on return size
    expect(queryExecutorMock.execute(same(query2))).andReturn(noDigit).once();
    Query query3 = new DefaultQuery("statement", null, queryExecutorMock);
    expect(getMock(QueryManager.class).createQuery(eq(nextFreeDocService.getHighestNumHQL()),
        eq("hql"))).andReturn(query3).once();
    expect(queryExecutorMock.execute(same(query3))).andReturn(Arrays.<Object>asList(name
        + "5")).once();

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
  public void test_getNumFromName() {
    String prefix = "asdf";
    List<Object> results = Arrays.<Object>asList(prefix + "NoDigit", prefix + "1234", prefix + "6");

    assertEquals(Long.valueOf(1235), nextFreeDocService.extractNumFromResults(prefix, results));
  }

  @Test
  public void test_getNumFromName_noDigit() {
    String prefix = "asdf";
    List<Object> results = Arrays.<Object>asList(prefix + "NoDigit", prefix + "StillNoDigit",
        "notStartingWithPrefix");

    assertNull(nextFreeDocService.extractNumFromResults(prefix, results));
  }

  @Test
  public void test_getNumFromName_emptyList() {
    String prefix = "asdf";
    List<Object> results = Collections.emptyList();

    assertNull(nextFreeDocService.extractNumFromResults(prefix, results));
  }

  @Test
  public void test_getHighestNumHQL() {
    assertEquals("SELECT doc.name FROM XWikiDocument doc WHERE doc.space=:space "
        + "AND doc.name LIKE :name ORDER BY LENGTH(doc.name) DESC, doc.name DESC",
        nextFreeDocService.getHighestNumHQL());
  }

  @Test
  public void test_getNextRandomPageDocRef_lengthOfRandomAlphaNumeric() {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("mywiki"));
    String prefix = "";
    int lengthOfRandomAlphanumeric = 3;

    Exception e = assertThrows(IllegalArgumentException.class, () -> {
      nextFreeDocService.getNextRandomPageDocRef(spaceRef, lengthOfRandomAlphanumeric, prefix);
    });

    assertEquals("Parameter int lengthOfRandomAlphanumeric has to be > 3", e.getMessage());
  }

  @Test
  public void test_getNextRandomPageDocRef_prefixNull() {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("mywiki"));
    String prefix = null;
    int lengthOfRandomAlphanumeric = 10;
    expect(getMock(IModelAccessFacade.class).exists(anyObject(DocumentReference.class)))
        .andReturn(false).once();

    replayDefault();
    DocumentReference docRef = nextFreeDocService.getNextRandomPageDocRef(spaceRef,
        lengthOfRandomAlphanumeric, prefix);
    verifyDefault();

    assertNotNull(docRef);
    assertEquals(10, docRef.getName().length());
  }

  @Test
  public void test_getNextRandomPageDocRef_prefixEmpty() {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("mywiki"));
    String prefix = "";
    int lengthOfRandomAlphanumeric = 10;
    expect(getMock(IModelAccessFacade.class).exists(anyObject(DocumentReference.class)))
        .andReturn(false).once();

    replayDefault();
    DocumentReference docRef = nextFreeDocService.getNextRandomPageDocRef(spaceRef,
        lengthOfRandomAlphanumeric, prefix);
    verifyDefault();

    assertNotNull(docRef);
    assertEquals(10, docRef.getName().length());
  }

  @Test
  public void test_getNextRandomPageDocRef_prefixNotEmpty() {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("mywiki"));
    String prefix = "asdf";
    int lengthOfRandomAlphanumeric = 10;
    expect(getMock(IModelAccessFacade.class).exists(anyObject(DocumentReference.class)))
        .andReturn(false).once();

    replayDefault();
    DocumentReference docRef = nextFreeDocService.getNextRandomPageDocRef(spaceRef,
        lengthOfRandomAlphanumeric, prefix);
    verifyDefault();

    assertNotNull(docRef);
    assertEquals(14, docRef.getName().length());
  }

  @Test
  public void test_getNextRandomPageDocRef_spaceRefNull() {
    SpaceReference spaceRef = null;
    String prefix = "";
    int lengthOfRandomAlphanumeric = 10;

    Exception e = assertThrows(IllegalArgumentException.class, () -> {
      nextFreeDocService.getNextRandomPageDocRef(spaceRef, lengthOfRandomAlphanumeric, prefix);
    });

    assertEquals("SpaceReference cannot be null.", e.getMessage());
  }

  @Test
  public void test_getNextRandomPageDocRef_DocumentReferenceExistsAlready() {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("mywiki"));
    String prefix = "";
    int lengthOfRandomAlphanumeric = 10;
    Capture<DocumentReference> docRefCapture = newCapture();
    expect(getMock(IModelAccessFacade.class).exists(capture(docRefCapture)))
        .andReturn(false).once();

    replayDefault();
    DocumentReference docRef = nextFreeDocService.getNextRandomPageDocRef(spaceRef,
        lengthOfRandomAlphanumeric, prefix);
    verifyDefault();
    // test if returned DocumentReference does not exist twice

  }

  @Test
  public void test_getNextRandomPageDocRef() {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("mywiki"));
    String prefix = "";
    int lengthOfRandomAlphanumeric = 10;
    Capture<DocumentReference> docRefCapture = newCapture();
    expect(getMock(IModelAccessFacade.class).exists(capture(docRefCapture)))
        .andReturn(false).once();

    replayDefault();
    DocumentReference docRef = nextFreeDocService.getNextRandomPageDocRef(spaceRef,
        lengthOfRandomAlphanumeric, prefix);
    verifyDefault();

    assertNotNull(docRef);
    assertEquals(spaceRef, docRef.getLastSpaceReference());
    assertEquals(10, docRef.getName().length());
    assertEquals(docRef, docRefCapture.getValue());
  }

}
