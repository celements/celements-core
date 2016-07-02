package com.celements.navigation.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.NavigationConfig;
import com.xpn.xwiki.web.Utils;

public class JavaNavigationFactoryTest extends AbstractComponentTest {

  private static final String TEST_NAV_CONFIGURATOR_HINT = "testNavConfig";

  private JavaNavigationFactory javaNavFactory;
  private JavaNavigationConfigurator testConfigurator;

  @Before
  public void setUp_JavaNavigationFactoryTest() throws Exception {
    testConfigurator = registerComponentMock(JavaNavigationConfigurator.class,
        TEST_NAV_CONFIGURATOR_HINT);
    javaNavFactory = (JavaNavigationFactory) Utils.getComponent(NavigationFactory.class,
        JavaNavigationFactory.JAVA_NAV_FACTORY_HINT);
  }

  @Test
  public void testGetDefaultConfigReference() {
    assertEquals("default", javaNavFactory.getDefaultConfigReference());
  }

  @Test
  public void testGetNavigationConfigString_defaults() {
    replayDefault();
    NavigationConfig navConfig = javaNavFactory.getNavigationConfig("testHint");
    assertNotNull(navConfig);
    assertSame(NavigationConfig.DEFAULTS, navConfig);
    verifyDefault();
  }

  @Test
  public void testGetNavigationConfigString() throws Exception {
    String configName = "configName";
    Integer fromHierarchyLevel = 22;
    Integer toHierarchyLevel = 60;
    Integer showInactiveToLevel = 55;
    String menuPart = "menuPart";
    String dataType = "dataType";
    SpaceReference nodeSpaceRef = new SpaceReference("NavConfigSpace", new WikiReference(
        getContext().getDatabase()));
    String layoutType = "navConfig2LayoutType";
    Integer itemsPerPage = 20;
    String presentationTypeHint = null;
    String cmCssClass = "cm_cssTestClass2";
    final NavigationConfig expectedNavConfig = new NavigationConfig(configName, fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType,
        itemsPerPage, presentationTypeHint, cmCssClass);
    expect(testConfigurator.getNavigationConfig()).andReturn(expectedNavConfig);
    replayDefault();
    NavigationConfig navConfig = javaNavFactory.getNavigationConfig(TEST_NAV_CONFIGURATOR_HINT);
    assertNotNull(navConfig);
    assertSame(expectedNavConfig, navConfig);
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfigString_false() {
    assertFalse(javaNavFactory.hasNavigationConfig("testHint"));
  }

  @Test
  public void testHasNavigationConfigString_true() {
    assertTrue(javaNavFactory.hasNavigationConfig(TEST_NAV_CONFIGURATOR_HINT));
  }

}
