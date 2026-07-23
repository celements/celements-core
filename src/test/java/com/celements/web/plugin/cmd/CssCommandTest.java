package com.celements.web.plugin.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.css.ICssExtensionRole;
import com.celements.javascript.FrontendResourceResolver;
import com.celements.model.access.IModelAccessFacade;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.web.css.CSS;
import com.celements.web.css.CSSString;

public class CssCommandTest extends AbstractComponentTest {

  private CssCommand cssCommand;

  @Before
  public void setUp_CssCommandTest() throws Exception {
    registerComponentMocks(FrontendResourceResolver.class, IModelAccessFacade.class,
        LayoutServiceRole.class);
  }

  @Test
  public void test_includeApplicationDefaultCSS_emptyList() {
    cssCommand = getBeanFactory().getBean(CssCommand.class);
    replayDefault();
    List<CSS> cssList = cssCommand.includeApplicationDefaultCSS();
    assertNotNull(cssList);
    verifyDefault();
  }

  @Test
  public void test_includeApplicationDefaultCSS_registerMockComponent_emptyList() throws Exception {
    ICssExtensionRole testCssExtMock = registerComponentMock(ICssExtensionRole.class, "testCssExt");
    expect(testCssExtMock.getCssList()).andReturn(Collections.<CSS>emptyList()).once();
    cssCommand = getBeanFactory().getBean(CssCommand.class);
    replayDefault();
    List<CSS> cssList = cssCommand.includeApplicationDefaultCSS();
    assertNotNull(cssList);
    verifyDefault();
  }

  @Test
  public void test_includeApplicationDefaultCSS_registerMockComponent_oneElem() throws Exception {
    ICssExtensionRole testCssExtMock = registerComponentMock(ICssExtensionRole.class, "testCssExt");
    expect(testCssExtMock.getCssList())
        .andReturn(Arrays.<CSS>asList(new CSSString(":celRes/test.css", getContext()))).once();
    cssCommand = getBeanFactory().getBean(CssCommand.class);
    replayDefault();
    List<CSS> cssList = cssCommand.includeApplicationDefaultCSS();
    assertNotNull(cssList);
    verifyDefault();
  }

}
