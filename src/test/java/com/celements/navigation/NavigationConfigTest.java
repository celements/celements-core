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
    NavigationConfig navConfig = new NavigationConfig();
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
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
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
  }

  @Test
  public void testGetPresentationType() throws Exception {
    presentationTypeHint = "myTestPresentationType";
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint))).andReturn(componentInstance);
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
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
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
    assertNull(navConfig.getNodeSpaceRef());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class), navConfig.getPresentationType());
  }

  @Test
  public void testOverlayValues_configName() throws Exception {
    configName = "configName1";
    NavigationConfig navConfig1 = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    String configName2 = "configName2";
    Integer fromHierarchyLevel2 = 22;
    Integer toHierarchyLevel2 = 60;
    Integer showInactiveToLevel2 = 55;
    String menuPart2 = "menuPart2";
    String dataType2 = "dataType2";
    SpaceReference nodeSpaceRef2 = new SpaceReference("NavConfigSpace2", new WikiReference(
        getContext().getDatabase()));
    String layoutType2 = "navConfig2LayoutType";
    Integer itemsPerPage2 = 50;
    String presentationTypeHint2 = "navConfigPresTypeHint2";
    String cmCssClass2 = "cm_cssTestClass2";
    NavigationConfig navConfig2 = new NavigationConfig(configName2, fromHierarchyLevel2,
        toHierarchyLevel2, showInactiveToLevel2, menuPart2, dataType2, nodeSpaceRef2, layoutType2,
        itemsPerPage2, presentationTypeHint2, cmCssClass2);
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint2))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertEquals("configName must equal to first parameter", configName, navConfig.getConfigName());
    // navConfig2 values for other fields must remain
    assertEquals("fromHieararchyLevel must equal to second parameter", fromHierarchyLevel2,
        (Integer) navConfig.getFromHierarchyLevel());
    assertEquals("toHieararchyLevel must equal to second parameter", toHierarchyLevel2,
        (Integer) navConfig.getToHierarchyLevel());
    assertEquals("showInactiveToLevel must equal to second parameter", showInactiveToLevel2,
        (Integer) navConfig.getShowInactiveToLevel());
    assertEquals("menuPart must equal to second parameter", menuPart2, navConfig.getMenuPart());
    assertEquals("nodeSpaceRef must equal to second parameter", nodeSpaceRef2,
        navConfig.getNodeSpaceRef());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_fromHierarchyLevel() throws Exception {
    fromHierarchyLevel = 15;
    NavigationConfig navConfig1 = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    String configName2 = "configName2";
    Integer fromHierarchyLevel2 = 22;
    Integer toHierarchyLevel2 = 60;
    Integer showInactiveToLevel2 = 55;
    String menuPart2 = "menuPart2";
    String dataType2 = "dataType2";
    SpaceReference nodeSpaceRef2 = new SpaceReference("NavConfigSpace2", new WikiReference(
        getContext().getDatabase()));
    String layoutType2 = "navConfig2LayoutType";
    Integer itemsPerPage2 = 50;
    String presentationTypeHint2 = "navConfigPresTypeHint2";
    String cmCssClass2 = "cm_cssTestClass2";
    NavigationConfig navConfig2 = new NavigationConfig(configName2, fromHierarchyLevel2,
        toHierarchyLevel2, showInactiveToLevel2, menuPart2, dataType2, nodeSpaceRef2, layoutType2,
        itemsPerPage2, presentationTypeHint2, cmCssClass2);
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint2))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertEquals("fromHieararchyLevel must equal to first parameter", fromHierarchyLevel,
        (Integer) navConfig.getFromHierarchyLevel());
    // navConfig2 values for other fields must remain
    assertEquals("configName must equal to second parameter", configName2,
        navConfig.getConfigName());
    assertEquals("toHieararchyLevel must equal to second parameter", toHierarchyLevel2,
        (Integer) navConfig.getToHierarchyLevel());
    assertEquals("showInactiveToLevel must equal to second parameter", showInactiveToLevel2,
        (Integer) navConfig.getShowInactiveToLevel());
    assertEquals("menuPart must equal to second parameter", menuPart2, navConfig.getMenuPart());
    assertEquals("nodeSpaceRef must equal to second parameter", nodeSpaceRef2,
        navConfig.getNodeSpaceRef());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_toHierarchyLevel() throws Exception {
    toHierarchyLevel = 70;
    NavigationConfig navConfig1 = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    String configName2 = "configName2";
    Integer fromHierarchyLevel2 = 22;
    Integer toHierarchyLevel2 = 60;
    Integer showInactiveToLevel2 = 55;
    String menuPart2 = "menuPart2";
    String dataType2 = "dataType2";
    SpaceReference nodeSpaceRef2 = new SpaceReference("NavConfigSpace2", new WikiReference(
        getContext().getDatabase()));
    String layoutType2 = "navConfig2LayoutType";
    Integer itemsPerPage2 = 50;
    String presentationTypeHint2 = "navConfigPresTypeHint2";
    String cmCssClass2 = "cm_cssTestClass2";
    NavigationConfig navConfig2 = new NavigationConfig(configName2, fromHierarchyLevel2,
        toHierarchyLevel2, showInactiveToLevel2, menuPart2, dataType2, nodeSpaceRef2, layoutType2,
        itemsPerPage2, presentationTypeHint2, cmCssClass2);
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint2))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertEquals("toHieararchyLevel must equal to first parameter", toHierarchyLevel,
        (Integer) navConfig.getToHierarchyLevel());
    // navConfig2 values for other fields must remain
    assertEquals("configName must equal to second parameter", configName2,
        navConfig.getConfigName());
    assertEquals("fromHieararchyLevel must equal to second parameter", fromHierarchyLevel2,
        (Integer) navConfig.getFromHierarchyLevel());
    assertEquals("showInactiveToLevel must equal to second parameter", showInactiveToLevel2,
        (Integer) navConfig.getShowInactiveToLevel());
    assertEquals("menuPart must equal to second parameter", menuPart2, navConfig.getMenuPart());
    assertEquals("nodeSpaceRef must equal to second parameter", nodeSpaceRef2,
        navConfig.getNodeSpaceRef());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_showInactiveToLevel() throws Exception {
    showInactiveToLevel = 30;
    NavigationConfig navConfig1 = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    String configName2 = "configName2";
    Integer fromHierarchyLevel2 = 22;
    Integer toHierarchyLevel2 = 60;
    Integer showInactiveToLevel2 = 55;
    String menuPart2 = "menuPart2";
    String dataType2 = "dataType2";
    SpaceReference nodeSpaceRef2 = new SpaceReference("NavConfigSpace2", new WikiReference(
        getContext().getDatabase()));
    String layoutType2 = "navConfig2LayoutType";
    Integer itemsPerPage2 = 50;
    String presentationTypeHint2 = "navConfigPresTypeHint2";
    String cmCssClass2 = "cm_cssTestClass2";
    NavigationConfig navConfig2 = new NavigationConfig(configName2, fromHierarchyLevel2,
        toHierarchyLevel2, showInactiveToLevel2, menuPart2, dataType2, nodeSpaceRef2, layoutType2,
        itemsPerPage2, presentationTypeHint2, cmCssClass2);
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint2))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertEquals("showInactiveToLevel must equal to first parameter", showInactiveToLevel,
        (Integer) navConfig.getShowInactiveToLevel());
    // navConfig2 values for other fields must remain
    assertEquals("configName must equal to second parameter", configName2,
        navConfig.getConfigName());
    assertEquals("fromHieararchyLevel must equal to second parameter", fromHierarchyLevel2,
        (Integer) navConfig.getFromHierarchyLevel());
    assertEquals("toHieararchyLevel must equal to second parameter", toHierarchyLevel2,
        (Integer) navConfig.getToHierarchyLevel());
    assertEquals("menuPart must equal to second parameter", menuPart2, navConfig.getMenuPart());
    assertEquals("nodeSpaceRef must equal to second parameter", nodeSpaceRef2,
        navConfig.getNodeSpaceRef());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_menuPart() throws Exception {
    menuPart = "menuPart1";
    NavigationConfig navConfig1 = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    String configName2 = "configName2";
    Integer fromHierarchyLevel2 = 22;
    Integer toHierarchyLevel2 = 60;
    Integer showInactiveToLevel2 = 55;
    String menuPart2 = "menuPart2";
    String dataType2 = "dataType2";
    SpaceReference nodeSpaceRef2 = new SpaceReference("NavConfigSpace2", new WikiReference(
        getContext().getDatabase()));
    String layoutType2 = "navConfig2LayoutType";
    Integer itemsPerPage2 = 50;
    String presentationTypeHint2 = "navConfigPresTypeHint2";
    String cmCssClass2 = "cm_cssTestClass2";
    NavigationConfig navConfig2 = new NavigationConfig(configName2, fromHierarchyLevel2,
        toHierarchyLevel2, showInactiveToLevel2, menuPart2, dataType2, nodeSpaceRef2, layoutType2,
        itemsPerPage2, presentationTypeHint2, cmCssClass2);
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint2))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertEquals("menuPart must equal to first parameter", menuPart, navConfig.getMenuPart());
    // navConfig2 values for other fields must remain
    assertEquals("configName must equal to second parameter", configName2,
        navConfig.getConfigName());
    assertEquals("fromHieararchyLevel must equal to second parameter", fromHierarchyLevel2,
        (Integer) navConfig.getFromHierarchyLevel());
    assertEquals("toHieararchyLevel must equal to second parameter", toHierarchyLevel2,
        (Integer) navConfig.getToHierarchyLevel());
    assertEquals("showInactiveToLevel must equal to second parameter", showInactiveToLevel2,
        (Integer) navConfig.getShowInactiveToLevel());
    assertEquals("nodeSpaceRef must equal to second parameter", nodeSpaceRef2,
        navConfig.getNodeSpaceRef());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_dataType() throws Exception {
    dataType = "dataType1";
    NavigationConfig navConfig1 = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    String configName2 = "configName2";
    Integer fromHierarchyLevel2 = 22;
    Integer toHierarchyLevel2 = 60;
    Integer showInactiveToLevel2 = 55;
    String menuPart2 = "menuPart2";
    String dataType2 = "dataType2";
    SpaceReference nodeSpaceRef2 = new SpaceReference("NavConfigSpace2", new WikiReference(
        getContext().getDatabase()));
    String layoutType2 = "navConfig2LayoutType";
    Integer itemsPerPage2 = 50;
    String presentationTypeHint2 = "navConfigPresTypeHint2";
    String cmCssClass2 = "cm_cssTestClass2";
    NavigationConfig navConfig2 = new NavigationConfig(configName2, fromHierarchyLevel2,
        toHierarchyLevel2, showInactiveToLevel2, menuPart2, dataType2, nodeSpaceRef2, layoutType2,
        itemsPerPage2, presentationTypeHint2, cmCssClass2);
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint2))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertEquals("dataType must equal to first parameter", dataType, navConfig.getDataType());
    // navConfig2 values for other fields must remain
    assertEquals("configName must equal to second parameter", configName2,
        navConfig.getConfigName());
    assertEquals("fromHieararchyLevel must equal to second parameter", fromHierarchyLevel2,
        (Integer) navConfig.getFromHierarchyLevel());
    assertEquals("toHieararchyLevel must equal to second parameter", toHierarchyLevel2,
        (Integer) navConfig.getToHierarchyLevel());
    assertEquals("showInactiveToLevel must equal to second parameter", showInactiveToLevel2,
        (Integer) navConfig.getShowInactiveToLevel());
    assertEquals("menuPart must equal to second parameter", menuPart2, navConfig.getMenuPart());
    assertEquals("nodeSpaceRef must equal to second parameter", nodeSpaceRef2,
        navConfig.getNodeSpaceRef());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_nodeSpaceRef() throws Exception {
    nodeSpaceRef = new SpaceReference("NavConfigSpace1", new WikiReference(
        getContext().getDatabase()));
    NavigationConfig navConfig1 = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    String configName2 = "configName2";
    Integer fromHierarchyLevel2 = 22;
    Integer toHierarchyLevel2 = 60;
    Integer showInactiveToLevel2 = 55;
    String menuPart2 = "menuPart2";
    String dataType2 = "dataType2";
    SpaceReference nodeSpaceRef2 = new SpaceReference("NavConfigSpace2", new WikiReference(
        getContext().getDatabase()));
    String layoutType2 = "navConfig2LayoutType";
    Integer itemsPerPage2 = 50;
    String presentationTypeHint2 = "navConfigPresTypeHint2";
    String cmCssClass2 = "cm_cssTestClass2";
    NavigationConfig navConfig2 = new NavigationConfig(configName2, fromHierarchyLevel2,
        toHierarchyLevel2, showInactiveToLevel2, menuPart2, dataType2, nodeSpaceRef2, layoutType2,
        itemsPerPage2, presentationTypeHint2, cmCssClass2);
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint2))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertEquals("nodeSpaceRef must equal to first parameter", nodeSpaceRef,
        navConfig.getNodeSpaceRef());
    // navConfig2 values for other fields must remain
    assertEquals("configName must equal to second parameter", configName2,
        navConfig.getConfigName());
    assertEquals("fromHieararchyLevel must equal to second parameter", fromHierarchyLevel2,
        (Integer) navConfig.getFromHierarchyLevel());
    assertEquals("toHieararchyLevel must equal to second parameter", toHierarchyLevel2,
        (Integer) navConfig.getToHierarchyLevel());
    assertEquals("showInactiveToLevel must equal to second parameter", showInactiveToLevel2,
        (Integer) navConfig.getShowInactiveToLevel());
    assertEquals("menuPart must equal to second parameter", menuPart2, navConfig.getMenuPart());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_layoutType() throws Exception {
    layoutType = "layoutType1";
    NavigationConfig navConfig1 = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    String configName2 = "configName2";
    Integer fromHierarchyLevel2 = 22;
    Integer toHierarchyLevel2 = 60;
    Integer showInactiveToLevel2 = 55;
    String menuPart2 = "menuPart2";
    String dataType2 = "dataType2";
    SpaceReference nodeSpaceRef2 = new SpaceReference("NavConfigSpace2", new WikiReference(
        getContext().getDatabase()));
    String layoutType2 = "navConfig2LayoutType";
    Integer itemsPerPage2 = 50;
    String presentationTypeHint2 = "navConfigPresTypeHint2";
    String cmCssClass2 = "cm_cssTestClass2";
    NavigationConfig navConfig2 = new NavigationConfig(configName2, fromHierarchyLevel2,
        toHierarchyLevel2, showInactiveToLevel2, menuPart2, dataType2, nodeSpaceRef2, layoutType2,
        itemsPerPage2, presentationTypeHint2, cmCssClass2);
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint2))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertEquals("layoutType must equal to first parameter", layoutType, navConfig.getLayoutType());
    // navConfig2 values for other fields must remain
    assertEquals("configName must equal to second parameter", configName2,
        navConfig.getConfigName());
    assertEquals("fromHieararchyLevel must equal to second parameter", fromHierarchyLevel2,
        (Integer) navConfig.getFromHierarchyLevel());
    assertEquals("toHieararchyLevel must equal to second parameter", toHierarchyLevel2,
        (Integer) navConfig.getToHierarchyLevel());
    assertEquals("showInactiveToLevel must equal to second parameter", showInactiveToLevel2,
        (Integer) navConfig.getShowInactiveToLevel());
    assertEquals("menuPart must equal to second parameter", menuPart2, navConfig.getMenuPart());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("nodeSpaceRef must equal to second parameter", nodeSpaceRef2,
        navConfig.getNodeSpaceRef());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_itemsPerPage() throws Exception {
    itemsPerPage = 20;
    NavigationConfig navConfig1 = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    String configName2 = "configName2";
    Integer fromHierarchyLevel2 = 22;
    Integer toHierarchyLevel2 = 60;
    Integer showInactiveToLevel2 = 55;
    String menuPart2 = "menuPart2";
    String dataType2 = "dataType2";
    SpaceReference nodeSpaceRef2 = new SpaceReference("NavConfigSpace2", new WikiReference(
        getContext().getDatabase()));
    String layoutType2 = "navConfig2LayoutType";
    Integer itemsPerPage2 = 50;
    String presentationTypeHint2 = "navConfigPresTypeHint2";
    String cmCssClass2 = "cm_cssTestClass2";
    NavigationConfig navConfig2 = new NavigationConfig(configName2, fromHierarchyLevel2,
        toHierarchyLevel2, showInactiveToLevel2, menuPart2, dataType2, nodeSpaceRef2, layoutType2,
        itemsPerPage2, presentationTypeHint2, cmCssClass2);
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint2))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertEquals("itemsPerPage must equal to first parameter", itemsPerPage,
        (Integer) navConfig.getNrOfItemsPerPage());
    // navConfig2 values for other fields must remain
    assertEquals("configName must equal to second parameter", configName2,
        navConfig.getConfigName());
    assertEquals("fromHieararchyLevel must equal to second parameter", fromHierarchyLevel2,
        (Integer) navConfig.getFromHierarchyLevel());
    assertEquals("toHieararchyLevel must equal to second parameter", toHierarchyLevel2,
        (Integer) navConfig.getToHierarchyLevel());
    assertEquals("showInactiveToLevel must equal to second parameter", showInactiveToLevel2,
        (Integer) navConfig.getShowInactiveToLevel());
    assertEquals("menuPart must equal to second parameter", menuPart2, navConfig.getMenuPart());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("nodeSpaceRef must equal to second parameter", nodeSpaceRef2,
        navConfig.getNodeSpaceRef());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertSame(componentInstance, navConfig.getPresentationType());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_presentationTypeHint() throws Exception {
    presentationTypeHint = "navConfigPresTypeHint1";
    NavigationConfig navConfig1 = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    String configName2 = "configName2";
    Integer fromHierarchyLevel2 = 22;
    Integer toHierarchyLevel2 = 60;
    Integer showInactiveToLevel2 = 55;
    String menuPart2 = "menuPart2";
    String dataType2 = "dataType2";
    SpaceReference nodeSpaceRef2 = new SpaceReference("NavConfigSpace2", new WikiReference(
        getContext().getDatabase()));
    String layoutType2 = "navConfig2LayoutType";
    Integer itemsPerPage2 = 50;
    String presentationTypeHint2 = "navConfigPresTypeHint2";
    String cmCssClass2 = "cm_cssTestClass2";
    NavigationConfig navConfig2 = new NavigationConfig(configName2, fromHierarchyLevel2,
        toHierarchyLevel2, showInactiveToLevel2, menuPart2, dataType2, nodeSpaceRef2, layoutType2,
        itemsPerPage2, presentationTypeHint2, cmCssClass2);
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertSame(componentInstance, navConfig.getPresentationType());
    // navConfig2 values for other fields must remain
    assertEquals("configName must equal to second parameter", configName2,
        navConfig.getConfigName());
    assertEquals("fromHieararchyLevel must equal to second parameter", fromHierarchyLevel2,
        (Integer) navConfig.getFromHierarchyLevel());
    assertEquals("toHieararchyLevel must equal to second parameter", toHierarchyLevel2,
        (Integer) navConfig.getToHierarchyLevel());
    assertEquals("showInactiveToLevel must equal to second parameter", showInactiveToLevel2,
        (Integer) navConfig.getShowInactiveToLevel());
    assertEquals("menuPart must equal to second parameter", menuPart2, navConfig.getMenuPart());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("nodeSpaceRef must equal to second parameter", nodeSpaceRef2,
        navConfig.getNodeSpaceRef());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_cmCssClass() throws Exception {
    cmCssClass = "cm_cssTestClass1";
    NavigationConfig navConfig1 = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    String configName2 = "configName2";
    Integer fromHierarchyLevel2 = 22;
    Integer toHierarchyLevel2 = 60;
    Integer showInactiveToLevel2 = 55;
    String menuPart2 = "menuPart2";
    String dataType2 = "dataType2";
    SpaceReference nodeSpaceRef2 = new SpaceReference("NavConfigSpace2", new WikiReference(
        getContext().getDatabase()));
    String layoutType2 = "navConfig2LayoutType";
    Integer itemsPerPage2 = 50;
    String presentationTypeHint2 = "navConfigPresTypeHint2";
    String cmCssClass2 = "cm_cssTestClass2";
    NavigationConfig navConfig2 = new NavigationConfig(configName2, fromHierarchyLevel2,
        toHierarchyLevel2, showInactiveToLevel2, menuPart2, dataType2, nodeSpaceRef2, layoutType2,
        itemsPerPage2, presentationTypeHint2, cmCssClass2);
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint2))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertEquals("cmCssClass must equal to first parameter", cmCssClass, navConfig.getCssClass());
    // navConfig2 values for other fields must remain
    assertEquals("configName must equal to second parameter", configName2,
        navConfig.getConfigName());
    assertEquals("fromHieararchyLevel must equal to second parameter", fromHierarchyLevel2,
        (Integer) navConfig.getFromHierarchyLevel());
    assertEquals("toHieararchyLevel must equal to second parameter", toHierarchyLevel2,
        (Integer) navConfig.getToHierarchyLevel());
    assertEquals("showInactiveToLevel must equal to second parameter", showInactiveToLevel2,
        (Integer) navConfig.getShowInactiveToLevel());
    assertEquals("menuPart must equal to second parameter", menuPart2, navConfig.getMenuPart());
    assertEquals("nodeSpaceRef must equal to second parameter", nodeSpaceRef2,
        navConfig.getNodeSpaceRef());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_defaults() throws Exception {
    NavigationConfig navConfig2 = new NavigationConfig();
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(NavigationConfig.DEFAULTS);
    assertFalse(navConfig.isEnabled());
    assertSame(navConfig, NavigationConfig.DEFAULTS);
    verifyDefault();
  }

}
