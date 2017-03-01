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
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.sajson.Builder;
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
  private String origElemId;

  @Before
  public void setUp_ContextMenuItemTest() {
    context = getContext();
    xwiki = getWikiMock();
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    context.setDoc(new XWikiDocument(myDocRef));
    velocityContext = new VelocityContext();
    context.put("vcontext", velocityContext);
    renderingEngine = createMockAndAddToDefault(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderingEngine).anyTimes();
    origElemId = "N1:Content.Agenda";
    theCMI = createCMI(origElemId);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testContextMenuItem_withPrefix() {
    expect(renderingEngine.interpretText(eq("link"), same(context.getDoc()), same(
        context))).andReturn("link");
    expect(renderingEngine.interpretText(eq("Test Menu Item"), same(context.getDoc()), same(
        context))).andReturn("Test Menu Item");
    expect(renderingEngine.interpretText(eq("shortcut"), same(context.getDoc()), same(
        context))).andReturn("shortcut");
    expect(renderingEngine.interpretText(eq(""), same(context.getDoc()), same(context))).andReturn(
        "");
    replayDefault();
    assertEquals("Content.Agenda", theCMI.getElemId());
    Builder builder = new Builder();
    theCMI.generateJSON(builder);
    assertEquals("Content.Agenda", velocityContext.get("elemId"));
    assertEquals(origElemId, velocityContext.get("origElemId"));
    assertNotNull(velocityContext.get("elemParams"));
    assertEquals(1, ((List<String>) velocityContext.get("elemParams")).size());
    verifyDefault();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testContextMenuItem_withPrefix_emptyElementId() {
    expect(renderingEngine.interpretText(eq("link"), same(context.getDoc()), same(
        context))).andReturn("link");
    expect(renderingEngine.interpretText(eq("Test Menu Item"), same(context.getDoc()), same(
        context))).andReturn("Test Menu Item");
    expect(renderingEngine.interpretText(eq("shortcut"), same(context.getDoc()), same(
        context))).andReturn("shortcut");
    expect(renderingEngine.interpretText(eq(""), same(context.getDoc()), same(context))).andReturn(
        "");
    replayDefault();
    String localOrigElemId = "N1:menuPartTest:";
    ContextMenuItem localCMI = createCMI(localOrigElemId);
    assertEquals("link", localCMI.getLink());
    assertEquals("Test Menu Item", localCMI.getText());
    assertEquals("shortcut", localCMI.getShortcut());
    assertEquals("", localCMI.getCmiIcon());
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    assertEquals("", localCMI.getElemId());
    assertEquals("", vcontext.get("elemId"));
    assertEquals(localOrigElemId, vcontext.get("origElemId"));
    assertNotNull(vcontext.get("elemParams"));
    assertEquals(2, ((List<String>) vcontext.get("elemParams")).size());
    assertEquals("menuPartTest", ((List<String>) vcontext.get("elemParams")).get(1));
    verifyDefault();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testContextMenuItem_woPrefix() {
    expect(renderingEngine.interpretText(eq("link"), same(context.getDoc()), same(
        context))).andReturn("link");
    expect(renderingEngine.interpretText(eq("Test Menu Item"), same(context.getDoc()), same(
        context))).andReturn("Test Menu Item");
    expect(renderingEngine.interpretText(eq("shortcut"), same(context.getDoc()), same(
        context))).andReturn("shortcut");
    expect(renderingEngine.interpretText(eq(""), same(context.getDoc()), same(context))).andReturn(
        "");
    replayDefault();
    ContextMenuItem localCMI = createCMI("Content.Test2");
    Builder builder = new Builder();
    localCMI.generateJSON(builder);
    assertEquals("Content.Test2", localCMI.getElemId());
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    assertEquals("Content.Test2", vcontext.get("elemId"));
    assertNotNull(vcontext.get("elemParams"));
    assertEquals(0, ((List<String>) vcontext.get("elemParams")).size());
    assertEquals("{\"link\" : \"link\", \"text\" : \"Test Menu Item\", \"icon\" : \"\","
        + " \"shortcut\" : {\"shortcut\" : true}}", builder.getJSON());
    verifyDefault();
  }

  @Test
  public void testContextMenuItem_isolate_VContext() {
    reset(xwiki);
    TestRenderingEngine mockRenderEngine = new TestRenderingEngine(renderingEngine);
    expect(xwiki.getRenderingEngine()).andReturn(mockRenderEngine).anyTimes();
    VelocityContext vcontextBefore = (VelocityContext) context.get("vcontext");
    assertNotNull(vcontextBefore);
    expect(renderingEngine.interpretText(eq("link"), same(context.getDoc()), same(
        context))).andReturn("link");
    expect(renderingEngine.interpretText(eq("Test Menu Item"), same(context.getDoc()), same(
        context))).andReturn("Test Menu Item");
    expect(renderingEngine.interpretText(eq("shortcut"), same(context.getDoc()), same(
        context))).andReturn("shortcut");
    expect(renderingEngine.interpretText(eq(""), same(context.getDoc()), same(context))).andReturn(
        "");
    replayDefault();
    ContextMenuItem localCMI = createCMI("Content.Test2");
    assertEquals("Content.Test2", localCMI.getElemId());
    assertSame(vcontextBefore, context.get("vcontext"));
    Builder builder = new Builder();
    localCMI.generateJSON(builder);
    assertFalse(mockRenderEngine.recordedVcontextList.isEmpty());
    for (VelocityContext vContext : mockRenderEngine.recordedVcontextList) {
      assertNotSame(vcontextBefore, vContext);
    }
    verifyDefault();
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private ContextMenuItem createCMI(String elementId) {
    BaseObject menuItem = new BaseObject();
    menuItem.setLargeStringValue("cmi_link", "link");
    menuItem.setStringValue("cmi_text", "Test Menu Item");
    menuItem.setStringValue("cmi_icon", null);
    menuItem.setStringValue("cmi_shortcut", "shortcut");
    return new ContextMenuItem(menuItem, elementId);
  }

  private class TestRenderingEngine implements XWikiRenderingEngine {

    List<VelocityContext> recordedVcontextList = new ArrayList<>();
    private XWikiRenderingEngine renderingEngine;

    public TestRenderingEngine(XWikiRenderingEngine renderingEngine) {
      this.renderingEngine = renderingEngine;
    }

    @Override
    public void addRenderer(String arg0, XWikiRenderer arg1) {
      renderingEngine.addRenderer(arg0, arg1);
    }

    @Override
    public String convertMultiLine(String arg0, String arg1, String arg2, String arg3,
        XWikiVirtualMacro arg4, XWikiContext arg5) {
      return renderingEngine.convertMultiLine(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public String convertSingleLine(String arg0, String arg1, String arg2, XWikiVirtualMacro arg3,
        XWikiContext arg4) {
      return renderingEngine.convertSingleLine(arg0, arg1, arg2, arg3, arg4);
    }

    @Override
    public void flushCache() {
      renderingEngine.flushCache();
    }

    @Override
    public XWikiRenderer getRenderer(String arg0) {
      return renderingEngine.getRenderer(arg0);
    }

    @Override
    public List<XWikiRenderer> getRendererList() {
      return renderingEngine.getRendererList();
    }

    @Override
    public List<String> getRendererNames() {
      return renderingEngine.getRendererNames();
    }

    @Override
    public String interpretText(String arg0, XWikiDocument arg1, XWikiContext context) {
      recordedVcontextList.add((VelocityContext) context.get("vcontext"));
      return renderingEngine.interpretText(arg0, arg1, context);
    }

    @Override
    public String renderDocument(XWikiDocument arg0, XWikiContext arg1) throws XWikiException {
      return renderingEngine.renderDocument(arg0, arg1);
    }

    @Override
    public String renderDocument(XWikiDocument arg0, XWikiDocument arg1, XWikiContext arg2)
        throws XWikiException {
      return renderingEngine.renderDocument(arg0, arg1, arg2);
    }

    @Override
    public String renderText(String arg0, XWikiDocument arg1, XWikiContext arg2) {
      return renderingEngine.renderText(arg0, arg1, arg2);
    }

    @Override
    public String renderText(String arg0, XWikiDocument arg1, XWikiDocument arg2,
        XWikiContext arg3) {
      return renderingEngine.renderText(arg0, arg1, arg2, arg3);
    }

    @Override
    public void virtualInit(XWikiContext arg0) {
      renderingEngine.virtualInit(arg0);
    }

  }
}
