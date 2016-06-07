package com.celements.common.observation.listener;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class AbstractDocumentDeleteListenerTest extends AbstractComponentTest {

  private TestDocumentDeleteListener listener;
  private XWikiContext context;
  private XWiki wiki;
  private RemoteObservationManagerContext remoteObsManContextMock;
  private ObservationManager obsManagerMock;

  private DocumentReference classRef;
  private DocumentReference docRef;
  private XWikiDocument docMock;
  private XWikiDocument origDocMock;

  private Event deletingEventMock;
  private Event deletedEventMock;

  @Before
  public void setUp_AbstractDocumentDeleteListenerTest() throws Exception {
    context = getContext();
    wiki = getWikiMock();
    classRef = new DocumentReference("wiki", "Classes", "SomeClass");
    docRef = new DocumentReference("wiki", "Space", "SomeDoc");
    docMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(docMock.getDocumentReference()).andReturn(docRef).anyTimes();
    origDocMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(origDocMock.getDocumentReference()).andReturn(docRef).anyTimes();

    listener = new TestDocumentDeleteListener();
    listener.modelAccess = Utils.getComponent(IModelAccessFacade.class);
    listener.injectWebUtilsService(Utils.getComponent(IWebUtilsService.class));
    listener.injecExecution(Utils.getComponent(Execution.class));
    listener.injectRemoteObservationManagerContext(
        remoteObsManContextMock = createMockAndAddToDefault(RemoteObservationManagerContext.class));
    listener.injectObservationManager(obsManagerMock = createMockAndAddToDefault(
        ObservationManager.class));
    listener.configSrc = Utils.getComponent(ConfigurationSource.class);

    deletingEventMock = createMockAndAddToDefault(Event.class);
    deletedEventMock = createMockAndAddToDefault(Event.class);
  }

  @Test
  public void testGetEvents() {
    Set<Class<? extends Event>> eventClasses = new HashSet<Class<? extends Event>>();
    for (Event theEvent : listener.getEvents()) {
      eventClasses.add(theEvent.getClass());
    }
    assertEquals(2, eventClasses.size());
    assertTrue("Expecting registration for DocumentDeletingEvent", eventClasses.contains(
        DocumentDeletingEvent.class));
    assertTrue("Expecting registration for DocumentDeletedEvent events", eventClasses.contains(
        DocumentDeletedEvent.class));
  }

  @Test
  public void testOnEvent_nullDoc_ing() {
    Event event = new DocumentDeletingEvent();

    replayDefault();
    listener.onEvent(event, null, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_nullDoc_ed() {
    Event event = new DocumentDeletedEvent();

    replayDefault();
    listener.onEvent(event, null, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_isRemote_ing() {
    Event event = new DocumentDeletingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(true).once();

    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_isRemote_ed() {
    Event event = new DocumentDeletedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(true).once();

    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noObj_ing() {
    Event event = new DocumentDeletingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getOriginalDocument()).andReturn(origDocMock).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(null).once();

    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noObj_ed() {
    Event event = new DocumentDeletedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getOriginalDocument()).andReturn(origDocMock).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(null).once();

    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noEvent_ing() {
    Event event = new DocumentDeletingEvent();
    deletingEventMock = null;

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getOriginalDocument()).andReturn(origDocMock).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();

    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noEvent_ed() {
    Event event = new DocumentDeletedEvent();
    deletedEventMock = null;

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getOriginalDocument()).andReturn(origDocMock).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();

    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_ing_origDocSet() throws XWikiException {
    Event event = new DocumentDeletingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getOriginalDocument()).andReturn(origDocMock).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    obsManagerMock.notify(same(deletingEventMock), same(docMock), same(context));
    expectLastCall().once();

    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_ing_origDocLoad() throws XWikiException {
    Event event = new DocumentDeletingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getOriginalDocument()).andReturn(null).once();
    expect(wiki.exists(eq(docRef), same(context))).andReturn(true).once();
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(origDocMock).once();
    docMock.setOriginalDocument(same(origDocMock));
    expectLastCall().once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    obsManagerMock.notify(same(deletingEventMock), same(docMock), same(context));
    expectLastCall().once();
    expect(origDocMock.clone()).andReturn(origDocMock).once();

    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_ing_origDocLoad_fail() throws XWikiException {
    Event event = new DocumentDeletingEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getOriginalDocument()).andReturn(null).once();
    expect(wiki.exists(eq(docRef), same(context))).andReturn(false).once();
    expect(wiki.getDocument(eq(docRef), same(context))).andThrow(new XWikiException()).once();
    docMock.setOriginalDocument(isNull(XWikiDocument.class));
    expectLastCall().once();

    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  @Test
  public void testOnEvent_ed() {
    Event event = new DocumentDeletedEvent();

    expect(remoteObsManContextMock.isRemoteState()).andReturn(false).once();
    expect(docMock.getOriginalDocument()).andReturn(origDocMock).once();
    expect(origDocMock.getXObject(eq(classRef))).andReturn(new BaseObject()).once();
    obsManagerMock.notify(same(deletedEventMock), same(docMock), same(context));
    expectLastCall().once();

    replayDefault();
    listener.onEvent(event, docMock, context);
    verifyDefault();
  }

  private class TestDocumentDeleteListener extends AbstractDocumentDeleteListener {

    Logger LOGGER = LoggerFactory.getLogger(TestDocumentDeleteListener.class);
    static final String NAME = "TestDocumentDeleteListener";

    @Override
    public String getName() {
      return NAME;
    }

    @Override
    protected DocumentReference getRequiredObjClassRef(WikiReference wikiRef) {
      return classRef;
    }

    @Override
    protected Event getDeletingEvent(DocumentReference docRef) {
      assertEquals(AbstractDocumentDeleteListenerTest.this.docRef, docRef);
      return deletingEventMock;
    }

    @Override
    protected Event getDeletedEvent(DocumentReference docRef) {
      assertEquals(AbstractDocumentDeleteListenerTest.this.docRef, docRef);
      return deletedEventMock;
    }

    @Override
    protected Logger getLogger() {
      return LOGGER;
    }

  }

}
