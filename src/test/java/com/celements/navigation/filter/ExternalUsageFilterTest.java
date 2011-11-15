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
package com.celements.navigation.filter;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.filter.ExternalUsageFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;

public class ExternalUsageFilterTest extends AbstractBridgedComponentTestCase {

  private ExternalUsageFilter filter;
  private XWikiContext context;
  private XWiki wiki;
  private XWikiRightService rightsService;
  private InternalRightsFilter rightsFilter;

  @Before
  public void setUp_ExternalUsageFilterTest() throws Exception {
    filter = new ExternalUsageFilter();
    rightsFilter = createMock(InternalRightsFilter.class);
    filter.setRightsFilter(rightsFilter);
    context = getContext();
    wiki = createMock(XWiki.class);
    context.setWiki(wiki);
    rightsService = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(rightsService).anyTimes();
  }

  @Test
  public void testGetRightsFilter() {
    filter.setRightsFilter(null);
    InternalRightsFilter rightsFilterNew = filter.getRightsFilter();
    assertNotNull(rightsFilterNew);
    assertSame("expecting singleton", rightsFilterNew, filter.getRightsFilter());
  }

  @Test
  public void testSetRightsFilter() {
    InternalRightsFilter rightsFilterNew = createMock(InternalRightsFilter.class);
    filter.setRightsFilter(rightsFilterNew);
    assertNotNull(filter.getRightsFilter());
    assertSame("expecting injected filter object", rightsFilterNew,
        filter.getRightsFilter());
  }

  @Test
  public void testConvertObject() {
    BaseObject baseObj = new BaseObject();
    expect(rightsService.hasProgrammingRights(same(context))).andReturn(true).anyTimes();
    replay(wiki, rightsService, rightsFilter);
    assertSame(baseObj, filter.convertObject(baseObj , context).getXWikiObject());
    verify(wiki, rightsService, rightsFilter);
  }

  @Test
  public void testGetMenuPart_delegate() {
    String menuPart = "mainPart";
    expect(rightsFilter.getMenuPart()).andReturn(menuPart).once();
    replay(wiki, rightsService, rightsFilter);
    assertEquals(menuPart, filter.getMenuPart());
    verify(wiki, rightsService, rightsFilter);
  }

  @Test
  public void testIncludeMenuItem_delegate() {
    BaseObject baseObj = new BaseObject();
    expect(rightsFilter.includeMenuItem(same(baseObj), same(context))).andReturn(true
        ).once();
    replay(wiki, rightsService, rightsFilter);
    assertEquals(true, filter.includeMenuItem(baseObj, context));
    verify(wiki, rightsService, rightsFilter);
  }

  @Test
  public void testSetMenuPart_delegate() {
    String menuPart = "mainPart";
    rightsFilter.setMenuPart(eq(menuPart));
    expectLastCall().once();
    replay(wiki, rightsService, rightsFilter);
    filter.setMenuPart(menuPart);
    verify(wiki, rightsService, rightsFilter);
  }

}
