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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.common.test.AbstractComponentTest;
import com.celements.sajson.Builder;
import com.celements.velocity.VelocityContextModifier;
import com.celements.velocity.VelocityService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class ContextMenuItemTest extends AbstractComponentTest {

  private ContextMenuItem theCMI;
  private VelocityService veloService;
  private String origElemId;

  @Before
  public void prepareTest() throws Exception {
    veloService = registerComponentMock(VelocityService.class);
    DocumentReference myDocRef = new DocumentReference(getContext().getDatabase(), "mySpace",
        "myDoc");
    getContext().setDoc(new XWikiDocument(myDocRef));
    origElemId = "N1:Content.Agenda";
    theCMI = createCMI(origElemId);
  }

  @Test
  public void testContextMenuItem_withPrefix() throws Exception {
    expectEvaluate();
    replayDefault();
    assertEquals("Content.Agenda", theCMI.getElemId());
    Builder builder = new Builder();
    theCMI.generateJSON(builder);
    verifyDefault();
  }

  @Test
  public void testContextMenuItem_withPrefix_emptyElementId() throws Exception {
    expectEvaluate();
    replayDefault();
    String localOrigElemId = "N1:menuPartTest:";
    ContextMenuItem localCMI = createCMI(localOrigElemId);
    assertEquals("link", localCMI.getLink());
    assertEquals("Test Menu Item", localCMI.getText());
    assertEquals("shortcut", localCMI.getShortcut());
    assertEquals("", localCMI.getCmiIcon());
    assertEquals("", localCMI.getElemId());
    verifyDefault();
  }

  @Test
  public void testContextMenuItem_woPrefix() throws Exception {
    expectEvaluate();
    replayDefault();
    ContextMenuItem localCMI = createCMI("Content.Test2");
    Builder builder = new Builder();
    localCMI.generateJSON(builder);
    assertEquals("Content.Test2", localCMI.getElemId());
    assertEquals("{\"link\" : \"link\", \"text\" : \"Test Menu Item\", \"icon\" : \"\","
        + " \"shortcut\" : {\"shortcut\" : true}}", builder.getJSON());
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

  private void expectEvaluate() throws XWikiVelocityException {
    expect(veloService.evaluateVelocityText(same(getContext().getDoc()), eq("link"), anyObject(
        VelocityContextModifier.class))).andReturn("link");
    expect(veloService.evaluateVelocityText(same(getContext().getDoc()), eq("Test Menu Item"),
        anyObject(VelocityContextModifier.class))).andReturn("Test Menu Item");
    expect(veloService.evaluateVelocityText(same(getContext().getDoc()), eq("shortcut"), anyObject(
        VelocityContextModifier.class))).andReturn("shortcut");
    expect(veloService.evaluateVelocityText(same(getContext().getDoc()), eq(""), anyObject(
        VelocityContextModifier.class))).andReturn("");
  }

}
