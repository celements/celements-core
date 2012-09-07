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

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.PageTypeCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class RTEConfigTest extends AbstractBridgedComponentTestCase {
  XWikiContext context;
  RTEConfig config;
  PageTypeCommand pagetype;
  private XWiki wiki;
  
  @Before
  public void setUp_RTEConfigTest() throws Exception {
    pagetype = createMock(PageTypeCommand.class);
    context = getContext();
    wiki = createMock(XWiki.class);
    context.setWiki(wiki);
    config = RTEConfig.getInstance(context);
    config.injectPageTypeInstance(pagetype);
  }

  @Test
  public void testGetRTEConfigField_page() throws XWikiException {
    String objValue = "style=test";
    BaseObject obj = new BaseObject();
    obj.setStringValue("styles", objValue);
    XWikiDocument doc = new XWikiDocument();
    doc.addObject(RTEConfig.PROP_CLASS_NAME, obj);
    context.setDoc(doc);
    assertEquals(objValue, config.getRTEConfigField("styles", context));
  }
  
  @Test
  public void testGetRTEConfigField_pageType() throws XWikiException {
    //Doc
    String pageTypeName= "testspace.testpagetype";
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setStringValue("page_type", pageTypeName);
    XWikiDocument doc = new XWikiDocument();
    doc.addObject("Celements2.PageTypeClass", pageTypeObj);
    context.setDoc(doc);
    //PageType
    String objValue = "style=pagetypetest";
    BaseObject obj = new BaseObject();
    obj.setStringValue("styles", objValue);
    XWikiDocument pageTypeXWikiDoc = new XWikiDocument();
    pageTypeXWikiDoc.setFullName(pageTypeName);
    pageTypeXWikiDoc.addObject(RTEConfig.PROP_CLASS_NAME, obj);
    context.put("pageTypeApi", pagetype);
    expect(wiki.getDocument(eq(pageTypeName), same(context))).andReturn(
        pageTypeXWikiDoc).once();
    expect(wiki.exists(eq(pageTypeName), same(context))).andReturn(true);
    expect(pagetype.getPageTypeDocFN(same(doc), same(context))).andReturn(pageTypeName);
    replay(wiki, pagetype);
    assertEquals(objValue, config.getRTEConfigField("styles", context));
    verify(wiki, pagetype);
  }

  @Test
  public void testGetRTEConfigField_webPreference_noPagetype_obj() throws XWikiException {
    //Doc
    XWikiDocument doc = new XWikiDocument("Test", "ExDoc");
    context.setDoc(doc);
    //WebPreferences
    String objValue = "style=webPrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setStringValue("styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument("Test", "WebPreferences");
    prefDoc.addObject(RTEConfig.PROP_CLASS_NAME, obj);
    expect(wiki.getDocument(eq("Test.WebPreferences"), same(context))).andReturn(
        prefDoc).once();
    replay(wiki);
    assertEquals(objValue, config.getRTEConfigField("styles", context));
    verify(wiki);
  }
  
  @Test
  public void testGetRTEConfigField_webPreference_hasPagetype_obj(
      ) throws XWikiException {
    //Doc
    String pageTypeName= "testpagetype";
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setStringValue("page_type", pageTypeName);
    XWikiDocument doc = new XWikiDocument("TestSpace", "TestDoc");
    doc.addObject("Celements2.PageTypeClass", pageTypeObj);
    context.setDoc(doc);
    XWikiDocument pageTypeXWikiDoc = new XWikiDocument();
    String pageTypeFN = "PageTypes." + pageTypeName;
    pageTypeXWikiDoc.setFullName(pageTypeFN);
    expect(wiki.getDocument(eq(pageTypeFN), same(context))).andReturn(
        pageTypeXWikiDoc).once();
    expect(wiki.exists(eq(pageTypeFN), same(context))).andReturn(true);
    expect(pagetype.getPageTypeDocFN(same(doc), same(context))).andReturn(pageTypeFN);
    //WebPreferences
    String objValue = "style=webPrefObjEmptyPageTypeTest";
    BaseObject obj = new BaseObject();
    obj.setStringValue("styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument("TestSpace", "WebPreferences");
    prefDoc.addObject(RTEConfig.PROP_CLASS_NAME, obj);
    expect(wiki.getDocument(eq("TestSpace.WebPreferences"), same(context))
        ).andReturn(prefDoc).once();
    replay(wiki, pagetype);
    assertEquals(objValue, config.getRTEConfigField("styles", context));
    verify(wiki, pagetype);
  }
  
  @Test
  public void testGetRTEConfigField_webPreferenceObj() throws XWikiException {
    //Doc
    XWikiDocument doc = new XWikiDocument("Test", "Document");
    context.setDoc(doc);
    //WebPreferences
    String objValue = "style=webPref_PrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setStringValue("rte_styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument("Test", "WebPreferences");
    prefDoc.addObject("XWiki.XWikiPreferences", obj);
    expect(wiki.getDocument(eq("Test.WebPreferences"), same(context))).andReturn(
        prefDoc).once();
    replay(wiki);
    assertEquals(objValue, config.getRTEConfigField("styles", context));
    verify(wiki);
  }
  
  @Test
  public void testGetRTEConfigField_xwikiPreference_obj() throws XWikiException {
    //Doc
    XWikiDocument doc = new XWikiDocument("Test", "Document");
    context.setDoc(doc);
    //WebPreferences
    XWikiDocument webPrefDoc = new XWikiDocument("Test", "WebPreferences");
    expect(wiki.getDocument(eq("Test.WebPreferences"), same(context))).andReturn(webPrefDoc).once();
    //XWikiPreferences
    String objValue = "style=xwikiPrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setStringValue("styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument("XWiki", "XWikiPreferences");
    prefDoc.addObject(RTEConfig.PROP_CLASS_NAME, obj);
    expect(wiki.getDocument(eq("XWiki.XWikiPreferences"), same(context))).andReturn(prefDoc).once();
    replay(wiki);
    assertEquals(objValue, config.getRTEConfigField("styles", context));
    verify(wiki);
  }
  
  @Test
  public void testGetRTEConfigField_xwikiPreferenceObj() throws XWikiException {
    //Doc
    XWikiDocument doc = new XWikiDocument("Test", "Document");
    context.setDoc(doc);
    //WebPreferences
    XWikiDocument webPrefDoc = new XWikiDocument("Test", "WebPreferences");
    expect(wiki.getDocument(eq("Test.WebPreferences"), same(context))).andReturn(webPrefDoc).once();
    //XWikiPreferences
    String objValue = "style=xwikiPref_PrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setStringValue("rte_styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument("XWiki", "XWikiPreferences");
    prefDoc.addObject("XWiki.XWikiPreferences", obj);
    expect(wiki.getDocument(eq("XWiki.XWikiPreferences"), same(context))).andReturn(prefDoc).once();
    replay(wiki);
    assertEquals(objValue, config.getRTEConfigField("styles", context));
    verify(wiki);
  }
  
  @Test
  public void testGetPreferenceFromConfigObject() throws XWikiException {
    String confDocName = "confdocname";
    BaseObject obj = new BaseObject();
    obj.setStringValue(RTEConfig.CONFIG_PROP_NAME, confDocName);
    XWikiDocument doc = new XWikiDocument();
    doc.addObject(RTEConfig.CONFIG_CLASS_NAME, obj);
    
    BaseObject confObj = new BaseObject();
    confObj.setStringValue("testprop", "testvalue");
    XWikiDocument confDoc = new XWikiDocument();
    confDoc.addObject(RTEConfig.PROP_CLASS_NAME, confObj);
    expect(wiki.getDocument(eq(confDocName), same(context))).andReturn(confDoc).once();
    replay(wiki);
    assertEquals("testvalue", config.getPreferenceFromConfigObject("testprop", doc, context));
    verify(wiki);
  }
  
  @Test
  public void testGetPreferenceFromPreferenceObject_noObj() {
    XWikiDocument doc = new XWikiDocument();
    assertEquals("", config.getPreferenceFromPreferenceObject("testprop", "Classes.ClassName", doc));
  }
  
  @Test
  public void testGetPreferenceFromPreferenceObject() {
    BaseObject obj = new BaseObject();
    obj.setStringValue("testprop", "testvalue");
    XWikiDocument doc = new XWikiDocument();
    doc.addObject("Classes.ClassName", obj);
    assertEquals("testvalue", config.getPreferenceFromPreferenceObject("testprop", "Classes.ClassName", doc));
  }

}
