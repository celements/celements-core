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
package com.celements.pagetype.xobject;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeReference;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeProviderTest extends AbstractComponentTest {

  private XObjectPageTypeProvider xObjPTprovider;
  private XWikiContext context;
  private XWiki xwiki;

  @Before
  public void setUp_XObjectPageTypeProviderTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    xObjPTprovider = (XObjectPageTypeProvider) Utils.getComponent(IPageTypeProviderRole.class,
        XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER);
  }

  @Test
  public void testComponentRegistration() {
    assertNotNull(Utils.getComponent(IPageTypeProviderRole.class,
        "com.celements.XObjectPageTypeProvider"));
  }

  @Test
  public void testGetPageTypeByReference() {
    PageTypeReference testPTref = new PageTypeReference("TestPageTypeRef", "xObjectProvider",
        Arrays.asList(""));
    DocumentReference pTdocRef = new DocumentReference(context.getDatabase(),
        XObjectPageTypeProvider.DEFAULT_PAGE_TYPES_SPACE, "TestPageTypeRef");
    replayDefault();
    XObjectPageTypeConfig ptObj = (XObjectPageTypeConfig) xObjPTprovider.getPageTypeByReference(
        testPTref);
    assertNotNull(ptObj);
    assertEquals("TestPageTypeRef", ptObj.getName());
    assertEquals(pTdocRef, ptObj.pageType.getDocumentReference());
    verifyDefault();
  }

  @Test
  public void testGetPageTypes() throws Exception {
    Set<String> allPageTypeNames = new HashSet<String>(Arrays.asList("PageTypes.RichText"));
    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(false).anyTimes();
    DocumentReference centralRichTextPTdocRef = new DocumentReference("celements2web", "PageTypes",
        "RichText");
    XWikiDocument centralRichTextPTdoc = new XWikiDocument(centralRichTextPTdocRef);
    expect(xwiki.getDocument(eq("celements2web:PageTypes.RichText"), same(context))).andReturn(
        centralRichTextPTdoc).anyTimes();
    List<String> pageTypeString = Arrays.asList("PageTypes.RichText");
    expect(xwiki.<String>search(isA(String.class), same(context))).andReturn(pageTypeString).times(
        2);
    replayDefault();
    List<PageTypeReference> allPageTypes = xObjPTprovider.getPageTypes();
    assertFalse("expecting RichtText page type reference.", allPageTypes.isEmpty());
    PageTypeReference richTextPTref = allPageTypes.get(0);
    assertEquals("RichText", richTextPTref.getConfigName());
    assertEquals(Arrays.asList(""), richTextPTref.getCategories());
    verifyDefault();
  }

}
