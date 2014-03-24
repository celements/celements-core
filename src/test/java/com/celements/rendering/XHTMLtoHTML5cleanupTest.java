package com.celements.rendering;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class XHTMLtoHTML5cleanupTest extends AbstractBridgedComponentTestCase {
  XHTMLtoHTML5cleanup cleaner;
  
  @Before
  public void setUp() throws Exception {
    cleaner = new XHTMLtoHTML5cleanup();
  }

  @Test
  public void testRemoveSelfclosingTags_null() {
    assertNull(cleaner.removeSelfclosingTags(null));
  }

  @Test
  public void testRemoveSelfclosingTags_empty() {
    assertEquals("", cleaner.removeSelfclosingTags(""));
  }

  @Test
  public void testRemoveSelfclosingTags_nothingToClean() {
    String xhtml = "<p>Hi</p><br><select name=\"abc\"><option value=\"1\">1</option>" +
        "<option value=\"2\">2</option></select>";
    assertEquals(xhtml, cleaner.removeSelfclosingTags(xhtml));
  }

  @Test
  public void testRemoveSelfclosingTags_br_hr() {
    String xhtml = "<p>Hi</p><br /><hr/><select name=\"abc\"><option value=\"1\">1</option>" +
        "<option value=\"2\">2</option></select>";
    String html5 = "<p>Hi</p><br ><hr><select name=\"abc\"><option value=\"1\">1</option>" +
        "<option value=\"2\">2</option></select>";
    assertEquals(html5, cleaner.removeSelfclosingTags(xhtml));
  }

  @Test
  public void testRemoveSelfclosingTags_input() {
    String xhtml = "<p>Hi</p><input type\"text\" name=\"name\" value=\"John\" />";
    String html5 = "<p>Hi</p><input type\"text\" name=\"name\" value=\"John\" >";
    assertEquals(html5, cleaner.removeSelfclosingTags(xhtml));
  }

  @Test
  public void testRemoveSelfclosingTags_img() {
    String xhtml = "<p>Hi</p><img src=\"test.jpg\" alt=\"describing /> problems\"/>";
    String html5 = "<p>Hi</p><img src=\"test.jpg\" alt=\"describing /> problems\">";
    assertEquals(html5, cleaner.removeSelfclosingTags(xhtml));
  }

  @Test
  public void testRemoveSelfclosingTags_link() {
    String xhtml = "<head><link rel=\"stylesheet\" media=\"mobile\" " +
        "type=\"text/css\" href=\"styles.css\" /></head>";
    String html5 = "<head><link rel=\"stylesheet\" media=\"mobile\" " +
        "type=\"text/css\" href=\"styles.css\" ></head>";
    assertEquals(html5, cleaner.removeSelfclosingTags(xhtml));
  }

  @Test
  public void testRemoveSelfclosingTags_span_div_p() {
    String xhtml = "<p>Hi</p><span/><p>there </p><div class=\"abc\" /><p />";
    String html5 = "<p>Hi</p><span><p>there </p><div class=\"abc\" ><p >";
    assertEquals(html5, cleaner.removeSelfclosingTags(xhtml));
  }

  @Test
  public void testRemoveSelfclosingTags_base() {
    String xhtml = "<base href=\"http://www.abc.ch\" /><p>Hi</p>";
    String html5 = "<base href=\"http://www.abc.ch\" ><p>Hi</p>";
    assertEquals(html5, cleaner.removeSelfclosingTags(xhtml));
  }

  @Test
  public void testRemoveSelfclosingTags_meta() {
    String xhtml = "<meta http-equiv=\"Content-Type\" content=\"text/html; " +
        "charset=UTF-8\" />";
    String html5 = "<meta http-equiv=\"Content-Type\" content=\"text/html; " +
        "charset=UTF-8\" >";
    assertEquals(html5, cleaner.removeSelfclosingTags(xhtml));
  }
}
