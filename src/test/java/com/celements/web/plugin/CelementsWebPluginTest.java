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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.celements.web.plugin.cmd.CelSendMail;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class CelementsWebPluginTest extends AbstractBridgedComponentTestCase {

  private CelementsWebPlugin plugin;
  private XWikiContext context;
  private XWiki xwiki;
  
  @Before
  public void setUp_CelementsWebPluginTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    plugin = new CelementsWebPlugin("celementsweb", "CelementsWebPlugin", context);
  }
  
  @Test
  public void testGetUsernameForToken() throws XWikiException {
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    XWikiStoreInterface store = createMock(XWikiStoreInterface.class);
    String userToken = "123456789012345678901234";
    List<String> userDocs = new Vector<String>();
    userDocs.add("Doc.Fullname");
    expect(xwiki.getStore()).andReturn(store).once();
    Capture<String> captHQL = new Capture<String>();
    Capture<List<?>> captParams = new Capture<List<?>>();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), 
        capture(captParams ), same(context))).andReturn(userDocs).once();
    replay(xwiki, store);
    assertEquals("Doc.Fullname", plugin.getUsernameForToken(userToken, context));
    assertTrue(captHQL.getValue().contains("token.tokenvalue=?"));
    assertTrue("There seems to be no database independent 'now' in hql.",
        captHQL.getValue().contains("token.validuntil>=?"));
    assertTrue(captParams.getValue().contains(plugin.encryptString("hash:SHA-512:",
        userToken)));
    verify(xwiki, store);
  }
  
  @Test
  public void testGetUsernameForToken_userFromMainwiki() throws XWikiException {
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    XWikiStoreInterface store = createMock(XWikiStoreInterface.class);
    String userToken = "123456789012345678901234";
    List<String> userDocs = new Vector<String>();
    userDocs.add("Doc.Fullname");
    expect(xwiki.getStore()).andReturn(store).once();
    Capture<String> captHQL = new Capture<String>();
    Capture<String> captHQL2 = new Capture<String>();
    Capture<List<?>> captParams = new Capture<List<?>>();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), 
        capture(captParams ), same(context))).andReturn(new ArrayList<String>()).once();
    expect(store.searchDocumentsNames(capture(captHQL2), eq(0), eq(0), 
        capture(captParams ), same(context))).andReturn(userDocs).once();
    replay(xwiki, store);
    assertEquals("xwiki:Doc.Fullname", plugin.getUsernameForToken(userToken, context));
    assertTrue(captHQL2.getValue().contains("token.tokenvalue=?"));
    assertTrue("There seems to be no database independent 'now' in hql.",
        captHQL2.getValue().contains("token.validuntil>=?"));
    assertTrue(captParams.getValue().contains(plugin.encryptString("hash:SHA-512:",
        userToken)));
    verify(xwiki, store);
  }
  
  @Test
  public void testCheckAuthByToken_noUser() throws XWikiException {
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    XWikiStoreInterface store = createMock(XWikiStoreInterface.class);
    String userToken = "123456789012345678901234";
    List<String> userDocs = new Vector<String>();
    expect(xwiki.getStore()).andReturn(store).once();
    Capture<String> captHQL = new Capture<String>();
    Capture<List<?>> captParams = new Capture<List<?>>();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), 
        capture(captParams ), same(context))).andReturn(userDocs).times(2);
    replay(xwiki, store);
    assertNull(plugin.checkAuthByToken(userToken, context));
  }
  
  @Test
  public void testCheckAuthByToken() throws XWikiException {
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    XWikiStoreInterface store = createMock(XWikiStoreInterface.class);
    String userToken = "123456789012345678901234";
    List<String> userDocs = new Vector<String>();
    userDocs.add("Doc.Fullname");
    expect(xwiki.getStore()).andReturn(store).once();
    Capture<String> captHQL = new Capture<String>();
    Capture<List<?>> captParams = new Capture<List<?>>();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), 
        capture(captParams ), same(context))).andReturn(userDocs).once();
    replay(xwiki, store);
    assertEquals("Doc.Fullname", plugin.checkAuthByToken(userToken, context).getUser());
    assertEquals("Doc.Fullname", context.getXWikiUser().getUser());
    assertEquals("Doc.Fullname", context.getUser());
    verify(xwiki, store);
  }
  
  @Test
  public void testEnableMappedMenuItems() {
    plugin.enableMappedMenuItems(context);
    assertTrue(context.get(GetMappedMenuItemsForParentCommand.
        CELEMENTS_MAPPED_MENU_ITEMS_KEY) != null);
    assertTrue(context.get(GetMappedMenuItemsForParentCommand.
        CELEMENTS_MAPPED_MENU_ITEMS_KEY) != null);
    assertTrue(((GetMappedMenuItemsForParentCommand)context.get(
        GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY)).is_isActive());
    
  }

  @Test
  public void testGetSupportedAdminLanguages() {
    assertNotNull(plugin.getSupportedAdminLanguages());
    assertEquals(Arrays.asList(new String[] {"de","fr","en","it"}),
        plugin.getSupportedAdminLanguages());
  }

  @Test
  public void testSetSupportedAdminLanguages() {
    List<String> injectedLangList = Arrays.asList(new String[] {"bla","bli","blo"});
    plugin.setSupportedAdminLanguages(injectedLangList);
    assertNotNull(plugin.getSupportedAdminLanguages());
    assertEquals(injectedLangList, plugin.getSupportedAdminLanguages());
  }

  @Test
  public void injectCelSendMail() {
    CelSendMail csm = new CelSendMail(context);
    plugin.injectCelSendMail(csm);
    assertSame(csm, plugin.getCelSendMail(context));
  }
  
  @Test
  public void testGetCelSendMail_notNull() {
    assertNotNull(plugin.getCelSendMail(context));
  }
  
  @Test
  public void testGetCelSendMail_notSingleton() {
    CelSendMail csm = plugin.getCelSendMail(context);
    assertNotSame(csm, plugin.getCelSendMail(context));
  }

  @Test
  public void testIsFormFilled_false() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("xpage", new String[]{"celements_ajax", "underlay", "overlay"});
    map.put("conf", new String[]{"OverlayConfig"});
    map.put("language", new String[]{"de"});
    assertFalse(plugin.isFormFilled(map, null));
  }

  @Test
  public void testIsFormFilled_true() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[]{"My Name"});
    map.put("xpage", new String[]{"celements_ajax", "underlay", "overlay"});
    map.put("conf", new String[]{"OverlayConfig"});
    map.put("language", new String[]{"de"});
    assertTrue(plugin.isFormFilled(map, null));
  }

  @Test
  public void testIsFormFilled_true_oneParam() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("search", new String[]{"Search Term"});
    assertTrue(plugin.isFormFilled(map, null));
  }

  @Test
  public void testGetIsFilledModifier_oneParam() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("search", new String[]{"Search Term"});
    assertEquals(0, plugin.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_oneParam_xredirect() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("search", new String[]{"Search Term"});
    map.put("xredirect", new String[]{"/My/Document"});
    assertEquals(1, plugin.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_oneParam_xredirect_additionalSame() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("search", new String[]{"Search Term"});
    map.put("name", new String[]{"My Name"});
    map.put("firstname", new String[]{"My First Name"});
    map.put("xredirect", new String[]{"/My/Document"});
    Set<String> additional = new HashSet<String>();
    additional.add("xredirect");
    additional.add("overlay");
    assertEquals(1, plugin.getIsFilledModifier(map, additional));
  }

  @Test
  public void testGetIsFilledModifier_oneParam_xredirect_additionalElse() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("search", new String[]{"Search Term"});
    map.put("name", new String[]{"My Name"});
    map.put("firstname", new String[]{"My First Name"});
    map.put("xredirect", new String[]{"/My/Document"});
    Set<String> additional = new HashSet<String>();
    additional.add("name");
    additional.add("overlay");
    assertEquals(2, plugin.getIsFilledModifier(map, additional));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[]{"My Name"});
    map.put("firstname", new String[]{"My First Name"});
    map.put("language", new String[]{"de"});
    assertEquals(1, plugin.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_xpage() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[]{"My Name"});
    map.put("firstname", new String[]{"My First Name"});
    map.put("xpage", new String[]{"celements_ajax", "underlay"});
    map.put("conf", new String[]{"OverlayConfig"});
    map.put("language", new String[]{"de"});
    assertEquals(2, plugin.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_overlay() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[]{"My Name"});
    map.put("firstname", new String[]{"My First Name"});
    map.put("xpage", new String[]{"overlay", "underlay"});
    map.put("conf", new String[]{"OverlayConfig"});
    assertEquals(2, plugin.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_overlay_lang() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[]{"My Name"});
    map.put("firstname", new String[]{"My First Name"});
    map.put("xpage", new String[]{"overlay", "underlay"});
    map.put("conf", new String[]{"OverlayConfig"});
    map.put("language", new String[]{"de"});
    assertEquals(3, plugin.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_ajax() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[]{"My Name"});
    map.put("firstname", new String[]{"My First Name"});
    map.put("xpage", new String[]{"celements_ajax", "underlay"});
    map.put("ajax_mode", new String[]{"MyAjax"});
    assertEquals(2, plugin.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_ajax_skin() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[]{"My Name"});
    map.put("firstname", new String[]{"My First Name"});
    map.put("xpage", new String[]{"celements_ajax", "underlay"});
    map.put("ajax_mode", new String[]{"MyAjax"});
    map.put("skin", new String[]{"plainpagetype"});
    assertEquals(3, plugin.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_ajax_skin_lang() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[]{"My Name"});
    map.put("firstname", new String[]{"My First Name"});
    map.put("xpage", new String[]{"celements_ajax", "underlay"});
    map.put("ajax_mode", new String[]{"MyAjax"});
    map.put("skin", new String[]{"plainpagetype"});
    map.put("language", new String[]{"de"});
    assertEquals(4, plugin.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_ajax_skin_lang_xredirect() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[]{"My Name"});
    map.put("firstname", new String[]{"My First Name"});
    map.put("xpage", new String[]{"celements_ajax", "underlay"});
    map.put("ajax_mode", new String[]{"MyAjax"});
    map.put("skin", new String[]{"plainpagetype"});
    map.put("language", new String[]{"de"});
    map.put("xredirect", new String[]{"/My/Document"});
    assertEquals(5, plugin.getIsFilledModifier(map, null));
  }
  
  @Test
  public void testArrayContains_false() {
    String[] array = {"overlay", "underlay", "middle"};
    assertFalse(plugin.arrayContains(array, "notavailable"));
    assertFalse(plugin.arrayContains(array, "overlayunderlay"));
  }
  
  @Test
  public void testArrayContains_true() {
    String[] array = {"overlay", "underlay", "middle"};
    assertTrue(plugin.arrayContains(array, "overlay"));
    assertTrue(plugin.arrayContains(array, "underlay"));
    assertTrue(plugin.arrayContains(array, "middle"));
  }

  @Test
  public void testGetPrepareVelocityContextService() {
   assertNotNull(plugin.getPrepareVelocityContextService()); 
  }
  
  @Test
  public void testGetPossibleLogins_none() {
    expect(xwiki.getXWikiPreference(eq("cellogin"), same(getContext()))).andReturn(null
        ).anyTimes();
    replay(xwiki);
    assertEquals("loginname", plugin.getPossibleLogins(getContext()));
    verify(xwiki);
  }
  
  @Test
  public void testGetPossibleLogins_local() {
    expect(xwiki.getXWikiPreference(eq("cellogin"), same(getContext()))).andReturn("a,b"
        ).once();
    replay(xwiki);
    assertEquals("a,b", plugin.getPossibleLogins(getContext()));
    verify(xwiki);
  }
  
  @Test
  public void testGetPossibleLogins_mainWiki() {
    expect(xwiki.getXWikiPreference(eq("cellogin"), same(getContext()))).andReturn("   "
        ).once();
    expect(xwiki.getXWikiPreference(eq("cellogin"), same(getContext()))).andReturn("x,y"
        ).once();
    replay(xwiki);
    assertEquals("x,y", plugin.getPossibleLogins(getContext()));
    verify(xwiki);
  }

}
