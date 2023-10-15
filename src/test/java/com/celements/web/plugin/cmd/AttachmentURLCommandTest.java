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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.filebase.IAttachmentServiceRole;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.web.service.UrlService;
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

  @Before
  public void setUp_AttachmentURLCommandTest() throws Exception {
    registerComponentMocks(IAttachmentServiceRole.class, UrlService.class);
    context = getXContext();
    wiki = getMock(XWiki.class);
    attUrlCmd = new AttachmentURLCommand();
    mockURLFactory = createDefaultMock(XWikiURLFactory.class);
    context.setURLFactory(mockURLFactory);
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context)))
            .andReturn("file").anyTimes();
  }

  @Test
  public void test_getAttachmentURL_fullURL() {
    replayDefault();
    assertEquals("http://www.bla.com/bla.txt", attUrlCmd.getAttachmentURL(
        "http://www.bla.com/bla.txt", context));
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_partURL() {
    replayDefault();
    assertEquals("/xwiki/bin/download/A/B/bla.txt", attUrlCmd.getAttachmentURL(
        "/xwiki/bin/download/A/B/bla.txt", context));
    verifyDefault();
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
    expect(getMock(UrlService.class).getURL(myDocRef, "view")).andReturn(viewURL.getPath());
    replayDefault();
    assertEquals("/mySpace/myDoc?xpage=bla&bli=blu", attUrlCmd.getAttachmentURL(
        "?xpage=bla&bli=blu", context));
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_fullInternalLink() throws Exception {
    var attRef = new AttachmentReference("bla.txt",
        new DocumentReference("celements2web", "A", "B"));
    String resultURL = "http://celements2web.localhost/file/A/B/bla.txt";
    expect(getMock(UrlService.class).getURL(attRef, "file")).andReturn(resultURL);
    XWikiAttachment blaAtt = new XWikiAttachment();
    blaAtt.setFilename("bla.txt");
    expect(getMock(IAttachmentServiceRole.class)
        .getAttachmentNameEqual(anyObject(AttachmentReference.class)))
            .andReturn(blaAtt);
    replayDefault();
    String attachmentURL = attUrlCmd.getAttachmentURL("celements2web:A.B;bla.txt", context);
    verifyDefault();
    assertTrue(attachmentURL, attachmentURL.matches(resultURL + "\\?version=\\d{14}"));
  }

  @Test
  public void test_getAttachmentURL_partInternalLink() throws Exception {
    var attRef = new AttachmentReference("bla.txt",
        new DocumentReference(context.getDatabase(), "A", "B"));
    String resultURL = "http://mydomain.ch/file/A/B/bla.txt";
    expect(getMock(UrlService.class).getURL(attRef, "file")).andReturn(resultURL);
    XWikiAttachment blaAtt = new XWikiAttachment();
    blaAtt.setFilename(attRef.getName());
    expect(getMock(IAttachmentServiceRole.class)
        .getAttachmentNameEqual(anyObject(AttachmentReference.class)))
            .andReturn(blaAtt);
    replayDefault();
    String attachmentURL = attUrlCmd.getAttachmentURL("A.B;bla.txt", context);
    verifyDefault();
    assertTrue(attachmentURL, attachmentURL.matches(resultURL + "\\?version=\\d{14}"));
  }

  @Test
  public void test_getAttachmentURL_partInternalLink_notExists() throws Exception {
    var attRef = new AttachmentReference("bla.txt",
        new DocumentReference(context.getDatabase(), "A", "B"));
    expect(getMock(IAttachmentServiceRole.class)
        .getAttachmentNameEqual(anyObject(AttachmentReference.class)))
            .andThrow(new AttachmentNotExistsException(attRef));
    replayDefault();
    assertNull(attUrlCmd.getAttachmentURL("A.B;bla.txt", context));
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_onDiskLink() throws XWikiException, MalformedURLException {
    String resultURL = "/appname/skin/resources/celJS/bla.js";
    expect(wiki.getSkinFile(eq("celJS/bla.js"), eq(true), same(context))).andReturn(resultURL);
    expect(wiki.getResourceLastModificationDate(eq("resources/celJS/bla.js"))).andReturn(
        new Date());
    replayDefault();
    String attachmentURL = attUrlCmd.getAttachmentURL("  :celJS/bla.js", context);
    String expectedURL = "/appname/file/resources/celJS/bla.js";
    assertTrue(attachmentURL, attachmentURL.matches(expectedURL + "\\?version=\\d{14}"));
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_query() {
    String url = "http://www.bla.com/bla.mjs";
    String query = "version=1234";
    replayDefault();
    assertEquals(url + "?" + query, attUrlCmd.getAttachmentURL(url, "file", query)
        .get().toUriString());
    verifyDefault();
  }

  @Test
  public void test_getAttachmentURL_query_inLink() {
    String url = "http://www.bla.com/bla.mjs?version=1234";
    replayDefault();
    assertEquals(url, attUrlCmd.getAttachmentURL(url, "file", "").get().toUriString());
    assertEquals(url, attUrlCmd.getAttachmentURL(url, "file", (String) null).get().toUriString());
    verifyDefault();
  }

  @Test
  public void isAttachmentLink_null() {
    assertFalse(attUrlCmd.isAttachmentLink(null));
  }

  @Test
  public void isAttachmentLink_empty() {
    assertFalse(attUrlCmd.isAttachmentLink(""));
  }

  @Test
  public void isAttachmentLink_url() {
    assertFalse(attUrlCmd.isAttachmentLink("/download/Space/Page/attachment.jpg"));
  }

  @Test
  public void isAttachmentLink_is() {
    assertTrue(attUrlCmd.isAttachmentLink("Space.Page;attachment.jpg"));
  }

  @Test
  public void isAttachmentLink_isSpecialChars() {
    assertTrue(attUrlCmd.isAttachmentLink("Teilnehmer.f8Nx9vyPOX8O2;Hans-002-Bearbeitet-2.jpg"));
  }

  @Test
  public void isAttachmentLink_isWithDb() {
    assertTrue(attUrlCmd.isAttachmentLink("db:Space.Page;attachment.jpg"));
  }

  @Test
  public void test_getAttachmentURL_Rubish() {
    replayDefault();
    assertEquals("http://A.B;bla.txt", attUrlCmd.getAttachmentURL("http://A.B;bla.txt", context));
    verifyDefault();
  }

  @Test
  public void testIsOnDiskLink_true() {
    assertTrue(attUrlCmd.isOnDiskLink(":bla.js"));
    assertTrue(attUrlCmd.isOnDiskLink("  :celJS/bla.js"));
  }

  @Test
  public void testIsOnDiskLink_false() {
    assertFalse(attUrlCmd.isOnDiskLink("bla.js"));
    assertFalse(attUrlCmd.isOnDiskLink("x:celJS/bla.js"));
    assertFalse(attUrlCmd.isOnDiskLink("x:A.B;bla.js"));
  }

  @Test
  public void test_getAttachmentURLPrefix() throws Exception {
    expect(mockURLFactory.createResourceURL(eq(""), eq(true), same(context))).andReturn(new URL(
        "http://test.fabian.dev:10080/skin/resources/"));
    replayDefault();
    assertEquals("http://test.fabian.dev:10080/file/resources/",
        attUrlCmd.getAttachmentURLPrefix());
    verifyDefault();
  }

}
