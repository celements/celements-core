package com.celements.ressource_url;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.reference.RefBuilder;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

public class RessourceUrlServiceTest extends AbstractComponentTest {

  private IModelAccessFacade modelAccessMock;
  private XWikiContext context;
  private XWikiURLFactory mockURLFactory;
  private RessourceUrlServiceRole resUrlServ;
  private XWiki wiki;

  @Before
  public void setUp_RessourceUrlServiceTest() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    context = getContext();
    wiki = getWikiMock();
    mockURLFactory = createMockAndAddToDefault(XWikiURLFactory.class);
    context.setURLFactory(mockURLFactory);
    resUrlServ = (RessourceUrlServiceRole) Utils.getComponent(RessourceUrlServiceRole.class);
  }

  @Test
  public void test_createRessourceUrl_fullURL() throws Exception {
    assertEquals("http://www.bla.com/bla.txt", resUrlServ.createRessourceUrl(
        "http://www.bla.com/bla.txt", Optional.empty()));
  }

  @Test
  public void test_createRessourceUrl_partURL() throws Exception {
    assertEquals("/xwiki/bin/download/A/B/bla.txt", resUrlServ.createRessourceUrl(
        "/xwiki/bin/download/A/B/bla.txt", Optional.empty()));
  }

  @Test
  public void test_createRessourceUrl_dynamicParamURL() throws Exception {
    String mySpaceName = "mySpace";
    String myDocName = "myDoc";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), mySpaceName,
        myDocName);
    XWikiDocument doc = new XWikiDocument(myDocRef);
    context.setDoc(doc);
    URL viewURL = new URL("http://localhost/mySpace/myDoc");
    expect(mockURLFactory.createURL(eq(mySpaceName), eq(myDocName), eq("view"), (String) isNull(),
        (String) isNull(), eq(context.getDatabase()), same(context))).andReturn(viewURL);
    expect(mockURLFactory.getURL(eq(viewURL), same(context))).andReturn(viewURL.getPath());
    replayDefault();
    assertEquals("/mySpace/myDoc?xpage=bla&bli=blu", resUrlServ.createRessourceUrl(
        "?xpage=bla&bli=blu", Optional.empty()));
    verifyDefault();
  }

  @Test
  public void test_createRessourceUrl_fullInternalLink() throws Exception {
    String resultURL = "http://celements2web.localhost/file/A/B/bla.txt";
    DocumentReference abDocRef = new RefBuilder().wiki("celements2web").space("A").doc("B")
        .build(DocumentReference.class);
    XWikiDocument abDoc = new XWikiDocument(abDocRef);
    List<XWikiAttachment> attachList = new ArrayList<>();
    XWikiAttachment blaAtt = new XWikiAttachment();
    String attName = "bla.txt";
    blaAtt.setFilename(attName);
    blaAtt.setDoc(abDoc);
    attachList.add(blaAtt);
    abDoc.setAttachmentList(attachList);
    URL tstURL = new URL(resultURL);
    expect(mockURLFactory.createAttachmentURL(eq("bla.txt"), eq("A"), eq("B"), eq("file"),
        (String) eq(null), eq("celements2web"), same(context))).andReturn(tstURL);
    expect(mockURLFactory.getURL(eq(tstURL), same(context))).andReturn(resultURL);
    expect(modelAccessMock.getDocument(eq(abDocRef))).andReturn(abDoc).atLeastOnce();
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("file");
    expect(modelAccessMock.getAttachmentNameEqual(same(abDoc), eq(attName))).andReturn(blaAtt)
        .atLeastOnce();
    replayDefault();
    String attachmentURL = resUrlServ.createRessourceUrl("celements2web:A.B;bla.txt",
        Optional.empty());
    assertNotNull(attachmentURL);
    assertTrue("expecting " + resultURL + " but got " + attachmentURL,
        attachmentURL.matches(resultURL + "\\?version=\\d{14}"));
    verifyDefault();
  }

  @Test
  public void test_createRessourceUrl_partInternalLink() throws Exception {
    String resultURL = "http://mydomain.ch/file/A/B/bla.txt";
    URL tstURL = new URL(resultURL);
    expect(mockURLFactory.createAttachmentURL(eq("bla.txt"), eq("A"), eq("B"), eq("file"),
        (String) eq(null), eq(context.getDatabase()), same(context))).andReturn(tstURL);
    expect(mockURLFactory.getURL(eq(tstURL), same(context))).andReturn(resultURL);
    DocumentReference abDocRef = new RefBuilder().wiki(getContext().getDatabase()).space("A")
        .doc("B").build(DocumentReference.class);
    XWikiDocument abDoc = new XWikiDocument(abDocRef);
    List<XWikiAttachment> attachList = new ArrayList<>();
    XWikiAttachment blaAtt = new XWikiAttachment();
    String attName = "bla.txt";
    blaAtt.setFilename(attName);
    blaAtt.setDoc(abDoc);
    attachList.add(blaAtt);
    abDoc.setAttachmentList(attachList);
    expect(modelAccessMock.getDocument(eq(abDocRef))).andReturn(abDoc).atLeastOnce();
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("file");
    expect(modelAccessMock.getAttachmentNameEqual(same(abDoc), eq(attName))).andReturn(blaAtt)
        .atLeastOnce();
    replayDefault();
    String attachmentURL = resUrlServ.createRessourceUrl("A.B;bla.txt", Optional.empty());
    assertNotNull(attachmentURL);
    assertTrue("expecting " + resultURL + " but got " + attachmentURL,
        attachmentURL.matches(resultURL + "\\?version=\\d{14}"));
    verifyDefault();
  }

  @Test
  public void test_createRessourceUrl_partInternalLink_notExists() throws Exception {
    DocumentReference abDocRef = new RefBuilder().wiki(getContext().getDatabase()).space("A")
        .doc("B").build(DocumentReference.class);
    XWikiDocument abDoc = new XWikiDocument(abDocRef);
    expect(modelAccessMock.getDocument(eq(abDocRef))).andReturn(abDoc).atLeastOnce();
    String attName = "bla.txt";
    AttachmentReference attRef = new RefBuilder().with(abDocRef).att(attName)
        .build(AttachmentReference.class);
    expect(modelAccessMock.getAttachmentNameEqual(same(abDoc), eq(attName)))
        .andThrow(new AttachmentNotExistsException(attRef)).atLeastOnce();
    replayDefault();
    assertThrows(UrlRessourceNotExistException.class,
        () -> resUrlServ.createRessourceUrl("A.B;bla.txt", Optional.empty()));
    verifyDefault();
  }

  @Test
  public void test_createRessourceUrl_onDiskLink() throws Exception {
    String resultURL = "/appname/skin/resources/celJS/bla.js";
    expect(wiki.getSkinFile(eq("celJS/bla.js"), eq(true), same(context))).andReturn(resultURL);
    expect(wiki.getResourceLastModificationDate(eq("resources/celJS/bla.js"))).andReturn(
        new Date());
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("download");
    replayDefault();
    String attachmentURL = resUrlServ.createRessourceUrl(" :celJS/bla.js", Optional.empty(),
        Optional.empty());
    String expectedURL = "/appname/download/resources/celJS/bla.js";
    assertNotNull(attachmentURL);
    assertTrue("expecting " + expectedURL + " but got " + attachmentURL,
        attachmentURL.matches(expectedURL + "\\?version=\\d{14}"));
    verifyDefault();
  }

  @Test
  public void test_createRessourceUrl_Rubish() throws Exception {
    assertEquals("http://A.B;bla.txt",
        resUrlServ.createRessourceUrl("http://A.B;bla.txt", Optional.empty(),
            Optional.empty()).toString());
  }

  @Test
  public void test_isAttachmentLink_null() {
    assertFalse(resUrlServ.isAttachmentLink(null));
  }

  @Test
  public void test_isAttachmentLink_empty() {
    assertFalse(resUrlServ.isAttachmentLink(""));
  }

  @Test
  public void test_isAttachmentLink_url() {
    assertFalse(resUrlServ.isAttachmentLink("/download/Space/Page/attachment.jpg"));
  }

  @Test
  public void test_isAttachmentLink_is() {
    assertTrue(resUrlServ.isAttachmentLink("Space.Page;attachment.jpg"));
  }

  @Test
  public void test_isAttachmentLink_isSpecialChars() {
    assertTrue(resUrlServ.isAttachmentLink("Teilnehmer.f8Nx9vyPOX8O2;Hans-002-Bearbeitet-2.jpg"));
  }

  @Test
  public void test_isAttachmentLink_isWithDb() {
    assertTrue(resUrlServ.isAttachmentLink("db:Space.Page;attachment.jpg"));
  }

  @Test
  public void test_isOnDiskLink_true() {
    assertTrue(resUrlServ.isOnDiskLink(":bla.js"));
    assertTrue(resUrlServ.isOnDiskLink(" :celJS/bla.js"));
  }

  @Test
  public void test_isOnDiskLink_false() {
    assertFalse(resUrlServ.isOnDiskLink("bla.js"));
    assertFalse(resUrlServ.isOnDiskLink("x:celJS/bla.js"));
    assertFalse(resUrlServ.isOnDiskLink("x:A.B;bla.js"));
  }

  @Test
  public void test_getRessourceURLPrefix() throws Exception {
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("file");
    expect(mockURLFactory.createResourceURL(eq(""), eq(true), same(context))).andReturn(new URL(
        "http://test.fabian.dev:10080/skin/resources/"));
    replayDefault();
    assertEquals("http://test.fabian.dev:10080/file/resources/",
        resUrlServ.getRessourceURLPrefix());
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_onDisk_queryString() throws Exception {
    String resultURL = "/appname/skin/resources/celJS/bla.js";
    expect(wiki.getSkinFile(eq("celJS/bla.js"), eq(true), same(context))).andReturn(resultURL);
    expect(wiki.getResourceLastModificationDate(eq("resources/celJS/bla.js"))).andReturn(
        new Date());
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("download");
    String queryString = "asf=oiu";
    replayDefault();
    String attachmentURL = resUrlServ.createRessourceUrl(":celJS/bla.js", Optional.empty(),
        Optional.of(queryString));
    String expectedURL = "/appname/download/resources/celJS/bla.js";
    assertTrue(attachmentURL,
        attachmentURL.matches(expectedURL + "\\?version=\\d{14}\\&" + queryString));
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_partInternalLink_queryString() throws Exception {
    String resultURL = "http://mydomain.ch/testAction/A/B/bla.txt";
    URL tstURL = new URL(resultURL);
    expect(mockURLFactory.createAttachmentURL(eq("bla.txt"), eq("A"), eq("B"), eq("testAction"),
        (String) eq(null), eq(context.getDatabase()), same(context))).andReturn(tstURL);
    expect(mockURLFactory.getURL(eq(tstURL), same(context))).andReturn(resultURL);
    DocumentReference abDocRef = new RefBuilder().wiki(getContext().getDatabase()).space("A")
        .doc("B").build(DocumentReference.class);
    XWikiDocument abDoc = new XWikiDocument(abDocRef);
    List<XWikiAttachment> attachList = new ArrayList<>();
    XWikiAttachment blaAtt = new XWikiAttachment();
    String attName = "bla.txt";
    blaAtt.setFilename(attName);
    blaAtt.setDoc(abDoc);
    attachList.add(blaAtt);
    abDoc.setAttachmentList(attachList);
    expect(modelAccessMock.getDocument(eq(abDocRef))).andReturn(abDoc).atLeastOnce();
    expect(modelAccessMock.getAttachmentNameEqual(same(abDoc), eq(attName))).andReturn(blaAtt)
        .atLeastOnce();
    String queryString = "asf=oiu";
    replayDefault();
    String attachmentURL = resUrlServ.createRessourceUrl("A.B;bla.txt", Optional.of("testAction"),
        Optional.of(queryString));
    assertTrue(attachmentURL,
        attachmentURL.matches(resultURL + "\\?version=\\d{14}\\&" + queryString));
    verifyDefault();
  }

}
