package com.celements.common.observation.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.observation.listener.AbstractDocumentCreateListener;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class AbstractDocumentCreateListenerTest extends AbstractBridgedComponentTestCase {

  private TestDocumentCreateListener listener;
  private XWikiContext context;
  private RemoteObservationManagerContext remoteObsManContextMock;
  private ObservationManager obsManagerMock;

  private DocumentReference classRef;
  private DocumentReference docRef;
  private XWikiDocument docMock;

  private Event creatingEventMock;
  private Event createdEventMock;

  @Before
  public void setUp_AbstractDocumentCreateListenerTest() throws Exception {
    context = getContext();
    classRef = new DocumentReference("wiki", "Classes", "SomeClass");
    docRef = new DocumentReference("wiki", "Space", "SomeDoc");
    docMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(docMock.getDocumentReference()).andReturn(docRef).anyTimes();

    listener = new TestDocumentCreateListener();
    listener.injectWebUtilsService(Utils.getComponent(IWebUtilsService.class));
    listener.injecExecution(Utils.getComponent(Execution.class));
    listener.injectRemoteObservationManagerContext(remoteObsManContextMock = 
        createMockAndAddToDefault(RemoteObservationManagerContext.class));
    listener.injectObservationManager(obsManagerMock = 
        createMockAndAddToDefault(ObservationManager.class));

    creatingEventMock = createMockAndAddToDefault(Event.class);
    createdEventMock = createMockAndAddToDefault(Event.class);
  }

  @Test
  public void testOnEvent_nullDoc_ing() {
    Event event = new DocumentCreatingEvent();
    
    replayDefault();
    listener.onEvent(event, null, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_nullDoc_ed() {
    Event event = new DocumentCreatedEvent();
    
    replayDefault();
    listener.onEvent(event, null, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_isRemote_ing() {
    Event event = new DocumentCreatingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(true).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_isRemote_ed() {
    Event event = new DocumentCreatedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(true).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noObj_ing() {
    Event event = new DocumentCreatingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(null).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noObj_ed() {
    Event event = new DocumentCreatedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(null).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noEvent_ing() {
    Event event = new DocumentCreatingEvent();
    creatingEventMock = null;

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noEvent_ed() {
    Event event = new DocumentCreatedEvent();
    createdEventMock = null;

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_ing() {
    Event event = new DocumentCreatingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    obsManagerMock.notify(same(creatingEventMock), same(docMock), same(context));
    expectLastCall().once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_ed() {
    Event event = new DocumentCreatedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    obsManagerMock.notify(same(createdEventMock), same(docMock), same(context));
    expectLastCall().once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  private class TestDocumentCreateListener extends AbstractDocumentCreateListener {

    Logger LOGGER = LoggerFactory.getLogger(TestDocumentCreateListener.class);
    static final String NAME = "TestDocumentCreateListener";

    @Override
    public String getName() {
      return NAME;
    }

    @Override
    protected DocumentReference getRequiredObjClassRef(WikiReference wikiRef) {
      return classRef;
    }

    @Override
    protected Event getCreatingEvent(DocumentReference docRef) {
      assertEquals(AbstractDocumentCreateListenerTest.this.docRef, docRef);
      return creatingEventMock;
    }

    @Override
    protected Event getCreatedEvent(DocumentReference docRef) {
      assertEquals(AbstractDocumentCreateListenerTest.this.docRef, docRef);
      return createdEventMock;
    }

    @Override
    protected Logger getLogger() {
      return LOGGER;
    }
    
  }

}
