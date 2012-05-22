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
import com.xpn.xwiki.render.XWikiRenderingEngine;
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
  public void testGetParentForLevel_1() {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(),"mySpace","myDoc");

    XWikiDocument doc = new XWikiDocument(docRef);
  
    context.setDoc(doc);
    assertNull(webUtilsService.getParentForLevel(1)); //root
  }
  
  @Test
  public void testGetParentForLevel_2() throws XWikiException {    
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(),"mySpace","myDoc"),
      parentRef = new DocumentReference(context.getDatabase(),"mySpace","parent1");
  
    XWikiDocument doc = new XWikiDocument(docRef);
    XWikiDocument docP = new XWikiDocument(parentRef);
    doc.setParentReference(parentRef.extractReference(EntityType.DOCUMENT));
    
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).once();
    expect(xwiki.getDocument(eq(parentRef), same(context))).andReturn(docP).once();
    expect(xwiki.exists(eq(parentRef), same(context))).andReturn(true).once();
    
    context.setDoc(doc);
    replayAll();
    assertEquals(parentRef,webUtilsService.getParentForLevel(2));
    verifyAll();
  }
  
  @Test
  public void testGetParentForLevel_3() throws XWikiException {    
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(),"mySpace","myDoc"),
      parentRef = new DocumentReference(context.getDatabase(),"mySpace","parent1");
  
    XWikiDocument doc = new XWikiDocument(docRef);
    XWikiDocument docP = new XWikiDocument(parentRef);
    doc.setParentReference(parentRef.extractReference(EntityType.DOCUMENT));
    
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).once();
    expect(xwiki.getDocument(eq(parentRef), same(context))).andReturn(docP).once();
    expect(xwiki.exists(eq(parentRef), same(context))).andReturn(true).once();
    
    context.setDoc(doc);
    replayAll();
    assertEquals(docRef,webUtilsService.getParentForLevel(3));
    verifyAll();
  }
  
  @Test
  public void testGetParentForLevel_IndexOutOfBounds() throws XWikiException {    
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(),"mySpace","myDoc"),
      parentRef = new DocumentReference(context.getDatabase(),"mySpace","parent1");
  
    XWikiDocument doc = new XWikiDocument(docRef);
    XWikiDocument docP = new XWikiDocument(parentRef);
    doc.setParentReference(parentRef.extractReference(EntityType.DOCUMENT));
    
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).times(3);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).times(3);
    expect(xwiki.getDocument(eq(parentRef), same(context))).andReturn(docP).times(3);
    expect(xwiki.exists(eq(parentRef), same(context))).andReturn(true).times(3);
    
    context.setDoc(doc);
    replayAll();
    try{
      webUtilsService.getParentForLevel(4);
      assertFalse(true);
    } catch(IndexOutOfBoundsException e){
      assertEquals("-1", e.getMessage());
    }    
    try{
      webUtilsService.getParentForLevel(0);
      assertFalse(true);
    } catch(IndexOutOfBoundsException e){
      assertEquals("Index: 3, Size: 2", e.getMessage());
    }
    try{
      webUtilsService.getParentForLevel(-1);
      assertFalse(true);
    } catch(IndexOutOfBoundsException e){
      assertEquals("Index: 4, Size: 2", e.getMessage());
    }
    verifyAll();
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
  public void testGetDocSectionAsJSON() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
      transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    transDoc.setContent("abc<table>blabla</table><table>abc</table>");
  
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>blabla</table>{/pre}"),
        eq(context.getDoc()), same(context))).andReturn("<table>blabla</table>"
        ).atLeastOnce();
    
    replayAll(doc, renderer);
    String json ="[{\"content\" : \"<table>blabla</table>\", \"section\" : 2," +
        " \"sectionNr\" : 3}]";
    assertEquals(json, webUtilsService.getDocSectionAsJSON("(?=<table)", docRef, 2));
    verifyAll(doc, renderer);
  }
  
  @Test
  public void testGetDocSection_empty() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
      transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(doc.getTranslatedDocument(same(context))
        ).andReturn(new XWikiDocument(transDocRef)).once();
    
    replayAll(doc);
    assertNull(webUtilsService.getDocSection("(?=<table)", docRef, 1));
    verifyAll(doc);
  }
  
  @Test
  public void testGetDocSection_first() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
      transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    transDoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}abc{/pre}"), eq(context.getDoc()), same(context))
        ).andReturn("abc").atLeastOnce();
    
    replayAll(doc, renderer);
    assertEquals("abc", webUtilsService.getDocSection("(?=<table)", docRef, 1));
    verifyAll(doc, renderer);
  }
  
  @Test
  public void testGetDocSection_firstEmptyRTE() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
      transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    transDoc.setContent("<p></p>  <br /> \n<table>blabla</table><table>abc</table>");
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>blabla</table>{/pre}"), eq(context.getDoc()), 
        same(context))).andReturn("<table>blabla</table>").atLeastOnce();
    
    replayAll(doc, renderer);
    assertEquals("<table>blabla</table>", 
        webUtilsService.getDocSection("(?=<table)", docRef, 1));
    verifyAll(doc, renderer);
  }
  
  @Test
  public void testGetDocSection_middle() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
      transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    transDoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>blabla</table>{/pre}"), eq(context.getDoc()),
        same(context))).andReturn("<table>blabla</table>").atLeastOnce();
    
    replayAll(doc, renderer);
    assertEquals("<table>blabla</table>", webUtilsService.getDocSection("(?=<table)",
        docRef, 2));
    verifyAll(doc, renderer);
  }
  
  @Test
  public void testGetDocSection_last() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
      transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    transDoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>abc</table>{/pre}"), eq(context.getDoc()),
        same(context))).andReturn("<table>abc</table>").atLeastOnce();
    
    replayAll(doc, renderer);
    assertEquals("<table>abc</table>", webUtilsService.getDocSection("(?=<table)",
        docRef, 3));
    verifyAll(doc, renderer);
  }

  @Test
  public void testCountSections_empty() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
      transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).once();
    
    replayAll(doc);
    assertEquals(0, webUtilsService.countSections("", docRef));
    verifyAll(doc);
  }

  @Test
  public void testCountSections_one() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
      transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    transDoc.setContent("<table>blabla</table>");
    
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    replayAll(doc);
    assertEquals(1, webUtilsService.countSections("(?=<table)", docRef));
    verifyAll(doc);
  }
  
  @Test
  public void testCountSections_emptyRTEStart() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
      transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    transDoc.setContent("<table>blabla</table>");
  
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    replayAll(doc);
    assertEquals(1, webUtilsService.countSections("(?=<table)", docRef));
    verifyAll(doc);
  }
  
  @Test
  public void testCountSections_several() throws XWikiException {
    DocumentReference
      docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
      transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    transDoc.setContent("abc<table>blabla</table><table>abc</table>");
  
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    replayAll(doc);
    assertEquals(3, webUtilsService.countSections("(?=<table)", docRef));
    verifyAll(doc);
  }

  @Test
  public void testGetSectionNr_negative() {
    assertEquals(1, webUtilsService.getSectionNr(-3, 5));
  }
  
  @Test
  public void testGetSectionNr_zero() {
    assertEquals(1, webUtilsService.getSectionNr(0, 5));
  }
  
  @Test
  public void testGetSectionNr_validNr() {
    assertEquals(3, webUtilsService.getSectionNr(3, 5));
  }

  @Test
  public void testGetSectionNr_toHighNr() {
    assertEquals(5, webUtilsService.getSectionNr(8, 5));
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
  public void testReplaceInternalWithExternalLinks_nothingToReplace() {
    String host = "www.bla.com";
    String test = "test";
    assertEquals(test, webUtilsService.replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_img_nothingToReplace() {
    String host = "www.bla.com/";
    String test = "<img src=\"http://www.bla.com/download/A/B/test.img\" />";
    assertEquals(test, webUtilsService.replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_img_newInternal() {
    String host = "www.bla.com";
    String test = "<img src=\"/download/A/B/test.img\" />";
    String result = "<img src=\"http://" + host + "/download/A/B/test.img\" />";
    assertEquals(result, webUtilsService.replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_img_dotInternal() {
    String host = "www.bla.com";
    String test = "<img src=\"../../download/A/B/test.img\" />";
    String result = "<img src=\"http://" + host + "/download/A/B/test.img\" />";
    assertEquals(result, webUtilsService.replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_href_nothingToReplace() {
    String host = "www.bla.com";
    String test = "<a href=\"http://www.bla.com/download/A/B/test.pdf\" >bla</a>";
    assertEquals(test, webUtilsService.replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"http://www.bla.com/skin/A/B/test.css\" >bla</a>";
    assertEquals(test, webUtilsService.replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"http://www.bla.com/view/A/B\" >bla</a>";
    assertEquals(test, webUtilsService.replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"http://www.bla.com/edit/A/B\" >bla</a>";
    assertEquals(test, webUtilsService.replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_href_newInternal() {
    String host = "www.bla.com";
    String test = "<a href=\"/download/A/B/test.pdf\" >bla</a>";
    String result = "<a href=\"http://" + host + "/download/A/B/test.pdf\" >bla</a>";
    assertEquals(result, webUtilsService.replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"/skin/A/B/test.css\" >bla</a>";
    result = "<a href=\"http://" + host + "/skin/A/B/test.css\" >bla</a>";
    assertEquals(result, webUtilsService.replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"/view/A/B\" >bla</a>";
    result = "<a href=\"http://" + host + "/view/A/B\" >bla</a>";
    assertEquals(result, webUtilsService.replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"/edit/A/B\" >bla</a>";
    result = "<a href=\"http://" + host + "/edit/A/B\" >bla</a>";
    assertEquals(result, webUtilsService.replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_href_dotInternal() {
    String host = "www.bla.com";
    String test = "<a href=\"../../download/A/B/test.pdf\" >bla</a>";
    String result = "<a href=\"http://" + host + "/download/A/B/test.pdf\" >bla</a>";
    assertEquals(result, webUtilsService.replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"../../skin/A/B/test.css\" >bla</a>";
    result = "<a href=\"http://" + host + "/skin/A/B/test.css\" >bla</a>";
    assertEquals(result, webUtilsService.replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"../../download/A/B\" >bla</a>";
    result = "<a href=\"http://" + host + "/download/A/B\" >bla</a>";
    assertEquals(result, webUtilsService.replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"../../download/A/B\" >bla</a>";
    result = "<a href=\"http://" + host + "/download/A/B\" >bla</a>";
    assertEquals(result, webUtilsService.replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testGetMajorVersion_nullDoc() {
    assertEquals("1", webUtilsService.getMajorVersion(null));
  }
  
  @Test
  public void testGetMajorVersion_noVersionSet() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(),
        "Space", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    assertEquals("1", webUtilsService.getMajorVersion(doc));
  }
  
  @Test
  public void testGetMajorVersion() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(),
        "Space", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setVersion("28.82");
    assertEquals("28", webUtilsService.getMajorVersion(doc));
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
