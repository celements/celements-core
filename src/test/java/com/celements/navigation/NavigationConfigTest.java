package com.celements.navigation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.NavigationConfig.Builder;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Optional;
import com.xpn.xwiki.web.Utils;

public class NavigationConfigTest extends AbstractComponentTest {

  private Builder navBuilder;

  @Before
  public void setUp_NavigationConfigTest() throws Exception {
    navBuilder = new NavigationConfig.Builder();
  }

  @Test
  public void testNavigationConfig_DEFAULTS() throws Exception {
    NavigationConfig navConfig = NavigationConfig.DEFAULTS;
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
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testGetConfigName_default() {
    NavigationConfig navConfig = navBuilder.build();
    assertEquals("", navConfig.getConfigName());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testGetConfigName() {
    String configName = "mainMenu";
    navBuilder.configName(configName);
    NavigationConfig navConfig = navBuilder.build();
    assertEquals(configName, navConfig.getConfigName());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testIsEnabled_true() {
    NavigationConfig navConfig = navBuilder.build();
    assertTrue(navConfig.isEnabled());
    // defaults for other fields must remain
    assertEquals("", navConfig.getConfigName());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testIsEnabled_false() {
    NavigationConfig navConfig = navBuilder.disable().build();
    assertFalse(navConfig.isEnabled());
    // defaults for other fields must remain
    assertEquals("", navConfig.getConfigName());
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testGetFromHierarchyLevel() {
    Integer fromHierarchyLevel = 10;
    navBuilder.fromHierarchyLevel(fromHierarchyLevel);
    NavigationConfig navConfig = navBuilder.build();
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
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testGetToHierarchyLevel() {
    Integer toHierarchyLevel = 11;
    navBuilder.toHierarchyLevel(toHierarchyLevel);
    NavigationConfig navConfig = navBuilder.build();
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
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testGetShowInactiveToLevel() {
    Integer showInactiveToLevel = 10;
    navBuilder.showInactiveToLevel(showInactiveToLevel);
    NavigationConfig navConfig = navBuilder.build();
    assertEquals("showInactiveToLevel must equals to parameter", showInactiveToLevel,
        (Integer) navConfig.getShowInactiveToLevel());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertEquals("", navConfig.getMenuPart());
    assertEquals("", navConfig.getCssClass());
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testGetMenuPart() {
    String menuPart = "mainMenu";
    navBuilder.menuPart(menuPart);
    NavigationConfig navConfig = navBuilder.build();
    assertEquals("menuPart must equals to parameter", menuPart, navConfig.getMenuPart());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getCssClass());
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testGetNodeSpaceRef() {
    String spaceName = "MySpace";
    SpaceReference nodeSpaceRef = new SpaceReference(spaceName, new WikiReference(
        getContext().getDatabase()));
    navBuilder.nodeSpaceRef(nodeSpaceRef);
    NavigationConfig navConfig = navBuilder.build();
    Optional<SpaceReference> firstReturnNodeSpace = navConfig.getNodeSpaceRef();
    assertEquals("expected equals nodeSpaceRef", nodeSpaceRef, firstReturnNodeSpace.get());
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
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testGetDataType() {
    String dataType = "testType";
    navBuilder.dataType(dataType);
    NavigationConfig navConfig = navBuilder.build();
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
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testGetLayoutType() {
    String layoutType = "testLayoutType";
    navBuilder.layoutType(layoutType);
    NavigationConfig navConfig = navBuilder.build();
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
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testGetNrOfItemsPerPage() {
    Integer itemsPerPage = 10;
    navBuilder.nrOfItemsPerPage(itemsPerPage);
    NavigationConfig navConfig = navBuilder.build();
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
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testGetPresentationType() throws Exception {
    String presentationTypeHint = "myTestPresentationType";
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint))).andReturn(componentInstance);
    replayDefault();
    navBuilder.presentationTypeHint(presentationTypeHint);
    NavigationConfig navConfig = navBuilder.build();
    assertSame(componentInstance, navConfig.getPresentationType().get());
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
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
  }

  @Test
  public void testGetCssClass() {
    String cmCssClass = "cm_cssTestClass";
    navBuilder.cmCssClass(cmCssClass);
    NavigationConfig navConfig = navBuilder.build();
    assertEquals("cmCssClass must equals to parameter", cmCssClass, navConfig.getCssClass());
    // defaults for other fields must remain
    assertTrue(navConfig.isEnabled());
    assertEquals("", navConfig.getConfigName());
    assertTrue("fromHieararchyLevel must be greater zero", navConfig.getFromHierarchyLevel() > 0);
    assertTrue("toHieararchyLevel must be greater zero", navConfig.getToHierarchyLevel() > 0);
    assertTrue("showInactiveToLevel must be greater equlas zero",
        navConfig.getShowInactiveToLevel() >= 0);
    assertEquals("", navConfig.getMenuPart());
    assertFalse(navConfig.getNodeSpaceRef().isPresent());
    assertEquals(NavigationConfig.PAGE_MENU_DATA_TYPE, navConfig.getDataType());
    assertEquals(NavigationConfig.LIST_LAYOUT_TYPE, navConfig.getLayoutType());
    assertEquals(NavigationConfig.UNLIMITED_ITEMS_PER_PAGE, navConfig.getNrOfItemsPerPage());
    assertNotNull(Utils.getComponent(IPresentationTypeRole.class));
    assertSame(Utils.getComponent(IPresentationTypeRole.class),
        navConfig.getPresentationType().get());
  }

  @Test
  public void testOverlayValues_configName() throws Exception {
    String configName = "configName1";
    navBuilder.configName(configName);
    NavigationConfig navConfig1 = navBuilder.build();
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
    NavigationConfig navConfig2 = new NavigationConfig.Builder().configName(
        configName2).fromHierarchyLevel(fromHierarchyLevel2).toHierarchyLevel(
            toHierarchyLevel2).showInactiveToLevel(showInactiveToLevel2).menuPart(
                menuPart2).dataType(dataType2).nodeSpaceRef(nodeSpaceRef2).layoutType(
                    layoutType2).nrOfItemsPerPage(itemsPerPage2).presentationTypeHint(
                        presentationTypeHint2).cmCssClass(cmCssClass2).build();
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
        navConfig.getNodeSpaceRef().get());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType().get());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_fromHierarchyLevel() throws Exception {
    Integer fromHierarchyLevel = 15;
    navBuilder.fromHierarchyLevel(fromHierarchyLevel);
    NavigationConfig navConfig1 = navBuilder.build();
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
    NavigationConfig navConfig2 = new NavigationConfig.Builder().configName(
        configName2).fromHierarchyLevel(fromHierarchyLevel2).toHierarchyLevel(
            toHierarchyLevel2).showInactiveToLevel(showInactiveToLevel2).menuPart(
                menuPart2).dataType(dataType2).nodeSpaceRef(nodeSpaceRef2).layoutType(
                    layoutType2).nrOfItemsPerPage(itemsPerPage2).presentationTypeHint(
                        presentationTypeHint2).cmCssClass(cmCssClass2).build();
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
        navConfig.getNodeSpaceRef().get());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType().get());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_toHierarchyLevel() throws Exception {
    Integer toHierarchyLevel = 70;
    navBuilder.toHierarchyLevel(toHierarchyLevel);
    NavigationConfig navConfig1 = navBuilder.build();
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
    NavigationConfig navConfig2 = new NavigationConfig.Builder().configName(
        configName2).fromHierarchyLevel(fromHierarchyLevel2).toHierarchyLevel(
            toHierarchyLevel2).showInactiveToLevel(showInactiveToLevel2).menuPart(
                menuPart2).dataType(dataType2).nodeSpaceRef(nodeSpaceRef2).layoutType(
                    layoutType2).nrOfItemsPerPage(itemsPerPage2).presentationTypeHint(
                        presentationTypeHint2).cmCssClass(cmCssClass2).build();
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
        navConfig.getNodeSpaceRef().get());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType().get());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_showInactiveToLevel() throws Exception {
    Integer showInactiveToLevel = 30;
    navBuilder.showInactiveToLevel(showInactiveToLevel);
    NavigationConfig navConfig1 = navBuilder.build();
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
    NavigationConfig navConfig2 = new NavigationConfig.Builder().configName(
        configName2).fromHierarchyLevel(fromHierarchyLevel2).toHierarchyLevel(
            toHierarchyLevel2).showInactiveToLevel(showInactiveToLevel2).menuPart(
                menuPart2).dataType(dataType2).nodeSpaceRef(nodeSpaceRef2).layoutType(
                    layoutType2).nrOfItemsPerPage(itemsPerPage2).presentationTypeHint(
                        presentationTypeHint2).cmCssClass(cmCssClass2).build();
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
        navConfig.getNodeSpaceRef().get());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType().get());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_menuPart() throws Exception {
    String menuPart = "menuPart1";
    navBuilder.menuPart(menuPart);
    NavigationConfig navConfig1 = navBuilder.build();
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
    NavigationConfig navConfig2 = new NavigationConfig.Builder().configName(
        configName2).fromHierarchyLevel(fromHierarchyLevel2).toHierarchyLevel(
            toHierarchyLevel2).showInactiveToLevel(showInactiveToLevel2).menuPart(
                menuPart2).dataType(dataType2).nodeSpaceRef(nodeSpaceRef2).layoutType(
                    layoutType2).nrOfItemsPerPage(itemsPerPage2).presentationTypeHint(
                        presentationTypeHint2).cmCssClass(cmCssClass2).build();
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
        navConfig.getNodeSpaceRef().get());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType().get());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_dataType() throws Exception {
    String dataType = "dataType1";
    navBuilder.dataType(dataType);
    NavigationConfig navConfig1 = navBuilder.build();
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
    NavigationConfig navConfig2 = new NavigationConfig.Builder().configName(
        configName2).fromHierarchyLevel(fromHierarchyLevel2).toHierarchyLevel(
            toHierarchyLevel2).showInactiveToLevel(showInactiveToLevel2).menuPart(
                menuPart2).dataType(dataType2).nodeSpaceRef(nodeSpaceRef2).layoutType(
                    layoutType2).nrOfItemsPerPage(itemsPerPage2).presentationTypeHint(
                        presentationTypeHint2).cmCssClass(cmCssClass2).build();
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
        navConfig.getNodeSpaceRef().get());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType().get());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_nodeSpaceRef() throws Exception {
    SpaceReference nodeSpaceRef = new SpaceReference("NavConfigSpace1", new WikiReference(
        getContext().getDatabase()));
    navBuilder.nodeSpaceRef(nodeSpaceRef);
    NavigationConfig navConfig1 = navBuilder.build();
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
    NavigationConfig navConfig2 = new NavigationConfig.Builder().configName(
        configName2).fromHierarchyLevel(fromHierarchyLevel2).toHierarchyLevel(
            toHierarchyLevel2).showInactiveToLevel(showInactiveToLevel2).menuPart(
                menuPart2).dataType(dataType2).nodeSpaceRef(nodeSpaceRef2).layoutType(
                    layoutType2).nrOfItemsPerPage(itemsPerPage2).presentationTypeHint(
                        presentationTypeHint2).cmCssClass(cmCssClass2).build();
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint2))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertEquals("nodeSpaceRef must equal to first parameter", nodeSpaceRef,
        navConfig.getNodeSpaceRef().get());
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
    assertSame(componentInstance, navConfig.getPresentationType().get());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_layoutType() throws Exception {
    String layoutType = "layoutType1";
    navBuilder.layoutType(layoutType);
    NavigationConfig navConfig1 = navBuilder.build();
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
    NavigationConfig navConfig2 = new NavigationConfig.Builder().configName(
        configName2).fromHierarchyLevel(fromHierarchyLevel2).toHierarchyLevel(
            toHierarchyLevel2).showInactiveToLevel(showInactiveToLevel2).menuPart(
                menuPart2).dataType(dataType2).nodeSpaceRef(nodeSpaceRef2).layoutType(
                    layoutType2).nrOfItemsPerPage(itemsPerPage2).presentationTypeHint(
                        presentationTypeHint2).cmCssClass(cmCssClass2).build();
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
        navConfig.getNodeSpaceRef().get());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType().get());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_itemsPerPage() throws Exception {
    Integer itemsPerPage = 20;
    navBuilder.nrOfItemsPerPage(itemsPerPage);
    NavigationConfig navConfig1 = navBuilder.build();
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
    NavigationConfig navConfig2 = new NavigationConfig.Builder().configName(
        configName2).fromHierarchyLevel(fromHierarchyLevel2).toHierarchyLevel(
            toHierarchyLevel2).showInactiveToLevel(showInactiveToLevel2).menuPart(
                menuPart2).dataType(dataType2).nodeSpaceRef(nodeSpaceRef2).layoutType(
                    layoutType2).nrOfItemsPerPage(itemsPerPage2).presentationTypeHint(
                        presentationTypeHint2).cmCssClass(cmCssClass2).build();
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
        navConfig.getNodeSpaceRef().get());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertSame(componentInstance, navConfig.getPresentationType().get());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_presentationTypeHint() throws Exception {
    String presentationTypeHint = "navConfigPresTypeHint1";
    navBuilder.presentationTypeHint(presentationTypeHint);
    NavigationConfig navConfig1 = navBuilder.build();
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
    NavigationConfig navConfig2 = new NavigationConfig.Builder().configName(
        configName2).fromHierarchyLevel(fromHierarchyLevel2).toHierarchyLevel(
            toHierarchyLevel2).showInactiveToLevel(showInactiveToLevel2).menuPart(
                menuPart2).dataType(dataType2).nodeSpaceRef(nodeSpaceRef2).layoutType(
                    layoutType2).nrOfItemsPerPage(itemsPerPage2).presentationTypeHint(
                        presentationTypeHint2).cmCssClass(cmCssClass2).build();
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        presentationTypeHint))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(navConfig1);
    assertTrue(navConfig.isEnabled());
    assertSame(componentInstance, navConfig.getPresentationType().get());
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
        navConfig.getNodeSpaceRef().get());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertEquals("cmCssClass must equal to second parameter", cmCssClass2, navConfig.getCssClass());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_cmCssClass() throws Exception {
    String cmCssClass = "cm_cssTestClass1";
    navBuilder.cmCssClass(cmCssClass);
    NavigationConfig navConfig1 = navBuilder.build();
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
    NavigationConfig navConfig2 = new NavigationConfig.Builder().configName(
        configName2).fromHierarchyLevel(fromHierarchyLevel2).toHierarchyLevel(
            toHierarchyLevel2).showInactiveToLevel(showInactiveToLevel2).menuPart(
                menuPart2).dataType(dataType2).nodeSpaceRef(nodeSpaceRef2).layoutType(
                    layoutType2).nrOfItemsPerPage(itemsPerPage2).presentationTypeHint(
                        presentationTypeHint2).cmCssClass(cmCssClass2).build();
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
        navConfig.getNodeSpaceRef().get());
    assertEquals("dataType must equal to second parameter", dataType2, navConfig.getDataType());
    assertEquals("layoutType must equal to second parameter", layoutType2,
        navConfig.getLayoutType());
    assertEquals("itemsPerPage must equal to second parameter", itemsPerPage2,
        (Integer) navConfig.getNrOfItemsPerPage());
    assertSame(componentInstance, navConfig.getPresentationType().get());
    verifyDefault();
  }

  @Test
  public void testOverlayValues_defaults() throws Exception {
    NavigationConfig navConfig2 = NavigationConfig.DEFAULTS;
    replayDefault();
    NavigationConfig navConfig = navConfig2.overlay(NavigationConfig.DEFAULTS);
    assertFalse(navConfig.isEnabled());
    assertSame(navConfig, NavigationConfig.DEFAULTS);
    verifyDefault();
  }

}
