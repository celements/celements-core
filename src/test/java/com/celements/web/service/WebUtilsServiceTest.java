package com.celements.web.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;

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
  public void testGetDocumentParentsList() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(),"mySpace","myDoc"),
      parentRef1 = new DocumentReference(context.getDatabase(),"mySpace","parent1"),
      parentRef2 = new DocumentReference(context.getDatabase(),"mySpace","parent2");
    
    XWikiDocument doc = new XWikiDocument(docRef);
    XWikiDocument docP1 = new XWikiDocument(parentRef1);
    XWikiDocument docP2 = new XWikiDocument(parentRef2);
    docP1.setParentReference(parentRef2.extractReference(EntityType.DOCUMENT));
    doc.setParentReference(parentRef1.extractReference(EntityType.DOCUMENT));
    
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.getDocument(eq(parentRef1), same(context))).andReturn(docP1).once();
    expect(xwiki.exists(eq(parentRef1), same(context))).andReturn(true).once();
    expect(xwiki.getDocument(eq(parentRef2), same(context))).andReturn(docP2).once();
    expect(xwiki.exists(eq(parentRef2), same(context))).andReturn(true).once();
    
    List<DocumentReference> docParentsList = Arrays.asList(parentRef1, parentRef2);
    replayAll();
    assertEquals(docParentsList, webUtilsService.getDocumentParentsList(docRef, false));
    verifyAll();
  }
  
  @Test
  public void testGetDocumentParentsList_includeDoc() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(),"mySpace","myDoc"),
      parentRef1 = new DocumentReference(context.getDatabase(),"mySpace","parent1"),
      parentRef2 = new DocumentReference(context.getDatabase(),"mySpace","parent2");
    
    XWikiDocument doc = new XWikiDocument(docRef);
    XWikiDocument docP1 = new XWikiDocument(parentRef1);
    XWikiDocument docP2 = new XWikiDocument(parentRef2);
    docP1.setParentReference(parentRef2.extractReference(EntityType.DOCUMENT));
    doc.setParentReference(parentRef1.extractReference(EntityType.DOCUMENT));
    
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).once();
    expect(xwiki.getDocument(eq(parentRef1), same(context))).andReturn(docP1).once();
    expect(xwiki.exists(eq(parentRef1), same(context))).andReturn(true).once();
    expect(xwiki.getDocument(eq(parentRef2), same(context))).andReturn(docP2).once();
    expect(xwiki.exists(eq(parentRef2), same(context))).andReturn(true).once();
    
    List<DocumentReference> docParentsList = Arrays.asList(docRef, parentRef1, parentRef2);
    replayAll();
    assertEquals(docParentsList, webUtilsService.getDocumentParentsList(docRef, true));
    verifyAll();
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


  private void replayAll(Object ... mocks) {
    replay(xwiki);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki);
    verify(mocks);
  }

}
