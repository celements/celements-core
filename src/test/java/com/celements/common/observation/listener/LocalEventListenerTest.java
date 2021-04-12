package com.celements.common.observation.listener;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.NotSerializableException;
import java.io.Serializable;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  public void prepareTest() throws Exception {
    registerComponentMocks(RemoteObservationManagerConfiguration.class,
        RemoteObservationManager.class);
    listener = (LocalEventListener) Utils.getComponent(EventListener.class,
        LocalEventListener.NAME);
    listener.injectLogger(LoggerFactory.getLogger(LocalEventListener.class));
    listener.logCountMap.clear();
  }

  @Test
  public void test_getName() {
    assertEquals("observation.remote", listener.getName());
  }

  @Test
  public void test_getEvents_enabled() {
    expect(getMock(RemoteObservationManagerConfiguration.class).isEnabled()).andReturn(
        true).anyTimes();

    replayDefault();
    assertEquals(1, listener.getEvents().size());
    assertEquals(AllEvent.ALLEVENT, listener.getEvents().get(0));
    verifyDefault();
  }

  @Test
  public void test_getEvents_disabled() {
    expect(getMock(RemoteObservationManagerConfiguration.class).isEnabled()).andReturn(
        false).anyTimes();

    replayDefault();
    assertEquals(0, listener.getEvents().size());
    verifyDefault();
  }

  @Test
  public void test_onEvent() {
    Event event = AllEvent.ALLEVENT;
    Object source = createMockAndAddToDefault(Serializable.class);
    Object data = createMockAndAddToDefault(Serializable.class);
    Capture<LocalEventData> localEvDataCapt = newCapture();

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
    Object source = createMockAndAddToDefault(Serializable.class);
    Object data = createMockAndAddToDefault(Serializable.class);

    replayDefault();
    listener.onEvent(event, source, data);
    verifyDefault();
  }

  @Test
  public void test_onEvent_Exception() {
    Event event = AllEvent.ALLEVENT;
    Object source = createMockAndAddToDefault(Serializable.class);
    Object data = createMockAndAddToDefault(Serializable.class);
    Throwable cause = new RuntimeException();

    getMock(RemoteObservationManager.class).notify(isA(LocalEventData.class));
    expectLastCall().andThrow(cause).once();

    replayDefault();
    listener.onEvent(event, source, data);
    verifyDefault();

    assertFalse(listener.logCountMap.containsKey(event.getClass()));
  }

  @Test
  public void test_onEvent_NotSerializableException() {
    Event event = AllEvent.ALLEVENT;
    Object source = createMockAndAddToDefault(Object.class);
    Object data = createMockAndAddToDefault(Object.class);
    Throwable exc = new RuntimeException("", new NotSerializableException());

    getMock(RemoteObservationManager.class).notify(isA(LocalEventData.class));
    expectLastCall().andThrow(exc).once();

    replayDefault();
    listener.onEvent(event, source, data);
    verifyDefault();

    assertTrue(listener.logCountMap.containsKey(event.getClass()));
    assertEquals(1, listener.logCountMap.get(event.getClass()).count.get());
  }

  @Test
  public void test_onEvent_NotSerializableException_logging() {
    int runCount = 13;
    Event event = AllEvent.ALLEVENT;
    Object source = createMockAndAddToDefault(Object.class);
    Object data = createMockAndAddToDefault(Object.class);
    Logger loggerMock = listener.injectLogger(createMockAndAddToDefault(Logger.class));

    Throwable exc = new RuntimeException("", new NotSerializableException());
    getMock(RemoteObservationManager.class).notify(isA(LocalEventData.class));
    expectLastCall().andThrow(exc).anyTimes();
    loggerMock.warn(anyObject(String.class), same(exc));
    expectLastCall().times(2);
    expect(loggerMock.isDebugEnabled()).andReturn(true).anyTimes();
    loggerMock.debug(anyObject(String.class), same(exc));
    expectLastCall().atLeastOnce();

    replayDefault();
    for (int i = 0; i < runCount; i++) {
      listener.onEvent(event, source, data);
    }
    assertEquals(runCount, listener.logCountMap.remove(event.getClass()).count.get());
    for (int i = 0; i < runCount; i++) {
      listener.onEvent(event, source, data);
    }
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
