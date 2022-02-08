package com.celements.filebase.uri;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.filebase.references.FileReference;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.reference.RefBuilder;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

public class FileUriServiceTest extends AbstractComponentTest {

  private IModelAccessFacade modelAccessMock;
  private XWikiContext context;
  private XWikiURLFactory mockURLFactory;
  private FileUriService fileUriServ;
  private XWiki wiki;

  @Before
  public void setUp_RessourceUrlServiceTest() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    context = getContext();
    wiki = getWikiMock();
    mockURLFactory = createMockAndAddToDefault(XWikiURLFactory.class);
    context.setURLFactory(mockURLFactory);
    fileUriServ = (FileUriService) Utils.getComponent(FileUriServiceRole.class);
  }

  @Test
  public void test_createFileUrl_fullURL() throws Exception {
    FileReference fileRef = FileReference.of("http://www.bla.com/bla.txt").build();
    assertEquals("http://www.bla.com/bla.txt",
        fileUriServ.createFileUri(fileRef, Optional.empty()).build().toString());
  }

  @Test
  public void test_createFileUrl_partURL() throws Exception {
    FileReference fileRef = FileReference.of("/xwiki/bin/download/A/B/bla.txt").build();
    assertEquals("/xwiki/bin/download/A/B/bla.txt", fileUriServ.createFileUri(fileRef,
        Optional.empty()).build().toString());
  }

  @Test
  public void test_getExternalFileURL_partURL() throws Exception {
    FileReference fileRef = FileReference.of("/xwiki/bin/download/A/B/bla.txt").build();
    URL viewURL = new URL("http://localhost");
    expect(mockURLFactory.getServerURL(same(context))).andReturn(viewURL);
    replayDefault();
    assertEquals("http://localhost/xwiki/bin/download/A/B/bla.txt",
        fileUriServ.createAbsoluteFileUri(fileRef, Optional.empty(), Optional.empty()).build()
            .toURL().toExternalForm().toString());
    verifyDefault();
  }

  @Test
  public void test_getExternalFileURL_partURL_external() throws Exception {
    FileReference fileRef = FileReference.of("http://myTesthost.ch/xwiki/bin/download/A/B/bla.txt")
        .build();
    URL viewURL = new URL("http://localhost");
    expect(mockURLFactory.getServerURL(same(context))).andReturn(viewURL);
    replayDefault();
    assertEquals("http://myTesthost.ch/xwiki/bin/download/A/B/bla.txt",
        fileUriServ.createAbsoluteFileUri(fileRef, Optional.empty(), Optional.empty()).build()
            .toURL().toExternalForm().toString());
    verifyDefault();
  }

  @Test
  public void test_createFileUrl_dynamicParamURL() throws Exception {
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
    FileReference fileRef = FileReference.of("?xpage=bla&bli=blu").build();
    assertEquals("/mySpace/myDoc?xpage=bla&bli=blu", fileUriServ.createFileUri(fileRef,
        Optional.empty()).build().toString());
    verifyDefault();
  }

  @Test
  public void test_createFileUrl_fullInternalLink() throws Exception {
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
    FileReference fileRef = FileReference.of("celements2web:A.B;bla.txt").build();
    String attachmentURL = fileUriServ.createFileUri(fileRef, Optional.empty()).build().toString();
    assertNotNull(attachmentURL);
    assertTrue("expecting " + resultURL + " but got " + attachmentURL,
        attachmentURL.matches(resultURL + "\\?version=\\d{14}"));
    verifyDefault();
  }

  @Test
  public void test_createFileUrl_partInternalLink() throws Exception {
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
    FileReference fileRef = FileReference.of("A.B;bla.txt").build();
    String attachmentURL = fileUriServ.createFileUri(fileRef, Optional.empty()).build().toString();
    assertNotNull(attachmentURL);
    assertTrue("expecting " + resultURL + " but got " + attachmentURL,
        attachmentURL.matches(resultURL + "\\?version=\\d{14}"));
    verifyDefault();
  }

  @Test
  public void test_createFileUrl_partInternalLink_notExists() throws Exception {
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
    FileReference fileRef = FileReference.of("A.B;bla.txt").build();
    assertThrows(FileNotExistException.class,
        () -> fileUriServ.createFileUri(fileRef, Optional.empty()));
    verifyDefault();
  }

  @Test
  public void test_createFileUrl_onDiskLink() throws Exception {
    String resultURL = "/appname/skin/resources/celJS/bla.js";
    expect(wiki.getSkinFile(eq("celJS/bla.js"), eq(true), same(context))).andReturn(resultURL);
    expect(wiki.getResourceLastModificationDate(eq("resources/celJS/bla.js"))).andReturn(
        new Date());
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("download");
    replayDefault();
    FileReference fileRef = FileReference.of(" :celJS/bla.js").build();
    String attachmentURL = fileUriServ.createFileUri(fileRef, Optional.empty(), Optional.empty())
        .toString();
    String expectedURL = "/appname/download/resources/celJS/bla.js";
    assertNotNull(attachmentURL);
    assertTrue("expecting " + expectedURL + " but got " + attachmentURL,
        attachmentURL.matches(expectedURL + "\\?version=\\d{14}"));
    verifyDefault();
  }

  @Test
  public void test_createFileUrl_Rubish() throws Exception {
    FileReference fileRef = FileReference.of("http://A.B;bla.txt").build();
    assertEquals("http://A.B;bla.txt",
        fileUriServ.createFileUri(fileRef, Optional.empty(), Optional.empty()).toString());
  }

  @Test
  public void test_getFileURLPrefix() throws Exception {
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("file");
    expect(mockURLFactory.createResourceURL(eq(""), eq(false), same(context))).andReturn(new URL(
        "http://test.fabian.dev:10080/resources/"));
    replayDefault();
    assertEquals("http://test.fabian.dev:10080/file/resources/",
        fileUriServ.getFileUriPrefix(Optional.empty()).toString());
    verifyDefault();
  }

  @Test
  public void test_createFileUrl_onDisk_queryString() throws Exception {
    String resultURL = "/appname/skin/resources/celJS/bla.js";
    expect(wiki.getSkinFile(eq("celJS/bla.js"), eq(true), same(context))).andReturn(resultURL);
    expect(wiki.getResourceLastModificationDate(eq("resources/celJS/bla.js"))).andReturn(
        new Date());
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("download");
    String queryString = "asf=oiu";
    replayDefault();
    FileReference fileRef = FileReference.of(":celJS/bla.js").build();
    String attachmentURL = fileUriServ.createFileUri(fileRef, Optional.empty(),
        Optional.of(queryString)).toString();
    String expectedURL = "/appname/download/resources/celJS/bla.js";
    assertTrue(attachmentURL,
        attachmentURL.matches(expectedURL + "\\?version=\\d{14}\\&" + queryString));
    verifyDefault();
  }

  @Test
  public void test_createFileUrl_partInternalLink_queryString() throws Exception {
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
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("file");
    String queryString = "asf=oiu";
    replayDefault();
    FileReference fileRef = FileReference.of("A.B;bla.txt").build();
    String attachmentURL = fileUriServ.createFileUri(fileRef, Optional.of("testAction"),
        Optional.of(queryString)).toString();
    assertTrue(attachmentURL,
        attachmentURL.matches(resultURL + "\\?version=\\d{14}\\&" + queryString));
    verifyDefault();
  }

  @Test
  public void test_addContextUrl() throws Exception {
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
    UriBuilder uri = UriBuilder.fromPath("").replaceQuery("sdf=asdf");
    replayDefault();
    assertEquals("/mySpace/myDoc?sdf=asdf", fileUriServ.addContextUri(uri).toString());
    verifyDefault();
  }

  @Test
  public void test_createOnDiskUrl() throws Exception {
    String mySpaceName = "mySpace";
    String myDocName = "myDoc";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), mySpaceName,
        myDocName);
    XWikiDocument doc = new XWikiDocument(myDocRef);
    context.setDoc(doc);
    Date lastModificationDate = new SimpleDateFormat("YYYYmmddHHMMss").parse("20201123101535");
    expect(wiki.getResourceLastModificationDate(eq("resources/celRes/test/bla.css"))).andReturn(
        lastModificationDate);
    String resultURL = "http://celements2web.localhost/skin/celRes/test/bla.css";
    expect(wiki.getSkinFile(eq("celRes/test/bla.css"), eq(true), same(context)))
        .andReturn(resultURL);
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("file");
    replayDefault();
    FileReference fileRef = FileReference.of(":celRes/test/bla.css").build();
    assertEquals(
        "http://celements2web.localhost/createOnDiskUrl/celRes/test/bla.css?version=20191230101135",
        fileUriServ.createOnDiskUri(fileRef, Optional.of("createOnDiskUrl")).toString());
    verifyDefault();
  }

}
