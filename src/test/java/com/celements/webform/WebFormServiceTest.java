package com.celements.webform;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class WebFormServiceTest extends AbstractBridgedComponentTestCase {

  private WebFormService webFormService;

  @Before
  public void setUp_CelementsWebPluginTest() throws Exception {
    webFormService = new WebFormService();
  }

  @Test
  public void testGetIsFilledModifier_oneParam() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("search", new String[] { "Search Term" });
    assertEquals(0, webFormService.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_oneParam_xredirect() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("search", new String[] { "Search Term" });
    map.put("xredirect", new String[] { "/My/Document" });
    assertEquals(1, webFormService.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_oneParam_xredirect_additionalSame() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("search", new String[] { "Search Term" });
    map.put("name", new String[] { "My Name" });
    map.put("firstname", new String[] { "My First Name" });
    map.put("xredirect", new String[] { "/My/Document" });
    Set<String> additional = new HashSet<String>();
    additional.add("xredirect");
    additional.add("overlay");
    assertEquals(1, webFormService.getIsFilledModifier(map, additional));
  }

  @Test
  public void testGetIsFilledModifier_oneParam_xredirect_additionalElse() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("search", new String[] { "Search Term" });
    map.put("name", new String[] { "My Name" });
    map.put("firstname", new String[] { "My First Name" });
    map.put("xredirect", new String[] { "/My/Document" });
    Set<String> additional = new HashSet<String>();
    additional.add("name");
    additional.add("overlay");
    assertEquals(2, webFormService.getIsFilledModifier(map, additional));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[] { "My Name" });
    map.put("firstname", new String[] { "My First Name" });
    map.put("language", new String[] { "de" });
    assertEquals(1, webFormService.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_xpage() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[] { "My Name" });
    map.put("firstname", new String[] { "My First Name" });
    map.put("xpage", new String[] { "celements_ajax", "underlay" });
    map.put("conf", new String[] { "OverlayConfig" });
    map.put("language", new String[] { "de" });
    assertEquals(2, webFormService.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_overlay() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[] { "My Name" });
    map.put("firstname", new String[] { "My First Name" });
    map.put("xpage", new String[] { "overlay", "underlay" });
    map.put("conf", new String[] { "OverlayConfig" });
    assertEquals(2, webFormService.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_overlay_lang() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[] { "My Name" });
    map.put("firstname", new String[] { "My First Name" });
    map.put("xpage", new String[] { "overlay", "underlay" });
    map.put("conf", new String[] { "OverlayConfig" });
    map.put("language", new String[] { "de" });
    assertEquals(3, webFormService.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_ajax() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[] { "My Name" });
    map.put("firstname", new String[] { "My First Name" });
    map.put("xpage", new String[] { "celements_ajax", "underlay" });
    map.put("ajax_mode", new String[] { "MyAjax" });
    assertEquals(2, webFormService.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_ajax_skin() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[] { "My Name" });
    map.put("firstname", new String[] { "My First Name" });
    map.put("xpage", new String[] { "celements_ajax", "underlay" });
    map.put("ajax_mode", new String[] { "MyAjax" });
    map.put("skin", new String[] { "plainpagetype" });
    assertEquals(3, webFormService.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_ajax_skin_lang() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[] { "My Name" });
    map.put("firstname", new String[] { "My First Name" });
    map.put("xpage", new String[] { "celements_ajax", "underlay" });
    map.put("ajax_mode", new String[] { "MyAjax" });
    map.put("skin", new String[] { "plainpagetype" });
    map.put("language", new String[] { "de" });
    assertEquals(4, webFormService.getIsFilledModifier(map, null));
  }

  @Test
  public void testGetIsFilledModifier_multipleParam_ajax_skin_lang_xredirect() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    map.put("name", new String[] { "My Name" });
    map.put("firstname", new String[] { "My First Name" });
    map.put("xpage", new String[] { "celements_ajax", "underlay" });
    map.put("ajax_mode", new String[] { "MyAjax" });
    map.put("skin", new String[] { "plainpagetype" });
    map.put("language", new String[] { "de" });
    map.put("xredirect", new String[] { "/My/Document" });
    assertEquals(5, webFormService.getIsFilledModifier(map, null));
  }

}
