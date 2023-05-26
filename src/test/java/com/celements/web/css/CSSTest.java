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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Attachment;

public class CSSTest extends AbstractComponentTest {

  private XWikiContext context;
  private CSSMock css;
  private CSS cssMock;

  @Before
  public void setUp_CSSTest() throws Exception {
    context = getContext();
    cssMock = createDefaultMock(CSS.class);
    css = new CSSMock(context, cssMock);
  }

  @Test
  public void testDisplayInclude_createCSSinclude() {
    String url = "/skin/Content/WebHome/myCSSFile.css";
    expect(cssMock.getCSS(same(context))).andReturn(url);
    expect(cssMock.isAlternate()).andReturn(false);
    expect(cssMock.getTitle()).andReturn("myTitle");
    expect(cssMock.getMedia()).andReturn("all");
    replayDefault();
    assertEquals("<link rel=\"stylesheet\" title=\"myTitle\" media=\"all\" "
        + "type=\"text/css\" href=\"" + url + "\" />\n", css.displayInclude(context));
    verifyDefault();
  }

  @Test
  public void testDisplayInclude_createCSSinclude_alternate() {
    String url = "/skin/Content/WebHome/myCSSFile.css";
    expect(cssMock.getCSS(same(context))).andReturn(url);
    expect(cssMock.isAlternate()).andReturn(true);
    expect(cssMock.getTitle()).andReturn("myTitle");
    expect(cssMock.getMedia()).andReturn("all");
    replayDefault();
    assertEquals("<link rel=\"alternate stylesheet\" title=\"myTitle\" media=\"all\" "
        + "type=\"text/css\" href=\"" + url + "\" />\n", css.displayInclude(context));
    verifyDefault();
  }

  @Test
  public void testDisplayInclude_null_URL() {
    String url = null;
    String basePath = "Content.WebHome;myCSSFile.css";
    expect(cssMock.getCSS(same(context))).andReturn(url);
    expect(cssMock.getCssBasePath()).andReturn(basePath);
    replayDefault();
    assertEquals("<!-- WARNING: css file not found: " + basePath + " -->\n", css.displayInclude(
        context));
    verifyDefault();
  }

  @Test
  public void testGetURLFromString() {
    AttachmentURLCommand attURLcmd = createDefaultMock(AttachmentURLCommand.class);
    css.testInjectAttURLcmd(attURLcmd);
    String cssPath = ":celRes/celements2.css";
    String urlPath = "/skin/resources/celRes/celements2.css";
    expect(attURLcmd.getAttachmentURL(eq(cssPath), same(context))).andReturn(urlPath);
    replayDefault();
    assertEquals(urlPath, css.getURLFromString(cssPath, context));
    verifyDefault();
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

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
    public boolean isAlternate() {
      return cssMock.isAlternate();
    }

    @Override
    public String getTitle() {
      return cssMock.getTitle();
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
