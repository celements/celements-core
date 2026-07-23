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
package com.celements.web.css;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.javascript.FrontendResourceResolver;
import com.celements.javascript.FrontendResourceResolver.FrontendResource;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

public class CSSEngineTest extends AbstractComponentTest {

  private CSSEngine cssEngine;
  private XWikiContext context;
  private XWiki wiki;

  @Before
  public void setUp_CSSEngineTest() throws Exception {
    registerComponentMocks(FrontendResourceResolver.class);
    context = getXContext();
    wiki = getMock(XWiki.class);
    cssEngine = getBeanFactory().getBean(CSSEngine.class);
    context.put("vcontext", new VelocityContext());
  }

  @Test
  public void testIncludeCSS_nullObject() {
    List<BaseObject> baseCSSList = new ArrayList<>();
    BaseObject cssObj = new BaseObject();
    baseCSSList.add(cssObj);
    baseCSSList.add(null);
    BaseObject cssObj1 = new BaseObject();
    baseCSSList.add(cssObj1);

    List<CSS> cssList = cssEngine.includeCSS("", "", baseCSSList, context);

    assertFalse("includeCSS must not add null objects to the css list.",
        cssListContains(cssList, null));
    assertTrue("includeCSS must add cssObj to the css list.", cssListContains(cssList, cssObj));
    assertTrue("includeCSS must not add cssObj1 to the css list.",
        cssListContains(cssList, cssObj1));
  }

  @Test
  public void testIncludeCSS_frontend() {
    String sourcePath = ":frontend/progon/vue-poc/main.ts";
    expect(getMock(FrontendResourceResolver.class).isFrontendSource(eq(sourcePath)))
        .andReturn(true);
    expect(getMock(FrontendResourceResolver.class).get(eq(sourcePath)))
        .andReturn(Optional.of(new FrontendResource("dist/vue-poc.BOsmCSyo.mjs",
            Arrays.asList("dist/assets/vue-poc-ahBOTvOT.css", "dist/assets/shared.Df9z3kSS.css"))));

    replayDefault();

    List<CSS> cssList = cssEngine.includeCSS(sourcePath, "css", null, context);

    assertEquals(2, cssList.size());
    assertEquals("dist/assets/vue-poc-ahBOTvOT.css", cssList.get(0).getCssBasePath());
    assertEquals("dist/assets/shared.Df9z3kSS.css", cssList.get(1).getCssBasePath());
    verifyDefault();
  }

  @Test
  public void testIncludeCSS_frontend_getCSS() {
    String sourcePath = ":frontend/progon/vue-poc/main.ts";
    expect(getMock(FrontendResourceResolver.class).isFrontendSource(eq(sourcePath)))
        .andReturn(true);
    expect(getMock(FrontendResourceResolver.class).get(eq(sourcePath)))
        .andReturn(Optional.of(new FrontendResource("dist/vue-poc.BOsmCSyo.mjs",
            Collections.singletonList("dist/assets/vue-poc-ahBOTvOT.css"))));
    expect(wiki.getSkinFile(eq("dist/assets/vue-poc-ahBOTvOT.css"), eq(true), same(context)))
        .andReturn("/appname/skin/resources/dist/assets/vue-poc-ahBOTvOT.css");

    replayDefault();

    List<CSS> cssList = cssEngine.includeCSS(sourcePath, "css", null, context);

    assertEquals("/appname/file/resources/dist/assets/vue-poc-ahBOTvOT.css",
        cssList.get(0).getCSS(context));
    verifyDefault();
  }

  @Test
  public void testIncludeCSS_frontend_noCss() {
    String sourcePath = ":frontend/progon/eventview/main.ts";
    expect(getMock(FrontendResourceResolver.class).isFrontendSource(eq(sourcePath)))
        .andReturn(true);
    expect(getMock(FrontendResourceResolver.class).get(eq(sourcePath))).andReturn(
        Optional.of(new FrontendResource("dist/eventview.Cq6C1_9z.mjs", Collections.emptyList())));

    replayDefault();

    List<CSS> cssList = cssEngine.includeCSS(sourcePath, "css", null, context);

    assertTrue(cssList.isEmpty());
    verifyDefault();
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private boolean cssListContains(List<CSS> cssList, BaseObject containsObj) {
    boolean found = false;
    for (CSS cssObj : cssList) {
      if (cssObj instanceof CSSBaseObject) {
        BaseObject foundObj = ((CSSBaseObject) cssObj).getObject();
        if (containsObj == foundObj) {
          found = true;
        }
      }
    }
    return found;
  }

}
