package com.celements.web.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class WebUtilsServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private WebUtilsService webUtilsService;

  @Before
  public void setUp_WebUtilsServiceTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    webUtilsService = (WebUtilsService) Utils.getComponent(IWebUtilsService.class);
  }

  @Test
  public void testGetAdminLanguage_defaultToDocLanguage() throws XWikiException {
    context.setLanguage("de");
    String userName = "XWiki.MyUser";
    context.setUser(userName);
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(userDoc);
    expect(xwiki.getWebPreference(eq("admin_language"), eq("de"), same(context))
        ).andReturn("de");
    replayAll();
    assertEquals("de", webUtilsService.getAdminLanguage());
    verifyAll();
  }

  @Test
  public void testGetAdminLanguage_contextUser() throws XWikiException {
    context.setLanguage("de");
    String userName = "XWiki.MyUser";
    context.setUser(userName);
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    DocumentReference xwikiUserClassRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiUsers");
    BaseObject userObj = new BaseObject();
    userObj.setXClassReference(xwikiUserClassRef);
    userObj.setStringValue("admin_language", "fr");
    userDoc.setXObject(0, userObj);
    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(userDoc);
    replayAll();
    assertEquals("fr", webUtilsService.getAdminLanguage());
    verifyAll();
  }

  @Test
  public void testGetAdminLanguage_notContextUser() throws XWikiException {
    context.setLanguage("de");
    String userName = "XWiki.MyUser";
    context.setUser("XWiki.NotMyUser");
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    DocumentReference xwikiUserClassRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiUsers");
    BaseObject userObj = new BaseObject();
    userObj.setXClassReference(xwikiUserClassRef);
    userObj.setStringValue("admin_language", "fr");
    userDoc.setXObject(0, userObj);
    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(userDoc);
    replayAll();
    assertEquals("fr", webUtilsService.getAdminLanguage(userName));
    verifyAll();
  }

  @Test
  public void testGetAdminLanguage_defaultToWebPreferences() throws XWikiException {
    context.setLanguage("de");
    String userName = "XWiki.MyUser";
    context.setUser("XWiki.NotMyUser");
    expect(xwiki.getWebPreference(eq("admin_language"), isA(String.class), same(context))
        ).andReturn("en");
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    DocumentReference xwikiUserClassRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiUsers");
    BaseObject userObj = new BaseObject();
    userObj.setXClassReference(xwikiUserClassRef);
    userObj.setStringValue("admin_language", "");
    userDoc.setXObject(0, userObj);
    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(userDoc);
    replayAll();
    assertEquals("en", webUtilsService.getAdminLanguage(userName));
    verifyAll();
  }

  @Test
  public void testIsAdminUser_noAdminRights_noRightsService() {
    context.setUser("XWiki.TestUser");
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "MyDocument");
    context.setDoc(new XWikiDocument(currentDocRef));
    expect(xwiki.getRightService()).andReturn(null).anyTimes();

    replayAll();
    assertFalse(webUtilsService.isAdminUser());
    verifyAll();
  }
  
  @Test
  public void testIsAdminUser_noContextDoc() {
    context.setUser("XWiki.TestUser");
    context.setDoc(null);
    XWikiRightService mockRightsService = createMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightsService).anyTimes();
    //hasAdminRights must not be called it will fail with an NPE

    replayAll(mockRightsService);
    assertFalse(webUtilsService.isAdminUser());
    verifyAll(mockRightsService);
  }
  
  @Test
  public void testIsAdminUser_noAdminRights_notInAdminGroup() throws Exception {
    context.setUser("XWiki.TestUser");
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "TestUser");
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "MyDocument");
    context.setDoc(new XWikiDocument(currentDocRef));
    XWikiRightService mockRightsService = createMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightsService).anyTimes();
    expect(mockRightsService.hasAdminRights(same(context))).andReturn(false).anyTimes();
    XWikiGroupService groupServiceMock = createMock(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(context))).andReturn(groupServiceMock);
    List<DocumentReference> emptyGroupList = Collections.emptyList();
    expect(groupServiceMock.getAllGroupsReferencesForMember(eq(userDocRef), eq(0), eq(0),
        same(context))).andReturn(emptyGroupList).atLeastOnce();
    replayAll(mockRightsService, groupServiceMock);
    assertFalse(webUtilsService.isAdminUser());
    verifyAll(mockRightsService, groupServiceMock);
  }
  
  @Test
  public void testIsAdminUser_noAdminRights_isInAdminGroup() throws Exception {
    context.setUser("XWiki.TestUser");
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "TestUser");
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "MyDocument");
    context.setDoc(new XWikiDocument(currentDocRef));
    XWikiRightService mockRightsService = createMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightsService).anyTimes();
    expect(mockRightsService.hasAdminRights(same(context))).andReturn(false).anyTimes();
    XWikiGroupService groupServiceMock = createMock(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(context))).andReturn(groupServiceMock);
    DocumentReference adminGroupDocRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiAdminGroup");
    expect(groupServiceMock.getAllGroupsReferencesForMember(eq(userDocRef), eq(0), eq(0),
        same(context))).andReturn(Arrays.asList(adminGroupDocRef)).atLeastOnce();
    replayAll(mockRightsService, groupServiceMock);
    assertTrue(webUtilsService.isAdminUser());
    verifyAll(mockRightsService, groupServiceMock);
  }
  
  @Test
  public void testIsAdminUser_adminRights_notInAdminGroup() {
    context.setUser("XWiki.TestUser");
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "MyDocument");
    context.setDoc(new XWikiDocument(currentDocRef));
    XWikiRightService mockRightsService = createMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightsService).anyTimes();
    expect(mockRightsService.hasAdminRights(same(context))).andReturn(true).anyTimes();

    replayAll(mockRightsService);
    assertNotNull(context.getXWikiUser());
    assertNotNull(context.getDoc());
    assertTrue(webUtilsService.isAdminUser());
    verifyAll(mockRightsService);
  }

  @Test
  public void testIsAdvancedAdmin_NPE_noUserObj() throws Exception {
    context.setUser("XWiki.XWikiGuest");
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiGuest");
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "MyDocument");
    context.setDoc(new XWikiDocument(currentDocRef));
    XWikiRightService mockRightsService = createMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightsService).anyTimes();
    expect(mockRightsService.hasAdminRights(same(context))).andReturn(true).anyTimes();
    XWikiDocument guestUserDoc = new XWikiDocument(userDocRef);
    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(guestUserDoc
        ).anyTimes();
    replayAll(mockRightsService);
    assertNotNull(context.getXWikiUser());
    assertNotNull(context.getDoc());
    assertFalse(webUtilsService.isAdvancedAdmin());
    verifyAll(mockRightsService);
  }

  @Test
  public void testResolveDocumentReference_mainWiki() {
    replayAll();
    DocumentReference testDocRef = webUtilsService.resolveDocumentReference(
        "xwiki:XWiki.test");
    assertEquals("xwiki", testDocRef.getWikiReference().getName());
    verifyAll();
  }

  @Test
  public void testResolveDocumentReference_localWiki() {
    replayAll();
    DocumentReference testDocRef = webUtilsService.resolveDocumentReference("XWiki.test");
    assertEquals(context.getDatabase(), testDocRef.getWikiReference().getName());
    verifyAll();
  }

  @Test
  public void testResolveSpaceReference_mainWiki() {
    replayAll();
    SpaceReference testSpaceRef = webUtilsService.resolveSpaceReference(
        "myMasterCellWiki:XWiki");
    EntityReference parent = testSpaceRef.getParent();
    assertEquals(WikiReference.class, parent.getClass());
    assertEquals("myMasterCellWiki", parent.getName());
    assertEquals("XWiki", testSpaceRef.getName());
    verifyAll();
  }

  @Test
  public void testResolveSpaceReference_localWiki() {
    replayAll();
    context.setDatabase("myFirstWiki");
    SpaceReference testSpaceRef = webUtilsService.resolveSpaceReference("mySpace");
    EntityReference parent = testSpaceRef.getParent();
    assertEquals(WikiReference.class, parent.getClass());
    assertEquals(context.getDatabase(), parent.getName());
    assertEquals("mySpace", testSpaceRef.getName());
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki);
    verify(mocks);
  }
  
  @Test
  public void testGetObjectsOrdered_docNull() {
    List<BaseObject> list = webUtilsService.getObjectsOrdered(null, getBOClassRef(), "", 
        false);
    assertNotNull(list);
    assertEquals(0, list.size());
  }
  
  @Test
  public void testGetObjectsOrdered_noObjects() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), 
        "S", "D"));
    List<BaseObject> list = webUtilsService.getObjectsOrdered(doc, getBOClassRef(), "s1",
        true, "s2", false);
    assertNotNull(list);
    assertEquals(0, list.size());
  }
  
  @Test
  public void testGetObjectsOrdered_oneObject() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), 
        "S", "D"));
    doc.addXObject(getSortTestBaseObjects().get(0));
    List<BaseObject> list = webUtilsService.getObjectsOrdered(doc, getBOClassRef(), "s1",
        true, "s2", true);
    assertEquals(1, list.size());
  }

  @Test
  public void testGetObjectsOrdered_onlyOneFieldSort() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), 
        "S", "D"));
    for (BaseObject obj : getSortTestBaseObjects()) {
      doc.addXObject(obj);
    }
    List<BaseObject> list = webUtilsService.getObjectsOrdered(doc, getBOClassRef(), "s1",
        true);
    assertEquals(5, list.size());
    assertEquals("a", list.get(0).getStringValue("s1"));
    assertEquals("b", list.get(1).getStringValue("s1"));
    assertEquals("t", list.get(1).getStringValue("s2"));
    assertEquals("b", list.get(2).getStringValue("s1"));
    assertEquals("s", list.get(2).getStringValue("s2"));
    assertEquals("c", list.get(3).getStringValue("s1"));
    assertEquals("d", list.get(4).getStringValue("s1"));
  }
  
  @Test
  public void testGetObjectsOrdered_severalObjects_asc() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), 
        "S", "D"));
    for (BaseObject obj : getSortTestBaseObjects()) {
      doc.addXObject(obj);
    }
    List<BaseObject> list = webUtilsService.getObjectsOrdered(doc, getBOClassRef(),  "s1",
        true, "d", true);
    assertEquals(5, list.size());
    assertEquals("a", list.get(0).getStringValue("s1"));
    assertEquals("b", list.get(1).getStringValue("s1"));
    assertEquals(new Date(200l), list.get(1).getDateValue("d"));
    assertEquals("b", list.get(2).getStringValue("s1"));
    assertEquals(new Date(400l), list.get(2).getDateValue("d"));
    assertEquals("c", list.get(3).getStringValue("s1"));
    assertEquals("d", list.get(4).getStringValue("s1"));
  }
  
  @Test
  public void testGetObjectsOrdered_severalObjects_desc() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), 
        "S", "D"));
    for (BaseObject obj : getSortTestBaseObjects()) {
      doc.addXObject(obj);
    }
    List<BaseObject> list = webUtilsService.getObjectsOrdered(doc, getBOClassRef(), "i",
        false, "l", false);
    assertEquals(5, list.size());
    assertEquals(3, list.get(0).getIntValue("i"));
    assertEquals(2, list.get(1).getIntValue("i"));
    assertEquals(4, list.get(1).getIntValue("l"));
    assertEquals(2, list.get(2).getIntValue("i"));
    assertEquals(3, list.get(2).getIntValue("l"));
    assertEquals(2, list.get(3).getIntValue("i"));
    assertEquals(1, list.get(3).getIntValue("l"));
    assertEquals(1, list.get(4).getIntValue("i"));
  }

  @Test
  public void testSplitStringByLength_noEmptyTrailingFields() {
    String testString = ("Market Leader. Business Grammar and"
        + " Usage, Band 1").substring(0, 35);
    String[] splitedStr = webUtilsService.splitStringByLength(testString, 35);
    assertEquals("Market Leader. Business Grammar and", splitedStr[0]);
    assertTrue("Expecting one Element but found [" + splitedStr.length
        + "].", splitedStr.length == 1);
  }
  
  @Test
  public void testSplitStringByLength() {
    String[] splitedStr = webUtilsService.splitStringByLength(
        "Market Leader. Business Grammar and Usage, Band 1", 35);
    assertEquals("Market Leader. Business Grammar and", splitedStr[0]);
    assertEquals(" Usage, Band 1", splitedStr[1]);
  }


  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private DocumentReference getBOClassRef() {
    return new DocumentReference(context.getDatabase(), "Classes", "TestClass");
  }
  
  private List<BaseObject> getSortTestBaseObjects() {
    List<BaseObject> objs = new ArrayList<BaseObject>();
    BaseObject obj = new BaseObject();
    obj.setXClassReference(getBOClassRef());
    obj.setStringValue("s1", "c");
    obj.setStringValue("s2", "u");
    obj.setIntValue("i", 1);
    obj.setDateValue("d", new Date(500l));
    obj.setLongValue("l", 5l);
    objs.add(obj);
    obj = new BaseObject();
    obj.setXClassReference(getBOClassRef());
    obj.setStringValue("s1", "b");
    obj.setStringValue("s2", "t");
    obj.setIntValue("i", 2);
    obj.setDateValue("d", new Date(400l));
    obj.setLongValue("l", 1l);
    objs.add(obj);
    obj = new BaseObject();
    obj.setXClassReference(getBOClassRef());
    obj.setStringValue("s1", "a");
    obj.setStringValue("s2", "r");
    obj.setIntValue("i", 3);
    obj.setDateValue("d", new Date(300l));
    obj.setLongValue("l", 2l);
    objs.add(obj);
    obj = new BaseObject();
    obj.setXClassReference(getBOClassRef());
    obj.setStringValue("s1", "b");
    obj.setStringValue("s2", "s");
    obj.setIntValue("i", 2);
    obj.setDateValue("d", new Date(200l));
    obj.setLongValue("l", 4l);
    objs.add(obj);
    obj = new BaseObject();
    obj.setXClassReference(getBOClassRef());
    obj.setStringValue("s1", "d");
    obj.setStringValue("s2", "v");
    obj.setIntValue("i", 2);
    obj.setDateValue("d", new Date(100l));
    obj.setLongValue("l", 3l);
    objs.add(obj);
    return objs;
  }
}
