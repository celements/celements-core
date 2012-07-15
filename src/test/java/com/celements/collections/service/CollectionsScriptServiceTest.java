package com.celements.collections.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.collections.ICollectionsService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class CollectionsScriptServiceTest extends AbstractBridgedComponentTestCase {

  private CollectionsScriptService collScriptService;

  private XWikiContext context;

  private XWiki xwiki;

  private XWikiRightService mockRightService;

  private ICollectionsService mockCollService;

  @Before
  public void setUp_CollectionsScriptServiceTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    mockRightService = createMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightService).anyTimes();
    collScriptService = new CollectionsScriptService();
    mockCollService = createMock(ICollectionsService.class);
    collScriptService.collectionsService = mockCollService;
    collScriptService.execution = Utils.getComponent(Execution.class);
  }

  @Test
  public void testComponentAvailable() {
    assertNotNull(Utils.getComponent(ScriptService.class, "collections"));
  }

  @Test
  public void testGetObjectsOrdered() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyPage");
    XWikiDocument xdoc = new XWikiDocument(docRef);
    Document document = new Document(xdoc, context);
    DocumentReference classRef = new DocumentReference(context.getDatabase(), "Classes",
        "MyClass");
    // FIXME !!! doc.getDocument() needs programming rights.
    // FIXME !!! view-rights look sufficient to me
    expect(mockRightService.hasProgrammingRights(same(context))).andReturn(true);
    BaseObject expectedBO = new BaseObject();
    expectedBO.setStringValue("myField", "abcd");
    List<BaseObject> expResultList = Arrays.asList(expectedBO);
    expect(
        mockCollService.getObjectsOrdered(same(xdoc), eq(classRef), eq("myField"),
            eq(true), (String) isNull(), eq(false))).andReturn(expResultList);
    replayAll();
    List<com.xpn.xwiki.api.Object> resultList = collScriptService.getObjectsOrdered(
        document, classRef, "myField", true);
    assertNotNull(resultList);
    assertEquals(1, resultList.size());
    assertEquals("abcd", resultList.get(0).getProperty("myField").getValue());
    verifyAll();
  }

  @Test
  public void testGetObjectsOrdered_orderByTwoFields() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyPage");
    XWikiDocument xdoc = new XWikiDocument(docRef);
    Document document = new Document(xdoc, context);
    DocumentReference classRef = new DocumentReference(context.getDatabase(), "Classes",
        "MyClass");
    // FIXME !!! doc.getDocument() needs programming rights.
    // FIXME !!! view-rights look sufficient to me
    expect(mockRightService.hasProgrammingRights(same(context))).andReturn(true);
    BaseObject expectedBO = new BaseObject();
    expectedBO.setStringValue("myField", "abcd");
    List<BaseObject> expResultList = Arrays.asList(expectedBO);
    expect(
        mockCollService.getObjectsOrdered(same(xdoc), eq(classRef), eq("myField"),
            eq(true), eq("myField2"), eq(false))).andReturn(expResultList);
    replayAll();
    List<com.xpn.xwiki.api.Object> resultList = collScriptService.getObjectsOrdered(
        document, classRef, "myField", true, "myField2", false);
    assertNotNull(resultList);
    assertEquals(1, resultList.size());
    assertEquals("abcd", resultList.get(0).getProperty("myField").getValue());
    verifyAll();
  }

  private void replayAll(Object... mocks) {
    replay(xwiki, mockRightService, mockCollService);
    replay(mocks);
  }

  private void verifyAll(Object... mocks) {
    verify(xwiki, mockRightService, mockCollService);
    verify(mocks);
  }

}
