package com.celements.javascript;

import static org.junit.Assert.*;

import java.util.Objects;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class ExtJsFileParameterTest {

  private ExtJsFileParameter testExtJsFileParam;

  @Before
  public void setUp_ExtJsFileParameterTest() throws Exception {
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

}
