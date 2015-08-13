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
package com.celements.navigation.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.NavigationClasses;
import com.celements.navigation.event.TreeNodeDeletedEvent;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class TreeNodeDocumentDeletedListenerTest extends AbstractBridgedComponentTestCase {
  
  private static final String _COMPONENT_NAME = "TreeNodeDocumentDeletedListener";
  private TreeNodeDocumentDeletedListener eventListener;
  private XWikiContext context;
  private ObservationManager obsManagerMock;

  @Before
  public void setUp_TreeNodeDocumentDeletedListenerTest() throws Exception {
    context = getContext();
    obsManagerMock = registerComponentMock(ObservationManager.class);
    eventListener = getTreeNodeDocumentDeletedListener();
  }

  @Test
  public void testComponentSingleton() {
    assertSame(eventListener, getTreeNodeDocumentDeletedListener());
  }

  @Test
  public void testGetName() {
    assertEquals(_COMPONENT_NAME, eventListener.getName());
  }

  @Test
  public void testGetEvents() {
    List<String> expectedEventClassList = Arrays.asList(new DocumentDeletedEvent(
        ).getClass().getName());
    replayDefault();
    List<Event> actualEventList = eventListener.getEvents();
    assertEquals(expectedEventClassList.size(), actualEventList.size());
    for (Event actualEvent : actualEventList) {
      assertTrue("Unexpected Event [" + actualEvent.getClass().getName() + "] found.",
          expectedEventClassList.contains(actualEvent.getClass().getName()));
    }
    verifyDefault();
  }

  @Test
  public void testGetOrginialDocument_noSourceDoc() {
    replayDefault();
    assertNull(eventListener.getOrginialDocument(null));
    verifyDefault();
  }

  @Test
  public void testGetOrginialDocument_noOrigialDoc() {
    DocumentReference treeNodeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "treeNodeDocName");
    XWikiDocument sourceDoc = new XWikiDocument(treeNodeDocRef);
    replayDefault();
    assertNull(eventListener.getOrginialDocument(sourceDoc));
    verifyDefault();
  }

  @Test
  public void testGetOrginialDocument_originalDoc() {
    DocumentReference treeNodeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "treeNodeDocName");
    XWikiDocument sourceDoc = new XWikiDocument(treeNodeDocRef);
    XWikiDocument origDoc = new XWikiDocument(treeNodeDocRef);
    sourceDoc.setOriginalDocument(origDoc);
    replayDefault();
    assertNotNull(eventListener.getOrginialDocument(sourceDoc));
    assertSame(origDoc, eventListener.getOrginialDocument(sourceDoc));
    verifyDefault();
  }

  @Test
  public void testOnEvent_remoteEvent() {
    DocumentReference treeNodeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "treeNodeDocName");
    Event docDelEvent = new DocumentDeletedEvent(treeNodeDocRef);
    XWikiDocument sourceDoc = new XWikiDocument(treeNodeDocRef);
    XWikiDocument origDoc = new XWikiDocument(treeNodeDocRef);
    sourceDoc.setOriginalDocument(origDoc);
    RemoteObservationManagerContext remoteObsManagerCtx = createMockAndAddToDefault(
        RemoteObservationManagerContext.class);
    eventListener.remoteObservationManagerContext = remoteObsManagerCtx;
    expect(remoteObsManagerCtx.isRemoteState()).andReturn(true).atLeastOnce();
    replayDefault();
    eventListener.onEvent(docDelEvent, sourceDoc, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_localEvent_nullDoc() {
    DocumentReference treeNodeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "treeNodeDocName");
    Event docDelEvent = new DocumentDeletedEvent(treeNodeDocRef);
    RemoteObservationManagerContext remoteObsManagerCtx = createMockAndAddToDefault(
        RemoteObservationManagerContext.class);
    eventListener.remoteObservationManagerContext = remoteObsManagerCtx;
    expect(remoteObsManagerCtx.isRemoteState()).andReturn(false).atLeastOnce();
    replayDefault();
    eventListener.onEvent(docDelEvent, null, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_localEvent_noObject() {
    DocumentReference treeNodeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "treeNodeDocName");
    Event docDelEvent = new DocumentDeletedEvent(treeNodeDocRef);
    XWikiDocument sourceDoc = new XWikiDocument(treeNodeDocRef);
    XWikiDocument origDoc = new XWikiDocument(treeNodeDocRef);
    sourceDoc.setOriginalDocument(origDoc);
    RemoteObservationManagerContext remoteObsManagerCtx = createMockAndAddToDefault(
        RemoteObservationManagerContext.class);
    eventListener.remoteObservationManagerContext = remoteObsManagerCtx;
    expect(remoteObsManagerCtx.isRemoteState()).andReturn(false).atLeastOnce();
    replayDefault();
    eventListener.onEvent(docDelEvent, sourceDoc, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_localEvent_menuItemObj() {
    DocumentReference treeNodeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "treeNodeDocName");
    Event docDelEvent = new DocumentDeletedEvent(treeNodeDocRef);
    XWikiDocument sourceDoc = new XWikiDocument(treeNodeDocRef);
    XWikiDocument origDoc = new XWikiDocument(treeNodeDocRef);
    sourceDoc.setOriginalDocument(origDoc);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(new NavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    origDoc.addXObject(menuItemObj);
    RemoteObservationManagerContext remoteObsManagerCtx = createMockAndAddToDefault(
        RemoteObservationManagerContext.class);
    eventListener.remoteObservationManagerContext = remoteObsManagerCtx;
    expect(remoteObsManagerCtx.isRemoteState()).andReturn(false).atLeastOnce();
    obsManagerMock.notify(isA(TreeNodeDeletedEvent.class), same(sourceDoc),
        same(context));
    expectLastCall().once();
    replayDefault();
    eventListener.onEvent(docDelEvent, sourceDoc, context);
    verifyDefault();
  }

  private TreeNodeDocumentDeletedListener getTreeNodeDocumentDeletedListener() {
    return (TreeNodeDocumentDeletedListener) Utils.getComponent(EventListener.class,
        _COMPONENT_NAME);
  }

}
