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

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class CelementsRightsCommandTest extends AbstractBridgedComponentTestCase {
  
  CelementsRightsCommand celRightsCmd = null;
  private XWikiContext context;
  private XWiki xwiki;
  
  @Before
  public void setUp_CelementsRightsCommandTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    celRightsCmd = new CelementsRightsCommand();
  }
  
  @Test
  public void testIsValidGroups_oneGroup() {
    BaseObject rightsObj = new BaseObject();
    String groups = "XWiki.Firstgroup";
    rightsObj.setLargeStringValue("groups", groups);
    assertTrue(celRightsCmd.isValidGroups(rightsObj));
  }
  
  public void testIsValidGroups_manyGroups() {
    BaseObject rightsObj = new BaseObject();
    String groups = "XWiki.Firstgroup,XWiki.Secondgroup";
    rightsObj.setLargeStringValue("groups", groups);
    assertFalse(celRightsCmd.isValidGroups(rightsObj));
  }
  
  public void testIsValidGroups_noGroups() {
    BaseObject rightsObj = new BaseObject();
    String groups = "";
    rightsObj.setLargeStringValue("groups", groups);
    assertTrue(celRightsCmd.isValidGroups(rightsObj));
  }
  
  @Test
  public void testIsValidUsers_noUser() {
    BaseObject rightsObj = new BaseObject();
    String users = "";
    rightsObj.setLargeStringValue("users", users);
    assertTrue(celRightsCmd.isValidUsers(rightsObj));
  }
  
  @Test
  public void testIsValidUsers_anyUser() {
    BaseObject rightsObj = new BaseObject();
    String users = "XWiki.SomeUser";
    rightsObj.setLargeStringValue("users", users);
    assertFalse(celRightsCmd.isValidUsers(rightsObj));
  }
  
  @Test
  public void testIsValidUsers_guestUser() {
    BaseObject rightsObj = new BaseObject();
    String users = "XWiki.XWikiGuest";
    rightsObj.setLargeStringValue("users", users);
    assertTrue(celRightsCmd.isValidUsers(rightsObj));
  }
  
  @Test
  public void testIsValidUsers_manyUsers() {
    BaseObject rightsObj = new BaseObject();
    String users = "XWiki.XWikiGuest,XWiki.SomeUser";
    rightsObj.setLargeStringValue("users", users);
    assertFalse(celRightsCmd.isValidUsers(rightsObj));
  }
  
  @Test
  public void testIsValidLevels_view() {
    BaseObject rightsObj = new BaseObject();
    String levels = "view";
    rightsObj.setLargeStringValue("levels", levels);
    assertTrue(celRightsCmd.isValidLevels(rightsObj));
  }
  
  @Test
  public void testIsValidLevels_edit() {
    BaseObject rightsObj = new BaseObject();
    String levels = "delete,view,undelete,edit";
    rightsObj.setLargeStringValue("levels", levels);
    assertTrue(celRightsCmd.isValidLevels(rightsObj));
  }
  
  @Test
  public void testIsValidLevels_invalid_2levels() {
    BaseObject rightsObj = new BaseObject();
    String levels = "view,edit";
    rightsObj.setLargeStringValue("levels", levels);
    assertFalse(celRightsCmd.isValidLevels(rightsObj));
  }
  
  @Test
  public void testIsValidLevels_invalid_wronglevel() {
    BaseObject rightsObj = new BaseObject();
    String levels = "edit";
    rightsObj.setLargeStringValue("levels", levels);
    assertFalse(celRightsCmd.isValidLevels(rightsObj));
  }
  
  @Test
  public void testIsValidLevels_invalid_wronglevels() {
    BaseObject rightsObj = new BaseObject();
    String levels = "comment,view,undelete,edit";
    rightsObj.setLargeStringValue("levels", levels);
    assertFalse(celRightsCmd.isValidLevels(rightsObj));
  }

  @Test
  public void testIsCelementsRights_invalidUser() throws XWikiException {
    BaseObject rightsObj = new BaseObject();
    String users = "XWiki.SomeUser";
    rightsObj.setLargeStringValue("users", users);
    String fullName = "MySpace.TestDoc";
    XWikiDocument testDoc = new XWikiDocument();
    testDoc.setFullName(fullName);
    testDoc.setObject("XWiki.XWikiRights", 0, rightsObj);
    expect(xwiki.getDocument(eq(fullName), same(context))).andReturn(testDoc).once();
    replay(xwiki);
    assertFalse(celRightsCmd.isCelementsRights(fullName, context));
    verify(xwiki);
  }

  @Test
  public void testIsCelementsRights_noObjects() throws XWikiException {
    String fullName = "MySpace.TestDoc";
    XWikiDocument testDoc = new XWikiDocument();
    testDoc.setFullName(fullName);
    expect(xwiki.getDocument(eq(fullName), same(context))).andReturn(testDoc).once();
    replay(xwiki);
    assertTrue(celRightsCmd.isCelementsRights(fullName, context));
    verify(xwiki);
  }

  @Test
  public void testIsCelementsRights_oneValidUser_validLevel() throws XWikiException {
    BaseObject rightsObj = new BaseObject();
    String users = "XWiki.XWikiGuest";
    rightsObj.setLargeStringValue("users", users);
    String levels = "view";
    rightsObj.setLargeStringValue("levels", levels);
    String fullName = "MySpace.TestDoc";
    XWikiDocument testDoc = new XWikiDocument();
    testDoc.setFullName(fullName);
    testDoc.setObject("XWiki.XWikiRights", 0, rightsObj);
    expect(xwiki.getDocument(eq(fullName), same(context))).andReturn(testDoc).once();
    replay(xwiki);
    assertTrue(celRightsCmd.isCelementsRights(fullName, context));
    verify(xwiki);
  }

  @Test
  public void testIsCelementsRights_noValidUser_validGroup_validLevel() throws XWikiException {
    BaseObject rightsObj = new BaseObject();
    String groups = "XWiki.Firstgroup";
    rightsObj.setLargeStringValue("groups", groups);
    String levels = "view";
    rightsObj.setLargeStringValue("levels", levels);
    String fullName = "MySpace.TestDoc";
    XWikiDocument testDoc = new XWikiDocument();
    testDoc.setFullName(fullName);
    testDoc.setObject("XWiki.XWikiRights", 0, rightsObj);
    expect(xwiki.getDocument(eq(fullName), same(context))).andReturn(testDoc).once();
    replay(xwiki);
    assertTrue(celRightsCmd.isCelementsRights(fullName, context));
    verify(xwiki);
  }
  
  @Test
  public void testIsCelementsRights_emptyLevel() throws XWikiException {
    BaseObject rightsObj = new BaseObject();
    String groups = "XWiki.Firstgroup";
    rightsObj.setLargeStringValue("groups", groups);
    String levels = "";
    rightsObj.setLargeStringValue("levels", levels);
    String fullName = "MySpace.TestDoc";
    XWikiDocument testDoc = new XWikiDocument();
    testDoc.setFullName(fullName);
    testDoc.setObject("XWiki.XWikiRights", 0, rightsObj);
    expect(xwiki.getDocument(eq(fullName), same(context))).andReturn(testDoc).once();
    replay(xwiki);
    assertTrue(celRightsCmd.isCelementsRights(fullName, context));
    verify(xwiki);
  }

}
