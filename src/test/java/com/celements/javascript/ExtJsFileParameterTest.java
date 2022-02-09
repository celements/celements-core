package com.celements.javascript;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.filebase.references.FileReference;
import com.celements.filebase.uri.FileUriServiceRole;
import com.xpn.xwiki.XWikiContext;

public class ExtJsFileParameterTest extends AbstractComponentTest {

  private XWikiContext context;
  private FileUriServiceRole fileUriSrv;
  private ExtJsFileParameter testExtJsFileParam;

  @Before
  public void setUp_ExtJsFileParameterTest() throws Exception {
    context = getContext();
    context.put("vcontext", new VelocityContext());
    fileUriSrv = registerComponentMock(FileUriServiceRole.class);
    testExtJsFileParam = new ExtJsFileParameter.Builder()
        .setAction("file")
        .setLazyLoad(true)
        .setJsFileRef(":safdas/sfasf.js")
        .setLoadMode(JsLoadMode.DEFER)
        .setQueryString("asdf=oijasdf")
        .build();
  }

  @Test
  public void test_hashCode() {
    assertEquals(Objects.hash(testExtJsFileParam.getJsFileRef(), "asdf=oijasdf"),
        testExtJsFileParam.hashCode());
  }

  @Test
  public void test_getJsFile() {
    assertEquals(FileReference.of(":safdas/sfasf.js").build(), testExtJsFileParam.getJsFileRef());
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
        .setJsFileRef(":safdas/sfasf.js")
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
        .setJsFileRef(":safdas/sfasf.js")
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
        .setJsFileRef(":safdas/sfasf.js")
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
        .setJsFileRef(":safdas/sfasf.js")
        .setLoadMode(JsLoadMode.DEFER)
        .setQueryString("asdf=oijasdf")
        .build();
    assertEquals(testExtJsFileParam, testExtJsFileParam2);
  }

  @Test
  public void test_toString() {
    assertEquals("ExtJsFileParameter [action=file, jsFileUrl=FileReference [name=sfasf.js,"
        + " type=ON_DISK, docRef=null, fullPath=:safdas/sfasf.js], loadMode=DEFER,"
        + " queryString=asdf=oijasdf, lazyLoad=true]", testExtJsFileParam.toString());
  }

  @Test
  public void testAddLazyExtJSfile() throws Exception {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileUrl = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileUrl + "\", \"initLoad\" : true}";
    expect(fileUriSrv.createFileUri(eq(FileReference.of(jsFile).build()), eq(Optional.empty()),
        eq(Optional.empty()))).andReturn(UriBuilder.fromPath(jsFileUrl)).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        new ExtJsFileParameter.Builder()
            .setJsFileRef(jsFile)
            .build().getLazyLoadTag());
    verifyDefault();
  }

  @Test
  public void testAddLazyExtJSfile_action() throws Exception {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileURL = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js";
    String action = "file";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileURL + "\", \"initLoad\" : true}";
    expect(fileUriSrv.createFileUri(eq(FileReference.of(jsFile).build()), eq(Optional.of(action)),
        eq(Optional.empty()))).andReturn(UriBuilder.fromPath(jsFileURL)).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        new ExtJsFileParameter.Builder()
            .setJsFileRef(jsFile)
            .setAction(action)
            .build().getLazyLoadTag());
    verifyDefault();
  }

  @Test
  public void testAddLazyExtJSfile_action_params() throws Exception {
    String jsFile = "mySpace.myDoc;loadTinyMCE-async.js";
    String queryString = "me=blu";
    String baseFileUrl = "/download/mySpace/myDoc/loadTinyMCE-async.js";
    String jsFileURL = baseFileUrl + "?" + queryString;
    String action = "file";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileURL + "\", \"initLoad\" : true}";
    UriBuilder expectedUri = UriBuilder.fromPath(baseFileUrl)
        .replaceQuery(queryString);
    expect(fileUriSrv.createFileUri(eq(FileReference.of(jsFile).build()), eq(Optional.of(action)),
        eq(Optional.of(queryString)))).andReturn(expectedUri).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        new ExtJsFileParameter.Builder()
            .setJsFileRef(jsFile)
            .setAction(action)
            .setQueryString("me=blu")
            .build().getLazyLoadTag());
    verifyDefault();
  }

  @Test
  public void testAddLazyExtJSfile_action_params_onDisk() throws Exception {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String queryString = "me=blu";
    String versionStr = "version=201507061937";
    String params = versionStr + "&" + queryString;
    String baseFileUrl = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileURL = baseFileUrl + "?" + params;
    String action = "file";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileURL + "\", \"initLoad\" : true}";
    UriBuilder expectedUri = UriBuilder.fromPath(baseFileUrl)
        .replaceQuery(params);
    expect(fileUriSrv.createFileUri(eq(FileReference.of(jsFile).build()), eq(Optional.of(action)),
        eq(Optional.of(queryString)))).andReturn(expectedUri).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        new ExtJsFileParameter.Builder()
            .setJsFileRef(jsFile)
            .setAction(action)
            .setQueryString(queryString)
            .build().getLazyLoadTag());
    verifyDefault();
  }
}
