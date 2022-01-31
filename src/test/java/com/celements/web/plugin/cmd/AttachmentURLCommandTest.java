/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.plugin.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.XWikiURLFactory;

public class AttachmentURLCommandTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki wiki;
  private AttachmentURLCommand attUrlCmd;
  private XWikiURLFactory mockURLFactory;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void setUp_AttachmentURLCommandTest() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    context = getContext();
    wiki = getWikiMock();
    attUrlCmd = new AttachmentURLCommand();
    mockURLFactory = createMockAndAddToDefault(XWikiURLFactory.class);
    context.setURLFactory(mockURLFactory);
  }

  @Test
  public void test_getAttachmentURL_fullURL() {
    assertEquals("http://www.bla.com/bla.txt", attUrlCmd.getAttachmentURL(
        "http://www.bla.com/bla.txt"));
  }

  @Test
  public void test_getAttachmentURL_partURL() {
    assertEquals("/xwiki/bin/download/A/B/bla.txt", attUrlCmd.getAttachmentURL(
        "/xwiki/bin/download/A/B/bla.txt"));
  }

  @Test
  public void test_getAttachmentURL_dynamicParamURL() throws MalformedURLException {
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
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("file");
    replayDefault();
    assertEquals("/mySpace/myDoc?xpage=bla&bli=blu", attUrlCmd.getAttachmentURL(
        "?xpage=bla&bli=blu"));
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_fullInternalLink() throws Exception {
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
    String attachmentURL = attUrlCmd.getAttachmentURL("celements2web:A.B;bla.txt");
    assertTrue(attachmentURL, attachmentURL.matches(resultURL + "\\?version=\\d{14}"));
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_partInternalLink() throws Exception {
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
    String attachmentURL = attUrlCmd.getAttachmentURL("A.B;bla.txt");
    assertTrue(attachmentURL, attachmentURL.matches(resultURL + "\\?version=\\d{14}"));
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_partInternalLink_notExists() throws Exception {
    DocumentReference abDocRef = new RefBuilder().wiki(getContext().getDatabase()).space("A")
        .doc("B").build(DocumentReference.class);
    XWikiDocument abDoc = new XWikiDocument(abDocRef);
    expect(modelAccessMock.getDocument(eq(abDocRef))).andReturn(abDoc).atLeastOnce();
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("file");
    String attName = "bla.txt";
    AttachmentReference attRef = new RefBuilder().with(abDocRef).att(attName)
        .build(AttachmentReference.class);
    expect(modelAccessMock.getAttachmentNameEqual(same(abDoc), eq(attName)))
        .andThrow(new AttachmentNotExistsException(attRef)).atLeastOnce();
    replayDefault();
    assertNull(attUrlCmd.getAttachmentURL("A.B;bla.txt"));
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_onDiskLink() throws XWikiException, MalformedURLException {
    String resultURL = "/appname/skin/resources/celJS/bla.js";
    expect(wiki.getSkinFile(eq("celJS/bla.js"), eq(true), same(context))).andReturn(resultURL);
    expect(wiki.getResourceLastModificationDate(eq("resources/celJS/bla.js"))).andReturn(
        new Date());
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("download");
    replayDefault();
    String attachmentURL = attUrlCmd.getAttachmentURL("  :celJS/bla.js");
    String expectedURL = "/appname/download/resources/celJS/bla.js";
    assertTrue(attachmentURL, attachmentURL.matches(expectedURL + "\\?version=\\d{14}"));
    verifyDefault();
  }

  @Test
  public void test_isAttachmentLink_null() {
    assertFalse(attUrlCmd.isAttachmentLink(null));
  }

  @Test
  public void test_isAttachmentLink_empty() {
    assertFalse(attUrlCmd.isAttachmentLink(""));
  }

  @Test
  public void test_isAttachmentLink_url() {
    assertFalse(attUrlCmd.isAttachmentLink("/download/Space/Page/attachment.jpg"));
  }

  @Test
  public void test_isAttachmentLink_is() {
    assertTrue(attUrlCmd.isAttachmentLink("Space.Page;attachment.jpg"));
  }

  @Test
  public void test_isAttachmentLink_isSpecialChars() {
    assertTrue(attUrlCmd.isAttachmentLink("Teilnehmer.f8Nx9vyPOX8O2;Hans-002-Bearbeitet-2.jpg"));
  }

  @Test
  public void test_isAttachmentLink_isWithDb() {
    assertTrue(attUrlCmd.isAttachmentLink("db:Space.Page;attachment.jpg"));
  }

  @Test
  public void test_getAttachmentURL_Rubish() {
    assertEquals("http://A.B;bla.txt", attUrlCmd.getAttachmentURL("http://A.B;bla.txt"));
  }

  @Test
  public void test_isOnDiskLink_true() {
    assertTrue(attUrlCmd.isOnDiskLink(":bla.js"));
    assertTrue(attUrlCmd.isOnDiskLink("  :celJS/bla.js"));
  }

  @Test
  public void test_isOnDiskLink_false() {
    assertFalse(attUrlCmd.isOnDiskLink("bla.js"));
    assertFalse(attUrlCmd.isOnDiskLink("x:celJS/bla.js"));
    assertFalse(attUrlCmd.isOnDiskLink("x:A.B;bla.js"));
  }

  @Test
  public void test_getAttachmentURLPrefix() throws Exception {
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("file");
    expect(mockURLFactory.createResourceURL(eq(""), eq(true), same(context))).andReturn(new URL(
        "http://test.fabian.dev:10080/skin/resources/"));
    replayDefault();
    assertEquals("http://test.fabian.dev:10080/file/resources/",
        attUrlCmd.getAttachmentURLPrefix());
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_onDisk_queryString() {
    String resultURL = "/appname/skin/resources/celJS/bla.js";
    expect(wiki.getSkinFile(eq("celJS/bla.js"), eq(true), same(context))).andReturn(resultURL);
    expect(wiki.getResourceLastModificationDate(eq("resources/celJS/bla.js"))).andReturn(
        new Date());
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("download");
    String queryString = "asf=oiu";
    replayDefault();
    String attachmentURL = attUrlCmd.getAttachmentURL(":celJS/bla.js", null, queryString);
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
    String attachmentURL = attUrlCmd.getAttachmentURL("A.B;bla.txt", "testAction", queryString);
    assertTrue(attachmentURL,
        attachmentURL.matches(resultURL + "\\?version=\\d{14}\\&" + queryString));
    verifyDefault();
  }

}
