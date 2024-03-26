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
package com.celements.web.plugin;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

public class CelementsWebPluginTest extends AbstractComponentTest {

  private CelementsWebPlugin plugin;
  private XWikiContext context;
  private XWiki xwiki;

  @Before
  public void setUp_CelementsWebPluginTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    // context.setUser calls xwiki.isVirtualMode in xwiki version 4.5
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    plugin = new CelementsWebPlugin("celementsweb", "CelementsWebPlugin", context);
  }

  @Test
  public void testEnableMappedMenuItems() {
    plugin.enableMappedMenuItems(context);
    assertTrue(context.get(
        GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY) != null);
    assertTrue(context.get(
        GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY) != null);
    assertTrue(((GetMappedMenuItemsForParentCommand) context.get(
        GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY)).isActive());

  }

  @Test
  public void testIsFormFilled_false() {
    Map<String, String[]> map = new HashMap<>();
    map.put("xpage", new String[] { "celements_ajax", "underlay", "overlay" });
    map.put("conf", new String[] { "OverlayConfig" });
    map.put("language", new String[] { "de" });
    assertFalse(plugin.isFormFilled(map, null));
  }

  @Test
  public void testIsFormFilled_true() {
    Map<String, String[]> map = new HashMap<>();
    map.put("name", new String[] { "My Name" });
    map.put("xpage", new String[] { "celements_ajax", "underlay", "overlay" });
    map.put("conf", new String[] { "OverlayConfig" });
    map.put("language", new String[] { "de" });
    assertTrue(plugin.isFormFilled(map, null));
  }

  @Test
  public void testIsFormFilled_true_oneParam() {
    Map<String, String[]> map = new HashMap<>();
    map.put("search", new String[] { "Search Term" });
    assertTrue(plugin.isFormFilled(map, null));
  }

  @Test
  public void testArrayContains_false() {
    String[] array = { "overlay", "underlay", "middle" };
    assertFalse(plugin.arrayContains(array, "notavailable"));
    assertFalse(plugin.arrayContains(array, "overlayunderlay"));
  }

  @Test
  public void testArrayContains_true() {
    String[] array = { "overlay", "underlay", "middle" };
    assertTrue(plugin.arrayContains(array, "overlay"));
    assertTrue(plugin.arrayContains(array, "underlay"));
    assertTrue(plugin.arrayContains(array, "middle"));
  }

  @Test
  public void testGetPrepareVelocityContextService() {
    assertNotNull(plugin.getPrepareVelocityContextService());
  }

}
