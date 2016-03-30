package com.celements.common.observation.listener;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.Serializable;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.AllEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.LocalEventData;
import org.xwiki.observation.remote.RemoteObservationManager;
import org.xwiki.observation.remote.RemoteObservationManagerConfiguration;

import com.celements.common.observation.converter.Local;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class LocalEventListenerTest extends AbstractComponentTest {
  
  private LocalEventListener listener;
  
  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    registerComponentMocks(RemoteObservationManagerConfiguration.class,
        RemoteObservationManager.class);
    listener = (LocalEventListener) Utils.getComponent(EventListener.class,
        LocalEventListener.NAME);
  }
  
  @Test
  public void test_getName() {
    assertEquals("observation.remote", listener.getName());
  }
  
  @Test
  public void test_getEvents_enabled() {
    expect(getMock(RemoteObservationManagerConfiguration.class).isEnabled()
        ).andReturn(true).anyTimes();
    
    replayDefault();
    assertEquals(1, listener.getEvents().size());
    assertEquals(AllEvent.ALLEVENT, listener.getEvents().get(0));
    verifyDefault();
  }
  
  @Test
  public void test_getEvents_disabled() {
    expect(getMock(RemoteObservationManagerConfiguration.class).isEnabled()
        ).andReturn(false).anyTimes();
    
    replayDefault();
    assertEquals(0, listener.getEvents().size());
    verifyDefault();
  }
  
  @Test
  public void test_onEvent() {
    Event event = AllEvent.ALLEVENT;
    Object source = createMockAndAddToDefault(Object.class);
    Object data = createMockAndAddToDefault(Object.class);
    Capture<LocalEventData> localEvDataCapt = new Capture<>();
    getMock(RemoteObservationManager.class).notify(capture(localEvDataCapt));
    expectLastCall().once();
    
    replayDefault();
    listener.onEvent(event, source, data);
    verifyDefault();
    
    assertSame(event, localEvDataCapt.getValue().getEvent());
    assertSame(source, localEvDataCapt.getValue().getSource());
    assertSame(data, localEvDataCapt.getValue().getData());
  }
  
  @Test
  public void test_onEvent_isLocal() {
    Event event = new LocalTestEvent();
    Object source = createMockAndAddToDefault(Object.class);
    Object data = createMockAndAddToDefault(Object.class);
    
    replayDefault();
    listener.onEvent(event, source, data);
    verifyDefault();
  }
  
  @Test
  public void test_onEvent_error() {
    Event event = AllEvent.ALLEVENT;
    Object source = createMockAndAddToDefault(Object.class);
    Object data = createMockAndAddToDefault(Object.class);
    getMock(RemoteObservationManager.class).notify(isA(LocalEventData.class));
    expectLastCall().andThrow(new RuntimeException()).once();
    
    replayDefault();
    listener.onEvent(event, source, data);
    verifyDefault();
  }

  @Local
  private class LocalTestEvent implements Event, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean matches(Object otherEvent) {
      return false;
    }    
  }

}
