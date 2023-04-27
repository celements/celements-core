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
import com.celements.model.access.IModelAccessFacade;
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
  private IModelAccessFacade modelAccessMock;
  private Document cellDockApiMock;

  @Before
  public void setUp_RenderCommandTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    velocityContext = (VelocityContext) getExecutionContext().getProperty("velocityContext");
    assertNotNull(velocityContext);
    cellDockApiMock = createMockAndAddToDefault(Document.class);
    velocityContext.put("celldoc", cellDockApiMock);
    context.put("vcontext", velocityContext);
    currentDoc = new XWikiDocument(new DocumentReference(context.getDatabase(), "Content",
        "MyPage"));
    context.setDoc(currentDoc);
    renderCmd = new RenderCommand();
    mockPageTypeService = registerComponentMock(IPageTypeRole.class);
    mockPageTypeResolver = registerComponentMock(IPageTypeResolverRole.class);
    renderingEngineMock = registerComponentMock(XWikiRenderingEngine.class);
    renderCmd.setRenderingEngine(renderingEngineMock);
    mockRightService = registerComponentMock(XWikiRightService.class);
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    expect(xwiki.getRightService()).andReturn(mockRightService).anyTimes();
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
    String cellDocFN = "xwikidb:MyLayout.Cell12";
    XWikiDocument cellDoc = new XWikiDocument(new DocumentReference(
        context.getDatabase(), "MyLayout", "Cell12"));
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.absent());

    replayDefault();
    assertEquals(cellDocFN, renderCmd.getRenderTemplatePath(cellDoc, "view"));
    verifyDefault();
  }

  @Test
  public void testGetRenderTemplatePath_NoViewTemplate_empty() throws Exception {
    String cellDocFN = "xwikidb:MyLayout.Cell12";
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn("").anyTimes();
    expect(ptMock.getName()).andReturn("CelementsCell").anyTimes();
    XWikiDocument cellDoc = new XWikiDocument(new DocumentReference(
        context.getDatabase(), "MyLayout", "Cell12"));
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    replayDefault();
    assertEquals(cellDocFN, renderCmd.getRenderTemplatePath(cellDoc, "view"));
    verifyDefault();
  }

  @Test
  public void testGetRenderTemplatePath_NoViewTemplate_null() throws Exception {
    String cellDocFN = "xwikidb:MyLayout.Cell12";
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(null).anyTimes();
    expect(ptMock.getName()).andReturn("CelementsCell").anyTimes();
    XWikiDocument cellDoc = new XWikiDocument(new DocumentReference(
        context.getDatabase(), "MyLayout", "Cell12"));
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    replayDefault();
    assertEquals(cellDocFN, renderCmd.getRenderTemplatePath(cellDoc, "view"));
    verifyDefault();
  }

  @Test
  public void testGetTranslatedContent() throws Exception {
    XWikiDocument templateDoc = new XWikiDocument(new DocumentReference(
        context.getDatabase(), "MySpace", "myDoc"));
    templateDoc.setDefaultLanguage("de");
    String expectedContent = "do something and velocity macro...\n";
    String transContent = "{pre}\n" + expectedContent + "{/pre}";
    templateDoc.setContent(transContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    replayDefault();
    assertEquals("expected removing pre-tags", "\n" + expectedContent,
        renderCmd.getTranslatedContent(templateDoc, "de"));
    verifyDefault();
  }

  @Test
  public void testGetTranslatedContent_wikiRenderer() throws Exception {
    XWikiDocument templateDoc = new XWikiDocument(new DocumentReference(
        context.getDatabase(), "MySpace", "myDoc"));
    templateDoc.setDefaultLanguage("fr");
    String expectedContent = "do something and velocity macro...\n";
    String transContent = "{pre}\n" + expectedContent + "{/pre}";
    templateDoc.setContent(transContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy",
        "xwiki"));
    replayDefault();
    assertEquals("expected removing pre-tags", "{pre}\n" + expectedContent + "{/pre}",
        renderCmd.getTranslatedContent(templateDoc, "fr"));
    verifyDefault();
  }

  @Test
  public void test_renderCelementsDocument_elemDocRef_renderMode() throws Exception {
    String expectedRenderedContent = "Expected rendered content";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    XWikiDocument myDoc = new XWikiDocument(myDocRef);
    myDoc.setDefaultLanguage("de");
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc)))
        .andReturn(Optional.absent());
    expect(modelAccessMock.getOrCreateDocument(eq(myDocRef))).andReturn(myDoc);
    expect(modelAccessMock.getDocumentOpt(eq(myDocRef))).andReturn(java.util.Optional.of(myDoc));
    String expectedContent = "expected Content $doc.fullName";
    myDoc.setContent(expectedContent);
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc), same(
        context))).andReturn(expectedRenderedContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:Content.myPage"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsDocument(myDocRef, "view"));
    verifyDefault();
  }

  @Test
  public void test_renderCelementsDocument_celldoc_preserved() throws Exception {
    String expectedRenderedContent = "Expected rendered content";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    XWikiDocument myDoc = new XWikiDocument(myDocRef);
    myDoc.setDefaultLanguage("de");
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc)))
        .andReturn(Optional.absent());
    expect(modelAccessMock.getOrCreateDocument(eq(myDocRef))).andReturn(myDoc);
    expect(modelAccessMock.getDocumentOpt(eq(myDocRef))).andReturn(java.util.Optional.of(myDoc));
    String expectedContent = "expected Content $doc.fullName";
    myDoc.setContent(expectedContent);
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc), same(
        context))).andAnswer(() -> {
          assertEquals("expecting celldoc to be set to the rendered cell document.", myDocRef,
              ((Document) velocityContext.get("celldoc")).getDocumentReference());
          return expectedRenderedContent;
        });
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:Content.myPage"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsDocument(myDocRef, "view"));
    verifyDefault();
    assertSame("celldoc must be restored after call", cellDockApiMock,
        velocityContext.get("celldoc"));
  }

  @Test
  public void test_renderCelementsDocument_noCellType_default() throws Exception {
    String expectedRenderedContent = "Expected rendered content";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    XWikiDocument myDoc = new XWikiDocument(myDocRef);
    myDoc.setDefaultLanguage("de");
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc))).andReturn(Optional.absent());
    expect(modelAccessMock.getDocumentOpt(eq(myDocRef))).andReturn(java.util.Optional.of(myDoc));
    String expectedContent = "expected Content $doc.fullName";
    myDoc.setContent(expectedContent);
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc), same(
        context))).andReturn(expectedRenderedContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:Content.myPage"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsDocument(myDoc, "view"));
    verifyDefault();
  }

  @Test
  public void test_renderCelementsDocument_noCellType_setDefault_null() throws Exception {
    renderCmd.setDefaultPageTypeReference(null);
    String expectedRenderedContent = "Expected rendered content";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    XWikiDocument myDoc = new XWikiDocument(myDocRef);
    myDoc.setDefaultLanguage("de");
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc))).andReturn(Optional.absent());
    expect(modelAccessMock.getDocumentOpt(eq(myDocRef))).andReturn(java.util.Optional.of(myDoc));
    String expectedContent = "expected Content $doc.fullName";
    myDoc.setContent(expectedContent);
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc), same(
        context))).andReturn(expectedRenderedContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:Content.myPage"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsDocument(myDoc, "view"));
    verifyDefault();
  }

  @Test
  public void test_renderCelementsDocument_access_denied() throws Exception {
    renderCmd.setDefaultPageTypeReference(null);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    XWikiDocument myDoc = new XWikiDocument(myDocRef);
    myDoc.setDefaultLanguage("de");
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc))).andReturn(Optional.absent())
        .anyTimes();
    expect(modelAccessMock.getOrCreateDocument(eq(myDocRef))).andReturn(myDoc).anyTimes();
    String expectedContent = "expected Content $doc.fullName";
    myDoc.setContent(expectedContent);
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc), same(
        context))).andReturn("Topic Content.MyPage does not exist").anyTimes();
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
  public void test_renderCelementsDocument_preserveVelocityContext() throws Exception {
    renderCmd.setDefaultPageTypeReference(null);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    XWikiDocument myDoc = new XWikiDocument(myDocRef);
    myDoc.setDefaultLanguage("de");
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc))).andReturn(Optional.absent())
        .anyTimes();
    expect(modelAccessMock.getOrCreateDocument(eq(myDocRef))).andReturn(myDoc);
    expect(modelAccessMock.getDocumentOpt(eq(myDocRef))).andReturn(java.util.Optional.of(myDoc));
    String expectedContent = "expected Content $doc.fullName";
    myDoc.setContent(expectedContent);
    String expectedRenderedContent = "expected rendered content of Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc),
        notSameVcontext(context))).andReturn(expectedRenderedContent);
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
    assertSame("celldoc must be restored after call", cellDockApiMock,
        velocityContext.get("celldoc"));
  }

  @Test
  @Deprecated
  public void test_renderCelementsCell_noCellType_setDefault_RichText_deprecated()
      throws Exception {
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
    expect(modelAccessMock.getOrCreateDocument(eq(elementDocRef))).andReturn(cellDoc);
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web", "Templates",
        "CellTypeView");
    expect(ptMock.getRenderTemplateForRenderMode(eq("view")))
        .andReturn("celements2web:Templates.CellTypeView");
    XWikiDocument templDoc = new XWikiDocument(renderTemplateDocRef);
    templDoc.setDefaultLanguage("de");
    expect(modelAccessMock.getDocumentOpt(eq(renderTemplateDocRef)))
        .andReturn(java.util.Optional.of(templDoc));
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    templDoc.setContent(expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(elementFullName));
    verifyDefault();
  }

  @Test
  public void test_renderCelementsCell_noCellType_setDefault_RichText() throws Exception {
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
    expect(modelAccessMock.getOrCreateDocument(eq(elementDocRef))).andReturn(cellDoc);
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web", "Templates",
        "CellTypeView");
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        "celements2web:Templates.CellTypeView");
    XWikiDocument templDoc = new XWikiDocument(renderTemplateDocRef);
    templDoc.setDefaultLanguage("de");
    expect(modelAccessMock.getDocumentOpt(eq(renderTemplateDocRef)))
        .andReturn(java.util.Optional.of(templDoc));
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    templDoc.setContent(expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(elementDocRef));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void test_renderCelementsCell_databaseDoc_deprecated() throws XWikiException {
    String elementFullName = "MyLayout.Cell15";
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(modelAccessMock.getOrCreateDocument(eq(elementDocRef))).andReturn(cellDoc);
    String renderTemplateFN = "celements2web:Templates.CellTypeView";
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web", "Templates",
        "CellTypeView");
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        renderTemplateFN).anyTimes();
    XWikiDocument templDoc = new XWikiDocument(renderTemplateDocRef);
    templDoc.setDefaultLanguage("de");
    expect(modelAccessMock.getDocumentOpt(eq(renderTemplateDocRef)))
        .andReturn(java.util.Optional.of(templDoc));
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    templDoc.setContent(expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(elementFullName));
    verifyDefault();
  }

  @Test
  public void test_renderCelementsCell_databaseDoc() throws XWikiException {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(modelAccessMock.getOrCreateDocument(eq(elementDocRef))).andReturn(cellDoc);
    String renderTemplateFN = "celements2web:Templates.CellTypeView";
    DocumentReference renderTemplateDocRef = new DocumentReference("celements2web", "Templates",
        "CellTypeView");
    expect(ptMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        renderTemplateFN).anyTimes();
    XWikiDocument templDoc = new XWikiDocument(renderTemplateDocRef);
    templDoc.setDefaultLanguage("de");
    expect(modelAccessMock.getDocumentOpt(eq(renderTemplateDocRef)))
        .andReturn(java.util.Optional.of(templDoc));
    expect(ptMock.getName()).andReturn("CelementsContentPageCell").anyTimes();
    String expectedContent = "Expected Template Content $doc.fullName";
    templDoc.setContent(expectedContent);
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(templDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "xwikidb:MyLayout.Cell15"), same(context))).andReturn(true).once();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderCelementsCell(elementDocRef));
    verifyDefault();
  }

  /**
   * if the sdoc passed to renderText is null a NPE will occur.
   *
   * @throws Exception
   */
  @Test
  @Deprecated
  public void test_renderCelementsCell_templateDoc_deprecated() throws Exception {
    String elementFullName = "MyLayout.Cell15";
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(modelAccessMock.getOrCreateDocument(eq(elementDocRef))).andReturn(cellDoc);
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
    verifyDefault();
  }

  /**
   * if the sdoc passed to renderText is null a NPE will occur.
   *
   * @throws Exception
   */
  @Test
  public void test_renderCelementsCell_templateDoc() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(modelAccessMock.getOrCreateDocument(eq(elementDocRef))).andReturn(cellDoc);
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
    verifyDefault();
  }

  /**
   * if the sdoc passed to renderText is null a NPE will occur.
   *
   * @throws Exception
   */
  @Test
  public void test_renderCelementsCell_templateDoc_lang() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(modelAccessMock.getOrCreateDocument(eq(elementDocRef))).andReturn(cellDoc);
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
    verifyDefault();
  }

  @Test
  @Deprecated
  public void test_renderCelementsCell_templateDoc_ioException_deprecated() throws Exception {
    String elementFullName = "MyLayout.Cell15";
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(modelAccessMock.getOrCreateDocument(eq(elementDocRef))).andReturn(cellDoc);
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
    verifyDefault();
  }

  @Test
  public void test_renderCelementsCell_templateDoc_ioException() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    IPageTypeConfig ptMock = createMockAndAddToDefault(IPageTypeConfig.class);
    PageTypeReference ptRefMock = createMockAndAddToDefault(PageTypeReference.class);
    expect(mockPageTypeResolver.resolvePageTypeReference(same(cellDoc)))
        .andReturn(Optional.of(ptRefMock));
    expect(mockPageTypeService.getPageTypeConfigForPageTypeRef(same(ptRefMock))).andReturn(ptMock);
    expect(modelAccessMock.getOrCreateDocument(eq(elementDocRef))).andReturn(cellDoc);
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
  public void test_renderTemplatePath_null_lang() throws Exception {
    String renderTemplatePath = ":Templates.CellTypeView";
    String templatePath = "celTemplates/CellTypeView.vm";
    String expectedContent = "Expected Template Content Content.MyPage";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath))).andReturn(
        expectedContent).once();
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderTemplatePath(renderTemplatePath, "", ""));
    verifyDefault();
  }

  @Test
  public void test_renderTemplatePath_lang() throws Exception {
    String renderTemplatePath = ":Templates.CellTypeView";
    String templatePath_lang = "celTemplates/CellTypeView_de.vm";
    String expectedContent = "Expected Template Content Content.MyPage";
    expect(xwiki.getResourceContent(eq("/templates/" + templatePath_lang))).andReturn(
        expectedContent).once();
    String expectedRenderedContent = "Expected Template Content Content.MyPage";
    expect(renderingEngineMock.renderText(eq(expectedContent), same(currentDoc), same(currentDoc),
        same(context))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderTemplatePath(
        renderTemplatePath, "de", ""));
    verifyDefault();
  }

  @Test
  public void test_renderTemplatePath_langNotFound_deflang() throws Exception {
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
  public void test_renderTemplatePath_langNotFound_deflangNotFound() throws Exception {
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
  public void test_renderTemplatePath_langNotFound_deflangNotFound_defaultNotFound()
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
  public void test_renderTemplatePath_deflang_equals_lang_notFound() throws Exception {
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
  public void test_renderDocument() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    cellDoc.setDefaultLanguage("en");
    String expectedRenderedContent = "expected Content";
    String contentEN = "english script $test";
    cellDoc.setContent(contentEN);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(renderingEngineMock.renderText(eq(contentEN), same(cellDoc), same(currentDoc), same(
        context))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderDocument(cellDoc, null, "en"));
    verifyDefault();
  }

  @Test
  public void test_renderDocument_includingDoc() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    cellDoc.setDefaultLanguage("en");
    String expectedRenderedContent = "expected Content";
    String contentEN = "english script $test";
    cellDoc.setContent(contentEN);
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
  public void test_renderDocument_docref() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    cellDoc.setDefaultLanguage("en");
    String expectedRenderedContent = "expected Content";
    String contentEN = "english script $test";
    cellDoc.setContent(contentEN);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    expect(renderingEngineMock.renderText(eq(contentEN), same(cellDoc), same(currentDoc), same(
        context))).andReturn(expectedRenderedContent);
    expect(modelAccessMock.getOrCreateDocument(elementDocRef)).andReturn(cellDoc).atLeastOnce();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderDocument(elementDocRef, "en"));
    verifyDefault();
  }

  @Test
  public void test_renderDocument_docref_docref() throws Exception {
    DocumentReference elementDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "Cell15");
    XWikiDocument cellDoc = new XWikiDocument(elementDocRef);
    cellDoc.setDefaultLanguage("en");
    String expectedRenderedContent = "expected Content";
    String contentEN = "english script $test";
    cellDoc.setContent(contentEN);
    expect(renderingEngineMock.getRendererNames()).andReturn(Arrays.asList("velocity", "groovy"));
    DocumentReference includeDocRef = new DocumentReference(context.getDatabase(), "Includeing",
        "TheIncludingDocumentName");
    XWikiDocument includeDoc = new XWikiDocument(includeDocRef);
    expect(renderingEngineMock.renderText(eq(contentEN), same(cellDoc), same(includeDoc), same(
        context))).andReturn(expectedRenderedContent);
    expect(modelAccessMock.getOrCreateDocument(elementDocRef)).andReturn(cellDoc).atLeastOnce();
    expect(modelAccessMock.getOrCreateDocument(includeDocRef)).andReturn(includeDoc).atLeastOnce();
    replayDefault();
    assertEquals(expectedRenderedContent, renderCmd.renderDocument(elementDocRef, includeDocRef,
        "en"));
    verifyDefault();
  }

  @Test
  public void test_renderCelementsDocument_vContext_null() throws Exception {
    renderCmd.setDefaultPageTypeReference(null);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Content", "myPage");
    XWikiDocument myDoc = new XWikiDocument(myDocRef);
    myDoc.setDefaultLanguage("de");
    expect(mockPageTypeResolver.resolvePageTypeReference(same(myDoc)))
        .andReturn(Optional.absent()).anyTimes();
    expect(modelAccessMock.getOrCreateDocument(eq(myDocRef))).andReturn(myDoc).anyTimes();
    String expectedContent = "expected Content $doc.fullName";
    myDoc.setContent(expectedContent);
    expect(renderingEngineMock.renderText(eq(expectedContent), same(myDoc), same(currentDoc), same(
        context))).andReturn("Topic Content.MyPage does not exist").anyTimes();
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
