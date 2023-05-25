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
package com.celements.navigation.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWikiContext;

public class RestructureSaveCommandTest extends AbstractComponentTest {

  private XWikiContext context;
  private ReorderSaveCommand restrSaveCmd;
  private ReorderSaveHandler mockHandler;

  @Before
  public void setUp_RestructureSaveCommandTest() throws Exception {
    context = getContext();
    restrSaveCmd = new ReorderSaveCommand();
    mockHandler = createDefaultMock(ReorderSaveHandler.class);
    restrSaveCmd.injected_Handler(mockHandler);
  }

  @Test
  public void testGetHandler() {
    restrSaveCmd.injected_Handler(null);
    assertNotNull(restrSaveCmd.getHandler());
    assertEquals("Expecting RestructureSaveHandler handler.", ReorderSaveHandler.class,
        restrSaveCmd.getHandler().getClass());
  }

  @Test
  public void testInjected_Handler() {
    restrSaveCmd.injected_Handler(mockHandler);
    assertNotNull(restrSaveCmd.getHandler());
    assertSame("Expecting injected handler object.", mockHandler, restrSaveCmd.getHandler());
  }

  @Test
  public void testRestructureSave() {
    String structureJSON = "[{\"CN1:MySpace.MyDoc\": [\"LIN1:MyDoc.Node1\","
        + " \"LIN1:MyDoc.Node2\"]}, "
        + "{\"CN1:MyDoc.Node1\": [\"LIN1:MyDoc.Node4\", \"LIN1:MyDoc.Node3\"]}]";
    mockHandler.openEvent(isA(EReorderLiteral.class));
    expectLastCall().anyTimes();
    mockHandler.closeEvent(isA(EReorderLiteral.class));
    expectLastCall().anyTimes();
    mockHandler.readPropertyKey(isA(String.class));
    expectLastCall().anyTimes();
    mockHandler.stringEvent(isA(String.class));
    expectLastCall().anyTimes();
    // Set<String> dirtyParents = Collections.emptySet();
    // expect(mockHandler.getDirtyParents()).andReturn(dirtyParents);
    replayDefault();
    restrSaveCmd.reorderSave("MySpace.MyDoc", structureJSON, context);
    verifyDefault();
  }

  @Test
  public void testRestructureSave_noChanges() {
    String structureJSON = "[{\"CN1:MySpace.MyDoc\": [\"LIN1:MyDoc.Node1\","
        + " \"LIN1:MyDoc.Node2\"]}, "
        + "{\"CN1:MyDoc.Node1\": [\"LIN1:MyDoc.Node4\", \"LIN1:MyDoc.Node3\"]}]";
    mockHandler.openEvent(isA(EReorderLiteral.class));
    expectLastCall().anyTimes();
    mockHandler.closeEvent(isA(EReorderLiteral.class));
    expectLastCall().anyTimes();
    mockHandler.readPropertyKey(isA(String.class));
    expectLastCall().anyTimes();
    mockHandler.stringEvent(isA(String.class));
    expectLastCall().anyTimes();
    // Set<String> dirtyParents = Collections.emptySet();
    // expect(mockHandler.getDirtyParents()).andReturn(dirtyParents);
    replayDefault();
    restrSaveCmd.reorderSave("MySpace.MyDoc", structureJSON, context);
    verifyDefault();
  }

}
