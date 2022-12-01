package com.celements.web.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.auth.user.User;
import com.celements.auth.user.UserService;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.parents.IDocumentParentsListerRole;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.web.comparators.XWikiAttachmentAscendingChangeDateComparator;
import com.celements.web.comparators.XWikiAttachmentAscendingNameComparator;
import com.celements.web.comparators.XWikiAttachmentDescendingChangeDateComparator;
import com.celements.web.comparators.XWikiAttachmentDescendingNameComparator;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class WebUtilsServiceTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private IModelAccessFacade modelAccessMock;
  private WebUtilsService webUtilsService;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IDocumentParentsListerRole.class, IRightsAccessFacadeRole.class,
        UserService.class);
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    context = getContext();
    xwiki = getWikiMock();
    webUtilsService = (WebUtilsService) Utils.getComponent(IWebUtilsService.class);
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
  }

  @Test
  public void testGetParentForLevel_1() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getMock(IDocumentParentsListerRole.class).getDocumentParentsList(eq(docRef), eq(
        true))).andReturn(Collections.<DocumentReference>emptyList()).once();

    replayDefault();
    context.setDoc(doc);
    assertNull(webUtilsService.getParentForLevel(1)); // root
    verifyDefault();
  }

  @Test
  public void testGetParentForLevel_2() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    DocumentReference parentRef = new DocumentReference(context.getDatabase(), "mySpace",
        "parent1");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getMock(IDocumentParentsListerRole.class).getDocumentParentsList(eq(docRef), eq(
        true))).andReturn(Arrays.asList(docRef, parentRef)).once();
    replayDefault();
    context.setDoc(doc);
    assertEquals(parentRef, webUtilsService.getParentForLevel(2));
    verifyDefault();
  }

  @Test
  public void testGetParentForLevel_3() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    DocumentReference parentRef = new DocumentReference(context.getDatabase(), "mySpace",
        "parent1");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getMock(IDocumentParentsListerRole.class).getDocumentParentsList(eq(docRef), eq(
        true))).andReturn(Arrays.asList(docRef, parentRef)).once();
    replayDefault();
    context.setDoc(doc);
    assertEquals(docRef, webUtilsService.getParentForLevel(3));
    verifyDefault();
  }

  @Test
  public void testGetParentForLevel_IndexOutOfBounds() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    DocumentReference parentRef = new DocumentReference(context.getDatabase(), "mySpace",
        "parent1");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getMock(IDocumentParentsListerRole.class).getDocumentParentsList(eq(docRef), eq(
        true))).andReturn(Arrays.asList(docRef, parentRef)).times(3);
    replayDefault();
    context.setDoc(doc);
    assertNull(webUtilsService.getParentForLevel(4));
    assertNull(webUtilsService.getParentForLevel(0));
    assertNull(webUtilsService.getParentForLevel(-1));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testGetDocumentParentsList() throws Exception {
    boolean includeDoc = false;
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    DocumentReference parentRef = new DocumentReference(context.getDatabase(), "mySpace",
        "parent1");
    List<DocumentReference> docParentsList = Arrays.asList(parentRef);
    expect(getMock(IDocumentParentsListerRole.class).getDocumentParentsList(eq(docRef), eq(
        includeDoc))).andReturn(docParentsList).once();
    replayDefault();
    assertSame(docParentsList, webUtilsService.getDocumentParentsList(docRef, includeDoc));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testGetDocumentParentsList_includeDoc() throws Exception {
    boolean includeDoc = true;
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    DocumentReference parentRef = new DocumentReference(context.getDatabase(), "mySpace",
        "parent1");
    List<DocumentReference> docParentsList = Arrays.asList(parentRef);
    expect(getMock(IDocumentParentsListerRole.class).getDocumentParentsList(eq(docRef), eq(
        includeDoc))).andReturn(docParentsList).once();
    replayDefault();
    assertSame(docParentsList, webUtilsService.getDocumentParentsList(docRef, includeDoc));
    verifyDefault();
  }

  @Test
  public void testGetDocSectionAsJSON() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
        transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    transDoc.setContent("abc<table>blabla</table><table>abc</table>");

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    XWikiRenderingEngine mockRenderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(mockRenderer).atLeastOnce();
    expect(mockRenderer.renderText(eq("{pre}<table>blabla</table>{/pre}"), eq(context.getDoc()),
        same(context))).andReturn("<table>blabla</table>").atLeastOnce();

    replayDefault(doc, mockRenderer);
    String json = "[{\"content\" : \"<table>blabla</table>\", \"section\" : 2,"
        + " \"sectionNr\" : 3}]";
    assertEquals(json, webUtilsService.getDocSectionAsJSON("(?=<table)", docRef, 2));
    verifyDefault(doc, mockRenderer);
  }

  @Test
  public void testGetDocSection_empty() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
        transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(doc.getTranslatedDocument(same(context))).andReturn(new XWikiDocument(
        transDocRef)).once();

    replayDefault(doc);
    assertNull(webUtilsService.getDocSection("(?=<table)", docRef, 1));
    verifyDefault(doc);
  }

  @Test
  public void testGetDocSection_first() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
        transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    transDoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    XWikiRenderingEngine mockRenderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(mockRenderer).atLeastOnce();
    expect(mockRenderer.renderText(eq("{pre}abc{/pre}"), eq(context.getDoc()), same(
        context))).andReturn("abc").atLeastOnce();

    replayDefault(doc, mockRenderer);
    assertEquals("abc", webUtilsService.getDocSection("(?=<table)", docRef, 1));
    verifyDefault(doc, mockRenderer);
  }

  @Test
  public void testGetDocSection_firstEmptyRTE() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
        transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    transDoc.setContent("<p></p>  <br /> \n<table>blabla</table><table>abc</table>");
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    XWikiRenderingEngine mockRenderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(mockRenderer).atLeastOnce();
    expect(mockRenderer.renderText(eq("{pre}<table>blabla</table>{/pre}"), eq(context.getDoc()),
        same(context))).andReturn("<table>blabla</table>").atLeastOnce();

    replayDefault(doc, mockRenderer);
    assertEquals("<table>blabla</table>", webUtilsService.getDocSection("(?=<table)", docRef, 1));
    verifyDefault(doc, mockRenderer);
  }

  @Test
  public void testGetDocSection_middle() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
        transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    transDoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    XWikiRenderingEngine mockRenderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(mockRenderer).atLeastOnce();
    expect(mockRenderer.renderText(eq("{pre}<table>blabla</table>{/pre}"), eq(context.getDoc()),
        same(context))).andReturn("<table>blabla</table>").atLeastOnce();

    replayDefault(doc, mockRenderer);
    assertEquals("<table>blabla</table>", webUtilsService.getDocSection("(?=<table)", docRef, 2));
    verifyDefault(doc, mockRenderer);
  }

  @Test
  public void testGetDocSection_last() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
        transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    transDoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    XWikiRenderingEngine mockRenderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(mockRenderer).atLeastOnce();
    expect(mockRenderer.renderText(eq("{pre}<table>abc</table>{/pre}"), eq(context.getDoc()), same(
        context))).andReturn("<table>abc</table>").atLeastOnce();

    replayDefault(doc, mockRenderer);
    assertEquals("<table>abc</table>", webUtilsService.getDocSection("(?=<table)", docRef, 3));
    verifyDefault(doc, mockRenderer);
  }

  @Test
  public void testCountSections_empty() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
        transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).once();

    replayDefault(doc);
    assertEquals(0, webUtilsService.countSections("", docRef));
    verifyDefault(doc);
  }

  @Test
  public void testCountSections_one() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
        transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    transDoc.setContent("<table>blabla</table>");

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    replayDefault(doc);
    assertEquals(1, webUtilsService.countSections("(?=<table)", docRef));
    verifyDefault(doc);
  }

  @Test
  public void testCountSections_emptyRTEStart() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
        transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    transDoc.setContent("<table>blabla</table>");

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    replayDefault(doc);
    assertEquals(1, webUtilsService.countSections("(?=<table)", docRef));
    verifyDefault(doc);
  }

  @Test
  public void testCountSections_several() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "DocName"),
        transDocRef = new DocumentReference(context.getDatabase(), "Space", "TransDocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiDocument transDoc = new XWikiDocument(transDocRef);
    transDoc.setContent("abc<table>blabla</table><table>abc</table>");

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(transDoc).atLeastOnce();
    replayDefault(doc);
    assertEquals(3, webUtilsService.countSections("(?=<table)", docRef));
    verifyDefault(doc);
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
  public void test_getAdminLanguage_fromUser() throws Exception {
    context.setLanguage("de");
    User user = createMockAndAddToDefault(User.class);
    expect(user.getAdminLanguage()).andReturn(com.google.common.base.Optional.of("fr"));

    replayDefault();
    assertEquals("fr", webUtilsService.getAdminLanguage(user));
    verifyDefault();
  }

  @Test
  public void test_getAdminLanguage_defaultPreferences() throws Exception {
    context.setLanguage("de");
    User user = createMockAndAddToDefault(User.class);
    expect(user.getAdminLanguage()).andReturn(com.google.common.base.Optional.<String>absent());
    expect(xwiki.getSpacePreference("admin_language", "de", context)).andReturn("es");

    replayDefault();
    assertEquals("es", webUtilsService.getAdminLanguage(user));
    verifyDefault();
  }

  @Test
  public void test_getAdminLanguage_defaultSystem() throws Exception {
    String systemDefaultAdminLang = "en";
    context.setLanguage("de");
    User user = createMockAndAddToDefault(User.class);
    expect(user.getAdminLanguage()).andReturn(com.google.common.base.Optional.<String>absent());
    expect(xwiki.getSpacePreference("admin_language", "de", context)).andReturn("");
    expect(xwiki.Param("celements.admin_language")).andReturn("");

    replayDefault();
    assertEquals(systemDefaultAdminLang, webUtilsService.getAdminLanguage(user));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testSerializeRef_null() {
    assertNull(webUtilsService.serializeRef(null));
  }

  @Test
  @Deprecated
  public void testSerializeRef_local_null() {
    assertNull(webUtilsService.serializeRef(null, true));
  }

  @Test
  @Deprecated
  public void testSerializeRef_default_null() {
    assertNull(webUtilsService.serializeRef(null, false));
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
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    assertEquals("1", webUtilsService.getMajorVersion(doc));
  }

  @Test
  public void testGetMajorVersion() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Space", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setVersion("28.82");
    assertEquals("28", webUtilsService.getMajorVersion(doc));
  }

  @Test
  public void testResolveEntityReference_default() {
    WikiReference wikiRef = new WikiReference("db");
    SpaceReference ref = new SpaceReference("mySpace", wikiRef);
    EntityReference ret = webUtilsService.resolveEntityReference(
        webUtilsService.getRefDefaultSerializer().serialize(ref), EntityType.SPACE);
    assertEquals(wikiRef, webUtilsService.getWikiRef(ret));
  }

  @Test
  public void testResolveEntityReference_default_withRef() {
    WikiReference wikiRef = new WikiReference("db");
    WikiReference otherWikiRef = new WikiReference("otherDB");
    SpaceReference ref = new SpaceReference("mySpace", wikiRef);
    EntityReference ret = webUtilsService.resolveEntityReference(
        webUtilsService.getRefDefaultSerializer().serialize(ref), EntityType.SPACE, otherWikiRef);
    assertEquals(wikiRef, webUtilsService.getWikiRef(ret));
  }

  @Test
  public void testResolveEntityReference_default_withNullRef() {
    WikiReference wikiRef = new WikiReference("db");
    SpaceReference ref = new SpaceReference("mySpace", wikiRef);
    EntityReference ret = webUtilsService.resolveEntityReference(
        webUtilsService.getRefDefaultSerializer().serialize(ref), EntityType.SPACE, null);
    assertEquals(wikiRef, webUtilsService.getWikiRef(ret));
  }

  @Test
  public void testResolveEntityReference_local() {
    WikiReference wikiRef = new WikiReference("db");
    SpaceReference ref = new SpaceReference("mySpace", wikiRef);
    EntityReference ret = webUtilsService.resolveEntityReference(
        webUtilsService.getRefLocalSerializer().serialize(ref), EntityType.SPACE);
    assertEquals(new WikiReference(context.getDatabase()), webUtilsService.getWikiRef(ret));
  }

  @Test
  public void testResolveEntityReference_local_withRef() {
    WikiReference wikiRef = new WikiReference("db");
    WikiReference otherWikiRef = new WikiReference("otherDB");
    SpaceReference ref = new SpaceReference("mySpace", wikiRef);
    EntityReference ret = webUtilsService.resolveEntityReference(
        webUtilsService.getRefLocalSerializer().serialize(ref), EntityType.SPACE, otherWikiRef);
    assertEquals(otherWikiRef, webUtilsService.getWikiRef(ret));
  }

  @Test
  public void testResolveEntityReference_local_withNullRef() {
    EntityReference ret = webUtilsService.resolveEntityReference("mySpace", EntityType.SPACE, null);
    assertEquals(new WikiReference(context.getDatabase()), webUtilsService.getWikiRef(ret));
  }

  @Test
  public void testResolveRelativeEntityReference() {
    EntityReference relativeEntityRef = webUtilsService.resolveRelativeEntityReference(
        "MySpace.ParentDoc", EntityType.DOCUMENT);
    assertEquals(EntityReference.class, relativeEntityRef.getClass());
    assertNull(relativeEntityRef.extractReference(EntityType.WIKI));
    assertEquals("ParentDoc", relativeEntityRef.getName());
    assertEquals("MySpace", relativeEntityRef.extractReference(EntityType.SPACE).getName());
  }

  @Test
  public void test_resolveWikiReference() {
    String wikiName = "wiki";
    assertEquals(new WikiReference(wikiName), webUtilsService.resolveWikiReference(wikiName));
    assertEquals(webUtilsService.getWikiRef(), webUtilsService.resolveWikiReference(null));
    assertEquals(webUtilsService.getWikiRef(), webUtilsService.resolveWikiReference(""));
  }

  @Test
  public void testResolveSpaceReference() {
    WikiReference wikiRef = new WikiReference("db");
    SpaceReference spaceRef = new SpaceReference("mySpace", wikiRef);
    SpaceReference ret = webUtilsService.resolveSpaceReference(
        webUtilsService.getRefDefaultSerializer().serialize(spaceRef));
    assertEquals(spaceRef, ret);
  }

  @Test
  public void testResolveDocumentReference() {
    WikiReference wikiRef = new WikiReference("db");
    SpaceReference spaceRef = new SpaceReference("mySpace", wikiRef);
    DocumentReference docRef = new DocumentReference("myDoc", spaceRef);
    DocumentReference ret = webUtilsService.resolveDocumentReference(
        webUtilsService.getRefDefaultSerializer().serialize(docRef));
    assertEquals(docRef, ret);
  }

  @Test
  public void testResolveAttachmentReference() {
    WikiReference wikiRef = new WikiReference("db");
    SpaceReference spaceRef = new SpaceReference("mySpace", wikiRef);
    DocumentReference docRef = new DocumentReference("myDoc", spaceRef);
    AttachmentReference attRef = new AttachmentReference("myFile", docRef);
    AttachmentReference ret = webUtilsService.resolveAttachmentReference(
        webUtilsService.getRefDefaultSerializer().serialize(attRef));
    assertEquals(attRef, ret);
  }

  @Test
  public void testGetObjectsOrdered_docNull() {
    List<BaseObject> list = webUtilsService.getObjectsOrdered(null, getBOClassRef(), "", false);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testGetObjectsOrdered_noObjects() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), "S", "D"));
    List<BaseObject> list = webUtilsService.getObjectsOrdered(doc, getBOClassRef(), "s1", true,
        "s2", false);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testGetObjectsOrdered_oneObject() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), "S", "D"));
    doc.addXObject(getSortTestBaseObjects().get(0));
    List<BaseObject> list = webUtilsService.getObjectsOrdered(doc, getBOClassRef(), "s1", true,
        "s2", true);
    assertEquals(1, list.size());
  }

  @Test
  public void testGetObjectsOrdered_onlyOneFieldSort() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), "S", "D"));
    for (BaseObject obj : getSortTestBaseObjects()) {
      doc.addXObject(obj);
    }
    List<BaseObject> list = webUtilsService.getObjectsOrdered(doc, getBOClassRef(), "s1", true);
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
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), "S", "D"));
    for (BaseObject obj : getSortTestBaseObjects()) {
      doc.addXObject(obj);
    }
    List<BaseObject> list = webUtilsService.getObjectsOrdered(doc, getBOClassRef(), "s1", true, "d",
        true);
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
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), "S", "D"));
    for (BaseObject obj : getSortTestBaseObjects()) {
      doc.addXObject(obj);
    }
    List<BaseObject> list = webUtilsService.getObjectsOrdered(doc, getBOClassRef(), "i", false, "l",
        false);
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
    String testString = ("Market Leader. Business Grammar and" + " Usage, Band 1").substring(0, 35);
    String[] splitedStr = webUtilsService.splitStringByLength(testString, 35);
    assertEquals("Market Leader. Business Grammar and", splitedStr[0]);
    assertTrue("Expecting one Element but found [" + splitedStr.length + "].",
        splitedStr.length == 1);
  }

  @Test
  public void testSplitStringByLength() {
    String[] splitedStr = webUtilsService.splitStringByLength(
        "Market Leader. Business Grammar and Usage, Band 1", 35);
    assertEquals("Market Leader. Business Grammar and", splitedStr[0]);
    assertEquals(" Usage, Band 1", splitedStr[1]);
  }

  @Test
  public void testReduceListToSize_all() {
    Attachment att1 = createMock(Attachment.class);
    Attachment att2 = createMock(Attachment.class);
    Attachment att3 = createMock(Attachment.class);
    List<Attachment> attachments = Arrays.asList(att1, att2, att3);
    replayDefault(att1, att2, att3);
    List<Attachment> resultList = webUtilsService.reduceListToSize(attachments, 0, 5);
    assertEquals(3, resultList.size());
    assertTrue(resultList.contains(att1));
    assertTrue(resultList.contains(att2));
    assertTrue(resultList.contains(att3));
    verifyDefault(att1, att2, att3);
  }

  @Test
  public void testReduceListToSize_first() {
    Attachment att1 = createMock(Attachment.class);
    Attachment att2 = createMock(Attachment.class);
    Attachment att3 = createMock(Attachment.class);
    List<Attachment> attachments = Arrays.asList(att1, att2, att3);
    replayDefault(att1, att2, att3);
    List<Attachment> resultList = webUtilsService.reduceListToSize(attachments, 0, 2);
    assertEquals(2, resultList.size());
    assertTrue(resultList.contains(att1));
    assertTrue(resultList.contains(att2));
    assertFalse(resultList.contains(att3));
    verifyDefault(att1, att2, att3);
  }

  @Test
  public void testReduceListToSize_last() {
    Attachment att1 = createMock(Attachment.class);
    Attachment att2 = createMock(Attachment.class);
    Attachment att3 = createMock(Attachment.class);
    List<Attachment> attachments = Arrays.asList(att1, att2, att3);
    replayDefault(att1, att2, att3);
    List<Attachment> resultList = webUtilsService.reduceListToSize(attachments, 1, 5);
    assertEquals(2, resultList.size());
    assertFalse(resultList.contains(att1));
    assertTrue(resultList.contains(att2));
    assertTrue(resultList.contains(att3));
    verifyDefault(att1, att2, att3);
  }

  @Test
  public void testGetWikiRef() {
    String wikiName = context.getDatabase();
    replayDefault();
    assertEquals(new WikiReference(wikiName), webUtilsService.getWikiRef());
    verifyDefault();
  }

  @Test
  public void testGetWikiRef_attRef() {
    String wikiName = "myTestWikiName";
    AttachmentReference attRef = new AttachmentReference("myFile.jpg", new DocumentReference(
        wikiName, "mySpaceName", "myDocName"));
    replayDefault();
    assertEquals(new WikiReference(wikiName), webUtilsService.getWikiRef(attRef));
    verifyDefault();
  }

  @Test
  public void testGetWikiRef_docRef() {
    String wikiName = "myTestWikiName";
    DocumentReference docRef = new DocumentReference(wikiName, "mySpaceName", "myDocName");
    replayDefault();
    assertEquals(new WikiReference(wikiName), webUtilsService.getWikiRef(docRef));
    verifyDefault();
  }

  @Test
  public void testGetWikiRef_spaceRef() {
    WikiReference wikiRef = new WikiReference("myTestWikiName");
    SpaceReference spaceRef = new SpaceReference("mySpaceName", wikiRef);
    replayDefault();
    assertEquals(wikiRef, webUtilsService.getWikiRef(spaceRef));
    verifyDefault();
  }

  @Test
  public void testGetWikiRef_wikiRef() {
    WikiReference wikiRef = new WikiReference("myTestWikiName");
    replayDefault();
    assertEquals(wikiRef, webUtilsService.getWikiRef(wikiRef));
    verifyDefault();
  }

  @Test
  public void testGetWikiRef_entityRef() {
    EntityReference wikiRef = new EntityReference("myTestWikiName", EntityType.WIKI);
    EntityReference ref = new EntityReference("mySpaceName", EntityType.SPACE, wikiRef);
    replayDefault();
    assertEquals(wikiRef, webUtilsService.getWikiRef(ref));
    verifyDefault();
  }

  @Test
  public void testGetWikiRef_null() {
    String wikiName = context.getDatabase();
    replayDefault();
    assertEquals(new WikiReference(wikiName), webUtilsService.getWikiRef((DocumentReference) null));
    verifyDefault();
  }

  @Test
  public void testGetWikiRef_doc() {
    String wikiName = "myTestWikiName";
    DocumentReference docRef = new DocumentReference(wikiName, "mySpaceName", "myDocName");
    XWikiDocument doc = new XWikiDocument(docRef);
    replayDefault();
    assertEquals(new WikiReference(wikiName), webUtilsService.getWikiRef(doc));
    verifyDefault();
  }

  @Test
  public void testGetWikiRef_doc_null() {
    String wikiName = context.getDatabase();
    replayDefault();
    assertEquals(new WikiReference(wikiName), webUtilsService.getWikiRef((XWikiDocument) null));
    verifyDefault();
  }

  @Test
  public void testGetAllowedLanguages_NPE() {
    context.setDoc(null);
    replayDefault();
    List<String> resultList = Arrays.asList();
    assertEquals("Expect empty list if no context or current doc available.", resultList,
        webUtilsService.getAllowedLanguages());
    verifyDefault();
  }

  @Test
  public void testGetAllowedLanguages_WebPreferences() {
    DocumentReference curDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument currentDoc = new XWikiDocument(curDocRef);
    context.setDoc(currentDoc);
    expect(xwiki.getXWikiPreference(eq("languages"), same(context))).andReturn("fr,it").anyTimes();
    expect(xwiki.getSpacePreference(eq("languages"), eq("mySpace"), eq(""), same(
        context))).andReturn("de,en").once();
    replayDefault();
    List<String> resultList = Arrays.asList("de", "en");
    assertEquals("Expect languages from space preferences", resultList,
        webUtilsService.getAllowedLanguages());
    verifyDefault();
  }

  @Test
  public void testGetAllowedLanguages_spaceName_WebPreferences() {
    DocumentReference curDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument currentDoc = new XWikiDocument(curDocRef);
    context.setDoc(currentDoc);
    expect(xwiki.getXWikiPreference(eq("languages"), same(context))).andReturn("fr,it").anyTimes();
    expect(xwiki.getSpacePreference(eq("languages"), eq("testSpace"), eq(""), same(
        context))).andReturn("de,en").once();
    replayDefault();
    List<String> resultList = Arrays.asList("de", "en");
    assertEquals("Expect languages from space preferences", resultList,
        webUtilsService.getAllowedLanguages("testSpace"));
    verifyDefault();
  }

  @Test
  public void testGetAllowedLanguages_deprecatedUsageOfFieldLanguage() {
    DocumentReference curDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument currentDoc = new XWikiDocument(curDocRef);
    context.setDoc(currentDoc);
    expect(xwiki.getSpacePreference(eq("languages"), eq("mySpace"), eq(""), same(
        context))).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("language"), same(context))).andReturn("fr it").anyTimes();
    expect(xwiki.getSpacePreference(eq("language"), eq("mySpace"), eq(""), same(
        context))).andReturn("de en").once();
    replayDefault();
    List<String> resultList = Arrays.asList("de", "en");
    assertEquals("Expect languages from space preferences", resultList,
        webUtilsService.getAllowedLanguages());
    verifyDefault();
  }

  @Test
  public void testGetAllowedLanguages_spaceName_deprecatedUsageOfFieldLanguage() {
    DocumentReference curDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument currentDoc = new XWikiDocument(curDocRef);
    context.setDoc(currentDoc);
    expect(xwiki.getSpacePreference(eq("languages"), eq("testSpace"), eq(""), same(
        context))).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("language"), same(context))).andReturn("fr it").anyTimes();
    expect(xwiki.getSpacePreference(eq("language"), eq("testSpace"), eq(""), same(
        context))).andReturn("de en").once();
    replayDefault();
    List<String> resultList = Arrays.asList("de", "en");
    assertEquals("Expect languages from space preferences", resultList,
        webUtilsService.getAllowedLanguages("testSpace"));
    verifyDefault();
  }

  @Test
  public void testGetParentSpace() {
    expect(xwiki.getSpacePreference(eq("parent"), same(context))).andReturn(
        "parentSpaceName").atLeastOnce();
    replayDefault();
    assertEquals("parentSpaceName", webUtilsService.getParentSpace());
    verifyDefault();
  }

  @Test
  public void testGetParentSpace_spaceName() {
    expect(xwiki.getSpacePreference(eq("parent"), eq("mySpace"), eq(""), same(context))).andReturn(
        "parentSpaceName").atLeastOnce();
    replayDefault();
    assertEquals("parentSpaceName", webUtilsService.getParentSpace("mySpace"));
    verifyDefault();
  }

  @Test
  public void testGetRequestParameterMap_none() {
    replayDefault();
    Map<String, String[]> requestMap = webUtilsService.getRequestParameterMap();
    verifyDefault();

    assertNull(requestMap);
  }

  @Test
  public void testGetRequestParameterMap() {
    XWikiRequest mockXWikiRequest = createMock(XWikiRequest.class);
    context.setRequest(mockXWikiRequest);
    Map<Object, Object> requestMap = new HashMap<>();
    requestMap.put("asdf", "1");
    requestMap.put("qwer", new String[] { "2", "3" });

    expect(mockXWikiRequest.getParameterMap()).andReturn(requestMap).once();

    replayDefault(mockXWikiRequest);
    Map<String, String[]> request = webUtilsService.getRequestParameterMap();
    verifyDefault(mockXWikiRequest);

    assertNotNull(request);
    assertEquals(2, request.size());
    String[] arr1 = request.get("asdf");
    assertNotNull(arr1);
    assertEquals(1, arr1.length);
    assertEquals("1", arr1[0]);
    String[] arr2 = request.get("qwer");
    assertNotNull(arr2);
    assertEquals(2, arr2.length);
    assertEquals("2", arr2[0]);
    assertEquals("3", arr2[1]);
  }

  @Test
  public void testGetInheritedTemplatedPath_null() {
    replayDefault();
    assertNull(webUtilsService.getInheritedTemplatedPath(null));
    verifyDefault();
  }

  @Test
  public void testGetInheritedTemplatedPath_local_exists() {
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(), "Templates",
        "myView");
    expect(modelAccessMock.exists(localTemplateRef)).andReturn(true);
    replayDefault();
    assertEquals("Templates.myView", webUtilsService.getInheritedTemplatedPath(localTemplateRef));
    verifyDefault();
  }

  @Test
  public void testGetInheritedTemplatedPath_central_exists() {
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(), "Templates",
        "myView");
    expect(modelAccessMock.exists(localTemplateRef)).andReturn(false);
    DocumentReference centralTemplateRef = new DocumentReference("celements2web", "Templates",
        "myView");
    expect(modelAccessMock.exists(centralTemplateRef)).andReturn(true);
    replayDefault();
    assertEquals("celements2web:Templates.myView", webUtilsService.getInheritedTemplatedPath(
        localTemplateRef));
    verifyDefault();
  }

  @Test
  public void testGetInheritedTemplatedPath_inital_central_exists() {
    DocumentReference centralTemplateRef = new DocumentReference("celements2web", "Templates",
        "myView");
    expect(modelAccessMock.exists(centralTemplateRef)).andReturn(true);
    replayDefault();
    assertEquals("celements2web:Templates.myView", webUtilsService.getInheritedTemplatedPath(
        centralTemplateRef));
    verifyDefault();
  }

  @Test
  public void testGetInheritedTemplatedPath_inital_central_notExists() {
    DocumentReference centralTemplateRef = new DocumentReference("celements2web", "Templates",
        "myView");
    expect(modelAccessMock.exists(centralTemplateRef)).andReturn(false);
    replayDefault();
    assertEquals(":Templates.myView", webUtilsService.getInheritedTemplatedPath(
        centralTemplateRef));
    verifyDefault();
  }

  @Test
  public void testGetInheritedTemplatedPath_disk_no_local_no_central() {
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(), "Templates",
        "myView");
    expect(modelAccessMock.exists(localTemplateRef)).andReturn(false);
    DocumentReference centralTemplateRef = new DocumentReference("celements2web", "Templates",
        "myView");
    expect(modelAccessMock.exists(centralTemplateRef)).andReturn(false);
    replayDefault();
    assertEquals(":Templates.myView", webUtilsService.getInheritedTemplatedPath(localTemplateRef));
    verifyDefault();
  }

  @Test
  public void testGetInheritedTemplatedPath_disk_inital_central() {
    DocumentReference centralTemplateRef = new DocumentReference("celements2web", "Templates",
        "myView");
    expect(modelAccessMock.exists(centralTemplateRef)).andReturn(false);
    replayDefault();
    assertEquals(":Templates.myView", webUtilsService.getInheritedTemplatedPath(
        centralTemplateRef));
    verifyDefault();
  }

  @Test
  public void testGetTemplatePathOnDisk_Template() {
    assertEquals("/templates/celTemplates/myScript.vm", webUtilsService.getTemplatePathOnDisk(
        ":Templates.myScript"));
  }

  @Test
  public void testGetTemplatePathOnDisk_Ajax() {
    assertEquals("/templates/celAjax/myScript.vm", webUtilsService.getTemplatePathOnDisk(
        ":Ajax.myScript"));
  }

  @Test
  public void testRenderInheritableDocument_local_exists() throws Exception {
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(), "Templates",
        "myView");
    expect(modelAccessMock.exists(localTemplateRef)).andReturn(true);
    XWikiDocument localTemplateDoc = new XWikiDocument(localTemplateRef);
    localTemplateDoc.setDefaultLanguage("de");
    expect(modelAccessMock.getDocumentOpt(eq(localTemplateRef)))
        .andReturn(Optional.of(localTemplateDoc));
    String localScriptText = "my expected local script";
    localTemplateDoc.setContent(localScriptText);
    XWikiRenderingEngine mockRenderingEngine = createMockAndAddToDefault(
        XWikiRenderingEngine.class);
    expect(mockRenderingEngine.getRendererNames()).andReturn(Arrays.asList("velocity",
        "groovy")).anyTimes();
    String expectedRenderedText = "my expected rendered local script";
    expect(mockRenderingEngine.renderText(eq(localScriptText), same(localTemplateDoc),
        (XWikiDocument) isNull(), same(context))).andReturn(expectedRenderedText).once();
    replayDefault();
    webUtilsService.injectedRenderingEngine = mockRenderingEngine;
    assertEquals(expectedRenderedText, webUtilsService.renderInheritableDocument(localTemplateRef,
        "de"));
    verifyDefault();
  }

  @Test
  public void testRenderInheritableDocument_central_exists() throws Exception {
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(), "Templates",
        "myView");
    expect(modelAccessMock.exists(localTemplateRef)).andReturn(false);
    DocumentReference centralTemplateRef = new DocumentReference("celements2web", "Templates",
        "myView");
    expect(modelAccessMock.exists(centralTemplateRef)).andReturn(true);
    XWikiDocument centralTemplateDoc = new XWikiDocument(centralTemplateRef);
    centralTemplateDoc.setDefaultLanguage("en");
    expect(modelAccessMock.getDocumentOpt(eq(centralTemplateRef)))
        .andReturn(Optional.of(centralTemplateDoc));
    String centralScriptText = "my expected central script";
    centralTemplateDoc.setContent(centralScriptText);
    XWikiRenderingEngine mockRenderingEngine = createMockAndAddToDefault(
        XWikiRenderingEngine.class);
    expect(mockRenderingEngine.getRendererNames()).andReturn(Arrays.asList("velocity",
        "groovy")).anyTimes();
    String expectedRenderedText = "my expected rendered central script";
    expect(mockRenderingEngine.renderText(eq(centralScriptText), same(centralTemplateDoc),
        (XWikiDocument) isNull(), same(context))).andReturn(expectedRenderedText).once();
    replayDefault();
    webUtilsService.injectedRenderingEngine = mockRenderingEngine;
    assertEquals(expectedRenderedText, webUtilsService.renderInheritableDocument(localTemplateRef,
        "en"));
    verifyDefault();
  }

  @Test
  public void testRenderInheritableDocument_disk_langSpecific_no_local_no_central()
      throws Exception {
    XWikiRenderingEngine mockRenderingEngine = createMockAndAddToDefault(
        XWikiRenderingEngine.class);
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(), "Templates",
        "myView");
    expect(modelAccessMock.exists(localTemplateRef)).andReturn(false);
    DocumentReference centralTemplateRef = new DocumentReference("celements2web", "Templates",
        "myView");
    expect(modelAccessMock.exists(centralTemplateRef)).andReturn(false);
    String diskScriptText = "my expected disk script fr";
    expect(xwiki.getResourceContent(eq("/templates/celTemplates/myView_fr.vm"))).andReturn(
        diskScriptText).once();
    String expectedRenderedText = "my expected rendered disk script";
    expect(mockRenderingEngine.renderText(eq(diskScriptText), (XWikiDocument) isNull(),
        (XWikiDocument) isNull(), same(context))).andReturn(expectedRenderedText).once();
    replayDefault();
    webUtilsService.injectedRenderingEngine = mockRenderingEngine;
    // TODO check for language doc on disc
    assertEquals(expectedRenderedText, webUtilsService.renderInheritableDocument(localTemplateRef,
        "fr"));
    verifyDefault();
  }

  @Test
  public void testRenderInheritableDocument_disk_noLang_no_local_no_central() throws Exception {
    XWikiRenderingEngine mockRenderingEngine = createMockAndAddToDefault(
        XWikiRenderingEngine.class);
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(), "Templates",
        "myView");
    expect(modelAccessMock.exists(localTemplateRef)).andReturn(false);
    DocumentReference centralTemplateRef = new DocumentReference("celements2web", "Templates",
        "myView");
    expect(modelAccessMock.exists(centralTemplateRef)).andReturn(false);
    expect(xwiki.getResourceContent(eq("/templates/celTemplates/myView_fr.vm"))).andThrow(
        new IOException()).once();
    String diskScriptText = "my expected disk script";
    expect(xwiki.getResourceContent(eq("/templates/celTemplates/myView.vm"))).andReturn(
        diskScriptText).once();
    String expectedRenderedText = "my expected rendered disk script";
    expect(mockRenderingEngine.renderText(eq(diskScriptText), (XWikiDocument) isNull(),
        (XWikiDocument) isNull(), same(context))).andReturn(expectedRenderedText).once();
    replayDefault();
    webUtilsService.injectedRenderingEngine = mockRenderingEngine;
    assertEquals(expectedRenderedText, webUtilsService.renderInheritableDocument(localTemplateRef,
        "fr"));
    verifyDefault();
  }

  @Test
  public void testRenderInheritableDocument_disk_defLang_no_local_no_central() throws Exception {
    XWikiRenderingEngine mockRenderingEngine = createMockAndAddToDefault(
        XWikiRenderingEngine.class);
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(), "Templates",
        "myView");
    expect(modelAccessMock.exists(localTemplateRef)).andReturn(false);
    DocumentReference centralTemplateRef = new DocumentReference("celements2web", "Templates",
        "myView");
    expect(modelAccessMock.exists(centralTemplateRef)).andReturn(false);
    expect(xwiki.getResourceContent(eq("/templates/celTemplates/myView_fr.vm"))).andThrow(
        new IOException()).once();
    String diskScriptText = "my expected disk script";
    expect(xwiki.getResourceContent(eq("/templates/celTemplates/myView_en.vm"))).andReturn(
        diskScriptText).once();
    String expectedRenderedText = "my expected rendered disk script";
    expect(mockRenderingEngine.renderText(eq(diskScriptText), (XWikiDocument) isNull(),
        (XWikiDocument) isNull(), same(context))).andReturn(expectedRenderedText).once();
    replayDefault();
    webUtilsService.injectedRenderingEngine = mockRenderingEngine;
    assertEquals(expectedRenderedText, webUtilsService.renderInheritableDocument(localTemplateRef,
        "fr", "en"));
    verifyDefault();
  }

  @Test
  public void testGetCentralWikiRef() {
    assertEquals("celements2web", webUtilsService.getCentralWikiRef().getName());
  }

  @Test
  public void testFilterAttachmentsByTag_null() {
    List<Attachment> attachments = new ArrayList<>();
    attachments.add(new Attachment(null, null, getContext()));
    attachments.add(new Attachment(null, null, getContext()));
    List<Attachment> atts = webUtilsService.filterAttachmentsByTag(attachments, null);
    assertEquals(2, atts.size());
  }

  @Test
  public void testFilterAttachmentsByTag_empty() {
    List<Attachment> attachments = new ArrayList<>();
    attachments.add(new Attachment(null, null, getContext()));
    attachments.add(new Attachment(null, null, getContext()));
    List<Attachment> atts = webUtilsService.filterAttachmentsByTag(attachments, "");
    assertEquals(2, atts.size());
  }

  @Test
  public void testFilterAttachmentsByTag_tagDoesNotExist() {
    List<Attachment> attachments = new ArrayList<>();
    attachments.add(new Attachment(null, null, getContext()));
    attachments.add(new Attachment(null, null, getContext()));
    expect(xwiki.exists(eq(webUtilsService.resolveDocumentReference("Tag.T")), same(
        getContext()))).andReturn(false).once();
    replayDefault();
    List<Attachment> atts = webUtilsService.filterAttachmentsByTag(attachments, "Tag.T");
    verifyDefault();
    assertEquals(2, atts.size());
  }

  @Test
  public void testFilterAttachmentsByTag_filterHasNoTagLists() throws Exception {
    String tagName = "Tag.Tags";
    List<Attachment> attachments = new ArrayList<>();
    attachments.add(new Attachment(null, null, getContext()));
    attachments.add(new Attachment(null, null, getContext()));
    DocumentReference tagRef = webUtilsService.resolveDocumentReference(tagName);
    DocumentReference tagClassRef = new DocumentReference(getContext().getDatabase(), "Classes",
        "FilebaseTag");
    expect(xwiki.exists(eq(tagRef), same(getContext()))).andReturn(true).once();
    XWikiDocument tagDoc = createDocMock(tagRef);
    expect(xwiki.getDocument(eq(tagRef), same(getContext()))).andReturn(tagDoc).once();
    expect(tagDoc.getXObjectSize(eq(tagClassRef))).andReturn(0);
    replayDefault();
    List<Attachment> atts = webUtilsService.filterAttachmentsByTag(attachments, tagName);
    verifyDefault();
    assertEquals(2, atts.size());
  }

  @Test
  public void testFilterAttachmentsByTag() throws Exception {
    String tagName = "Tag.Tags";
    String docName = "Content_attachments.Filebase";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(),
        "Content_attachments", "Filebase");
    XWikiDocument theDoc = new XWikiDocument(docRef);
    DocumentReference tagRef = webUtilsService.resolveDocumentReference(tagName);
    DocumentReference tagClassRef = new DocumentReference(getContext().getDatabase(), "Classes",
        "FilebaseTag");
    expect(xwiki.exists(eq(tagRef), same(getContext()))).andReturn(true).once();
    XWikiDocument tagDoc = createMock(XWikiDocument.class);
    expect(tagDoc.clone()).andReturn(tagDoc).anyTimes();
    expect(xwiki.getDocument(eq(tagRef), same(getContext()))).andReturn(tagDoc).once();
    expect(tagDoc.getXObjectSize(eq(tagClassRef))).andReturn(3);
    expect(tagDoc.getXObject(eq(tagClassRef), eq("attachment"), eq(docName + "/abc.jpg"), eq(
        false))).andReturn(new BaseObject()).once();
    expect(tagDoc.getXObject(eq(tagClassRef), eq("attachment"), eq(docName + "/bcd.jpg"), eq(
        false))).andReturn(null).once();
    expect(tagDoc.getXObject(eq(tagClassRef), eq("attachment"), eq(docName + "/cde.jpg"), eq(
        false))).andReturn(new BaseObject()).once();
    List<Attachment> attachments = new ArrayList<>();
    attachments.add(new Attachment(theDoc.newDocument(getContext()), new XWikiAttachment(theDoc,
        "abc.jpg"), getContext()));
    attachments.add(new Attachment(theDoc.newDocument(getContext()), new XWikiAttachment(theDoc,
        "bcd.jpg"), getContext()));
    attachments.add(new Attachment(theDoc.newDocument(getContext()), new XWikiAttachment(theDoc,
        "cde.jpg"), getContext()));
    replayDefault(tagDoc);
    List<Attachment> atts = webUtilsService.filterAttachmentsByTag(attachments, tagName);
    verifyDefault(tagDoc);
    assertEquals(2, atts.size());
    assertEquals("abc.jpg", atts.get(0).getFilename());
    assertEquals("cde.jpg", atts.get(1).getFilename());
  }

  @Test
  public void testGetAttachment() throws Exception {
    DocumentReference attDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument attDoc = new XWikiDocument(attDocRef);
    XWikiAttachment att = new XWikiAttachment(attDoc, "myFile");
    attDoc.setAttachmentList(Arrays.asList(att));
    AttachmentReference attRef = new AttachmentReference(att.getFilename(), attDocRef);

    expect(xwiki.getDocument(eq(attDocRef), same(context))).andReturn(attDoc).once();

    replayDefault();
    XWikiAttachment ret = webUtilsService.getAttachment(attRef);
    verifyDefault();

    assertSame(att, ret);
  }

  @Test
  public void testGetAttachment_notFound() throws Exception {
    DocumentReference attDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument attDoc = new XWikiDocument(attDocRef);
    attDoc.setAttachmentList(Arrays.asList(new XWikiAttachment()));
    AttachmentReference attRef = new AttachmentReference("myFile", attDocRef);

    expect(xwiki.getDocument(eq(attDocRef), same(context))).andReturn(attDoc).once();

    replayDefault();
    XWikiAttachment ret = webUtilsService.getAttachment(attRef);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void testGetAttachment_XWE() throws Exception {
    DocumentReference attDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    AttachmentReference attRef = new AttachmentReference("myFile", attDocRef);
    Throwable cause = new XWikiException();

    expect(xwiki.getDocument(eq(attDocRef), same(context))).andThrow(cause).once();

    replayDefault();
    try {
      webUtilsService.getAttachment(attRef);
      fail("expecting XWikiException");
    } catch (XWikiException xwe) {
      assertSame(cause, xwe);
    }
    verifyDefault();
  }

  @Test
  public void testGetAttachmentApi() throws Exception {
    DocumentReference attDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument attDoc = new XWikiDocument(attDocRef);
    XWikiAttachment att = new XWikiAttachment(attDoc, "myFile");
    attDoc.setAttachmentList(Arrays.asList(att));
    AttachmentReference attRef = new AttachmentReference(att.getFilename(), attDocRef);

    expect(xwiki.getDocument(eq(attDocRef), same(context))).andReturn(attDoc).times(2);

    replayDefault();
    Attachment ret = webUtilsService.getAttachmentApi(attRef);
    verifyDefault();

    assertEquals(att.getFilename(), ret.getFilename());
    assertEquals(att.getDoc().getDocumentReference(), ret.getDocument().getDocumentReference());
  }

  @Test
  public void testGetAttachmentApi_notFound() throws Exception {
    DocumentReference attDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument attDoc = new XWikiDocument(attDocRef);
    attDoc.setAttachmentList(Arrays.asList(new XWikiAttachment()));
    AttachmentReference attRef = new AttachmentReference("myFile", attDocRef);

    expect(xwiki.getDocument(eq(attDocRef), same(context))).andReturn(attDoc).once();

    replayDefault();
    Attachment ret = webUtilsService.getAttachmentApi(attRef);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void testGetAttachmentApi_XWE() throws Exception {
    DocumentReference attDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    AttachmentReference attRef = new AttachmentReference("myFile", attDocRef);
    Throwable cause = new XWikiException();

    expect(xwiki.getDocument(eq(attDocRef), same(context))).andThrow(cause).once();

    replayDefault();
    try {
      webUtilsService.getAttachmentApi(attRef);
      fail("expecting XWikiException");
    } catch (XWikiException xwe) {
      assertSame(cause, xwe);
    }
    verifyDefault();
  }

  @Test
  public void testResolveEntityTypeForFullName_wiki() {
    String fullName = "myWiki";
    assertSame(EntityType.WIKI, webUtilsService.resolveEntityTypeForFullName(fullName));
    assertSame(EntityType.WIKI, webUtilsService.resolveEntityTypeForFullName(fullName,
        EntityType.WIKI));
  }

  @Test
  public void testResolveEntityTypeForFullName_space() {
    String fullName = "myWiki:mySpace";
    assertSame(EntityType.SPACE, webUtilsService.resolveEntityTypeForFullName(fullName));
  }

  @Test
  public void testResolveEntityTypeForFullName_space_local() {
    String fullName = "mySpace";
    assertSame(EntityType.SPACE, webUtilsService.resolveEntityTypeForFullName(fullName,
        EntityType.SPACE));
  }

  @Test
  public void testResolveEntityTypeForFullName_doc() {
    String fullName = "myWiki:mySpace.myDoc";
    assertSame(EntityType.DOCUMENT, webUtilsService.resolveEntityTypeForFullName(fullName));
  }

  @Test
  public void testResolveEntityTypeForFullName_doc_local() {
    String fullName = "mySpace.myDoc";
    assertSame(EntityType.DOCUMENT, webUtilsService.resolveEntityTypeForFullName(fullName));
  }

  @Test
  public void testResolveEntityTypeForFullName_att() {
    String fullName = "myWiki:mySpace.myDoc@myAtt.jpg";
    assertSame(EntityType.ATTACHMENT, webUtilsService.resolveEntityTypeForFullName(fullName));
  }

  @Test
  public void testResolveEntityTypeForFullName_att_local() {
    String fullName = "mySpace.myDoc@myAtt";
    assertSame(EntityType.ATTACHMENT, webUtilsService.resolveEntityTypeForFullName(fullName));
  }

  @Test
  public void testResolveEntityTypeForFullName_null() {
    String fullName = null;
    assertNull(webUtilsService.resolveEntityTypeForFullName(fullName));
  }

  @Test
  public void testResolveEntityTypeForFullName_empty() {
    String fullName = "";
    assertNull(webUtilsService.resolveEntityTypeForFullName(fullName));
  }

  @Test
  public void testResolveEntityTypeForFullName_invalid() {
    String fullName = "mySpace myDoc";
    EntityType entityType = webUtilsService.resolveEntityTypeForFullName(fullName);
    assertNull("expected null but got " + entityType, entityType);
  }

  @Test
  public void testResolveEntityTypeForFullName_specialChars() {
    assertEquals(EntityType.SPACE, webUtilsService.resolveEntityTypeForFullName("mySpace_myDoc"));
    assertEquals(EntityType.SPACE, webUtilsService.resolveEntityTypeForFullName("mySpace-myDoc"));
  }

  @Test
  public void testGetAttachmentListSorted_none() {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);

    expect(docMock.getAttachmentList()).andReturn(Collections.<XWikiAttachment>emptyList()).once();

    replayDefault();
    List<XWikiAttachment> ret = webUtilsService.getAttachmentListSorted(docMock, null);
    verifyDefault();

    assertEquals(0, ret.size());
  }

  @Test
  public void testGetAttachmentListSorted_single() {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    XWikiAttachment att = createMockAndAddToDefault(XWikiAttachment.class);

    expect(docMock.getAttachmentList()).andReturn(Arrays.asList(att)).once();

    replayDefault();
    List<XWikiAttachment> ret = webUtilsService.getAttachmentListSorted(docMock, null);
    verifyDefault();

    assertEquals(1, ret.size());
    assertSame(att, ret.get(0));
  }

  @Test
  public void testGetAttachmentListSorted_noComparator() {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    XWikiAttachment att1 = createMockAndAddToDefault(XWikiAttachment.class);
    XWikiAttachment att2 = createMockAndAddToDefault(XWikiAttachment.class);

    expect(docMock.getAttachmentList()).andReturn(Arrays.asList(att1, att2)).once();

    replayDefault();
    List<XWikiAttachment> ret = webUtilsService.getAttachmentListSorted(docMock, null);
    verifyDefault();

    assertEquals(2, ret.size());
    assertSame(att1, ret.get(0));
    assertSame(att2, ret.get(1));
  }

  @Test
  public void testGetAttachmentListSorted_name_asc() {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    XWikiAttachment att1 = createMockAndAddToDefault(XWikiAttachment.class);
    XWikiAttachment att2 = createMockAndAddToDefault(XWikiAttachment.class);

    expect(docMock.getAttachmentList()).andReturn(Arrays.asList(att1, att2)).once();
    expect(att1.getFilename()).andReturn("name2").once();
    expect(att2.getFilename()).andReturn("name1").once();

    replayDefault();
    List<XWikiAttachment> ret = webUtilsService.getAttachmentListSorted(docMock,
        new XWikiAttachmentAscendingNameComparator());
    verifyDefault();

    assertEquals(2, ret.size());
    assertSame(att2, ret.get(0));
    assertSame(att1, ret.get(1));
  }

  @Test
  public void testGetAttachmentListSorted_name_desc() {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    XWikiAttachment att1 = createMockAndAddToDefault(XWikiAttachment.class);
    XWikiAttachment att2 = createMockAndAddToDefault(XWikiAttachment.class);

    expect(docMock.getAttachmentList()).andReturn(Arrays.asList(att1, att2)).once();
    expect(att1.getFilename()).andReturn("name1").once();
    expect(att2.getFilename()).andReturn("name2").once();

    replayDefault();
    List<XWikiAttachment> ret = webUtilsService.getAttachmentListSorted(docMock,
        new XWikiAttachmentDescendingNameComparator());
    verifyDefault();

    assertEquals(2, ret.size());
    assertSame(att2, ret.get(0));
    assertSame(att1, ret.get(1));
  }

  @Test
  public void testGetAttachmentListSorted_date_asc() {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    XWikiAttachment att1 = createMockAndAddToDefault(XWikiAttachment.class);
    XWikiAttachment att2 = createMockAndAddToDefault(XWikiAttachment.class);

    expect(docMock.getAttachmentList()).andReturn(Arrays.asList(att1, att2)).once();
    expect(att1.getDate()).andReturn(new Date(1)).once();
    expect(att2.getDate()).andReturn(new Date(0)).once();

    replayDefault();
    List<XWikiAttachment> ret = webUtilsService.getAttachmentListSorted(docMock,
        new XWikiAttachmentAscendingChangeDateComparator());
    verifyDefault();

    assertEquals(2, ret.size());
    assertSame(att2, ret.get(0));
    assertSame(att1, ret.get(1));
  }

  @Test
  public void testGetAttachmentListSorted_date_desc() {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    XWikiAttachment att1 = createMockAndAddToDefault(XWikiAttachment.class);
    XWikiAttachment att2 = createMockAndAddToDefault(XWikiAttachment.class);

    expect(docMock.getAttachmentList()).andReturn(Arrays.asList(att1, att2)).once();
    expect(att1.getDate()).andReturn(new Date(0)).once();
    expect(att2.getDate()).andReturn(new Date(1)).once();

    replayDefault();
    List<XWikiAttachment> ret = webUtilsService.getAttachmentListSorted(docMock,
        new XWikiAttachmentDescendingChangeDateComparator());
    verifyDefault();

    assertEquals(2, ret.size());
    assertSame(att2, ret.get(0));
    assertSame(att1, ret.get(1));
  }

  @Test
  public void testGetAttachmentListSorted_imageOnly() {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    XWikiAttachment att1 = createMockAndAddToDefault(XWikiAttachment.class);
    XWikiAttachment att2 = createMockAndAddToDefault(XWikiAttachment.class);
    XWikiAttachment att3 = createMockAndAddToDefault(XWikiAttachment.class);

    expect(docMock.getAttachmentList()).andReturn(Arrays.asList(att1, att2, att3)).once();
    expect(att1.isImage(same(context))).andReturn(true).once();
    expect(att2.isImage(same(context))).andReturn(false).once();
    expect(att3.isImage(same(context))).andReturn(true).once();

    replayDefault();
    List<XWikiAttachment> ret = webUtilsService.getAttachmentListSorted(docMock, null, true);
    verifyDefault();

    assertEquals(2, ret.size());
    assertSame(att1, ret.get(0));
    assertSame(att3, ret.get(1));
  }

  @Test
  public void testGetAttachmentListSorted_reduced() {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    XWikiAttachment att1 = createMockAndAddToDefault(XWikiAttachment.class);
    XWikiAttachment att2 = createMockAndAddToDefault(XWikiAttachment.class);
    XWikiAttachment att3 = createMockAndAddToDefault(XWikiAttachment.class);
    XWikiAttachment att4 = createMockAndAddToDefault(XWikiAttachment.class);

    expect(docMock.getAttachmentList()).andReturn(Arrays.asList(att1, att2, att3, att4)).once();

    replayDefault();
    List<XWikiAttachment> ret = webUtilsService.getAttachmentListSorted(docMock, null, false, 1, 2);
    verifyDefault();

    assertEquals(2, ret.size());
    assertSame(att2, ret.get(0));
    assertSame(att3, ret.get(1));
  }

  @Test
  public void testGetDefaultLanguage_cfgSrc() throws Exception {
    String lang = "xk";
    getConfigurationSource().setProperty(ModelContext.CFG_KEY_DEFAULT_LANG, lang);
    replayDefault();
    assertEquals(lang, webUtilsService.getDefaultLanguage());
    verifyDefault();
  }

  @Test
  public void testGetDefaultLanguage_withSpaceRef() throws Exception {
    final String lang = "en";
    final WikiReference wikiRef = new WikiReference("wiki");
    final SpaceReference spaceRef = new SpaceReference("space", wikiRef);
    DocumentReference webPrefDocRef = new DocumentReference("WebPreferences", spaceRef);

    expect(modelAccessMock.exists(webPrefDocRef)).andReturn(true);
    expect(modelAccessMock.getOrCreateDocument(webPrefDocRef)).andReturn(new XWikiDocument(
        webPrefDocRef));

    assertEquals("xwikidb", getContext().getDatabase());
    assertNull(getContext().getDoc());
    replayDefault();
    assertEquals(lang, webUtilsService.getDefaultLanguage(spaceRef));
    verifyDefault();
    assertEquals("xwikidb", getContext().getDatabase());
    assertNull(getContext().getDoc());
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private DocumentReference getBOClassRef() {
    return new DocumentReference(context.getDatabase(), "Classes", "TestClass");
  }

  private List<BaseObject> getSortTestBaseObjects() {
    List<BaseObject> objs = new ArrayList<>();
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
