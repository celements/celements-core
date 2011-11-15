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
package com.celements.web.pagetype;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.XWikiRequest;

public class PageTypeCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private XWikiRequest request;
  private PageTypeCommand pageTypeCmd;

  @Before
  public void setUp_PageTypeTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    request = createMock(XWikiRequest.class);
    context.setRequest(request);
    pageTypeCmd = new PageTypeCommand();
  }

  @Test
  public void testGetPageType_Default() throws Exception {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    //No PageType Object prepared -> Default PageType is RichText
    String pageTypeFN = pageTypeCmd.getPageType(doc, context);
    assertEquals("RichText", pageTypeFN);
  }

  @Test
  public void testGetPageType_MyPageType() throws Exception {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    doc.setNew(false);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setStringValue("page_type", "MyPageType");
    doc.setObject(PageTypeCommand.PAGE_TYPE_CLASSNAME, 0, pageTypeObj);
    String pageTypeFN = pageTypeCmd.getPageType(doc, context);
    assertEquals("MyPageType", pageTypeFN);
  }

  @Test
  public void testGetPageType_NewDocFromTemplate() throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    doc.setNew(true);
    String templDocName = "Blog.ArticleTemplate";
    XWikiDocument templDoc = new XWikiDocument();
    templDoc.setFullName(templDocName);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setStringValue("page_type", "Article");
    templDoc.setObject(PageTypeCommand.PAGE_TYPE_CLASSNAME, 0, pageTypeObj);
    expect(xwiki.exists(eq(templDocName), same(context))).andReturn(true);
    expect(xwiki.getDocument(eq(templDocName), same(context))).andReturn(templDoc);
    expect(request.get(eq("template"))).andReturn(templDocName);
    replay(xwiki, request);
    String pageTypeFN = pageTypeCmd.getPageType(doc, context);
    assertEquals("Expecting 'Article' PageType.", "Article", pageTypeFN);
    verify(xwiki, request);
  }

  @Test
  public void testGetPageTypeDocFN_Default() {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    //No PageType Object prepared -> Default PageType is RichText
    String pageTypeFN = pageTypeCmd.getPageTypeDocFN(doc, context);
    assertEquals("PageTypes.RichText", pageTypeFN);
  }

  @Test
  public void testGetPageTypeDocFN() {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    doc.setNew(false);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setStringValue("page_type", "MyPageType");
    doc.setObject(PageTypeCommand.PAGE_TYPE_CLASSNAME, 0, pageTypeObj);
    String pageTypeFN = pageTypeCmd.getPageTypeDocFN(doc, context);
    assertEquals("PageTypes.MyPageType", pageTypeFN);
  }

  @Test
  public void testGetPageTypeDocFN_FullNamePageType() {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    doc.setNew(false);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setStringValue("page_type", "MyPTspace.MyPageType");
    doc.setObject(PageTypeCommand.PAGE_TYPE_CLASSNAME, 0, pageTypeObj);
    String pageTypeFN = pageTypeCmd.getPageTypeDocFN(doc, context);
    assertEquals("MyPTspace.MyPageType", pageTypeFN);
  }

  @Test
  public void testGetPageTypeWithDefault_nullAsDefault() {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    assertNull(pageTypeCmd.getPageTypeWithDefault(doc, null, context));
  }

}
