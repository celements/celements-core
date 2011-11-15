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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.XWikiURLFactory;

public class AttachmentURLCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki wiki;
  private AttachmentURLCommand attUrlCmd;
  private XWikiURLFactory mockURLFactory;

  @Before
  public void setUp_AttachmentURLCommandTest() throws Exception {
    context = getContext();
    wiki = createMock(XWiki.class);
    context.setWiki(wiki);
    attUrlCmd = new AttachmentURLCommand();
    mockURLFactory = createMock(XWikiURLFactory.class);
    context.setURLFactory(mockURLFactory);
  }

  @Test
  public void testGetAttachmentURL_fullURL() {
    assertEquals("http://www.bla.com/bla.txt", attUrlCmd.getAttachmentURL(
        "http://www.bla.com/bla.txt", context));
  }
  
  @Test
  public void testGetAttachmentURL_partURL() {
    assertEquals("/xwiki/bin/download/A/B/bla.txt", attUrlCmd.getAttachmentURL(
        "/xwiki/bin/download/A/B/bla.txt", context));
  }
  
  @Test
  public void testGetAttachmentURL_dynamicParamURL() throws MalformedURLException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("mySpace.myDoc");
    context.setDoc(doc);
    URL viewURL = new URL("http://localhost/mySpace/myDoc");
    expect(context.getURLFactory().createURL(eq(doc.getSpace()), eq(doc.getName()),
        eq("view"), (String)isNull(), (String)isNull(), eq(context.getDatabase()),
        same(context))).andReturn(viewURL);
    expect(context.getURLFactory().getURL(eq(viewURL), same(context))
        ).andReturn(viewURL.getPath());
    replay(context.getURLFactory());
    assertEquals("/mySpace/myDoc?xpage=bla&bli=blu", attUrlCmd.getAttachmentURL(
        "?xpage=bla&bli=blu", context));
    verify(context.getURLFactory());
  }
  
  @Test
  public void testGetAttachmentURL_fullInternalLink(
      ) throws XWikiException, MalformedURLException {
    String resultURL = "http://celements2web.localhost/file/A/B/bla.txt";
    XWikiDocument abdoc = new XWikiDocument();
    abdoc.setFullName("A.B");
    abdoc.setDatabase("celements2web");
    List<XWikiAttachment> attachList = new ArrayList<XWikiAttachment>();
    XWikiAttachment blaAtt = new XWikiAttachment();
    blaAtt.setFilename("bla.txt");
    blaAtt.setDoc(abdoc);
    attachList.add(blaAtt);
    abdoc.setAttachmentList(attachList);
    URL tstURL = new URL(resultURL);
    expect(mockURLFactory.createAttachmentURL(eq("bla.txt"), eq("A"), eq("B"),
        eq("file"), (String)eq(null), eq("celements2web"), same(context))
        ).andReturn(tstURL);
    expect(mockURLFactory.getURL(eq(tstURL), same(context))
        ).andReturn(resultURL);
    expect(wiki.getDocument(eq("celements2web:A.B"), same(context))
        ).andReturn(abdoc).anyTimes();
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))
        ).andReturn("file");
    replay(mockURLFactory, wiki);
    String attachmentURL = attUrlCmd.getAttachmentURL(
        "celements2web:A.B;bla.txt", context);
    assertTrue(attachmentURL, attachmentURL.matches(resultURL + "\\?version=\\d{14}"));
    verify(mockURLFactory, wiki);
  }
  
  @Test
  public void testGetAttachmentURL_partInternalLink(
      ) throws XWikiException, MalformedURLException {
    String resultURL = "http://mydomain.ch/file/A/B/bla.txt";
    URL tstURL = new URL(resultURL);
    expect(mockURLFactory.createAttachmentURL(eq("bla.txt"), eq("A"), eq("B"),
        eq("file"), (String)eq(null), eq(context.getDatabase()), same(context))
        ).andReturn(tstURL);
    expect(mockURLFactory.getURL(eq(tstURL), same(context))
        ).andReturn(resultURL);
    XWikiDocument abdoc = new XWikiDocument();
    abdoc.setFullName("A.B");
    List<XWikiAttachment> attachList = new ArrayList<XWikiAttachment>();
    XWikiAttachment blaAtt = new XWikiAttachment();
    blaAtt.setFilename("bla.txt");
    blaAtt.setDoc(abdoc);
    attachList.add(blaAtt);
    abdoc.setAttachmentList(attachList);
    expect(wiki.getDocument(eq("A.B"), same(context))).andReturn(abdoc).anyTimes();
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
    "celements.attachmenturl.defaultaction"), eq("file"), same(context))
    ).andReturn("file");
    replay(mockURLFactory, wiki);
    String attachmentURL = attUrlCmd.getAttachmentURL("A.B;bla.txt", context);
    assertTrue(attachmentURL, attachmentURL.matches(resultURL + "\\?version=\\d{14}"));
    verify(mockURLFactory, wiki);
  }
  
  @Test
  public void testGetAttachmentURL_partInternalLink_notExists(
      ) throws XWikiException, MalformedURLException {
    XWikiDocument abdoc = new XWikiDocument();
    abdoc.setFullName("A.B");
    expect(wiki.getDocument(eq("A.B"), same(context))).andReturn(abdoc).anyTimes();
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
    "celements.attachmenturl.defaultaction"), eq("file"), same(context))
    ).andReturn("file");
    replay(mockURLFactory, wiki);
    assertNull(attUrlCmd.getAttachmentURL("A.B;bla.txt", context));
    verify(mockURLFactory, wiki);
  }
  
  @Test
  public void testGetAttachmentURL_onDiskLink() throws XWikiException, 
      MalformedURLException {
    String resultURL = "/appname/skin/resources/celJS/bla.js";
    expect(wiki.getSkinFile(eq("celJS/bla.js"), eq(true), same(context))).andReturn(
        resultURL);
    expect(wiki.getResourceLastModificationDate(eq("resources/celJS/bla.js"))).andReturn(
        new Date());
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
      "celements.attachmenturl.defaultaction"), eq("file"), same(context))
      ).andReturn("download");
    replay(wiki);
    String attachmentURL = attUrlCmd.getAttachmentURL("  :celJS/bla.js",
        context);
    String expectedURL = "/appname/download/resources/celJS/bla.js";
    assertTrue(attachmentURL, attachmentURL.matches(expectedURL + "\\?version=\\d{14}"));
    verify(wiki);
  }

  @Test
  public void testGetAttachmentURL_Rubish() {
    assertEquals("http://A.B;bla.txt", attUrlCmd.getAttachmentURL(
        "http://A.B;bla.txt", context));
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
  

}
