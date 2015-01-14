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

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.utils.IWebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Attachment;

public class CSSTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private CSSMock css;
  private CSS cssMock;

  @Before
  public void setUp_CSSTest() throws Exception {
    context = getContext();
    cssMock = createMock(CSS.class);
    css = new CSSMock(context, cssMock);
  }

  @Test
  public void testDisplayInclude_createCSSinclude() {
    String url = "/skin/Content/WebHome/myCSSFile.css";
    expect(cssMock.getCSS(same(context))).andReturn(url);
    expect(cssMock.getMedia()).andReturn("all");
    replay(cssMock);
    assertEquals("<link rel=\"stylesheet\" media=\"all\" type=\"text/css\" href=\""
        + url + "\" />\n", css.displayInclude(context));
    verify(cssMock);
  }

  @Test
  public void testDisplayInclude_null_URL() {
    String url = null;
    String basePath = "Content.WebHome;myCSSFile.css";
    expect(cssMock.getCSS(same(context))).andReturn(url);
    expect(cssMock.getCssBasePath()).andReturn(basePath);
    replay(cssMock);
    assertEquals("<!-- WARNING: css file not found: " + basePath + " -->\n",
        css.displayInclude(context)); 
    verify(cssMock);
  }

  @Test
  public void testGetURLFromString() {
    IWebUtils utils = createMock(IWebUtils.class);
    css.testInjectUtils(utils);
    String cssPath = ":celRes/celements2.css";
    String urlPath = "/skin/resources/celRes/celements2.css";
    expect(utils.getAttachmentURL(eq(cssPath), same(context))).andReturn(urlPath);
    replay(cssMock, utils);
    assertEquals(urlPath, css.getURLFromString(cssPath, context));
    verify(cssMock, utils);
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private class CSSMock extends CSS {

    private CSS cssMock;

    public CSSMock(XWikiContext context, CSS cssMock) {
      super(context);
      this.cssMock = cssMock;
    }

    @Override
    public Attachment getAttachment() {
      return cssMock.getAttachment();
    }

    @Override
    public String getCSS(XWikiContext context) {
      return cssMock.getCSS(context);
    }

    @Override
    public String getMedia() {
      return cssMock.getMedia();
    }

    @Override
    public boolean isAttachment() {
      return cssMock.isAttachment();
    }

    @Override
    public boolean isContentCSS() {
      return cssMock.isContentCSS();
    }

    @Override
    public String getCssBasePath() {
      return cssMock.getCssBasePath();
    }

  }

}
