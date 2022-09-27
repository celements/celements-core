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

import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.INavigation;
import com.celements.rendering.RenderCommand;
import com.celements.web.service.UrlService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderer;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.render.XWikiVirtualMacro;
import com.xpn.xwiki.web.Utils;

public class RenderedContentDynLoadPresentationTypeTest extends AbstractComponentTest {

  private XWikiContext context;
  private INavigation nav;
  private XWiki xwiki;
  private DocumentReference currentDocRef;
  private XWikiDocument currentDoc;
  private RenderedContentDynLoadPresentationType vtPresType;
  private TestRenderEngine testRenderEngine;
  private RenderCommand renderCmdMock;
  private UrlService urlServiceMock;

  @Before
  public void setUp_RenderedContentDynLoadPresentationTypeTest() throws Exception {
    xwiki = getWikiMock();
    context = getContext();
    urlServiceMock = registerComponentMock(UrlService.class);
    currentDocRef = new DocumentReference(context.getDatabase(), "MySpace", "MyCurrentDoc");
    currentDoc = new XWikiDocument(currentDocRef);
    context.setDoc(currentDoc);
    nav = createMockAndAddToDefault(INavigation.class);
    testRenderEngine = new TestRenderEngine();
    expect(xwiki.getRenderingEngine()).andReturn(testRenderEngine).anyTimes();
    vtPresType = (RenderedContentDynLoadPresentationType) Utils
        .getComponent(IPresentationTypeRole.class, "renderedContentDynLoad");
    renderCmdMock = createMockAndAddToDefault(RenderCommand.class);
    vtPresType.renderCmd = renderCmdMock;
  }

  @Test
  public void testComponentLoaded() {
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class, "renderedContentDynLoad"));
  }

  @Test
  public void testGetRenderCommand_default() {
    vtPresType.renderCmd = null;
    assertNotNull(vtPresType.getRenderCommand());
  }

  @Test
  public void testGetRenderCommand_inject() {
    RenderCommand testMock = new RenderCommand();
    vtPresType.renderCmd = testMock;
    assertSame(testMock, vtPresType.getRenderCommand());
  }

  @Test
  public void testWriteNodeContent() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "MyPage");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    StringBuilder outStream = new StringBuilder();
    boolean isFirstItem = true;
    boolean isLastItem = false;
    boolean isLeaf = true;
    String queryString = "xpage=ajax&ajax_mode=rendering/renderDocumentWithPageType&ajax=1";
    String expectedUrl = "/MySpace/MyCurrentDoc?" + queryString;
    String expectedNodeContent = "<cel-lazy-load src=\"" + expectedUrl
        + "\" size=32 ></cel-lazy-load>\n";
    expect(nav.addUniqueElementId(eq(currentDocRef))).andReturn(
        "id=\"N3:Content:Content.MyPage\"").once();
    expect(nav.addCssClasses(eq(currentDocRef), eq(true), eq(isFirstItem), eq(isLastItem), eq(
        isLeaf), eq(1))).andReturn("class=\"cel_cm_navigation_menuitem"
            + " first cel_nav_isLeaf RichText\"").once();
    expect(urlServiceMock.getURL(eq(currentDocRef), eq("view"), eq(queryString)))
        .andReturn(expectedUrl);
    replayDefault();
    vtPresType.writeNodeContent(outStream, isFirstItem, isLastItem, currentDocRef, isLeaf, 1, nav);
    assertEquals("<div class=\"cel_cm_navigation_menuitem first cel_nav_isLeaf RichText\""
        + " id=\"N3:Content:Content.MyPage\">\n" + expectedNodeContent + "</div>\n",
        outStream.toString());
    verifyDefault();
  }

  // *****************************************************************
  // * H E L P E R S *
  // *****************************************************************/

  public class TestRenderEngine implements XWikiRenderingEngine {

    private List<VelocityContext> storedVelocityContext;
    private XWikiRenderingEngine mockRenderEngine;

    public TestRenderEngine() {
      mockRenderEngine = createMock(XWikiRenderingEngine.class);
    }

    public XWikiRenderingEngine getMock() {
      return mockRenderEngine;
    }

    @Override
    public void addRenderer(String name, XWikiRenderer renderer) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String convertMultiLine(String macroname, String params, String data, String allcontent,
        XWikiVirtualMacro macro, XWikiContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String convertSingleLine(String macroname, String params, String allcontent,
        XWikiVirtualMacro macro, XWikiContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void flushCache() {
      throw new UnsupportedOperationException();
    }

    @Override
    public XWikiRenderer getRenderer(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<XWikiRenderer> getRendererList() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getRendererNames() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String interpretText(String text, XWikiDocument includingdoc, XWikiContext context) {
      VelocityContext velocityContext = (VelocityContext) context.get("vcontext");
      storedVelocityContext.add((VelocityContext) velocityContext.clone());
      return mockRenderEngine.interpretText(text, includingdoc, context);
    }

    @Override
    public String renderDocument(XWikiDocument doc, XWikiContext context) throws XWikiException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String renderDocument(XWikiDocument doc, XWikiDocument includingdoc,
        XWikiContext context) throws XWikiException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String renderText(String text, XWikiDocument includingdoc, XWikiContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String renderText(String text, XWikiDocument contentdoc, XWikiDocument includingdoc,
        XWikiContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void virtualInit(XWikiContext context) {
      throw new UnsupportedOperationException();
    }

  }

}
