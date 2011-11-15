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
package com.celements.navigation.cmd;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;

import org.junit.*;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class GetMappedMenuItemsForParentCommandTest
  extends AbstractBridgedComponentTestCase {

  private GetMappedMenuItemsForParentCommand _getMenuItemsCmd;
  private XWikiContext _context;
  private XWiki _xwiki;

  @Before
  public void setUp_GetMappedMenuItemsForParentCommandTest() throws Exception {
    _context = getContext();
    _xwiki = createMock(XWiki.class);
    _context.setWiki(_xwiki);
    _getMenuItemsCmd = new GetMappedMenuItemsForParentCommand();
  }

  @Test
  public void testGetTreeNodesForParentKey_emptyResult() throws XWikiException {
    XWikiStoreInterface store = createMock(XWikiStoreInterface.class);
    expect(_xwiki.getStore()).andReturn(store).anyTimes();
    expect(store.searchDocumentsNames((String)anyObject(), eq(0), eq(0),
        (List<?>)anyObject(), same(_context))).andReturn(new ArrayList<String>()
            ).anyTimes();
    replay(_xwiki, store);
    assertNotNull(_getMenuItemsCmd.getTreeNodesForParentKey("", _context));
    assertEquals(0, _getMenuItemsCmd.getTreeNodesForParentKey("", _context).size());
    verify(_xwiki, store);
  }
  
  @Test
  public void testGetTreeNodesForParentKey_notActive() throws XWikiException {
    _getMenuItemsCmd.set_isActive(false);
    assertEquals(0, _getMenuItemsCmd.getTreeNodesForParentKey("Space.Doc", _context
        ).size());
  }

  @Test
  public void testGetHQL() {
    assertTrue(_getMenuItemsCmd.getHQL().contains(" and doc.translation='0'"));
    assertTrue(_getMenuItemsCmd.getHQL().contains(" and obj.id=menuitem.id"));
    assertTrue(_getMenuItemsCmd.getHQL().endsWith("order by menuitem.menu_position"));
  }

}
