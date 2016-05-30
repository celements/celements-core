package com.celements.mandatory.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.WikiCreatedEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.mandatory.IMandatoryDocumentCompositorRole;
import com.xpn.xwiki.web.Utils;

public class WikiCreatedEventListenerTest extends AbstractBridgedComponentTestCase {

  private WikiCreatedEventListener listener;

  private RemoteObservationManagerContext remoteObsMngContextMock;
  private IMandatoryDocumentCompositorRole mandatoryDocCmpMock;

  @Before
  public void setUp_WikiCreatedEventListenerTest() throws Exception {
    listener = (WikiCreatedEventListener) Utils.getComponent(EventListener.class,
        "celements.mandatory.WikiCreatedEventListener");
    remoteObsMngContextMock = createMockAndAddToDefault(RemoteObservationManagerContext.class);
    listener.remoteObservationManagerContext = remoteObsMngContextMock;
    mandatoryDocCmpMock = createMockAndAddToDefault(IMandatoryDocumentCompositorRole.class);
    listener.mandatoryDocCmp = mandatoryDocCmpMock;
  }

  @Test
  public void testGetName() {
    assertEquals("celements.mandatory.WikiCreatedEventListener", listener.getName());
  }

  @Test
  public void testGetEvents() {
    assertEquals(1, listener.getEvents().size());
    assertSame(WikiCreatedEvent.class, listener.getEvents().get(0).getClass());
  }

  @Test
  public void testOnEvent() {
    String database = "db";
    Event event = new WikiCreatedEvent(database);

    expect(remoteObsMngContextMock.isRemoteState()).andReturn(false).atLeastOnce();
    mandatoryDocCmpMock.checkAllMandatoryDocuments();
    expectLastCall().andDelegateTo(new TestMandatoryDocumentCompositor(database)).once();

    String db = getContext().getDatabase();
    replayDefault();
    listener.onEvent(event, null, null);
    verifyDefault();
    assertEquals(db, getContext().getDatabase());
  }

  @Test
  public void testOnEvent_remote() {
    expect(remoteObsMngContextMock.isRemoteState()).andReturn(true).atLeastOnce();

    replayDefault();
    listener.onEvent(new WikiCreatedEvent(), null, null);
    verifyDefault();
  }

  private class TestMandatoryDocumentCompositor implements IMandatoryDocumentCompositorRole {

    private final String database;

    TestMandatoryDocumentCompositor(String database) {
      this.database = database;
    }

    @Override
    public void checkAllMandatoryDocuments() {
      assertEquals(database, getContext().getDatabase());
    }

  }

}
