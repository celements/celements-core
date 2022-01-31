package com.celements.javascript;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Objects;
import java.util.Optional;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.xpn.xwiki.XWikiContext;

public class ExtJsFileParameterTest extends AbstractComponentTest {

  private ExtJsFileParameter testExtJsFileParam;
  private AttachmentURLCommand attUrlCmd = null;
  private XWikiContext context;

  @Before
  public void setUp_ExtJsFileParameterTest() throws Exception {
    context = getContext();
    context.put("vcontext", new VelocityContext());
    attUrlCmd = createMockAndAddToDefault(AttachmentURLCommand.class);
    testExtJsFileParam = new ExtJsFileParameter.Builder()
        .setAction("file")
        .setLazyLoad(true)
        .setJsFile(":safdas/sfasf.js")
        .setLoadMode(JsLoadMode.DEFER)
        .setQueryString("asdf=oijasdf")
        .build();
  }

  @Test
  public void test_hashCode() {
    assertEquals(Objects.hash(testExtJsFileParam.getJsFileEntry(), "asdf=oijasdf", true),
        testExtJsFileParam.hashCode());
  }

  @Test
  public void test_getJsFileEntry() {
    JsFileEntry testJsFileEntry = new JsFileEntry();
    testJsFileEntry.setFilepath(":safdas/sfasf.js");
    assertEquals(testJsFileEntry, testExtJsFileParam.getJsFileEntry());
    assertNotSame(testExtJsFileParam.getJsFileEntry(), testExtJsFileParam.getJsFileEntry());
  }

  @Test
  public void test_getJsFile() {
    assertEquals(":safdas/sfasf.js", testExtJsFileParam.getJsFile());
  }

  @Test
  public void test_getLoadMode() {
    assertEquals(JsLoadMode.DEFER, testExtJsFileParam.getLoadMode());
  }

  @Test
  public void test_getAction() {
    Optional<String> action = testExtJsFileParam.getAction();
    assertTrue(action.isPresent());
    assertEquals("file", action.get());
  }

  @Test
  public void test_getAction_absent() {
    ExtJsFileParameter testExtJsFileParam2 = new ExtJsFileParameter.Builder()
        .setLazyLoad(true)
        .setJsFile(":safdas/sfasf.js")
        .setLoadMode(JsLoadMode.DEFER)
        .build();
    assertFalse(testExtJsFileParam2.getAction().isPresent());
  }

  @Test
  public void test_getQueryString() {
    Optional<String> queryString = testExtJsFileParam.getQueryString();
    assertTrue(queryString.isPresent());
    assertEquals("asdf=oijasdf", queryString.get());
  }

  @Test
  public void test_getQueryString_absent() {
    ExtJsFileParameter testExtJsFileParam2 = new ExtJsFileParameter.Builder()
        .setLazyLoad(true)
        .setJsFile(":safdas/sfasf.js")
        .setLoadMode(JsLoadMode.DEFER)
        .build();
    assertFalse(testExtJsFileParam2.getQueryString().isPresent());
  }

  @Test
  public void test_isLazyLoad() {
    assertTrue(testExtJsFileParam.isLazyLoad());
  }

  @Test
  public void test_equalsObject_sameAction() {
    ExtJsFileParameter testExtJsFileParam2 = new ExtJsFileParameter.Builder()
        .setAction("file")
        .setLazyLoad(true)
        .setJsFile(":safdas/sfasf.js")
        .setLoadMode(JsLoadMode.DEFER)
        .setQueryString("asdf=oijasdf")
        .build();
    assertEquals(testExtJsFileParam, testExtJsFileParam2);
  }

  @Test
  public void test_equalsObject_diffAction() {
    ExtJsFileParameter testExtJsFileParam2 = new ExtJsFileParameter.Builder()
        .setAction("test")
        .setLazyLoad(true)
        .setJsFile(":safdas/sfasf.js")
        .setLoadMode(JsLoadMode.DEFER)
        .setQueryString("asdf=oijasdf")
        .build();
    assertEquals(testExtJsFileParam, testExtJsFileParam2);
  }

  @Test
  public void test_toString() {
    assertEquals("ExtJsFileParameter [action=file, jsFileEntry=JsFileEntry"
        + " [jsFileUrl=:safdas/sfasf.js, loadMode=DEFER,"
        + " ObjectBean [documentReference=null, classReference=null, number=null]],"
        + " queryString=asdf=oijasdf, lazyLoad=true]", testExtJsFileParam.toString());
  }

  @Test
  public void testAddLazyExtJSfile() {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileURL = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileURL + "\", \"initLoad\" : true}";
    expect(attUrlCmd.getAttachmentURL(eq(jsFile), same(context))).andReturn(jsFileURL).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        new ExtJsFileParameter.Builder()
            .setJsFile(jsFile)
            .build().getLazyLoadTag(attUrlCmd));
    verifyDefault();
  }

  @Test
  public void testAddLazyExtJSfile_action() {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileURL = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js";
    String action = "file";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileURL + "\", \"initLoad\" : true}";
    expect(attUrlCmd.getAttachmentURL(eq(jsFile), eq(action), same(context))).andReturn(
        jsFileURL).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        new ExtJsFileParameter.Builder()
            .setJsFile(jsFile)
            .setAction(action)
            .build().getLazyLoadTag(attUrlCmd));
    verifyDefault();
  }

  @Test
  public void testAddLazyExtJSfile_action_params() {
    String jsFile = "mySpace.myDoc;loadTinyMCE-async.js";
    String jsFileURL = "/download/mySpace/myDoc/loadTinyMCE-async.js";
    String action = "file";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileURL + "?me=blu\", \"initLoad\" : true}";
    expect(attUrlCmd.getAttachmentURL(eq(jsFile), eq(action), same(context))).andReturn(
        jsFileURL).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        new ExtJsFileParameter.Builder()
            .setJsFile(jsFile)
            .setAction(action)
            .setQueryString("me=blu")
            .build().getLazyLoadTag(attUrlCmd));
    verifyDefault();
  }

  @Test
  public void testAddLazyExtJSfile_action_params_onDisk() {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileURL = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js"
        + "?version=201507061937";
    String action = "file";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileURL + "&me=blu\", \"initLoad\" : true}";
    expect(attUrlCmd.getAttachmentURL(eq(jsFile), eq(action), same(context))).andReturn(
        jsFileURL).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        new ExtJsFileParameter.Builder()
            .setJsFile(jsFile)
            .setAction(action)
            .setQueryString("me=blu")
            .build().getLazyLoadTag(attUrlCmd));
    verifyDefault();
  }
}
