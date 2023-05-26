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
package com.celements.pagetype.java;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeReference;
import com.google.common.collect.Sets;
import com.xpn.xwiki.web.Utils;

public class JavaPageTypeProviderTest extends AbstractComponentTest {

  private static final String _TEST_PAGE_TYPE = "TestPageType";
  private JavaPageTypeProvider ptProvider;
  private IJavaPageTypeRole testPageType;
  private PageTypeReference pageTypeRef;

  @Before
  public void setUp_JavaPageTypeProviderTest() throws Exception {
    testPageType = registerComponentMock(IJavaPageTypeRole.class, _TEST_PAGE_TYPE);
    ptProvider = (JavaPageTypeProvider) Utils.getComponent(IPageTypeProviderRole.class,
        JavaPageTypeProvider.PROVIDER_HINT);
    pageTypeRef = new PageTypeReference(_TEST_PAGE_TYPE, JavaPageTypeProvider.PROVIDER_HINT,
        Arrays.asList(""));
    expect(testPageType.getName()).andReturn(_TEST_PAGE_TYPE).anyTimes();
    expect(testPageType.getCategoryNames()).andReturn(Sets.newHashSet("", "pageType")).anyTimes();
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

  @Test
  public void test_buildTypeRefsMap() {
    replayDefault();
    Map<PageTypeReference, IJavaPageTypeRole> map = ptProvider.buildTypeRefsMap();
    verifyDefault();
    assertNotNull(map);
    assertTrue("expecting at least the TestPageType", map.size() >= 1);
  }

}
