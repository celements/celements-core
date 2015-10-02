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
package com.celements.pagetype.xobject.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.pagetype.xobject.event.XObjectPageTypeDeletedEvent;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeDocumentDeletedListenerTest
    extends AbstractBridgedComponentTestCase {
  
  private static final String _COMPONENT_NAME = "XObjectPageTypeDocumentDeletedListener";
  private XObjectPageTypeDocumentDeletedListener eventListener;
  private XWikiContext context;
  private ObservationManager defaultObservationManager;
  private ComponentManager componentManager;
  private ObservationManager obsManagerMock;

  @Before
  public void setUp_XObjectPageTypeDocumentDeletedListenerTest() throws Exception {
    componentManager = Utils.getComponentManager();
    context = getContext();
    eventListener = getXObjPageTypeDocUpdatedListener();
    defaultObservationManager = Utils.getComponent(ObservationManager.class);
    componentManager.release(defaultObservationManager);
    ComponentDescriptor<ObservationManager> obsManagDesc =
      componentManager.getComponentDescriptor(ObservationManager.class, "default");
    obsManagerMock = createMockAndAddToDefault(ObservationManager.class);
    componentManager.registerComponent(obsManagDesc, obsManagerMock);
  }

  @After
  public void tearDown_XObjectPageTypeDocumentDeletedListenerTest() throws Exception {
    componentManager.release(obsManagerMock);
    ComponentDescriptor<ObservationManager> obsManagDesc =
      componentManager.getComponentDescriptor(ObservationManager.class, "default");
    componentManager.registerComponent(obsManagDesc, defaultObservationManager);
  }

  @Test
  public void testComponentSingleton() {
    assertSame(eventListener, getXObjPageTypeDocUpdatedListener());
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
    DocumentReference pageTypeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "pageTypeDocName");
    XWikiDocument sourceDoc = new XWikiDocument(pageTypeDocRef);
    replayDefault();
    assertNull(eventListener.getOrginialDocument(sourceDoc));
    verifyDefault();
  }

  @Test
  public void testGetOrginialDocument_originalDoc() {
    DocumentReference pageTypeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "pageTypeDocName");
    XWikiDocument sourceDoc = new XWikiDocument(pageTypeDocRef);
    XWikiDocument origDoc = new XWikiDocument(pageTypeDocRef);
    sourceDoc.setOriginalDocument(origDoc);
    replayDefault();
    assertNotNull(eventListener.getOrginialDocument(sourceDoc));
    assertSame(origDoc, eventListener.getOrginialDocument(sourceDoc));
    verifyDefault();
  }

  @Test
  public void testOnEvent_remoteEvent() {
    DocumentReference pageTypeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "pageTypeDocName");
    Event docDelEvent = new DocumentDeletedEvent(pageTypeDocRef);
    XWikiDocument sourceDoc = new XWikiDocument(pageTypeDocRef);
    XWikiDocument origDoc = new XWikiDocument(pageTypeDocRef);
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
    DocumentReference pageTypeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "pageTypeDocName");
    Event docDelEvent = new DocumentDeletedEvent(pageTypeDocRef);
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
    DocumentReference pageTypeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "pageTypeDocName");
    Event docDelEvent = new DocumentDeletedEvent(pageTypeDocRef);
    XWikiDocument sourceDoc = new XWikiDocument(pageTypeDocRef);
    XWikiDocument origDoc = new XWikiDocument(pageTypeDocRef);
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
  public void testOnEvent_localEvent_pageTypePropObj() {
    DocumentReference pageTypeDocRef = new DocumentReference(context.getDatabase(),
        "spaceName", "pageTypeDocName");
    Event docDelEvent = new DocumentDeletedEvent(pageTypeDocRef);
    XWikiDocument sourceDoc = new XWikiDocument(pageTypeDocRef);
    XWikiDocument origDoc = new XWikiDocument(pageTypeDocRef);
    sourceDoc.setOriginalDocument(origDoc);
    BaseObject pageTypePropObj = new BaseObject();
    pageTypePropObj.setXClassReference(getPageTypePropertiesClassRef());
    origDoc.addXObject(pageTypePropObj);
    RemoteObservationManagerContext remoteObsManagerCtx = createMockAndAddToDefault(
        RemoteObservationManagerContext.class);
    eventListener.remoteObservationManagerContext = remoteObsManagerCtx;
    expect(remoteObsManagerCtx.isRemoteState()).andReturn(false).atLeastOnce();
    obsManagerMock.notify(isA(XObjectPageTypeDeletedEvent.class), same(sourceDoc),
        same(context));
    expectLastCall().once();
    replayDefault();
    eventListener.onEvent(docDelEvent, sourceDoc, context);
    verifyDefault();
  }

  private DocumentReference getPageTypePropertiesClassRef() {
    return Utils.getComponent(IPageTypeClassConfig.class).getPageTypePropertiesClassRef(
        new WikiReference(context.getDatabase()));
  }

  private XObjectPageTypeDocumentDeletedListener getXObjPageTypeDocUpdatedListener() {
    return (XObjectPageTypeDocumentDeletedListener) Utils.getComponent(
        EventListener.class, _COMPONENT_NAME);
  }

}
