package com.celements.common.observation.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.observation.listener.AbstractDocumentUpdateListener;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.copydoc.ICopyDocumentRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class AbstractDocumentUpdateListenerTest extends AbstractBridgedComponentTestCase {

  private TestDocumentUpdateListener listener;
  private XWikiContext context;
  private RemoteObservationManagerContext remoteObsManContextMock;
  private ObservationManager obsManagerMock;

  private DocumentReference classRef;
  private List<String> fields;
  private DocumentReference docRef;
  private XWikiDocument docMock;
  private XWikiDocument origDocMock;

  private Event creatingEventMock;
  private Event createdEventMock;
  private Event updatingEventMock;
  private Event updatedEventMock;
  private Event deletingEventMock;
  private Event deletedEventMock;

  @Before
  public void setUp_AbstractDocumentUpdateListenerTest() throws Exception {
    context = getContext();
    classRef = new DocumentReference("wiki", "Classes", "SomeClass");
    fields = Arrays.asList("field1", "field2");
    docRef = new DocumentReference("wiki", "Space", "SomeDoc");
    docMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(docMock.getDocumentReference()).andReturn(docRef).anyTimes();
    origDocMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(origDocMock.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(docMock.getOriginalDocument()).andReturn(origDocMock).anyTimes();

    listener = new TestDocumentUpdateListener();
    listener.injectWebUtilsService(Utils.getComponent(IWebUtilsService.class));
    listener.injecExecution(Utils.getComponent(Execution.class));
    listener.injectCopyDocService(Utils.getComponent(ICopyDocumentRole.class));
    listener.injectRemoteObservationManagerContext(remoteObsManContextMock = 
        createMockAndAddToDefault(RemoteObservationManagerContext.class));
    listener.injectObservationManager(obsManagerMock = 
        createMockAndAddToDefault(ObservationManager.class));

    creatingEventMock = createMockAndAddToDefault(Event.class);
    createdEventMock = createMockAndAddToDefault(Event.class);
    updatingEventMock = createMockAndAddToDefault(Event.class);
    updatedEventMock = createMockAndAddToDefault(Event.class);
    deletingEventMock = createMockAndAddToDefault(Event.class);
    deletedEventMock = createMockAndAddToDefault(Event.class);
  }

  @Test
  public void testOnEvent_nullDoc_ing() {
    Event event = new DocumentUpdatingEvent();
    
    replayDefault();
    listener.onEvent(event, null, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_nullDoc_ed() {
    Event event = new DocumentUpdatedEvent();
    
    replayDefault();
    listener.onEvent(event, null, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_isRemote_ing() {
    Event event = new DocumentUpdatingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(true).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_isRemote_ed() {
    Event event = new DocumentUpdatedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(true).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noObjs_ing() {
    Event event = new DocumentUpdatingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(null).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(null).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noObjs_ed() {
    Event event = new DocumentUpdatedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(null).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(null).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_create_ing_noEvent() {
    Event event = new DocumentUpdatingEvent();
    creatingEventMock = null;

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(null).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_create_ed_noEvent() {
    Event event = new DocumentUpdatedEvent();
    createdEventMock = null;

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(null).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_create_ing() {
    Event event = new DocumentUpdatingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(null).once();
    obsManagerMock.notify(same(creatingEventMock), same(docMock), same(context));
    expectLastCall().once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_create_ed() {
    Event event = new DocumentUpdatedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(null).once();
    obsManagerMock.notify(same(createdEventMock), same(docMock), same(context));
    expectLastCall().once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_delete_ing_noEvent() {
    Event event = new DocumentUpdatingEvent();
    deletingEventMock = null;

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(null).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_delete_ed_noEvent() {
    Event event = new DocumentUpdatedEvent();
    deletedEventMock = null;

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(null).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_delete_ing() {
    Event event = new DocumentUpdatingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(null).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    obsManagerMock.notify(same(deletingEventMock), same(docMock), same(context));
    expectLastCall().once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_delete_ed() {
    Event event = new DocumentUpdatedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(null).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    obsManagerMock.notify(same(deletedEventMock), same(docMock), same(context));
    expectLastCall().once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_update_ing_noChange() {
    Event event = new DocumentUpdatingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_update_ed_noChange() {
    Event event = new DocumentUpdatedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_update_ing_noEvent() {
    Event event = new DocumentUpdatingEvent();
    updatingEventMock = null;

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(getBaseObject1()).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(getBaseObject2()).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_update_ed_noEvent() {
    Event event = new DocumentUpdatedEvent();
    updatedEventMock = null;

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(getBaseObject1()).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(getBaseObject2()).once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_update_ing() {
    Event event = new DocumentUpdatingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(getBaseObject1()).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(getBaseObject2()).once();
    obsManagerMock.notify(same(updatingEventMock), same(docMock), same(context));
    expectLastCall().once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_update_ed() {
    Event event = new DocumentUpdatedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getXObject(eq(classRef))).andReturn(getBaseObject1()).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(getBaseObject2()).once();
    obsManagerMock.notify(same(updatedEventMock), same(docMock), same(context));
    expectLastCall().once();
    
    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  private BaseObject getBaseObject1() {
    BaseObject bObj = new BaseObject();
    bObj.setStringValue(fields.get(0), "val");
    bObj.setStringValue(fields.get(1), "val1");
    return bObj;
  }

  private BaseObject getBaseObject2() {
    BaseObject bObj = new BaseObject();
    bObj.setStringValue(fields.get(0), "val");
    bObj.setStringValue(fields.get(1), "val2");
    return bObj;
  }

  private class TestDocumentUpdateListener extends AbstractDocumentUpdateListener {

    Logger LOGGER = LoggerFactory.getLogger(TestDocumentUpdateListener.class);
    static final String NAME = "TestDocumentUpdateListener";

    @Override
    public String getName() {
      return NAME;
    }

    @Override
    protected DocumentReference getRequiredObjClassRef(WikiReference wikiRef) {
      return classRef;
    }

    @Override
    protected Logger getLogger() {
      return LOGGER;
    }

    @Override
    protected Event getCreatingEvent(DocumentReference docRef) {
      assertEquals(AbstractDocumentUpdateListenerTest.this.docRef, docRef);
      return creatingEventMock;
    }

    @Override
    protected Event getCreatedEvent(DocumentReference docRef) {
      assertEquals(AbstractDocumentUpdateListenerTest.this.docRef, docRef);
      return createdEventMock;
    }

    @Override
    protected Event getUpdatingEvent(DocumentReference docRef) {
      assertEquals(AbstractDocumentUpdateListenerTest.this.docRef, docRef);
      return updatingEventMock;
    }

    @Override
    protected Event getUpdatedEvent(DocumentReference docRef) {
      assertEquals(AbstractDocumentUpdateListenerTest.this.docRef, docRef);
      return updatedEventMock;
    }

    @Override
    protected Event getDeletingEvent(DocumentReference docRef) {
      assertEquals(AbstractDocumentUpdateListenerTest.this.docRef, docRef);
      return deletingEventMock;
    }

    @Override
    protected Event getDeletedEvent(DocumentReference docRef) {
      assertEquals(AbstractDocumentUpdateListenerTest.this.docRef, docRef);
      return deletedEventMock;
    }
    
  }

}
