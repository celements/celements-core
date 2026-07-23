package com.celements.javascript;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.javascript.FrontendResourceResolver.FrontendResource;
import com.celements.model.access.IModelAccessFacade;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.web.css.CSS;
import com.celements.web.plugin.cmd.ExternalJavaScriptFilesCommand;
import com.xpn.xwiki.XWikiContext;

public class FrontendResourceIncludeCssListenerTest extends AbstractComponentTest {

  private FrontendResourceIncludeCssListener listener;
  private XWikiContext context;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(ExternalJavaScriptFilesCommand.class, FrontendResourceResolver.class,
        IModelAccessFacade.class, LayoutServiceRole.class);
    context = getXContext();
    context.put("vcontext", new VelocityContext());
    listener = getBeanFactory().getBean(FrontendResourceIncludeCssListener.class);
  }

  @Test
  public void test_beforeAllExtFinish_includeFrontendCss() {
    String sourcePath = ":frontend/progon/vue-poc/main.ts";
    expect(getMock(ExternalJavaScriptFilesCommand.class).streamExtJsFiles())
        .andReturn(Stream.of(sourcePath)).anyTimes();
    expect(getMock(FrontendResourceResolver.class).isFrontendSource(eq(sourcePath))).andReturn(true)
        .anyTimes();
    expect(getMock(FrontendResourceResolver.class).get(eq(sourcePath)))
        .andReturn(Optional.of(new FrontendResource("dist/vue-poc.BOsmCSyo.mjs",
            Collections.singletonList("dist/assets/vue-poc-ahBOTvOT.css"))));
    replayDefault();
    listener.beforeAllExtFinish(getMock(ExternalJavaScriptFilesCommand.class));
    verifyDefault();
    assertEquals(Collections.singletonList("dist/assets/vue-poc-ahBOTvOT.css"),
        getIncludedCssBasePaths());
  }

  @SuppressWarnings("unchecked")
  private List<String> getIncludedCssBasePaths() {
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    List<CSS> cssList = (List<CSS>) vcontext.get("cel_css_list_page");
    return cssList.stream().map(CSS::getCssBasePath).toList();
  }
}
