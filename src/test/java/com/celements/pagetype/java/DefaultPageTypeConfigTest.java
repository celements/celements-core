package com.celements.pagetype.java;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class DefaultPageTypeConfigTest extends AbstractBridgedComponentTestCase {

  private IJavaPageTypeRole pageTypeImplMock;
  private DefaultPageTypeConfig testPageType;
  private IWebUtilsService webUtilsService;
  private ComponentManager componentManager;
  private ComponentDescriptor<IWebUtilsService> webUtilsManagDesc;
  private IWebUtilsService webUtilsMock;
  private XWikiContext context;

  @Before
  public void setUp_DefaultPageTypeConfigTest() throws Exception {
    context = getContext();
    componentManager = Utils.getComponentManager();
    pageTypeImplMock = createMockAndAddToDefault(IJavaPageTypeRole.class);
    testPageType = new DefaultPageTypeConfig(pageTypeImplMock);
    webUtilsService = getComponentManager().lookup(IWebUtilsService.class);
    componentManager.release(webUtilsService);
    webUtilsManagDesc = componentManager.getComponentDescriptor(IWebUtilsService.class,
        "default");
    webUtilsMock = createMockAndAddToDefault(IWebUtilsService.class);
    componentManager.registerComponent(webUtilsManagDesc, webUtilsMock);
  }

  @After
  public void tearDown_DefaultPageTypeConfigTest() throws Exception {
    componentManager.release(webUtilsMock);
    componentManager.registerComponent(webUtilsManagDesc, webUtilsService);
  }

  @Test
  public void testGetName() {
    String expectedTestPageName = "TestPageName";
    expect(pageTypeImplMock.getName()).andReturn(expectedTestPageName).anyTimes();
    replayDefault();
    assertEquals(expectedTestPageName, testPageType.getName());
    verifyDefault();
  }

  @Test
  public void testGetCategories() {
    List<String> expectedCategories = Arrays.asList("", "pageType");
    expect(pageTypeImplMock.getCategories()).andReturn(expectedCategories).anyTimes();
    replayDefault();
    assertEquals(expectedCategories, testPageType.getCategories());
    verifyDefault();
  }

  @Test
  public void testHasPageTitle_false() {
    expect(pageTypeImplMock.hasPageTitle()).andReturn(false).anyTimes();
    replayDefault();
    assertFalse(testPageType.hasPageTitle());
    verifyDefault();
  }

  @Test
  public void testHasPageTitle_true() {
    expect(pageTypeImplMock.hasPageTitle()).andReturn(true).anyTimes();
    replayDefault();
    assertTrue(testPageType.hasPageTitle());
    verifyDefault();
  }

  @Test
  public void testDisplayInFrameLayout_false() {
    expect(pageTypeImplMock.displayInFrameLayout()).andReturn(false).anyTimes();
    replayDefault();
    assertFalse(testPageType.displayInFrameLayout());
    verifyDefault();
  }

  @Test
  public void testDisplayInFrameLayout_true() {
    expect(pageTypeImplMock.displayInFrameLayout()).andReturn(true).anyTimes();
    replayDefault();
    assertTrue(testPageType.displayInFrameLayout());
    verifyDefault();
  }

  @Test
  public void testGetRenderTemplateForRenderMode_view() {
    String templateName = "ImageStatsView";
    String expectedViewTemplates = "Templates." + templateName;
    expect(pageTypeImplMock.getRenderTemplateForRenderMode(eq("view"))).andReturn(
        templateName).anyTimes();
    DocumentReference localTemplateDocRef = new DocumentReference(context.getDatabase(),
        "Templates", templateName);
    expect(webUtilsMock.getInheritedTemplatedPath(eq(localTemplateDocRef))).andReturn(
        expectedViewTemplates);
    replayDefault();
    assertEquals(expectedViewTemplates, testPageType.getRenderTemplateForRenderMode(
        "view"));
    verifyDefault();
  }

  @Test
  public void testGetRenderTemplateForRenderMode_edit() {
    String templateName = "ImageStatsEdit";
    String expectedEditTemplates = "Templates." + templateName;
    expect(pageTypeImplMock.getRenderTemplateForRenderMode(eq("edit"))).andReturn(
        templateName).anyTimes();
    DocumentReference localTemplateDocRef = new DocumentReference(context.getDatabase(),
        "Templates", templateName);
    expect(webUtilsMock.getInheritedTemplatedPath(eq(localTemplateDocRef))).andReturn(
        expectedEditTemplates);
    replayDefault();
    assertEquals(expectedEditTemplates, testPageType.getRenderTemplateForRenderMode(
        "edit"));
    verifyDefault();
  }

  @Test
  public void testIsVisible_false() {
    expect(pageTypeImplMock.isVisible()).andReturn(false).anyTimes();
    replayDefault();
    assertFalse(testPageType.isVisible());
    verifyDefault();
  }

  @Test
  public void testIsVisible_true() {
    expect(pageTypeImplMock.isVisible()).andReturn(true).anyTimes();
    replayDefault();
    assertTrue(testPageType.isVisible());
    verifyDefault();
  }

}
