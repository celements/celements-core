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
package com.celements.navigation.presentation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;
import com.xpn.xwiki.web.XWikiURLFactory;

public class SitemapPresentationTypeTest extends AbstractComponentTest {

  private XWikiContext context;
  private DocumentReference currentDocRef;
  private IWebUtilsService wUServiceMock;
  private XWikiDocument currentDoc;
  private XWiki xwiki;
  private SitemapPresentationType sitemapPres;
  private XWikiStoreInterface mockXWikiStore;
  private XWikiMessageTool admMessTool;
  private XWikiURLFactory urlFactoryMock;

  @Before
  public void setUp_SitemapPresentationTypeTest() throws Exception {
    context = getContext();
    currentDocRef = new DocumentReference(context.getDatabase(), "MySpace", "MyCurrentDoc");
    currentDoc = new XWikiDocument(currentDocRef);
    context.setDoc(currentDoc);
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    urlFactoryMock = createMock(XWikiURLFactory.class);
    context.setURLFactory(urlFactoryMock);
    mockXWikiStore = createMock(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(mockXWikiStore).anyTimes();
    currentDoc.setStore(mockXWikiStore);
    wUServiceMock = createMock(IWebUtilsService.class);
    admMessTool = createMock(XWikiMessageTool.class);
    expect(wUServiceMock.getAdminMessageTool()).andReturn(admMessTool).anyTimes();
    sitemapPres = (SitemapPresentationType) Utils.getComponent(IPresentationTypeRole.class,
        "sitemap");
    sitemapPres.webUtilsService = wUServiceMock;
  }

  @Test
  public void testAddLanguageLinks() throws Exception {
    StringBuilder outStream = new StringBuilder();
    expect(xwiki.getDocument(eq(currentDocRef), same(context))).andReturn(currentDoc).atLeastOnce();
    List<String> allowedLangs = new ArrayList<>();
    allowedLangs.add("de");
    allowedLangs.add("fr");
    allowedLangs.add("en");
    List<String> docTransList = new ArrayList<>();
    // docTransList.add("de"); <-- defaultLanguage DOES NOT show up in translist!!!
    docTransList.add("en");
    expect(mockXWikiStore.getTranslationList(same(currentDoc), same(context))).andReturn(
        docTransList).atLeastOnce();
    expect(wUServiceMock.getDefaultLanguage(eq("MySpace"))).andReturn("de").anyTimes();
    expect(wUServiceMock.getAllowedLanguages(eq("MySpace"))).andReturn(allowedLangs).anyTimes();
    expect(admMessTool.get(eq("cel_de"))).andReturn("German").once();
    expect(admMessTool.get(eq("cel_fr"))).andReturn("French").once();
    expect(admMessTool.get(eq("cel_en"))).andReturn("English").once();
    String currDocDeEditUrlStr = "http://test.ch/edit/MySpace/MyCurrentDoc?language=de";
    URL currentDocDeEditUrl = new URL(currDocDeEditUrlStr);
    expect(urlFactoryMock.createURL(eq("MySpace"), eq("MyCurrentDoc"), eq("edit"), eq(
        "language=de"), (String) isNull(), eq("xwikidb"), same(context))).andReturn(
            currentDocDeEditUrl);
    expect(urlFactoryMock.getURL(same(currentDocDeEditUrl), same(context))).andReturn(
        currDocDeEditUrlStr);
    String currDocFrEditUrlStr = "http://test.ch/edit/MySpace/MyCurrentDoc?language=fr";
    URL currentDocFrEditUrl = new URL(currDocFrEditUrlStr);
    expect(urlFactoryMock.createURL(eq("MySpace"), eq("MyCurrentDoc"), eq("edit"), eq(
        "language=fr"), (String) isNull(), eq("xwikidb"), same(context))).andReturn(
            currentDocFrEditUrl);
    expect(urlFactoryMock.getURL(same(currentDocFrEditUrl), same(context))).andReturn(
        currDocFrEditUrlStr);
    String currDocEnEditUrlStr = "http://test.ch/edit/MySpace/MyCurrentDoc?language=en";
    URL currentDocEnEditUrl = new URL(currDocEnEditUrlStr);
    expect(urlFactoryMock.createURL(eq("MySpace"), eq("MyCurrentDoc"), eq("edit"), eq(
        "language=en"), (String) isNull(), eq("xwikidb"), same(context))).andReturn(
            currentDocEnEditUrl);
    expect(urlFactoryMock.getURL(same(currentDocEnEditUrl), same(context))).andReturn(
        currDocEnEditUrlStr);
    replayAll();
    sitemapPres.addLanguageLinks(outStream, currentDocRef);
    assertEquals("<div class=\"docLangs\"><a title=\"German\" href=\"" + currDocDeEditUrlStr
        + "\" target=\"_blank\" class=\"defaultLanguage transExists\">de</a>"
        + "<a title=\"French\" href=\"" + currDocFrEditUrlStr
        + "\" target=\"_blank\" class=\"transNotExists\">fr</a><a title=\"English\"" + " href=\""
        + currDocEnEditUrlStr + "\" target=\"_blank\" class=\"transExists\">" + "en</a></div>",
        outStream.toString());
    verifyAll();
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private void replayAll(Object... mocks) {
    replay(xwiki, wUServiceMock, mockXWikiStore, admMessTool, urlFactoryMock);
    replay(mocks);
  }

  private void verifyAll(Object... mocks) {
    verify(xwiki, wUServiceMock, mockXWikiStore, admMessTool, urlFactoryMock);
    verify(mocks);
  }

}
