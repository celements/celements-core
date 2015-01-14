package com.celements.pagetype.java;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeReference;
import com.xpn.xwiki.web.Utils;

public class JavaPageTypeProviderTest extends AbstractBridgedComponentTestCase {

  private static final String _TEST_PAGE_TYPE = "TestPageType";
  private JavaPageTypeProvider ptProvider;
  private IJavaPageTypeRole testPageType;
  private PageTypeReference pageTypeRef;

  @Before
  public void setUp_JavaPageTypeProviderTest() throws Exception {
    ptProvider = (JavaPageTypeProvider) Utils.getComponent(
        IPageTypeProviderRole.class, JavaPageTypeProvider.PROVIDER_HINT);
    testPageType = createMockAndAddToDefault(IJavaPageTypeRole.class);
    ptProvider.javaPageTypesMap.put(_TEST_PAGE_TYPE, testPageType);
    pageTypeRef = new PageTypeReference(_TEST_PAGE_TYPE,
        JavaPageTypeProvider.PROVIDER_HINT, Arrays.asList(""));
    expect(testPageType.getName()).andReturn(_TEST_PAGE_TYPE).anyTimes();
    expect(testPageType.getCategories()).andReturn(Arrays.asList("", "pageType")).anyTimes();
  }

  @After
  public void tearDown_JavaPageTypeProviderTest() {
    ptProvider.javaPageTypesMap.remove(_TEST_PAGE_TYPE);
  }

  @Test
  public void testGetPageTypes() {
    replayDefault();
    List<PageTypeReference> pageTypeList = ptProvider.getPageTypes();
    assertTrue(pageTypeList.contains(pageTypeRef));
    verifyDefault();
  }

  @Test
  public void testGetPageTypeByReference() {
    replayDefault();
    IPageTypeConfig pageTypeConfig = ptProvider.getPageTypeByReference(pageTypeRef);
    assertNotNull(pageTypeConfig);
    assertEquals(_TEST_PAGE_TYPE, pageTypeConfig.getName());
    verifyDefault();
  }

}
