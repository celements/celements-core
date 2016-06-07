package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.css.ICssExtensionRole;
import com.celements.web.css.CSS;
import com.celements.web.css.CSSString;

public class CssCommandTest extends AbstractBridgedComponentTestCase {

  private CssCommand cssCommand;

  @Before
  public void setUp_CssCommandTest() throws Exception {
    cssCommand = new CssCommand();
  }

  @Test
  public void test_includeApplicationDefaultCSS_emptyList() {
    replayDefault();
    List<CSS> cssList = cssCommand.includeApplicationDefaultCSS();
    assertNotNull(cssList);
    verifyDefault();
  }

  @Test
  public void test_includeApplicationDefaultCSS_registerMockComponent_emptyList() throws Exception {
    ICssExtensionRole testCssExtMock = registerComponentMock(ICssExtensionRole.class, "testCssExt");
    expect(testCssExtMock.getCssList()).andReturn(Collections.<CSS>emptyList()).once();
    replayDefault();
    List<CSS> cssList = cssCommand.includeApplicationDefaultCSS();
    assertNotNull(cssList);
    verifyDefault();
  }

  @Test
  public void test_includeApplicationDefaultCSS_registerMockComponent_oneElem() throws Exception {
    ICssExtensionRole testCssExtMock = registerComponentMock(ICssExtensionRole.class, "testCssExt");
    expect(testCssExtMock.getCssList()).andReturn(Arrays.<CSS>asList(new CSSString(
        ":celRes/test.css", getContext()))).once();
    replayDefault();
    List<CSS> cssList = cssCommand.includeApplicationDefaultCSS();
    assertNotNull(cssList);
    verifyDefault();
  }

}
