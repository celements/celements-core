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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.INavigation;
import com.celements.rendering.RenderCommand;
import com.celements.web.classcollections.DocumentDetailsClasses;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.render.XWikiRenderer;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.render.XWikiVirtualMacro;
import com.xpn.xwiki.web.Utils;

public class RenderedExtractPresentationTypeTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private INavigation nav;
  private XWiki xwiki;
  private DocumentReference currentDocRef;
  private XWikiDocument currentDoc;
  private RenderedExtractPresentationType vtPresType;
  private TestRenderEngine testRenderEngine;
  private RenderCommand renderCmdMock;

  @Before
  public void setUp_RenderedExtractPresentationTypeTest() throws Exception {
    context = getContext();
    currentDocRef = new DocumentReference(context.getDatabase(), "MySpace", "MyCurrentDoc");
    currentDoc = new XWikiDocument(currentDocRef);
    context.setDoc(currentDoc);
    nav = createMockAndAddToDefault(INavigation.class);
    xwiki = getWikiMock();
    testRenderEngine = new TestRenderEngine();
    expect(xwiki.getRenderingEngine()).andReturn(testRenderEngine).anyTimes();
    vtPresType = (RenderedExtractPresentationType) Utils.getComponent(IPresentationTypeRole.class,
        "renderedExtract");
    renderCmdMock = createMockAndAddToDefault(RenderCommand.class);
    vtPresType.renderCmd = renderCmdMock;
  }

  @After
  public void tearDown_() {
    vtPresType.webUtilsService = Utils.getComponent(IWebUtilsService.class);
  }

  @Test
  public void testComponentLoaded() {
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class, "renderedExtract"));
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
    IWebUtilsService webUtilsServiceMock = createMockAndAddToDefault(IWebUtilsService.class);
    vtPresType.webUtilsService = webUtilsServiceMock;
    context.put("vcontext", new VelocityContext());
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "MyPage");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    StringBuilder outStream = new StringBuilder();
    boolean isFirstItem = true;
    boolean isLastItem = false;
    boolean isLeaf = true;
    String expectedNodeExtract = "expected rendered extract for node";
    BaseObject extractObj = new BaseObject();
    extractObj.setXClassReference(getDocDetailsClasses().getDocumentExtractClassRef(
        context.getDatabase()));
    extractObj.setStringValue(DocumentDetailsClasses.FIELD_DOC_EXTRACT_LANGUAGE, "de");
    extractObj.setStringValue(DocumentDetailsClasses.FIELD_DOC_EXTRACT_CONTENT,
        expectedNodeExtract);
    currentDoc.addXObject(extractObj);
    expect(nav.addUniqueElementId(eq(currentDocRef))).andReturn(
        "id=\"N3:Content:Content.MyPage\"").once();
    expect(nav.addCssClasses(eq(currentDocRef), eq(true), eq(isFirstItem), eq(isLastItem), eq(
        isLeaf), eq(1))).andReturn("class=\"cel_cm_navigation_menuitem"
            + " first cel_nav_isLeaf RichText\"").once();
    expect(xwiki.getDocument(eq(currentDocRef), same(context))).andReturn(currentDoc).atLeastOnce();
    DocumentReference templateDocRef = new DocumentReference(context.getDatabase(), "Templates",
        "RenderedExtract");
    String templateDiskPath = ":celTemplates/RenderedExtract.vm";
    expect(webUtilsServiceMock.getInheritedTemplatedPath(eq(templateDocRef))).andReturn(
        templateDiskPath);
    expect(renderCmdMock.renderTemplatePath(eq(templateDiskPath), eq("de"), eq("")))
        .andReturn(expectedNodeExtract);
    replayDefault();
    String expectedRenderedExtract = "<div class=\"cel_cm_navigation_menuitem first"
        + " cel_nav_isLeaf RichText\" id=\"N3:Content:Content.MyPage\">\n" + expectedNodeExtract
        + "</div>\n";
    vtPresType.writeNodeContent(outStream, isFirstItem, isLastItem, currentDocRef, isLeaf, 1, nav);
    assertEquals(expectedRenderedExtract, outStream.toString());
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    assertEquals(expectedNodeExtract, vcontext.get("extractContent"));
    assertEquals(currentDocRef, vcontext.get("extractDocRef"));
    assertEquals(currentDocRef, ((Document) vcontext.get("extractDoc")).getDocumentReference());
    verifyDefault();
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private DocumentDetailsClasses getDocDetailsClasses() {
    return (DocumentDetailsClasses) Utils.getComponent(IClassCollectionRole.class,
        "celements.documentDetails");
  }

  public class TestRenderEngine implements XWikiRenderingEngine {

    private List<VelocityContext> storedVelocityContext;
    private XWikiRenderingEngine mockRenderEngine;

    public TestRenderEngine() {
      mockRenderEngine = createMockAndAddToDefault(XWikiRenderingEngine.class);
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
