package com.celements.web.service;


import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.Cookie;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.User;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;
import com.xpn.xwiki.web.XWikiResponse;

public class PrepareVelocityContextServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private PrepareVelocityContextService prepVeloContextService;
  private XWikiDocument skinDoc;
  private XWikiRightService rightServiceMock;
  private DocumentReference curDocRef;
  private XWikiDocument currentDoc;
  private IPageTypeRole ptServiceMock;
  private IPageTypeResolverRole ptResolverMock;

  @Before
  public void setUp_PrepareVelocityContextServiceTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    rightServiceMock = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightServiceMock ).anyTimes();
    VelocityContext vContext = new VelocityContext();
    context.put("vcontext", vContext);
    prepVeloContextService = (PrepareVelocityContextService) Utils.getComponent(
        IPrepareVelocityContext.class);
    skinDoc = createMockAndAddToDefault(XWikiDocument.class);
    expect(skinDoc.getFullName()).andReturn("XWiki.Celements2Skin").anyTimes();
    expect(skinDoc.getDocumentReference()).andReturn(new DocumentReference(
        context.getDatabase(), "XWiki", "Celements2Skin")).anyTimes();
    expect(skinDoc.newDocument(same(context))).andReturn(new Document(skinDoc, context)
      ).anyTimes();
    expect(xwiki.getDocument(eq("celements2web:XWiki.Celements2Skin"), same(context))
      ).andReturn(skinDoc).anyTimes();
    expect(xwiki.getDocument(eq(new DocumentReference("celements2web", "XWiki",
        "Celements2Skin")), same(context))
      ).andReturn(skinDoc).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    ptResolverMock = createMockAndAddToDefault(IPageTypeResolverRole.class);
    prepVeloContextService.pageTypeResolver = ptResolverMock;
    ptServiceMock = createMockAndAddToDefault(IPageTypeRole.class);
    prepVeloContextService.pageTypeService = ptServiceMock;
    curDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    currentDoc = new XWikiDocument(curDocRef);
    context.setDoc(currentDoc);
  }

  @Test
  public void testPrepareVelocityContext_checkNPEs_forNull_Context() throws Exception {
    replayDefault();
    //context.setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it must be
    //set after calling replay
    context.setUser("XWiki.myTestUser");
    context.remove("vcontext");
    prepVeloContextService.prepareVelocityContext((XWikiContext)null);
    verifyDefault();
  }
  
//  @Test
//  public void testPrepareVelocityContext_checkNPEs_forNull_vContext() throws Exception {
//    expect(xwiki.isMultiLingual(same(context))).andReturn(false).atLeastOnce();
//    replayDefault();
//    //context.setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it must be
//    //set after calling replay
//    context.setUser("XWiki.myTestUser");
//    context.remove("vcontext");
//    prepVeloContextService.prepareVelocityContext(context);
//    verifyDefault();
//  }
//
//  @Test
//  public void testPrepareVelocityContext_checkNPEs_forNull_doc() throws Exception {
//    context.setDoc(null);
//    expect(xwiki.isMultiLingual(same(context))).andReturn(false).atLeastOnce();
//    expect(xwiki.getPluginApi(eq(prepVeloContextService.getVelocityName()), same(context))
//        ).andReturn(null
//      ).anyTimes();
//    expect(xwiki.getSkin(same(context))).andReturn("celements2web:Skins.CellSkin"
//        ).anyTimes();
//    DocumentReference cellSkinDoc = new DocumentReference("celements2web","Skins",
//        "CellSkin");
//    expect(xwiki.getDocument(eq(cellSkinDoc), same(context))).andReturn(new XWikiDocument(
//        cellSkinDoc)).atLeastOnce();
//    expect(xwiki.getUser(eq("XWiki.myTestUser"), same(context))
//        ).andReturn(new User(context.getXWikiUser(), context)).atLeastOnce();
//    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
//        "myTestUser");
//    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(
//        new XWikiDocument(userDocRef)).anyTimes();
//    expect(xwiki.getSpacePreference(eq("admin_language"), eq(""), same(context))
//        ).andReturn("").anyTimes();
//    expect(xwiki.Param("celements.admin_language")).andReturn("").anyTimes();
//    expect(skinDoc.getURL(eq("view"), same(context))).andReturn("").anyTimes();
//    // if context doc is null -> getPageTypeRefForCurrentDoc returns null
//    expect(ptResolverMock.getPageTypeRefForCurrentDoc()).andReturn(null).atLeastOnce();
//    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(true
//        ).atLeastOnce();
//    expect(xwiki.getDocument(eq("PageTypes.RichText"), same(context))).andReturn(
//        new XWikiDocument()).atLeastOnce();
//    expect(xwiki.getSpacePreference(eq("editbox_width"), same(context))).andReturn("123"
//        ).anyTimes();
//    expect(xwiki.getSpacePreference(eq("showRightPanels"), same(context))).andReturn(null
//        ).atLeastOnce();
//    expect(xwiki.getSpacePreference(eq("showLeftPanels"), same(context))).andReturn(null
//        ).atLeastOnce();
//    replayDefault();
//    //context.setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it must be
//    //set after calling replay
//    context.setUser("XWiki.myTestUser");
//    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
//    prepVeloContextService.prepareVelocityContext(vcontext);
//    verifyDefault();
//  }
//
//  @Test
//  public void testIsTdocLanguageWrong_false() {
//    context.setLanguage("fr");
//    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
//        "MyDoc");
//    XWikiDocument tdoc = new XWikiDocument(docRef);
//    tdoc.setDefaultLanguage("de");
//    tdoc.setLanguage("fr");
//    tdoc.setTranslation(1);
//    Document vTdocBefore = tdoc.newDocument(context);
//    replayDefault();
//    assertFalse(prepVeloContextService.isTdocLanguageWrong(vTdocBefore));
//    verifyDefault();
//  }
//
//  @Test
//  public void testFixTdocForInvalidLanguage_wrong_tdoc() throws Exception {
//    context.setLanguage("fr");
//    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
//        "MyDoc");
//    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
//    expect(docMock.getDocumentReference()).andReturn(docRef).anyTimes();
//    expect(docMock.getDefaultLanguage()).andReturn("de").anyTimes();
//    expect(docMock.getLanguage()).andReturn("").anyTimes();
//    XWikiDocument tdoc = new XWikiDocument(docRef);
//    tdoc.setDefaultLanguage("de");
//    tdoc.setLanguage("fr");
//    tdoc.setTranslation(1);
//    expect(docMock.getTranslatedDocument(same(context))).andReturn(tdoc);
//    context.setDoc(docMock);
//    VelocityContext vcontext = new VelocityContext();
//    replayDefault();
//    prepVeloContextService.fixTdocForInvalidLanguage(vcontext);
//    Document vTdoc = (Document) vcontext.get("tdoc");
//    assertEquals("fr", vTdoc.getLanguage());
//    verifyDefault();
//  }
//
//  @Test
//  public void testFixTdocForInvalidLanguage_right_tdoc() throws Exception {
//    context.setLanguage("fr");
//    //IMPORTANT: do not touch velocity context if the tdoc is in the correct language.
//    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
//        "MyDoc");
//    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
//    expect(docMock.getDocumentReference()).andReturn(docRef).anyTimes();
//    expect(docMock.getDefaultLanguage()).andReturn("de").anyTimes();
//    expect(docMock.getLanguage()).andReturn("").anyTimes();
//    XWikiDocument tdoc = new XWikiDocument(docRef);
//    tdoc.setDefaultLanguage("de");
//    tdoc.setLanguage("fr");
//    tdoc.setTranslation(1);
//    context.setDoc(docMock);
//    VelocityContext vcontext = new VelocityContext();
//    Document tdocBefore = tdoc.newDocument(context);
//    vcontext.put("tdoc", tdocBefore);
//    replayDefault();
//    prepVeloContextService.fixTdocForInvalidLanguage(vcontext);
//    Document vTdoc = (Document) vcontext.get("tdoc");
//    assertEquals("fr", vTdoc.getLanguage());
//    assertSame(tdocBefore, vTdoc);
//    verifyDefault();
//  }
//
//  @Test
//  public void testFixLanguagePreference() throws Exception {
//    VelocityContext vContext = new VelocityContext();
//    context.put("vcontext", vContext);
//    XWikiRequest requestMock = createMockAndAddToDefault(XWikiRequest.class);
//    context.setRequest(requestMock);
//    expect(xwiki.isMultiLingual(same(context))).andReturn(true).atLeastOnce();
//    expect(xwiki.getUserPreferenceFromCookie(eq("language"), same(context))
//        ).andReturn("").atLeastOnce();
//    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
//        "myTestUser");
//    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(
//        new XWikiDocument(userDocRef)).atLeastOnce();
//    expect(xwiki.Param(eq("xwiki.language.preferDefault"), eq("0"))).andReturn("0"
//        ).atLeastOnce();
//    expect(xwiki.getSpacePreference(eq("preferDefaultLanguage"), eq("0"), same(context))
//        ).andReturn("0").atLeastOnce();
//    expect(requestMock.getParameter(eq("language"))).andReturn("").atLeastOnce();
//    expect(requestMock.getHeader(eq("Accept-Language"))).andReturn("en,de").atLeastOnce();
//    Enumeration<Locale> testEnum = new Vector<Locale>(Arrays.asList(new Locale("en"),
//        new Locale("de"))).elements();
//    expect(requestMock.getLocales()).andReturn(testEnum).atLeastOnce();
//    expect(xwiki.Param(eq("xwiki.language.forceSupported"), eq("0"))).andReturn("1"
//        ).atLeastOnce();
//    expect(xwiki.getSpacePreference(eq("languages"), eq("mySpace"), eq(""), same(context))
//        ).andReturn("en,de,fr").atLeastOnce();
//    replayDefault();
//    //context.setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it must be
//    //set after calling replay
//    context.setUser("XWiki.myTestUser");
//    context.remove("vcontext");
//    vContext.put("language", "de");
//    prepVeloContextService.fixLanguagePreference(vContext);
//    verifyDefault();
//    assertEquals("en", vContext.get("language"));
//  }
//
//  @Test
//  public void testFixLanguagePreference_noAcceptLanguageValid() throws Exception {
//    VelocityContext vContext = new VelocityContext();
//    context.put("vcontext", vContext);
//    XWikiRequest requestMock = createMockAndAddToDefault(XWikiRequest.class);
//    context.setRequest(requestMock);
//    expect(xwiki.isMultiLingual(same(context))).andReturn(true).atLeastOnce();
//    expect(xwiki.getUserPreferenceFromCookie(eq("language"), same(context))
//        ).andReturn("").atLeastOnce();
//    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
//        "myTestUser");
//    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(
//        new XWikiDocument(userDocRef)).atLeastOnce();
//    expect(xwiki.Param(eq("xwiki.language.preferDefault"), eq("0"))).andReturn("0"
//        ).atLeastOnce();
//    expect(xwiki.getSpacePreference(eq("preferDefaultLanguage"), eq("0"), same(context))
//        ).andReturn("0").atLeastOnce();
//    expect(requestMock.getParameter(eq("language"))).andReturn("").atLeastOnce();
//    expect(requestMock.getHeader(eq("Accept-Language"))).andReturn("de").atLeastOnce();
//    Enumeration<Locale> testEnum = new Vector<Locale>(Arrays.asList(new Locale("de"))
//        ).elements();
//    expect(requestMock.getLocales()).andReturn(testEnum).atLeastOnce();
//    expect(xwiki.Param(eq("xwiki.language.forceSupported"), eq("0"))).andReturn("1"
//        ).atLeastOnce();
//    expect(xwiki.getSpacePreference(eq("languages"), eq("mySpace"), eq(""), same(context))
//        ).andReturn("en").atLeastOnce();
//    replayDefault();
//    //context.setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it must be
//    //set after calling replay
//    context.setUser("XWiki.myTestUser");
//    context.remove("vcontext");
//    vContext.put("language", "de");
//    prepVeloContextService.fixLanguagePreference(vContext);
//    verifyDefault();
//    assertEquals("", vContext.get("language"));
//  }
//
//  @Test
//  public void testFixLanguagePreference_invalidLanguageFromCookie() throws Exception {
//    VelocityContext vContext = new VelocityContext();
//    context.put("vcontext", vContext);
//    XWikiRequest requestMock = createMockAndAddToDefault(XWikiRequest.class);
//    context.setRequest(requestMock);
//    expect(xwiki.isMultiLingual(same(context))).andReturn(true).atLeastOnce();
//    expect(xwiki.getUserPreferenceFromCookie(eq("language"), same(context))
//        ).andReturn("ru").atLeastOnce(); // test invalid language from cookie
//    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
//        "myTestUser");
//    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(
//        new XWikiDocument(userDocRef)).atLeastOnce();
//    expect(xwiki.Param(eq("xwiki.language.preferDefault"), eq("0"))).andReturn("0"
//        ).atLeastOnce();
//    expect(xwiki.getSpacePreference(eq("preferDefaultLanguage"), eq("0"), same(context))
//        ).andReturn("0").atLeastOnce();
//    expect(requestMock.getParameter(eq("language"))).andReturn("").atLeastOnce();
//    expect(requestMock.getHeader(eq("Accept-Language"))).andReturn("de").atLeastOnce();
//    Enumeration<Locale> testEnum = new Vector<Locale>(Arrays.asList(new Locale("de"))
//        ).elements();
//    expect(requestMock.getLocales()).andReturn(testEnum).atLeastOnce();
//    expect(xwiki.Param(eq("xwiki.language.forceSupported"), eq("0"))).andReturn("1"
//        ).atLeastOnce();
//    expect(xwiki.getSpacePreference(eq("languages"), eq("mySpace"), eq(""), same(context))
//        ).andReturn("en").atLeastOnce();
//    replayDefault();
//    //context.setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it must be
//    //set after calling replay
//    context.setUser("XWiki.myTestUser");
//    context.remove("vcontext");
//    vContext.put("language", "de");
//    prepVeloContextService.fixLanguagePreference(vContext);
//    verifyDefault();
//    assertEquals("", vContext.get("language"));
//  }
//
//  @Test
//  public void testFixLanguagePreference_addCookieOnlyOnce() throws Exception {
//    VelocityContext vContext = new VelocityContext();
//    context.put("vcontext", vContext);
//    XWikiRequest requestMock = createMockAndAddToDefault(XWikiRequest.class);
//    context.setRequest(requestMock);
//    XWikiResponse responseMock = createMockAndAddToDefault(XWikiResponse.class);
//    context.setResponse(responseMock);
//    expect(xwiki.isMultiLingual(same(context))).andReturn(true).atLeastOnce();
//    expect(xwiki.getXWikiPreference(eq("celSuppressInvalidLang"),
//        eq("celements.language.suppressInvalid"), eq("0"), same(context))).andReturn("1"
//            ).atLeastOnce();
//    responseMock.addCookie(isA(Cookie.class));
//    expectLastCall().once();
//    expect(requestMock.getParameter(eq("language"))).andReturn("fr").atLeastOnce();
//    expect(xwiki.getSpacePreference(eq("languages"), eq("mySpace"), eq(""), same(context))
//        ).andReturn("en,de,fr").atLeastOnce();
//    replayDefault();
//    //context.setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it must be
//    //set after calling replay
//    context.setUser("XWiki.myTestUser");
//    context.remove("vcontext");
//    vContext.put("language", "de");
//    prepVeloContextService.fixLanguagePreference(vContext);
//    ExecutionContext execContext = prepVeloContextService.execution.getContext();
//    Object addCookieBefore = execContext.getProperty(
//        PrepareVelocityContextService.ADD_LANGUAGE_COOKIE_DONE);
//    assertNotNull(addCookieBefore);
//    assertTrue((Boolean)addCookieBefore);
//    //check addCookie is only called once
//    prepVeloContextService.fixLanguagePreference(vContext);
//    verifyDefault();
//    assertEquals("fr", vContext.get("language"));
//  }
//
//  @Test
//  public void testIsInvalidLanguageOrDefault_default() {
//    replayDefault();
//    assertTrue(prepVeloContextService.isInvalidLanguageOrDefault("default"));
//    verifyDefault();
//  }
//
//  @Test
//  public void testIsInvalidLanguageOrDefault_yes() {
//    expect(xwiki.getSpacePreference(eq("languages"), eq("mySpace"), eq(""), same(context))
//        ).andReturn("en,de").atLeastOnce();
//    expect(xwiki.getXWikiPreference(eq("celSuppressInvalidLang"),
//        eq("celements.language.suppressInvalid"), eq("0"), same(context))).andReturn("1"
//            ).atLeastOnce();
//    replayDefault();
//    assertFalse(prepVeloContextService.isInvalidLanguageOrDefault("en"));
//    verifyDefault();
//  }
//
//  @Test
//  public void testIsInvalidLanguageOrDefault_no() {
//    expect(xwiki.getSpacePreference(eq("languages"), eq("mySpace"), eq(""), same(context))
//        ).andReturn("en,de").atLeastOnce();
//    expect(xwiki.getXWikiPreference(eq("celSuppressInvalidLang"),
//        eq("celements.language.suppressInvalid"), eq("0"), same(context))).andReturn("1"
//            ).atLeastOnce();
//    replayDefault();
//    assertTrue(prepVeloContextService.isInvalidLanguageOrDefault("fr"));
//    verifyDefault();
//  }
//
//  @Test
//  public void testIsInvalidLanguageOrDefault_no_noSuppress() {
//    expect(xwiki.getXWikiPreference(eq("celSuppressInvalidLang"),
//        eq("celements.language.suppressInvalid"), eq("0"), same(context))).andReturn("0"
//            ).atLeastOnce();
//    replayDefault();
//    assertFalse(prepVeloContextService.isInvalidLanguageOrDefault("fr"));
//    verifyDefault();
//  }
//
//  @Test
//  public void testInitCelementsVelocity_checkNPEs_forEmptyVContext(
//      ) throws XWikiException {
//    VelocityContext vContext = new VelocityContext();
//    context.put("vcontext", vContext);
//    expect(xwiki.getPluginApi(eq(prepVeloContextService.getVelocityName()), same(context))
//        ).andReturn(null
//      ).anyTimes();
//    expect(xwiki.getSpacePreference(eq("language"), eq("mySpace"), eq(""), same(context))
//        ).andReturn("").anyTimes();
//    expect(xwiki.getSpacePreference(eq("languages"), eq("mySpace"), eq(""), same(context))
//        ).andReturn("").anyTimes();
//    expect(xwiki.getSpacePreference(eq("skin"), same(context))).andReturn(""
//      ).anyTimes();
//    expect(xwiki.getSpacePreference(eq("admin_language"), eq("de"), same(context))
//      ).andReturn("").anyTimes();
//    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
//        "myTestUser");
//    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(
//      new XWikiDocument(userDocRef)).anyTimes();
//    expect(skinDoc.getURL(eq("view"), same(context))).andReturn("").anyTimes();
//    expect(xwiki.getSpacePreference(eq("editbox_width"), same(context))).andReturn("123"
//      ).anyTimes();
//    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(true
//      ).atLeastOnce();
//    expect(xwiki.getDocument(eq("PageTypes.RichText"), same(context))).andReturn(
//        new XWikiDocument()).atLeastOnce();
//    expect(xwiki.getSkin(same(context))).andReturn("celements2web:Skins.CellSkin"
//        ).anyTimes();
//    DocumentReference cellSkinDoc = new DocumentReference("celements2web","Skins",
//        "CellSkin");
//    expect(xwiki.getDocument(eq(cellSkinDoc), same(context))).andReturn(new XWikiDocument(
//        cellSkinDoc)).atLeastOnce();
//    expect(xwiki.getUser(eq("XWiki.myTestUser"), same(context))
//      ).andReturn(new User(context.getXWikiUser(), context)).atLeastOnce();
//    XWikiGroupService groupServiceMock = createMockAndAddToDefault(
//        XWikiGroupService.class);
//    expect(xwiki.getGroupService(same(context))).andReturn(groupServiceMock).anyTimes();
//    List<DocumentReference> groupRefList = Collections.emptyList();
//    expect(groupServiceMock.getAllGroupsReferencesForMember(eq(userDocRef), eq(0), eq(0),
//        same(context))).andReturn(groupRefList).atLeastOnce();
//    context.setWiki(xwiki);
//    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq("XWiki.myTestUser"),
//        eq("mySpace.myDoc"), same(context))).andReturn(false).anyTimes();
//    expect(rightServiceMock.hasAdminRights(same(context))).andReturn(false).anyTimes();
//    expect(xwiki.Param("celements.admin_language")).andReturn("").anyTimes();
//    PageTypeReference ptRef = new PageTypeReference("RichText", "testProvider",
//        Arrays.asList("cat1"));
//    expect(ptResolverMock.getPageTypeRefForCurrentDoc()).andReturn(ptRef).atLeastOnce();
//    replayDefault();
//    //context.setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it must be
//    //set after calling replay
//    context.setUser("XWiki.myTestUser");
//    prepVeloContextService.initCelementsVelocity(vContext);
//    assertEquals("expecting tinyMCE_width be set.", "123", vContext.get("tinyMCE_width"));
//    verifyDefault();
//  }
//
//  @Test
//  public void testInitCelementsVelocity_checkNPEs_forMissingURLfactory(
//      ) throws XWikiException {
//    context.setURLFactory(null);
//    assertNull(context.getURLFactory());
//    VelocityContext vContext = new VelocityContext();
//    context.put("vcontext", vContext);
//    expect(xwiki.getPluginApi(eq(prepVeloContextService.getVelocityName()), same(context))
//        ).andReturn(null
//      ).anyTimes();
//    expect(xwiki.getSpacePreference(eq("language"), eq("mySpace"), eq(""), same(context))
//        ).andReturn("").anyTimes();
//    expect(xwiki.getSpacePreference(eq("languages"), eq("mySpace"), eq(""), same(context))
//        ).andReturn("").anyTimes();
//    expect(xwiki.getSpacePreference(eq("skin"), same(context))).andReturn(""
//      ).anyTimes();
//    expect(xwiki.getSpacePreference(eq("admin_language"), eq("de"), same(context))
//      ).andReturn("").anyTimes();
//    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
//        "myTestUser");
//    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(
//      new XWikiDocument(userDocRef)).anyTimes();
//    expect(skinDoc.getURL(eq("view"), same(context))).andThrow(
//        new NullPointerException()).anyTimes();
//    expect(xwiki.getSpacePreference(eq("editbox_width"), same(context))).andReturn("123"
//      ).anyTimes();
//    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(true
//      ).atLeastOnce();
//    expect(xwiki.getDocument(eq("PageTypes.RichText"), same(context))).andReturn(
//        new XWikiDocument()).atLeastOnce();
//    expect(xwiki.getSkin(same(context))).andReturn("celements2web:Skins.CellSkin"
//        ).anyTimes();
//    DocumentReference cellSkinDoc = new DocumentReference("celements2web","Skins",
//        "CellSkin");
//    expect(xwiki.getDocument(eq(cellSkinDoc), same(context))).andReturn(new XWikiDocument(
//        cellSkinDoc)).atLeastOnce();
//    expect(xwiki.getUser(eq("XWiki.myTestUser"), same(context))
//      ).andReturn(new User(context.getXWikiUser(), context)).atLeastOnce();
//    XWikiGroupService groupServiceMock = createMockAndAddToDefault(
//        XWikiGroupService.class);
//    expect(xwiki.getGroupService(same(context))).andReturn(groupServiceMock).anyTimes();
//    List<DocumentReference> groupRefList = Collections.emptyList();
//    expect(groupServiceMock.getAllGroupsReferencesForMember(eq(userDocRef), eq(0), eq(0),
//        same(context))).andReturn(groupRefList).atLeastOnce();
//    context.setWiki(xwiki);
//    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq("XWiki.myTestUser"),
//        eq("mySpace.myDoc"), same(context))).andReturn(false).anyTimes();
//    expect(rightServiceMock.hasAdminRights(same(context))).andReturn(false).anyTimes();
//    expect(xwiki.Param("celements.admin_language")).andReturn("").anyTimes();
//    PageTypeReference ptRef = new PageTypeReference("RichText", "testProvider",
//        Arrays.asList("cat1"));
//    expect(ptResolverMock.getPageTypeRefForCurrentDoc()).andReturn(ptRef).atLeastOnce();
//    replayDefault();
//    //context.setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it must be
//    //set after calling replay
//    context.setUser("XWiki.myTestUser");
//    prepVeloContextService.initCelementsVelocity(vContext);
//    assertEquals("expecting tinyMCE_width be set.", "123", vContext.get("tinyMCE_width"));
//    verifyDefault();
//  }
//
//  @Test
//  public void testInitPanelsVelocity_checkNPEs_forEmptyVContext() {
//    context.setDoc(null);
//    context.put("vcontext", new VelocityContext());
//    expect(xwiki.getSpacePreference(eq("showRightPanels"), same(context))).andReturn(null
//      ).atLeastOnce();
//    expect(xwiki.getSpacePreference(eq("showLeftPanels"), same(context))).andReturn(null
//      ).atLeastOnce();
//    replayDefault();
//    prepVeloContextService.initPanelsVelocity((VelocityContext) context.get("vcontext"));
//    verifyDefault();
//  }
//  
//  @Test
//  public void testGetRTEwidth_default() throws Exception {
//    expect(xwiki.getSpacePreference(eq("editbox_width"), same(context))).andReturn("");
//    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(true
//      ).atLeastOnce();
//    expect(xwiki.getDocument(eq("PageTypes.RichText"), same(context))).andReturn(
//        new XWikiDocument()).atLeastOnce();
//    replayDefault();
//    assertEquals("453", prepVeloContextService.getRTEwidth(context));
//    verifyDefault();
//  }
//  
//  @Test
//  public void testGetRTEwidth_preferences() throws Exception {
//    expect(xwiki.getSpacePreference(eq("editbox_width"), same(context))).andReturn("500");
//    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(true
//      ).atLeastOnce();
//    expect(xwiki.getDocument(eq("PageTypes.RichText"), same(context))).andReturn(
//        new XWikiDocument()).atLeastOnce();
//    replayDefault();
//    assertEquals("500", prepVeloContextService.getRTEwidth(context));
//    verifyDefault();
//  }
//
//  @Test
//  public void testGetRTEwidth_pageType() throws Exception {
//    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
//    context.setRequest(request);
//    expect(xwiki.getSpacePreference(eq("editbox_width"), same(context))).andReturn("500"
//        ).anyTimes();
//    XWikiDocument theDoc = new XWikiDocument(new DocumentReference(context.getDatabase(),
//        "MySpace", "myPage"));
//    BaseObject pageTypeObj = new BaseObject();
//    pageTypeObj.setStringValue("page_type", "SpecialRichText");
//    DocumentReference pagTypeClassRef = new DocumentReference(context.getDatabase(), 
//        "Celements2", "PageType");
//    pageTypeObj.setXClassReference(pagTypeClassRef);
//    theDoc.setXObjects(pagTypeClassRef, Arrays.asList(pageTypeObj));
//    context.setDoc(theDoc);
//    expect(request.get(eq("template"))).andReturn(null).anyTimes();
//    DocumentReference specialPTRef = new DocumentReference(context.getDatabase(),
//        "PageTypes", "SpecialRichText");
//    expect(xwiki.exists(eq("PageTypes.SpecialRichText"), same(context))).andReturn(true
//        ).atLeastOnce();
//    XWikiDocument pageTypeDoc = new XWikiDocument(specialPTRef);
//    expect(xwiki.getDocument(eq("PageTypes.SpecialRichText"), same(context))).andReturn(
//        pageTypeDoc).once();
//    BaseObject pageTypePropObj = new BaseObject();
//    pageTypePropObj.setIntValue("rte_width", 700);
//    DocumentReference pageTypePropClassRef = new DocumentReference(context.getDatabase(), 
//        "Celements2", "PageTypeProperties");
//    pageTypePropObj.setXClassReference(pageTypePropClassRef);
//    pageTypeDoc.setXObjects(pageTypePropClassRef, Arrays.asList(pageTypePropObj));
//    replayDefault();
//    assertEquals("700", prepVeloContextService.getRTEwidth(context));
//    verifyDefault();
//  }
//
//  @Test
//  public void testGetRightPanels() throws Exception {
//    List<String> expectedPanels = Arrays.asList("Panels.VideoPlayerPanel",
//        "Panels.GalleryPanel","Panels.BgPosEditPanel");
//    String ptConfigName = "Movie";
//    PageTypeReference ptRef = new PageTypeReference(ptConfigName,
//        "com.celements.XObjectPageTypeProvider", Arrays.asList(""));
//    expect(ptResolverMock.getPageTypeRefForCurrentDoc()).andReturn(ptRef).atLeastOnce();
//    DocumentReference pageTypeDocRef = new DocumentReference(context.getDatabase(),
//        "PageTypes", ptConfigName);
//    expect(xwiki.exists(eq("PageTypes.Movie"), same(context))).andReturn(true
//        ).atLeastOnce();
//    XWikiDocument ptConfigDoc = new XWikiDocument(pageTypeDocRef);
//    BaseObject rightPanelsConfigObj = new BaseObject();
//    DocumentReference panelsConfigClassRef = new DocumentReference(context.getDatabase(),
//        "Class", "PanelConfigClass");
//    rightPanelsConfigObj.setXClassReference(panelsConfigClassRef);
//    rightPanelsConfigObj.setStringValue("config_name", "rightPanels");
//    rightPanelsConfigObj.setIntValue("show_panels", 1);
//    rightPanelsConfigObj.setStringValue("panels", "Panels.VideoPlayerPanel,"
//        + "Panels.GalleryPanel,Panels.BgPosEditPanel");
//    ptConfigDoc.addXObject(rightPanelsConfigObj);
//    expect(xwiki.getDocument(eq("PageTypes.Movie"), same(context))).andReturn(ptConfigDoc
//        ).atLeastOnce();
//    replayDefault();
//    assertEquals(expectedPanels, prepVeloContextService.getRightPanels());
//    verifyDefault();
//  }
//
//  @Test
//  public void testGetLeftPanels() throws Exception {
//    List<String> expectedPanels = Arrays.asList("Panels.VideoPlayerPanel",
//        "Panels.GalleryPanel","Panels.BgPosEditPanel");
//    String ptConfigName = "Movie";
//    PageTypeReference ptRef = new PageTypeReference(ptConfigName,
//        "com.celements.XObjectPageTypeProvider", Arrays.asList(""));
//    expect(ptResolverMock.getPageTypeRefForCurrentDoc()).andReturn(ptRef).atLeastOnce();
//    DocumentReference pageTypeDocRef = new DocumentReference(context.getDatabase(),
//        "PageTypes", ptConfigName);
//    expect(xwiki.exists(eq("PageTypes.Movie"), same(context))).andReturn(true
//        ).atLeastOnce();
//    XWikiDocument ptConfigDoc = new XWikiDocument(pageTypeDocRef);
//    BaseObject leftPanelsConfigObj = new BaseObject();
//    DocumentReference panelsConfigClassRef = new DocumentReference(context.getDatabase(),
//        "Class", "PanelConfigClass");
//    leftPanelsConfigObj.setXClassReference(panelsConfigClassRef);
//    leftPanelsConfigObj.setStringValue("config_name", "leftPanels");
//    leftPanelsConfigObj.setIntValue("show_panels", 1);
//    leftPanelsConfigObj.setStringValue("panels", "Panels.VideoPlayerPanel,"
//        + "Panels.GalleryPanel,Panels.BgPosEditPanel");
//    ptConfigDoc.addXObject(leftPanelsConfigObj);
//    expect(xwiki.getDocument(eq("PageTypes.Movie"), same(context))).andReturn(ptConfigDoc
//        ).atLeastOnce();
//    replayDefault();
//    assertEquals(expectedPanels, prepVeloContextService.getLeftPanels());
//    verifyDefault();
//  }
//
//  @Test
//  public void testGetPaeTypeDoc_noContextDoc_NPE() {
//    context.setDoc(null);
//    replayDefault();
//    prepVeloContextService.getPageTypeDoc(context);
//    verifyDefault();
//  }
//
//  @Test
//  public void testGetPaeTypeDoc_noPageTypeRef_NPE() {
//    expect(ptResolverMock.getPageTypeRefForCurrentDoc()).andReturn(null).atLeastOnce();
//    replayDefault();
//    prepVeloContextService.getPageTypeDoc(context);
//    verifyDefault();
//  }

}
