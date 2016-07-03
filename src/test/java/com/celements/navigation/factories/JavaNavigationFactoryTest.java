package com.celements.navigation.factories;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.NavigationConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.xobject.XObjectPageTypeProvider;
import com.xpn.xwiki.web.Utils;

public class JavaNavigationFactoryTest extends AbstractComponentTest {

  private static final String TEST_NAV_PAGETYPE = "MyPageType";

  private JavaNavigationFactory javaNavFactory;
  private JavaNavigationConfigurator testConfigurator;
  private DocumentReference pageTypeDocRef;
  private DocumentReference testDocRef;
  private IPageTypeResolverRole pageTypeResolverMock;
  private PageTypeReference pageTypeRef;
  private DocumentReference defaultPageTypeDocRef;
  private PageTypeReference defaultPageTypeRef;

  @Before
  public void setUp_JavaNavigationFactoryTest() throws Exception {
    pageTypeResolverMock = registerComponentMock(IPageTypeResolverRole.class);
    testConfigurator = registerComponentMock(JavaNavigationConfigurator.class, TEST_NAV_PAGETYPE);
    javaNavFactory = (JavaNavigationFactory) Utils.getComponent(NavigationFactory.class,
        JavaNavigationFactory.JAVA_NAV_FACTORY_HINT);
    pageTypeDocRef = new DocumentReference(getContext().getDatabase(), "PageTypes",
        TEST_NAV_PAGETYPE);
    pageTypeRef = new PageTypeReference(pageTypeDocRef.getName(),
        XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER, Collections.<String>emptyList());
    testDocRef = new DocumentReference(getContext().getDatabase(), "MySpace", "MyTestDoc");
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(testDocRef))).andReturn(
        pageTypeRef).anyTimes();
    defaultPageTypeDocRef = new DocumentReference(getContext().getDatabase(), "PageTypes",
        "RichText");
    defaultPageTypeRef = new PageTypeReference(defaultPageTypeDocRef.getName(),
        XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER, Collections.<String>emptyList());
    expect(pageTypeResolverMock.getPageTypeRefForCurrentDoc()).andReturn(
        defaultPageTypeRef).anyTimes();
  }

  @Test
  public void testGetDefaultConfigReference() {
    replayDefault();
    assertEquals(defaultPageTypeRef, javaNavFactory.getDefaultConfigReference());
    verifyDefault();
  }

  @Test
  public void testGetNavigationConfigString_defaults() {
    expect(testConfigurator.handles(eq(defaultPageTypeRef))).andReturn(false);
    replayDefault();
    NavigationConfig navConfig = javaNavFactory.getNavigationConfig(defaultPageTypeRef);
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
    expect(testConfigurator.handles(eq(pageTypeRef))).andReturn(true);
    expect(testConfigurator.getNavigationConfig(eq(pageTypeRef))).andReturn(expectedNavConfig);
    replayDefault();
    NavigationConfig navConfig = javaNavFactory.getNavigationConfig(pageTypeRef);
    assertNotNull(navConfig);
    assertSame(expectedNavConfig, navConfig);
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfigString_false() {
    expect(testConfigurator.handles(eq(defaultPageTypeRef))).andReturn(false);
    replayDefault();
    assertFalse(javaNavFactory.hasNavigationConfig(defaultPageTypeRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfigString_true() {
    expect(testConfigurator.handles(eq(pageTypeRef))).andReturn(true);
    replayDefault();
    assertTrue(javaNavFactory.hasNavigationConfig(pageTypeRef));
    verifyDefault();
  }

}
