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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.cache.CacheFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderingEngine;

public class RenderCommandTest extends AbstractBridgedComponentTestCase {

  private RenderCommand renderCmd;
  private XWikiContext context;
  private XWiki xwiki;
  private PageTypeCommand mockPageTypeCmd;
  private XWikiRenderingEngine renderingEngineMock;
  private XWikiDocument currentDoc;
  private VelocityContext velocityContext;

  @Before
  public void setUp_RenderCommandTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    velocityContext = new VelocityContext();
    context.put("vcontext", velocityContext);
    currentDoc = new XWikiDocument(new DocumentReference(context.getDatabase(), "Content",
        "MyPage"));
    context.setDoc(currentDoc);
    renderCmd = new RenderCommand();
    mockPageTypeCmd = createMock(PageTypeCommand.class);
    renderCmd.inject_PageTypeCmd(mockPageTypeCmd);
    renderingEngineMock = createMock(XWikiRenderingEngine.class);
    renderCmd.setRenderingEngine(renderingEngineMock);
  }

  @Test
  public void testPageTypeCmd() {
    renderCmd.inject_PageTypeCmd(null);
    assertNotNull(renderCmd.pageTypeCmd());
    assertSame("expecting same instance.", renderCmd.pageTypeCmd(), 
        renderCmd.pageTypeCmd());
    assertNotSame("expecting not same instance.", renderCmd.pageTypeCmd(), 
        new RenderCommand().pageTypeCmd());
  }

  @Test
  public void testInject_pageTypeCmd() {
    renderCmd.inject_PageTypeCmd(mockPageTypeCmd);
    assertNotNull(renderCmd.pageTypeCmd());
    assertSame("Expecting injected mock object.", mockPageTypeCmd,
        renderCmd.pageTypeCmd());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetRenderingEngine() throws Exception {
    expect(xwiki.Param(isA(String.class), eq("0"))).andReturn("0").anyTimes();
    expect(xwiki.Param(isA(String.class), eq("1"))).andReturn("1").anyTimes();
    expect(xwiki.Param(eq("xwiki.render.cache.capacity"))).andReturn(null).anyTimes();
    expect(xwiki.getXWikiPreference(eq("macros_languages"), eq("velocity,groovy"),
        same(context))).andReturn("velocity,groovy").anyTimes();
    expect(xwiki.getXWikiPreference(eq("macros_velocity"), eq("XWiki.VelocityMacros"),
        same(context))).andReturn("XWiki.VelocityMacros").anyTimes();
    expect(xwiki.getXWikiPreference(eq("macros_groovy"), eq("XWiki.GroovyMacros"),
        same(context))).andReturn("XWiki.GroovyMacros").anyTimes();
    expect(xwiki.getMacroList(same(context))).andReturn("").anyTimes();
    CacheFactory cacheFactory = createMock(CacheFactory.class);
    expect(xwiki.getCacheFactory()).andReturn(cacheFactory);
    XWikiConfig conf = new XWikiConfig();
    expect(xwiki.getConfig()).andReturn(conf);
    replay(xwiki, mockPageTypeCmd, renderingEngineMock);
    renderCmd.setRenderingEngine(null);
    assertNotNull(renderCmd.getRenderingEngine());
    assertSame("Expecting singleton.", renderCmd.getRenderingEngine(), 
        renderCmd.getRenderingEngine());
    List<String> rendererNames = renderCmd.getRenderingEngine().getRendererNames();
    assertTrue("expecting that velocity renderer is activated by default",
        rendererNames.contains("velocity"));
    assertTrue("expecting that groovy renderer is activated by default",
        rendererNames.contains("groovy"));
    assertEquals("expecting only groovy and velocity renderer by default", 2,
        rendererNames.size());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock);
  }

  @Test
  public void testSetRenderingEngine() throws Exception {
    replay(xwiki, mockPageTypeCmd, renderingEngineMock);
    renderCmd.setRenderingEngine(renderingEngineMock);
    assertNotNull(renderCmd.getRenderingEngine());
    assertSame("Expecting injected mock object.", renderingEngineMock,
        renderCmd.getRenderingEngine());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock);
  }

  @Test
  public void testGetRenderTemplatePath_NoCellType() throws Exception {
    String cellDocFN = "MyLayout.Cell12";
    replay(xwiki, mockPageTypeCmd, renderingEngineMock);
    assertEquals(cellDocFN, renderCmd.getRenderTemplatePath(null, cellDocFN, "view"));
    verify(xwiki, mockPageTypeCmd, renderingEngineMock);
  }

  @Test
  public void testGetRenderTemplatePath_NoViewTemplate_empty() throws Exception {
    String cellDocFN = "MyLayout.Cell12";
    PageType ptMock = createMock(PageType.class);
    expect(ptMock.getRenderTemplate(eq("view"), same(context))).andReturn("").anyTimes();
    expect(ptMock.getFullName()).andReturn("PageTypes.CelementsCell").anyTimes();
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock);
    assertEquals(cellDocFN, renderCmd.getRenderTemplatePath(ptMock, cellDocFN, "view"));
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock);
  }

  @Test
  public void testGetRenderTemplatePath_NoViewTemplate_null() throws Exception {
    String cellDocFN = "MyLayout.Cell12";
    PageType ptMock = createMock(PageType.class);
    expect(ptMock.getRenderTemplate(eq("view"), same(context))).andReturn(null).anyTimes();
    expect(ptMock.getFullName()).andReturn("PageTypes.CelementsCell").anyTimes();
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock);
    assertEquals(cellDocFN, renderCmd.getRenderTemplatePath(ptMock, cellDocFN, "view"));
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock);
  }

  @Test
  public void testGetTemplateDoc() throws Exception {
    DocumentReference templateDocRef = new DocumentReference(context.getDatabase(),
        "Templates", "CelementsPageContentView");
    XWikiDocument templateDoc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(templateDocRef), same(context))).andReturn(templateDoc
        ).once();
    replay(xwiki, templateDoc);
    assertSame("expected templateDoc.", templateDoc, renderCmd.getTemplateDoc(
        templateDocRef));
    verify(xwiki, templateDoc);
  }

  @Test
  public void testGetTranslatedContent() throws Exception {
    XWikiDocument templateDoc = createMock(XWikiDocument.class);
    String expectedContent = "do something and velocity macro...\n";
    String transContent = "{pre}\n"
      + expectedContent
      + "{/pre}";
    expect(templateDoc.getTranslatedContent(eq("de"), same(context))).andReturn(
        transContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity",
        "groovy"));
    expect(templateDoc.getDocumentReference()).andReturn(new DocumentReference(
        context.getDatabase(), "MySpace", "myDoc")).anyTimes();
    replay(xwiki, renderingEngineMock, templateDoc);
    assertEquals("expected removing pre-tags", "\n" + expectedContent,
        renderCmd.getTranslatedContent(templateDoc, "de"));
    verify(xwiki, renderingEngineMock, templateDoc);
  }

  @Test
  public void testGetTranslatedContent_wikiRenderer() throws Exception {
    XWikiDocument templateDoc = createMock(XWikiDocument.class);
    String expectedContent = "do something and velocity macro...\n";
    String transContent = "{pre}\n"
      + expectedContent
      + "{/pre}";
    expect(templateDoc.getTranslatedContent(eq("fr"), same(context))).andReturn(
        transContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity",
        "groovy", "xwiki"));
    expect(templateDoc.getDocumentReference()).andReturn(new DocumentReference(
        context.getDatabase(), "MySpace", "myDoc")).anyTimes();
    replay(xwiki, renderingEngineMock, templateDoc);
    assertEquals("expected removing pre-tags", "{pre}\n" + expectedContent + "{/pre}",
        renderCmd.getTranslatedContent(templateDoc, "fr"));
    verify(xwiki, renderingEngineMock, templateDoc);
  }

  @Test
  public void testRenderCelementsDocument_noCellType_default() throws Exception {
    String expectedRenderedContent = "Expected rendered content";
    XWikiDocument myDoc = createMock(XWikiDocument.class);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content",
        "myPage");
    expect(myDoc.getDocumentReference()).andReturn(myDocRef).atLeastOnce();
    expect(mockPageTypeCmd.getPageTypeWithDefaultObj(same(myDoc), (String)isNull(),
        same(context))).andReturn(null);
    expect(xwiki.getDocument(eq(myDocRef), same(context))).andReturn(myDoc).once();
    String expectedContent = "expected Content $doc.fullName";
    expect(myDoc.getTranslatedContent(eq("de"), same(context))).andReturn(expectedContent
        );
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc),
        same(currentDoc), same(context))).andReturn(expectedRenderedContent);  
    expect(myDoc.newDocument(same(context))).andReturn(new Document(myDoc, context));
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity",
      "groovy"));
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, myDoc);
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsDocument(myDoc,
        "view"));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.",
        myDocRef, ((Document)velocityContext.get("celldoc")).getDocumentReference());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, myDoc);
  }

  @Test
  public void testRenderCelementsDocument_noCellType_setDefault_null() throws Exception {
    renderCmd.setDefaultPageType(null);
    String expectedRenderedContent = "Expected rendered content";
    XWikiDocument myDoc = createMock(XWikiDocument.class);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content",
        "myPage");
    expect(myDoc.getDocumentReference()).andReturn(myDocRef).atLeastOnce();
    expect(mockPageTypeCmd.getPageTypeWithDefaultObj(same(myDoc), (String)isNull(),
        same(context))).andReturn(null);
    expect(xwiki.getDocument(eq(myDocRef), same(context))).andReturn(myDoc).once();
    String expectedContent = "expected Content $doc.fullName";
    expect(myDoc.getTranslatedContent(eq("de"), same(context))).andReturn(expectedContent
        );
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc),
        same(currentDoc), same(context))).andReturn(expectedRenderedContent);
    expect(myDoc.newDocument(same(context))).andReturn(new Document(myDoc, context));
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity",
      "groovy"));
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, myDoc);
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsDocument(myDoc,
        "view"));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.",
        myDocRef, ((Document)velocityContext.get("celldoc")).getDocumentReference());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, myDoc);
  }

  @Test
  public void testRenderCelementsDocument_noCellType_setDefault_RichText_deprecated(
      ) throws Exception {
    String defaultPT = "RichText";
    renderCmd.setDefaultPageType(defaultPT);
    String elementFullName = "MyLayout.Cell15";
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(),
        "MyLayout", "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    PageType ptMock = createMock(PageType.class);
    expect(mockPageTypeCmd.getPageTypeWithDefaultObj(same(cellDoc), eq(defaultPT),
        same(context))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplateFN = "celements2web:Templates.CellTypeView";
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web",
        "Templates", "CellTypeView");
    expect(ptMock.getRenderTemplate(eq("view"), same(context))).andReturn(
        renderTemplateFN).anyTimes();
    XWikiDocument templMock = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(renderTemplateDocRef), same(context))).andReturn(
        templMock);
    expect(ptMock.getFullName()).andReturn("PageTypes.CelementsContentPageCell"
        ).anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    expect(templMock.getTranslatedContent(eq("de"), same(context))).andReturn(
        expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templMock),
        same(currentDoc), same(context))).andReturn(expectedRenderedContent);  
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity",
      "groovy"));
    expect(templMock.getDocumentReference()).andReturn(new DocumentReference(
        context.getDatabase(), "MySpace", "myDoc")).anyTimes();
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(
        elementFullName));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.",
        elementFullName, ((Document)velocityContext.get("celldoc")).getFullName());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
  }

  @Test
  public void testRenderCelementsDocument_noCellType_setDefault_RichText(
      ) throws Exception {
    String defaultPT = "RichText";
    renderCmd.setDefaultPageType(defaultPT);
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(),
        "MyLayout", "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    PageType ptMock = createMock(PageType.class);
    expect(mockPageTypeCmd.getPageTypeWithDefaultObj(same(cellDoc), eq(defaultPT),
        same(context))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplateFN = "celements2web:Templates.CellTypeView";
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web",
        "Templates", "CellTypeView");
    expect(ptMock.getRenderTemplate(eq("view"), same(context))).andReturn(
        renderTemplateFN).anyTimes();
    XWikiDocument templMock = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(renderTemplateDocRef), same(context))).andReturn(
        templMock);
    expect(ptMock.getFullName()).andReturn("PageTypes.CelementsContentPageCell"
        ).anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    expect(templMock.getTranslatedContent(eq("de"), same(context))).andReturn(
        expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templMock),
        same(currentDoc), same(context))).andReturn(expectedRenderedContent);  
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity",
      "groovy"));
    expect(templMock.getDocumentReference()).andReturn(new DocumentReference(
        context.getDatabase(), "MySpace", "myDoc")).anyTimes();
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(
        elementDocRef));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.",
        elementDocRef, ((Document)velocityContext.get("celldoc")).getDocumentReference());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
  }

  @Test
  public void testRenderCelementsCell_databaseDoc_deprecated() throws XWikiException {
    String elementFullName = "MyLayout.Cell15";
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(),
        "MyLayout", "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    PageType ptMock = createMock(PageType.class);
    expect(mockPageTypeCmd.getPageTypeWithDefaultObj(same(cellDoc), (String)isNull(),
        same(context))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplateFN = "celements2web:Templates.CellTypeView";
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web",
        "Templates", "CellTypeView");
    expect(ptMock.getRenderTemplate(eq("view"), same(context))).andReturn(
        renderTemplateFN).anyTimes();
    XWikiDocument templMock = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(renderTemplateDocRef), same(context))).andReturn(
        templMock);
    expect(ptMock.getFullName()).andReturn("PageTypes.CelementsContentPageCell"
        ).anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    expect(templMock.getTranslatedContent(eq("de"), same(context))).andReturn(
        expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templMock),
        same(currentDoc), same(context))).andReturn(expectedRenderedContent);  
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity",
      "groovy"));
    expect(templMock.getDocumentReference()).andReturn(new DocumentReference(
        context.getDatabase(), "MySpace", "myDoc")).anyTimes();
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(
        elementFullName));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.",
        elementFullName, ((Document)velocityContext.get("celldoc")).getFullName());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
  }

  @Test
  public void testRenderCelementsCell_databaseDoc() throws XWikiException {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(),
        "MyLayout", "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    PageType ptMock = createMock(PageType.class);
    expect(mockPageTypeCmd.getPageTypeWithDefaultObj(same(cellDoc), (String)isNull(),
        same(context))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplateFN = "celements2web:Templates.CellTypeView";
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web",
        "Templates", "CellTypeView");
    expect(ptMock.getRenderTemplate(eq("view"), same(context))).andReturn(
        renderTemplateFN).anyTimes();
    XWikiDocument templMock = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(renderTemplateDocRef), same(context))).andReturn(
        templMock);
    expect(ptMock.getFullName()).andReturn("PageTypes.CelementsContentPageCell"
        ).anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    expect(templMock.getTranslatedContent(eq("de"), same(context))).andReturn(
        expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templMock),
        same(currentDoc), same(context))).andReturn(expectedRenderedContent);  
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity",
      "groovy"));
    expect(templMock.getDocumentReference()).andReturn(new DocumentReference(
        context.getDatabase(), "MySpace", "myDoc")).anyTimes();
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(
        elementDocRef));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.",
        elementDocRef, ((Document)velocityContext.get("celldoc")).getDocumentReference());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
  }

  /**
   * if the sdoc passed to renderText is null a NPE will occur.
   * @throws Exception
   */
  @Test
  public void testRenderCelementsCell_templateDoc_deprecated() throws Exception {
    String elementFullName = "MyLayout.Cell15";
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(),
        "MyLayout", "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    PageType ptMock = createMock(PageType.class);
    expect(mockPageTypeCmd.getPageTypeWithDefaultObj(same(cellDoc), (String)isNull(),
        same(context))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplatePath = ":Templates.CellTypeView";
    expect(ptMock.getRenderTemplate(eq("view"), same(context))).andReturn(
        renderTemplatePath).anyTimes();
    XWikiDocument templMock = createMock(XWikiDocument.class);
    String templatePath = "celTemplates/CellTypeView.vm";
    expect(ptMock.getFullName()).andReturn("PageTypes.CelementsContentPageCell"
        ).anyTimes();
    String expectedContent = "Expected Template Content Content.MyPage";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andReturn(
        expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc),
        same(currentDoc), same(context))).andReturn(expectedRenderedContent);  
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(
        elementFullName));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.",
        elementFullName, ((Document)velocityContext.get("celldoc")).getFullName());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
  }

  /**
   * if the sdoc passed to renderText is null a NPE will occur.
   * @throws Exception
   */
  @Test
  public void testRenderCelementsCell_templateDoc() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(),
        "MyLayout", "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    PageType ptMock = createMock(PageType.class);
    expect(mockPageTypeCmd.getPageTypeWithDefaultObj(same(cellDoc), (String)isNull(),
        same(context))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplatePath = ":Templates.CellTypeView";
    expect(ptMock.getRenderTemplate(eq("view"), same(context))).andReturn(
        renderTemplatePath).anyTimes();
    XWikiDocument templMock = createMock(XWikiDocument.class);
    String templatePath = "celTemplates/CellTypeView.vm";
    expect(ptMock.getFullName()).andReturn("PageTypes.CelementsContentPageCell"
        ).anyTimes();
    String expectedContent = "Expected Template Content Content.MyPage";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andReturn(
        expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc),
        same(currentDoc), same(context))).andReturn(expectedRenderedContent);  
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(
        elementDocRef));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.",
        elementDocRef, ((Document)velocityContext.get("celldoc")).getDocumentReference());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
  }

  @Test
  public void testRenderCelementsCell_templateDoc_ioException_deprecated(
      ) throws Exception {
    String elementFullName = "MyLayout.Cell15";
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(),
        "MyLayout", "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    PageType ptMock = createMock(PageType.class);
    expect(mockPageTypeCmd.getPageTypeWithDefaultObj(same(cellDoc), (String)isNull(),
        same(context))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplatePath = ":Templates.CellTypeView";
    expect(ptMock.getRenderTemplate(eq("view"), same(context))).andReturn(
        renderTemplatePath).anyTimes();
    XWikiDocument templMock = createMock(XWikiDocument.class);
    String templatePath = "celTemplates/CellTypeView.vm";
    expect(ptMock.getFullName()).andReturn("PageTypes.CelementsContentPageCell"
        ).anyTimes();
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andThrow(
        new IOException());
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
    assertEquals("", renderCmd.renderCelementsCell(elementFullName));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.",
        elementFullName, ((Document)velocityContext.get("celldoc")).getFullName());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
  }

  @Test
  public void testRenderCelementsCell_templateDoc_ioException() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(),
        "MyLayout", "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    PageType ptMock = createMock(PageType.class);
    expect(mockPageTypeCmd.getPageTypeWithDefaultObj(same(cellDoc), (String)isNull(),
        same(context))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplatePath = ":Templates.CellTypeView";
    expect(ptMock.getRenderTemplate(eq("view"), same(context))).andReturn(
        renderTemplatePath).anyTimes();
    XWikiDocument templMock = createMock(XWikiDocument.class);
    String templatePath = "celTemplates/CellTypeView.vm";
    expect(ptMock.getFullName()).andReturn("PageTypes.CelementsContentPageCell"
        ).anyTimes();
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andThrow(
        new IOException());
    replay(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
    assertEquals("", renderCmd.renderCelementsCell(elementDocRef));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.",
        elementDocRef, ((Document)velocityContext.get("celldoc")).getDocumentReference());
    verify(xwiki, mockPageTypeCmd, renderingEngineMock, ptMock, templMock);
  }

  @Test
  public void testGetTemplatePathOnDisk() {
    assertEquals("/templates/celTemplates/CellPageContentView.vm",
        renderCmd.getTemplatePathOnDisk(":Templates.CellPageContentView"));
    assertEquals("/templates/celTemplates/CellPageContentView.vm",
        renderCmd.getTemplatePathOnDisk(":CellPageContentView"));
  }


}
