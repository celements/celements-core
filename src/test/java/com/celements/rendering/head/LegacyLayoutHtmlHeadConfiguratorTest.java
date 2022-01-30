package com.celements.rendering.head;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.javascript.ExtJsFileParameter;
import com.xpn.xwiki.web.Utils;

public class LegacyLayoutHtmlHeadConfiguratorTest extends AbstractComponentTest {

  private LegacyLayoutHtmlHeadConfigurator configurator;

  @Before
  public void LegacyLayoutHtmlHeadConfiguratorTest_setUp() throws Exception {
    configurator = (LegacyLayoutHtmlHeadConfigurator) Utils.getComponent(
        HtmlHeadConfiguratorRole.class,
        LegacyLayoutHtmlHeadConfigurator.NAME);
  }

  @Test
  public void test_getAllInitialJavaScriptFiles() {
    expect(getWikiMock().getXWikiPreferenceAsInt(eq("cel_disable_swfobject"), eq(0),
        same(getContext()))).andReturn(0);
    ExtJsFileParameter initCelements = new ExtJsFileParameter.Builder()
        .setJsFile(":celJS/initCelements.min.js").setAction("file").build();
    replayDefault();
    List<ExtJsFileParameter> resultList = configurator.getAllInitialJavaScriptFiles();
    assertNotNull(resultList);
    assertTrue("expecting initCelements.js", resultList.contains(initCelements));
    verifyDefault();
  }

}
