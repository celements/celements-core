package com.celements.common.classes.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.WikiCreatedEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.classes.IClassesCompositorComponent;
import com.celements.common.test.AbstractBridgedComponentTestCase;

public class WikiCreatedEventListenerTest extends AbstractBridgedComponentTestCase {

  private WikiCreatedEventListener listener;

  private RemoteObservationManagerContext remoteObsMngContextMock;
  private IClassesCompositorComponent classesCmpMock;

  @Before
  public void setUp_WikiCreatedEventListenerTest() throws Exception {
    listener = (WikiCreatedEventListener) getComponentManager().lookup(
        EventListener.class, "celements.classes.WikiCreatedEventListener");
    remoteObsMngContextMock = createMockAndAddToDefault(
        RemoteObservationManagerContext.class);
    listener.remoteObservationManagerContext = remoteObsMngContextMock;
    classesCmpMock = createMockAndAddToDefault(IClassesCompositorComponent.class);
    listener.classesCompositor = classesCmpMock;
  }

  @Test
  public void testGetName() {
    assertEquals("celements.classes.WikiCreatedEventListener", listener.getName());
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
    classesCmpMock.checkAllClassCollections();
    expectLastCall().andDelegateTo(new TestClassesCompositor(database)).once();
    
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

  private class TestClassesCompositor implements IClassesCompositorComponent {
    
    private final String database;
    
    TestClassesCompositor(String database) {
      this.database = database;
    }

    @Override
    public void checkAllClassCollections() {
      assertEquals(database, getContext().getDatabase());
    }

    @Override
    public boolean isActivated(String name) {
      throw new UnsupportedOperationException();
    }
    
  }

}
