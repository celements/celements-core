package com.celements.navigation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.web.Utils;

public class NavigationConfigTest extends AbstractComponentTest {

  private String configName;
  private Integer fromHierarchyLevel;
  private Integer toHierarchyLevel;
  private Integer showInactiveToLevel;
  private String menuPart;
  private String dataType;
  private SpaceReference nodeSpaceRef;
  private Integer itemsPerPage;
  private String presentationTypeHint;
  private String cmCssClass;
  private String layoutType;

  @Before
  public void setUp_NavigationConfigTest() throws Exception {
    configName = null;
    fromHierarchyLevel = null;
    toHierarchyLevel = null;
    showInactiveToLevel = null;
    menuPart = null;
    dataType = null;
    nodeSpaceRef = null;
    itemsPerPage = null;
    presentationTypeHint = null;
    cmCssClass = null;
    layoutType = null;
  }

  @Test
  public void testNavigationConfig() throws Exception {
    replayDefault();
    NavigationConfig navConfig = new NavigationConfig();
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
    assertFalse(navConfig.isEnabled());
    assertEquals("toHieararchyLevel default must equal DEFAULT_MAX_LEVEL",
        NavigationConfig.DEFAULT_MAX_LEVEL, navConfig.getToHierarchyLevel());
    assertEquals("fromHieararchyLevel default must equal DEFAULT_MIN_LEVEL",
        NavigationConfig.DEFAULT_MIN_LEVEL, navConfig.getFromHierarchyLevel());
    assertTrue("DEFAULT_MAX_LEVEL must be greater zero", NavigationConfig.DEFAULT_MAX_LEVEL > 0);
    assertTrue("DEFAULT_MIN_LEVEL must be greater zero", NavigationConfig.DEFAULT_MIN_LEVEL > 0);
    assertTrue("DEFAULT_MAX_LEVEL must be greater DEFAULT_MIN_LEVEL",
        NavigationConfig.DEFAULT_MAX_LEVEL > NavigationConfig.DEFAULT_MIN_LEVEL);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    verifyDefault();
  }

  @Test
  public void testGetConfigName_default() {
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertEquals("", navConfig.getConfigName());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testGetConfigName_empty() {
    String configName = "";
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertEquals("", navConfig.getConfigName());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testGetConfigName() {
    String configName = "mainMenu";
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertEquals(configName, navConfig.getConfigName());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testIsEnabled() {
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertTrue(navConfig.isEnabled());
    // defaults for other fields must remain
    assertEquals("", navConfig.getConfigName());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testGetFromHierarchyLevel() {
    fromHierarchyLevel = 10;
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertEquals("fromHieararchyLevel must equals to parameter", fromHierarchyLevel,
        (Integer) navConfig.getFromHierarchyLevel());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testGetToHierarchyLevel() {
    toHierarchyLevel = 11;
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertEquals("toHieararchyLevel must equals to parameter", toHierarchyLevel,
        (Integer) navConfig.getToHierarchyLevel());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testGetShowInactiveToLevel() {
    showInactiveToLevel = 10;
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertEquals("showInactiveToLevel must equals to parameter", showInactiveToLevel,
        (Integer) navConfig.getShowInactiveToLevel());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testGetMenuPart() {
    menuPart = "mainMenu";
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertEquals("menuPart must equals to parameter", menuPart, navConfig.getMenuPart());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testGetNodeSpaceRef() {
    String spaceName = "MySpace";
    nodeSpaceRef = new SpaceReference(spaceName, new WikiReference(getContext().getDatabase()));
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    SpaceReference firstReturnNodeSpace = navConfig.getNodeSpaceRef();
    assertEquals("expected equals nodeSpaceRef", nodeSpaceRef, firstReturnNodeSpace);
    assertNotSame("expected defensive copy of nodeSpaceRef", nodeSpaceRef, firstReturnNodeSpace);
    assertNotSame("expected defensive copy of nodeSpaceRef", firstReturnNodeSpace,
        navConfig.getNodeSpaceRef());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getCssClass());
    assertEquals("", navConfig.getMenuPart());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testGetDataType() {
    String dataType = "testType";
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertEquals(dataType, navConfig.getDataType());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testGetLayoutType() {
    String layoutType = "testLayoutType";
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertEquals(layoutType, navConfig.getLayoutType());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(1, navConfig.getNrOfItemsPerPage());
  }

  @Test
  public void testGetNrOfItemsPerPage() {
    itemsPerPage = 10;
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertEquals("itemsPerPage must equals to parameter", itemsPerPage,
        (Integer) navConfig.getNrOfItemsPerPage());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testGetPresentationType() throws Exception {
    presentationTypeHint = "myTestPresentationType";
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(presentationTypeHint))).andReturn(
        componentInstance);
    replayDefault();
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertSame(componentInstance, navConfig.getPresentationType());
    verifyDefault();
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
  }

  @Test
  public void testGetCssClass() {
    cmCssClass = "cm_cssTestClass";
    NavigationConfig navConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    assertEquals("cmCssClass must equals to parameter", cmCssClass, navConfig.getCssClass());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
  }

}
