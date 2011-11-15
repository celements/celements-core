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
package com.celements.web.contextmenu;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.render.XWikiRenderer;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.render.XWikiVirtualMacro;


public class ContextMenuItemTest extends AbstractBridgedComponentTestCase {

  private ContextMenuItem theCMI;
  private XWikiContext context;
  private VelocityContext velocityContext;
  private XWiki xwiki;
  private XWikiRenderingEngine renderingEngine;

  @Before
  public void setUp_ContextMenuItemTest() {
    context = getContext();
    context.setDoc(new XWikiDocument());
    velocityContext = new VelocityContext();
    context.put("vcontext", velocityContext);
    theCMI = createCMI("N1:Content.Agenda");
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    renderingEngine = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderingEngine).anyTimes();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testContextMenuItem_withPrefix() {
    replay(xwiki, renderingEngine);
    assertEquals("Content.Agenda", theCMI.getElemId());
    assertEquals("Content.Agenda", velocityContext.get("elemId"));
    assertNotNull(velocityContext.get("elemParams"));
    assertEquals(1, ((List<String>)velocityContext.get("elemParams")).size());
    verify(xwiki, renderingEngine);
  }


  @SuppressWarnings("unchecked")
  @Test
  public void testContextMenuItem_withPrefix_emptyElementId() {
    expect(renderingEngine.interpretText(eq("link"), same(context.getDoc()),
        same(context))).andReturn("link");
    expect(renderingEngine.interpretText(eq("Test Menu Item"), same(context.getDoc()),
        same(context))).andReturn("Test Menu Item");
    expect(renderingEngine.interpretText(eq("shortcut"), same(context.getDoc()),
        same(context))).andReturn("shortcut");
    expect(renderingEngine.interpretText(eq(""), same(context.getDoc()), same(context))
        ).andReturn("");
    replay(xwiki, renderingEngine);
    ContextMenuItem localCMI = createCMI("N1:menuPartTest:");
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    assertEquals("", localCMI.getElemId());
    assertEquals("", vcontext.get("elemId"));
    assertNotNull(vcontext.get("elemParams"));
    assertEquals(2, ((List<String>)vcontext.get("elemParams")).size());
    assertEquals("menuPartTest", ((List<String>)vcontext.get("elemParams")).get(1));
    verify(xwiki, renderingEngine);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testContextMenuItem_woPrefix() {
    expect(renderingEngine.interpretText(eq("link"), same(context.getDoc()),
        same(context))).andReturn("link");
    expect(renderingEngine.interpretText(eq("Test Menu Item"), same(context.getDoc()),
        same(context))).andReturn("Test Menu Item");
    expect(renderingEngine.interpretText(eq("shortcut"), same(context.getDoc()),
        same(context))).andReturn("shortcut");
    expect(renderingEngine.interpretText(eq(""), same(context.getDoc()), same(context))
        ).andReturn("");
    replay(xwiki, renderingEngine);
    assertEquals("Content.Test2", createCMI("Content.Test2").getElemId());
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    assertEquals("Content.Test2", vcontext.get("elemId"));
    assertNotNull(vcontext.get("elemParams"));
    assertEquals(0, ((List<String>)vcontext.get("elemParams")).size());
    verify(xwiki, renderingEngine);
  }

  @Test
  public void testContextMenuItem_isolate_VContext() {
    reset(xwiki);
    TestRenderingEngine mockRenderEngine = new TestRenderingEngine(renderingEngine);
    expect(xwiki.getRenderingEngine()).andReturn(mockRenderEngine).anyTimes();
    VelocityContext vcontextBefore = (VelocityContext) context.get("vcontext");
    assertNotNull(vcontextBefore);
    expect(renderingEngine.interpretText(eq("link"), same(context.getDoc()),
        same(context))).andReturn("link");
    expect(renderingEngine.interpretText(eq("Test Menu Item"), same(context.getDoc()),
        same(context))).andReturn("Test Menu Item");
    expect(renderingEngine.interpretText(eq("shortcut"), same(context.getDoc()),
        same(context))).andReturn("shortcut");
    expect(renderingEngine.interpretText(eq(""), same(context.getDoc()), same(context))
        ).andReturn("");
    replay(xwiki, renderingEngine);
    assertEquals("Content.Test2", createCMI("Content.Test2").getElemId());
    assertSame(vcontextBefore, context.get("vcontext"));
    assertFalse(mockRenderEngine.recordedVcontextList.isEmpty());
    for (VelocityContext vContext : mockRenderEngine.recordedVcontextList) {
      assertNotSame(vcontextBefore, vContext);
    }
    verify(xwiki, renderingEngine);
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private ContextMenuItem createCMI(String elementId) {
    BaseObject menuItem = new BaseObject();
    menuItem.setLargeStringValue("cmi_link", "link");
    menuItem.setStringValue("cmi_text", "Test Menu Item");
    menuItem.setStringValue("cmi_icon", null);
    menuItem.setStringValue("cmi_shortcut", "shortcut");
    return new ContextMenuItem(menuItem, elementId, context);
  }

  private class TestRenderingEngine implements XWikiRenderingEngine {

    List<VelocityContext> recordedVcontextList = new ArrayList<VelocityContext>();
    private XWikiRenderingEngine renderingEngine;

    public TestRenderingEngine(XWikiRenderingEngine renderingEngine) {
      this.renderingEngine = renderingEngine;
    }

    public void addRenderer(String arg0, XWikiRenderer arg1) {
      renderingEngine.addRenderer(arg0, arg1);
    }

    public String convertMultiLine(String arg0, String arg1, String arg2,
        String arg3, XWikiVirtualMacro arg4, XWikiContext arg5) {
      return renderingEngine.convertMultiLine(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public String convertSingleLine(String arg0, String arg1, String arg2,
        XWikiVirtualMacro arg3, XWikiContext arg4) {
      return renderingEngine.convertSingleLine(arg0, arg1, arg2, arg3, arg4);
    }

    public void flushCache() {
      renderingEngine.flushCache();
    }

    public XWikiRenderer getRenderer(String arg0) {
      return renderingEngine.getRenderer(arg0);
    }

    public List<XWikiRenderer> getRendererList() {
      return renderingEngine.getRendererList();
    }

    public List<String> getRendererNames() {
      return renderingEngine.getRendererNames();
    }

    public String interpretText(String arg0, XWikiDocument arg1,
        XWikiContext context) {
      recordedVcontextList.add((VelocityContext) context.get("vcontext"));
      return renderingEngine.interpretText(arg0, arg1, context);
    }

    public String renderDocument(XWikiDocument arg0, XWikiContext arg1)
        throws XWikiException {
      return renderingEngine.renderDocument(arg0, arg1);
    }

    public String renderDocument(XWikiDocument arg0, XWikiDocument arg1,
        XWikiContext arg2) throws XWikiException {
      return renderingEngine.renderDocument(arg0, arg1, arg2);
    }

    public String renderText(String arg0, XWikiDocument arg1, XWikiContext arg2) {
      return renderingEngine.renderText(arg0, arg1, arg2);
    }

    public String renderText(String arg0, XWikiDocument arg1,
        XWikiDocument arg2, XWikiContext arg3) {
      return renderingEngine.renderText(arg0, arg1, arg2, arg3);      
    }

    public void virtualInit(XWikiContext arg0) {
      renderingEngine.virtualInit(arg0);
    }
    
  }
}
