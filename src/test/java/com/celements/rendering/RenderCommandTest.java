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
package com.celements.rendering;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.cache.CacheFactory;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class RenderCommandTest extends AbstractComponentTest {

  private RenderCommand renderCmd;
  private XWikiContext context;
  private XWiki xwiki;
  private IPageTypeRole mockPageTypeService;
  private XWikiRenderingEngine renderingEngineMock;
  private XWikiDocument currentDoc;
  private VelocityContext velocityContext;
  private XWikiRightService mockRightService;
  private IPageTypeResolverRole mockPageTypeResolver;

  @Before
  public void setUp_RenderCommandTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    velocityContext = (VelocityContext) getExecutionContext().getProperty("velocityContext");
    assertNotNull(velocityContext);
    context.put("vcontext", velocityContext);
    currentDoc = new XWikiDocument(new DocumentReference(context.getDatabase(), "Content",
        "MyPage"));
    context.setDoc(currentDoc);
    renderCmd = new RenderCommand();
    mockPageTypeService = createMockAndAddToDefault(IPageTypeRole.class);
    renderCmd.injectedPageTypeService = mockPageTypeService;
    mockPageTypeResolver = createMockAndAddToDefault(IPageTypeResolverRole.class);
    renderCmd.injectedPageTypeResolver = mockPageTypeResolver;
    renderingEngineMock = createMockAndAddToDefault(XWikiRenderingEngine.class);
    renderCmd.setRenderingEngine(renderingEngineMock);
    mockRightService = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightService).anyTimes();
  }

  @Test
  public void testPageTypeService() {
    renderCmd.injectedPageTypeService = null;
    assertNotNull(renderCmd.getPageTypeService());
    assertSame("expecting same instance.", renderCmd.getPageTypeService(),
        renderCmd.getPageTypeService());
  }

  @Test
  public void testInject_PageTypeService() {
    renderCmd.injectedPageTypeService = mockPageTypeService;
    assertNotNull(renderCmd.getPageTypeService());
    assertSame("Expecting injected mock object.", mockPageTypeService,
        renderCmd.getPageTypeService());
  }

  @Test
  public void testPageTypeResolver() {
    renderCmd.injectedPageTypeResolver = null;
    assertNotNull(renderCmd.getPageTypeResolver());
    assertSame("expecting same instance.", renderCmd.getPageTypeResolver(),
        renderCmd.getPageTypeResolver());
  }

  @Test
  public void testInject_PageTypeResolver() {
    renderCmd.injectedPageTypeResolver = mockPageTypeResolver;
    assertNotNull(renderCmd.getPageTypeResolver());
    assertSame("Expecting injected mock object.", mockPageTypeResolver,
        renderCmd.getPageTypeResolver());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetRenderingEngine() throws Exception {
    expect(xwiki.Param(isA(String.class), eq("0"))).andReturn("0").anyTimes();
    expect(xwiki.Param(isA(String.class), eq("1"))).andReturn("1").anyTimes();
    expect(xwiki.Param(eq("xwiki.render.cache.capacity"))).andReturn(null).anyTimes();
    expect(xwiki.getXWikiPreference(eq("macros_languages"), eq("velocity,groovy"), same(
        context))).andReturn("velocity,groovy").anyTimes();
    expect(xwiki.getXWikiPreference(eq("macros_velocity"), eq("XWiki.VelocityMacros"), same(
        context))).andReturn("XWiki.VelocityMacros").anyTimes();
    expect(xwiki.getXWikiPreference(eq("macros_groovy"), eq("XWiki.GroovyMacros"), same(
        context))).andReturn("XWiki.GroovyMacros").anyTimes();
    expect(xwiki.getMacroList(same(context))).andReturn("").anyTimes();
    CacheFactory cacheFactory = createMock(CacheFactory.class);
    expect(xwiki.getCacheFactory()).andReturn(cacheFactory);
    XWikiConfig conf = new XWikiConfig();
    expect(xwiki.getConfig()).andReturn(conf);
    replayDefault();
    renderCmd.setRenderingEngine(null);
    assertNotNull(renderCmd.getRenderingEngine());
    assertSame("Expecting singleton.", renderCmd.getRenderingEngine(),
        renderCmd.getRenderingEngine());
    List<String> rendererNames = renderCmd.getRenderingEngine().getRendererNames();
    assertTrue("expecting that velocity renderer is activated by default", rendererNames.contains(
        "velocity"));
    assertTrue("expecting that groovy renderer is activated by default", rendererNames.contains(
        "groovy"));
    assertEquals("expecting only groovy and velocity renderer by default", 2, rendererNames.size());
    verifyDefault();
  }

  @Test
  public void testSetRenderingEngine() throws Exception {
    replayDefault();
    renderCmd.setRenderingEngine(renderingEngineMock);
    assertNotNull(renderCmd.getRenderingEngine());
    assertSame("Expecting injected mock object.", renderingEngineMock,
        renderCmd.getRenderingEngine());
    verifyDefault();
  }

  @Test
  public void testGetRenderTemplatePath_NoCellType() throws Exception {
    String cellDocFN = "MyLayout.Cell12";
    replayDefault();
    assertEquals(cellDocFN, renderCmd.getRenderTemplatePath(null, cellDocFN, "view"));
    verifyDefault();
  }

  @Test
  public void testGetRenderTemplatePath_NoViewTemplate_empty() throws Exception {
    String cellDocFN = "MyLayout.Cell12";
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn("").anyTimes();
    expect(ptMock.getName()).andReturn("CelementsCell").anyTimes();
    replayDefault();
    assertEquals(cellDocFN, renderCmd.getRenderTemplatePath(ptMock, cellDocFN, "view"));
    verifyDefault();
  }

  @Test
  public void testGetRenderTemplatePath_NoViewTemplate_null() throws Exception {
    String cellDocFN = "MyLayout.Cell12";
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(null).anyTimes();
    expect(ptMock.getName()).andReturn("CelementsCell").anyTimes();
    replayDefault();
    assertEquals(cellDocFN, renderCmd.getRenderTemplatePath(ptMock, cellDocFN, "view"));
    verifyDefault();
  }

  @Test
  public void testGetTemplateDoc() throws Exception {
    DocumentReference templateDocRef = new DocumentReference(context.getDatabase(), "Templates",
        "CelementsPageContentView");
    XWikiDocument templateDoc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(templateDocRef), same(context))).andReturn(templateDoc).once();
    replay(xwiki, templateDoc);
    assertSame("expected templateDoc.", templateDoc, renderCmd.getTemplateDoc(templateDocRef));
    verify(xwiki, templateDoc);
  }

  @Test
  public void testGetTranslatedContent() throws Exception {
    XWikiDocument templateDoc = createMockAndAddToDefault(XWikiDocument.class);
    String expectedContent = "do something and velocity macro...\n";
    String transContent = "{pre}\n" + expectedContent + "{/pre}";
    expect(templateDoc.getTranslatedContent(eq("de"), same(context))).andReturn(transContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(templateDoc.getDocumentReference()).andReturn(new DocumentReference(
        context.getDatabase(), "MySpace", "myDoc")).anyTimes();
    replay(xwiki, renderingEngineMock, templateDoc);
    assertEquals("expected removing pre-tags", "\n" + expectedContent,
        renderCmd.getTranslatedContent(templateDoc, "de"));
    verify(xwiki, renderingEngineMock, templateDoc);
  }

  @Test
  public void testGetTranslatedContent_wikiRenderer() throws Exception {
    XWikiDocument templateDoc = createMockAndAddToDefault(XWikiDocument.class);
    String expectedContent = "do something and velocity macro...\n";
    String transContent = "{pre}\n" + expectedContent + "{/pre}";
    expect(templateDoc.getTranslatedContent(eq("fr"), same(context))).andReturn(transContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy",
        "xwiki"));
    expect(templateDoc.getDocumentReference()).andReturn(new DocumentReference(
        context.getDatabase(), "MySpace", "myDoc")).anyTimes();
    replay(xwiki, renderingEngineMock, templateDoc);
    assertEquals("expected removing pre-tags", "{pre}\n" + expectedContent + "{/pre}",
        renderCmd.getTranslatedContent(templateDoc, "fr"));
    verify(xwiki, renderingEngineMock, templateDoc);
  }

  @Test
  public void testRenderCelementsDocument_elemDocRef_renderMode() throws Exception {
    String expectedRenderedContent = "Expected rendered content";
    XWikiDocument myDoc = createMockAndAddToDefault(XWikiDocument.class);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    expect(myDoc.getDocumentReference()).andReturn(myDocRef).atLeastOnce();
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc)))
        .andReturn(Optional.absent());
    expect(xwiki.getDocument(eq(myDocRef), same(context))).andReturn(myDoc).atLeastOnce();
    String expectedContent = "expected Content $doc.fullName";
    expect(myDoc.getTranslatedContent(eq("de"), same(context))).andReturn(expectedContent);
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc), same(
        context))).andReturn(expectedRenderedContent);
    expect(myDoc.newDocument(same(context))).andReturn(new Document(myDoc, context));
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:Content.myPage"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsDocument(myDocRef, "view"));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", myDocRef,
        ((Document) velocityContext.get("celldoc")).getDocumentReference());
    verifyDefault();
  }

  @Test
  public void testRenderCelementsDocument_noCellType_default() throws Exception {
    String expectedRenderedContent = "Expected rendered content";
    XWikiDocument myDoc = createMockAndAddToDefault(XWikiDocument.class);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    expect(myDoc.getDocumentReference()).andReturn(myDocRef).atLeastOnce();
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc))).andReturn(Optional.absent());
    expect(xwiki.getDocument(eq(myDocRef), same(context))).andReturn(myDoc).once();
    String expectedContent = "expected Content $doc.fullName";
    expect(myDoc.getTranslatedContent(eq("de"), same(context))).andReturn(expectedContent);
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc), same(
        context))).andReturn(expectedRenderedContent);
    expect(myDoc.newDocument(same(context))).andReturn(new Document(myDoc, context));
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:Content.myPage"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsDocument(myDoc, "view"));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", myDocRef,
        ((Document) velocityContext.get("celldoc")).getDocumentReference());
    verifyDefault();
  }

  @Test
  public void testRenderCelementsDocument_noCellType_setDefault_null() throws Exception {
    renderCmd.setDefaultPageTypeReference(null);
    String expectedRenderedContent = "Expected rendered content";
    XWikiDocument myDoc = createMockAndAddToDefault(XWikiDocument.class);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    expect(myDoc.getDocumentReference()).andReturn(myDocRef).atLeastOnce();
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc))).andReturn(Optional.absent());
    expect(xwiki.getDocument(eq(myDocRef), same(context))).andReturn(myDoc).once();
    String expectedContent = "expected Content $doc.fullName";
    expect(myDoc.getTranslatedContent(eq("de"), same(context))).andReturn(expectedContent);
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc), same(
        context))).andReturn(expectedRenderedContent);
    expect(myDoc.newDocument(same(context))).andReturn(new Document(myDoc, context));
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:Content.myPage"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsDocument(myDoc, "view"));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", myDocRef,
        ((Document) velocityContext.get("celldoc")).getDocumentReference());
    verifyDefault();
  }

  @Test
  public void testRenderCelementsDocument_access_denied() throws Exception {
    renderCmd.setDefaultPageTypeReference(null);
    XWikiDocument myDoc = createMockAndAddToDefault(XWikiDocument.class);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    expect(myDoc.getDocumentReference()).andReturn(myDocRef).anyTimes();
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc))).andReturn(Optional.absent())
        .anyTimes();
    expect(xwiki.getDocument(eq(myDocRef), same(context))).andReturn(myDoc).anyTimes();
    String expectedContent = "expected Content $doc.fullName";
    expect(myDoc.getTranslatedContent(eq("de"), same(context))).andReturn(
        expectedContent).anyTimes();
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc), same(
        context))).andReturn("Topic Content.MyPage does not exist").anyTimes();
    expect(myDoc.newDocument(same(context))).andReturn(new Document(myDoc, context)).anyTimes();
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity",
        "groovy")).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:Content.myPage"), same(context))).andReturn(false).once();
    replayDefault();
    assertEquals("expecting empty because no access rights to template", "",
        renderCmd.renderCelementsDocument(myDoc, "de", "view"));
    verifyDefault();
  }

  @Test
  public void testRenderCelementsDocumentPreserveVelocityContext() throws Exception {
    renderCmd.setDefaultPageTypeReference(null);
    XWikiDocument myDoc = createMockAndAddToDefault(XWikiDocument.class);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    expect(myDoc.getDocumentReference()).andReturn(myDocRef).anyTimes();
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc))).andReturn(Optional.absent())
        .anyTimes();
    expect(xwiki.getDocument(eq(myDocRef), same(context))).andReturn(myDoc).anyTimes();
    String expectedContent = "expected Content $doc.fullName";
    expect(myDoc.getTranslatedContent(eq("de"), same(context))).andReturn(
        expectedContent).anyTimes();
    String expectedRenderedContent = "expected rendered content of Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc),
        notSameVcontext(context))).andReturn(expectedRenderedContent).atLeastOnce();
    expect(myDoc.newDocument(same(context))).andReturn(new Document(myDoc, context)).anyTimes();
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity",
        "groovy")).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:Content.myPage"), same(context))).andReturn(true).once();
    VelocityContext expectedVContext = (VelocityContext) context.get("vcontext");
    replayDefault();
    assertNotNull(expectedVContext);
    assertNotNull(getExecutionContext().getProperty("velocityContext"));
    assertSame(expectedVContext, getExecutionContext().getProperty("velocityContext"));
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsDocumentPreserveVelocityContext(
        myDocRef, "de", "view"));
    assertSame(expectedVContext, context.get("vcontext"));
    assertSame(expectedVContext, getExecutionContext().getProperty("velocityContext"));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testRenderCelementsCell_noCellType_setDefault_RichText_deprecated() throws Exception {
    PageTypeReference defaultPTRef = new PageTypeReference("RichText", "xObjectProvider",
        Arrays.asList(""));
    renderCmd.setDefaultPageTypeReference(defaultPTRef);
    String elementFullName = "MyLayout.Cell15";
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplateFN = "celements2web:Templates.CellTypeView";
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web", "Templates",
        "CellTypeView");
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        renderTemplateFN).anyTimes();
    XWikiDocument templMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(renderTemplateDocRef), same(context))).andReturn(templMock);
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    expect(templMock.getTranslatedContent(eq("de"), same(context))).andReturn(expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templMock), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(templMock.getDocumentReference()).andReturn(new DocumentReference(context.getDatabase(),
        "MySpace", "myDoc")).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(elementFullName));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", elementFullName,
        ((Document) velocityContext.get("celldoc")).getFullName());
    verifyDefault();
  }

  @Test
  public void testRenderCelementsCell_noCellType_setDefault_RichText() throws Exception {
    PageTypeReference defaultPTRef = new PageTypeReference("RichText", "xObjectProvider",
        Arrays.asList(""));
    renderCmd.setDefaultPageTypeReference(defaultPTRef);
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplateFN = "celements2web:Templates.CellTypeView";
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web", "Templates",
        "CellTypeView");
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        renderTemplateFN).anyTimes();
    XWikiDocument templMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(renderTemplateDocRef), same(context))).andReturn(templMock);
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    expect(templMock.getTranslatedContent(eq("de"), same(context))).andReturn(expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templMock), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(templMock.getDocumentReference()).andReturn(new DocumentReference(context.getDatabase(),
        "MySpace", "myDoc")).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(elementDocRef));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", elementDocRef,
        ((Document) velocityContext.get("celldoc")).getDocumentReference());
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testRenderCelementsCell_databaseDoc_deprecated() throws XWikiException {
    String elementFullName = "MyLayout.Cell15";
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplateFN = "celements2web:Templates.CellTypeView";
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web", "Templates",
        "CellTypeView");
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        renderTemplateFN).anyTimes();
    XWikiDocument templMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(renderTemplateDocRef), same(context))).andReturn(templMock);
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    expect(templMock.getTranslatedContent(eq("de"), same(context))).andReturn(expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templMock), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(templMock.getDocumentReference()).andReturn(new DocumentReference(context.getDatabase(),
        "MySpace", "myDoc")).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(elementFullName));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", elementFullName,
        ((Document) velocityContext.get("celldoc")).getFullName());
    verifyDefault();
  }

  @Test
  public void testRenderCelementsCell_databaseDoc() throws XWikiException {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplateFN = "celements2web:Templates.CellTypeView";
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web", "Templates",
        "CellTypeView");
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        renderTemplateFN).anyTimes();
    XWikiDocument templMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(renderTemplateDocRef), same(context))).andReturn(templMock);
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    expect(templMock.getTranslatedContent(eq("de"), same(context))).andReturn(expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templMock), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(templMock.getDocumentReference()).andReturn(new DocumentReference(context.getDatabase(),
        "MySpace", "myDoc")).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(elementDocRef));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", elementDocRef,
        ((Document) velocityContext.get("celldoc")).getDocumentReference());
    verifyDefault();
  }

  /**
   * if the sdoc passed to renderText is null a NPE will occur.
   *
   * @throws Exception
   */
  @Test
  @Deprecated
  public void testRenderCelementsCell_templateDoc_deprecated() throws Exception {
    String elementFullName = "MyLayout.Cell15";
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplatePath = ":Templates.CellTypeView";
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        renderTemplatePath).anyTimes();
    String templatePath_lang = "celTemplates/CellTypeView_de.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_lang))).andThrow(
        new IOException());
    String templatePath = "celTemplates/CellTypeView.vm";
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    String expectedContent = "Expected Template Content Content.MyPage";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andReturn(expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(elementFullName));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", elementFullName,
        ((Document) velocityContext.get("celldoc")).getFullName());
    verifyDefault();
  }

  /**
   * if the sdoc passed to renderText is null a NPE will occur.
   *
   * @throws Exception
   */
  @Test
  public void testRenderCelementsCell_templateDoc() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplatePath = ":Templates.CellTypeView";
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        renderTemplatePath).anyTimes();
    String templatePath_lang = "celTemplates/CellTypeView_de.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_lang))).andThrow(
        new IOException());
    String templatePath = "celTemplates/CellTypeView.vm";
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    String expectedContent = "Expected Template Content Content.MyPage";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andReturn(expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(elementDocRef));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", elementDocRef,
        ((Document) velocityContext.get("celldoc")).getDocumentReference());
    verifyDefault();
  }

  /**
   * if the sdoc passed to renderText is null a NPE will occur.
   *
   * @throws Exception
   */
  @Test
  public void testRenderCelementsCell_templateDoc_lang() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplatePath = ":Templates.CellTypeView";
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        renderTemplatePath).anyTimes();
    String templatePath_lang = "celTemplates/CellTypeView_de.vm";
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    String expectedContent = "Expected Template Content Content.MyPage";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_lang))).andReturn(
        expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(elementDocRef));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", elementDocRef,
        ((Document) velocityContext.get("celldoc")).getDocumentReference());
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testRenderCelementsCell_templateDoc_ioException_deprecated() throws Exception {
    String elementFullName = "MyLayout.Cell15";
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplatePath = ":Templates.CellTypeView";
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        renderTemplatePath).anyTimes();
    String templatePath_lang = "celTemplates/CellTypeView_de.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_lang))).andThrow(
        new IOException());
    String templatePath = "celTemplates/CellTypeView.vm";
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andThrow(
        new IOException()).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals("", renderCmd.renderCelementsCell(elementFullName));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", elementFullName,
        ((Document) velocityContext.get("celldoc")).getFullName());
    verifyDefault();
  }

  @Test
  public void testRenderCelementsCell_templateDoc_ioException() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(xwiki.getDocument(eq(elementDocRef), same(context))).andReturn(cellDoc);
    String renderTemplatePath = ":Templates.CellTypeView";
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        renderTemplatePath).anyTimes();
    String templatePath_lang = "celTemplates/CellTypeView_de.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_lang))).andThrow(
        new IOException());
    String templatePath = "celTemplates/CellTypeView.vm";
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andThrow(
        new IOException()).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals("", renderCmd.renderCelementsCell(elementDocRef));
    assertEquals("Document object in velocity space musst be a Document api object.",
        Document.class, velocityContext.get("celldoc").getClass());
    assertEquals("expecting celldoc to be set to the rendered cell document.", elementDocRef,
        ((Document) velocityContext.get("celldoc")).getDocumentReference());
    verifyDefault();
  }

  @Test
  public void testGetTemplatePathOnDisk() {
    assertEquals("/templates/celTemplates/CellPageContentView.vm", renderCmd.getTemplatePathOnDisk(
        ":Templates.CellPageContentView"));
    assertEquals("/templates/celTemplates/CellPageContentView.vm", renderCmd.getTemplatePathOnDisk(
        ":CellPageContentView"));
  }

  @Test
  public void testRenderTemplatePath_null_lang() throws Exception {
    String renderTemplatePath = ":Templates.CellTypeView";
    String templatePath = "celTemplates/CellTypeView.vm";
    String expectedContent = "Expected Template Content Content.MyPage";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andReturn(
        expectedContent).once();
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderTemplatePath(renderTemplatePath, null));
    verifyDefault();
  }

  @Test
  public void testRenderTemplatePath_lang() throws Exception {
    String renderTemplatePath = ":Templates.CellTypeView";
    String templatePath_lang = "celTemplates/CellTypeView_de.vm";
    String expectedContent = "Expected Template Content Content.MyPage";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_lang))).andReturn(
        expectedContent).once();
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderTemplatePath(renderTemplatePath, "de"));
    verifyDefault();
  }

  @Test
  public void testRenderTemplatePath_langNotFound_deflang() throws Exception {
    String renderTemplatePath = ":Templates.CellTypeView";
    String templatePath_lang = "celTemplates/CellTypeView_de.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_lang))).andThrow(
        new IOException()).once();
    String templatePath_deflang = "celTemplates/CellTypeView_en.vm";
    String expectedContent = "Expected Template Content Content.MyPage";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_deflang))).andReturn(
        expectedContent).once();
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderTemplatePath(renderTemplatePath, "de",
        "en"));
    verifyDefault();
  }

  @Test
  public void testRenderTemplatePath_langNotFound_deflangNotFound() throws Exception {
    String renderTemplatePath = ":Templates.CellTypeView";
    String templatePath_lang = "celTemplates/CellTypeView_de.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_lang))).andThrow(
        new IOException()).once();
    String templatePath_deflang = "celTemplates/CellTypeView_en.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_deflang))).andThrow(
        new IOException()).once();
    String templatePath = "celTemplates/CellTypeView.vm";
    String expectedContent = "Expected Template Content Content.MyPage";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andReturn(
        expectedContent).once();
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderTemplatePath(renderTemplatePath, "de",
        "en"));
    verifyDefault();
  }

  @Test
  public void testRenderTemplatePath_langNotFound_deflangNotFound_defaultNotFound()
      throws Exception {
    String renderTemplatePath = ":Templates.CellTypeView";
    String templatePath_lang = "celTemplates/CellTypeView_de.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_lang))).andThrow(
        new IOException()).once();
    String templatePath_deflang = "celTemplates/CellTypeView_en.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_deflang))).andThrow(
        new IOException()).once();
    String templatePath = "celTemplates/CellTypeView.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andThrow(
        new IOException()).once();
    replayDefault();
    assertEquals("", renderCmd.renderTemplatePath(renderTemplatePath, "de", "en"));
    verifyDefault();
  }

  /**
   * try to read _en file only once!
   *
   * @throws Exception
   */
  @Test
  public void testRenderTemplatePath_deflang_equals_lang_notFound() throws Exception {
    String renderTemplatePath = ":Templates.CellTypeView";
    String templatePath_lang = "celTemplates/CellTypeView_en.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_lang))).andThrow(
        new IOException()).once();
    String expectedContent = "Expected Template Content Content.MyPage";
    String templatePath = "celTemplates/CellTypeView.vm";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andReturn(
        expectedContent).once();
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderTemplatePath(renderTemplatePath, "en",
        "en"));
    verifyDefault();
  }

  @Test
  public void testRenderDocument() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = createMockAndAddToDefault(XWikiDocument.class);
    String expectedRenderedContent = "expected Content";
    expect(cellDoc.getDocumentReference()).andReturn(elementDocRef).anyTimes();
    String contentEN = "english script $test";
    expect(cellDoc.getTranslatedContent(eq("en"), same(context))).andReturn(contentEN);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(renderingEngineMock.renderText(eq(contentEN), same(cellDoc), same(currentDoc), same(
        context))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderDocument(cellDoc, null, "en"));
    verifyDefault();
  }

  @Test
  public void testRenderDocument_includingDoc() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = createMockAndAddToDefault(XWikiDocument.class);
    String expectedRenderedContent = "expected Content";
    expect(cellDoc.getDocumentReference()).andReturn(elementDocRef).anyTimes();
    String contentEN = "english script $test";
    expect(cellDoc.getTranslatedContent(eq("en"), same(context))).andReturn(contentEN);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    DocumentReference includeDocRef = new DocumentReference(context.getDatabase(), "Includeing",
        "TheIncludingDocumentName");
    XWikiDocument includeDoc = new XWikiDocument(includeDocRef);
    expect(renderingEngineMock.renderText(eq(contentEN), same(cellDoc), same(includeDoc), same(
        context))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderDocument(cellDoc, includeDoc, "en"));
    verifyDefault();
  }

  @Test
  public void testRenderDocument_docref() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = createMockAndAddToDefault(XWikiDocument.class);
    String expectedRenderedContent = "expected Content";
    expect(cellDoc.getDocumentReference()).andReturn(elementDocRef).anyTimes();
    String contentEN = "english script $test";
    expect(cellDoc.getTranslatedContent(eq("en"), same(context))).andReturn(contentEN);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(renderingEngineMock.renderText(eq(contentEN), same(cellDoc), same(currentDoc), same(
        context))).andReturn(expectedRenderedContent);
    expect(xwiki.getDocument(elementDocRef, getContext())).andReturn(cellDoc).atLeastOnce();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderDocument(elementDocRef, "en"));
    verifyDefault();
  }

  @Test
  public void testRenderDocument_docref_docref() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = createMockAndAddToDefault(XWikiDocument.class);
    String expectedRenderedContent = "expected Content";
    expect(cellDoc.getDocumentReference()).andReturn(elementDocRef).anyTimes();
    String contentEN = "english script $test";
    expect(cellDoc.getTranslatedContent(eq("en"), same(context))).andReturn(contentEN);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    DocumentReference includeDocRef = new DocumentReference(context.getDatabase(), "Includeing",
        "TheIncludingDocumentName");
    XWikiDocument includeDoc = new XWikiDocument(includeDocRef);
    expect(renderingEngineMock.renderText(eq(contentEN), same(cellDoc), same(includeDoc), same(
        context))).andReturn(expectedRenderedContent);
    expect(xwiki.getDocument(elementDocRef, getContext())).andReturn(cellDoc).atLeastOnce();
    expect(xwiki.getDocument(includeDocRef, getContext())).andReturn(includeDoc).atLeastOnce();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderDocument(elementDocRef, includeDocRef,
        "en"));
    verifyDefault();
  }

  @Test
  public void testRenderDocument_docref_exp() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = createMockAndAddToDefault(XWikiDocument.class);
    expect(cellDoc.getDocumentReference()).andReturn(elementDocRef).anyTimes();
    expect(xwiki.getDocument(elementDocRef, getContext())).andThrow(
        new XWikiException()).atLeastOnce();
    replayDefault();
    assertEquals("", renderCmd.renderDocument(elementDocRef, "en"));
    verifyDefault();
  }

  @Test
  public void testRenderDocument_docref_docref_exp() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = createMockAndAddToDefault(XWikiDocument.class);
    expect(cellDoc.getDocumentReference()).andReturn(elementDocRef).anyTimes();
    DocumentReference includeDocRef = new DocumentReference(context.getDatabase(), "Includeing",
        "TheIncludingDocumentName");
    expect(xwiki.getDocument(elementDocRef, getContext())).andReturn(cellDoc).atLeastOnce();
    expect(xwiki.getDocument(includeDocRef, getContext())).andThrow(
        new XWikiException()).atLeastOnce();
    replayDefault();
    assertEquals("", renderCmd.renderDocument(elementDocRef, includeDocRef, "en"));
    verifyDefault();
  }

  @Test
  public void testRenderCelementsDocument_vContext_null() throws Exception {
    renderCmd.setDefaultPageTypeReference(null);
    XWikiDocument myDoc = createMockAndAddToDefault(XWikiDocument.class);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    expect(myDoc.getDocumentReference()).andReturn(myDocRef).anyTimes();
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc)))
        .andReturn(Optional.absent()).anyTimes();
    expect(xwiki.getDocument(eq(myDocRef), same(context))).andReturn(myDoc).anyTimes();
    String expectedContent = "expected Content $doc.fullName";
    expect(myDoc.getTranslatedContent(eq("de"), same(context))).andReturn(
        expectedContent).anyTimes();
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc), same(
        context))).andReturn("Topic Content.MyPage does not exist").anyTimes();
    expect(myDoc.newDocument(same(context))).andReturn(new Document(myDoc, context)).anyTimes();
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity",
        "groovy")).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:Content.myPage"), same(context))).andReturn(true).anyTimes();
    context.remove("vcontext");
    replayDefault();
    assertEquals("expecting empty because velocity context is null.", "",
        renderCmd.renderCelementsDocument(myDoc, "de", "view"));
    verifyDefault();
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private ExecutionContext getExecutionContext() {
    return Utils.getComponent(Execution.class).getContext();
  }

  private XWikiContext notSameVcontext(final XWikiContext contextValue) {
    final VelocityContext initVcontext = (VelocityContext) contextValue.get("vcontext");
    reportMatcher(new IArgumentMatcher() {

      @Override
      public void appendTo(StringBuffer buffer) {
        buffer.append("notSameVcontext(" + contextValue + ")");
      }

      @Override
      public boolean matches(Object argument) {
        if (argument instanceof XWikiContext) {
          XWikiContext theContext = (XWikiContext) argument;
          VelocityContext execVcontext = (VelocityContext) getExecutionContext().getProperty(
              "velocityContext");
          if (theContext != null) {
            VelocityContext vContext = (VelocityContext) theContext.get("vcontext");
            return (initVcontext != vContext) && (vContext == execVcontext);
          }
        }
        return false;
      }
    });
    return null;
  }

}
