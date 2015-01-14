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
package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

public class ContextMenuCSSClassesCommandTest extends AbstractBridgedComponentTestCase{

  private ContextMenuCSSClassesCommand cmCssClassesCmd;
  private XWikiContext context;
  private XWiki xwiki;

  @Before
  public void setUp_ContextMenuCSSClassesCommand() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    cmCssClassesCmd = new ContextMenuCSSClassesCommand();
  }

  @Test
  public void testGetCMhql() {
    String hql = cmCssClassesCmd.getCMhql() + " ";
    assertNotNull(hql);
    assertTrue(hql.contains("doc.name from XWikiDocument as doc"));
    assertTrue(hql.contains("BaseObject as obj"));
    assertTrue(hql.contains(" obj.name = doc.fullName "));
    assertTrue(hql.contains(" doc.space = 'CelementsContextMenu' "));
    assertTrue(hql.contains(" obj.className = 'Celements2.ContextMenuItemClass' "));
    assertTrue(hql.matches("select doc.name from \\w.*?(, \\w.*?)*"
        + " where \\w.*?( and \\w.*?)*"));
  }

  @Test
  public void testGetCMcssClassesOneDB() throws Exception {
    List<Object> cmStringList = new ArrayList<Object>(Arrays.asList(
        "abcClass", "secondClass"));
    expect(xwiki.search(isA(String.class), same(context))).andReturn(cmStringList);
    replay(xwiki);
    Set<String> resultSet = cmCssClassesCmd.getCMcssClassesOneDB(context);
    assertTrue(resultSet.contains("abcClass"));
    assertTrue(resultSet.contains("secondClass"));
    verify(xwiki);
  }

  @Test
  public void testGetCMcssClassesOneDB_preventDouplicates() throws Exception {
    List<Object> cmStringList = new ArrayList<Object>(Arrays.asList(
        "abcClass", "secondClass","abcClass", "secondClass","thirdClass"));
    expect(xwiki.search(isA(String.class), same(context))).andReturn(cmStringList);
    replay(xwiki);
    Set<String> resultSet = cmCssClassesCmd.getCMcssClassesOneDB(context);
    assertTrue(resultSet.contains("abcClass"));
    assertTrue(resultSet.contains("secondClass"));
    assertTrue(resultSet.contains("thirdClass"));
    assertEquals("Douplicates must be removed.", 3, resultSet.size());
    verify(xwiki);
  }

  @Test
  public void testGetCM_CSSclasses_notViewAction() {
    context.setAction("edit");
    context.setDatabase("myCelements");
    replay(xwiki);
    List<String> resultSet = cmCssClassesCmd.getCM_CSSclasses(context);
    assertTrue("do not return any classes if action != 'view'", resultSet.isEmpty());
    assertEquals("Must switch back the database", "myCelements", context.getDatabase());
    verify(xwiki);
  }

  @Test
  public void testGetCM_CSSclasses() throws Exception {
    context.setAction("view");
    context.setDatabase("myCelements");
    List<Object> cmStringList = new ArrayList<Object>(Arrays.asList(
        "abcClass", "secondClass"));
    expect(xwiki.search(isA(String.class), same(context))).andReturn(cmStringList);
    List<Object> cmStringList2 = new ArrayList<Object>(Arrays.asList(
        "abcClass", "secondClass","abcClass", "secondClass","thirdClass"));
    expect(xwiki.search(isA(String.class), same(context))).andReturn(cmStringList2);
    replay(xwiki);
    List<String> resultSet = cmCssClassesCmd.getCM_CSSclasses(context);
    assertTrue(resultSet.contains("abcClass"));
    assertTrue(resultSet.contains("secondClass"));
    assertTrue(resultSet.contains("thirdClass"));
    assertEquals("Douplicates must be removed.", 3, resultSet.size());
    assertEquals("Must switch back the database", "myCelements", context.getDatabase());
    verify(xwiki);
  }

  @Test
  public void testGetAllContextMenuCSSClassesAsJSON_empty() {
    assertEquals("[]", cmCssClassesCmd.getAllContextMenuCSSClassesAsJSON(context));
  }

}
