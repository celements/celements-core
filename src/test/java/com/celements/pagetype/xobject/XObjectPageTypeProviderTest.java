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

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.same;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.cmd.PageTypeCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeProviderTest extends AbstractBridgedComponentTestCase {

  private XObjectPageTypeProvider xObjPTprovider;
  private XWikiContext context;
  private XWiki xwiki;
  private PageTypeCommand pageTypeCmdMock;
  private PageTypeCommand backupPageTypeCmd;

  @Before
  public void setUp_XObjectPageTypeProviderTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    xObjPTprovider = (XObjectPageTypeProvider) Utils.getComponent(
        IPageTypeProviderRole.class, XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER);
    pageTypeCmdMock = createMockAndAddToDefault(PageTypeCommand.class);
    backupPageTypeCmd = xObjPTprovider.pageTypeCmd;
    xObjPTprovider.pageTypeCmd = pageTypeCmdMock;
  }

  @After
  public void tearDown_XObjectPageTypeProviderTest() {
    xObjPTprovider.pageTypeCmd = backupPageTypeCmd;
  }

  @Test
  public void testComponentRegistration() {
    assertNotNull(Utils.getComponent(IPageTypeProviderRole.class,
        "com.celements.XObjectPageTypeProvider"));
  }

  @Test
  public void testGetPageTypeByReference() {
    PageTypeReference testPTref = new PageTypeReference("TestPageTypeRef",
        "xObjectProvider", Arrays.asList(""));
    expect(pageTypeCmdMock.completePageTypeDocName(eq("TestPageTypeRef"))).andReturn(
        "PageTypes.TestPageTypeRef");
    replayDefault();
    IPageTypeConfig ptObj = xObjPTprovider.getPageTypeByReference(testPTref);
    assertNotNull(ptObj);
    assertEquals("TestPageTypeRef", ptObj.getName());
    verifyDefault();
  }

  @Test
  public void testGetPageTypes() throws Exception {
    Set<String> allPageTypeNames = new HashSet<String>(Arrays.asList(
        "PageTypes.RichText"));
    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(false
        ).anyTimes();
    DocumentReference centralRichTextPTdocRef = new DocumentReference("celements2web",
        "PageTypes", "RichText");
    XWikiDocument centralRichTextPTdoc = new XWikiDocument(centralRichTextPTdocRef);
    expect(xwiki.getDocument(eq("celements2web:PageTypes.RichText"), same(context))
        ).andReturn(centralRichTextPTdoc).anyTimes();
    List<String> pageTypeString = Arrays.asList("PageTypes.RichText");
    expect(xwiki.<String>search(isA(String.class), same(context))).andReturn(
        pageTypeString).times(2);
    replayDefault();
    List<PageTypeReference> allPageTypes = xObjPTprovider.getPageTypes();
    assertFalse("expecting RichtText page type reference.", allPageTypes.isEmpty());
    PageTypeReference richTextPTref = allPageTypes.get(0);
    assertEquals("RichText", richTextPTref.getConfigName());
    assertEquals(Arrays.asList(""), richTextPTref.getCategories());
    verifyDefault();
  }

}
