package com.celements.common.classes.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.ApplicationReadyEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.classes.IClassesCompositorComponent;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class ApplicationReadyEventListenerTest extends AbstractBridgedComponentTestCase {

  private ApplicationReadyEventListener listener;

  private RemoteObservationManagerContext remoteObsMngContextMock;
  private IClassesCompositorComponent classesCmpMock;

  @Before
  public void setUp_ApplicationReadyEventListenerTest() throws Exception {
    listener = (ApplicationReadyEventListener) Utils.getComponent(EventListener.class,
        "celements.classes.ApplicationReadyEventListener");
    remoteObsMngContextMock = createMockAndAddToDefault(
        RemoteObservationManagerContext.class);
    listener.remoteObservationManagerContext = remoteObsMngContextMock;
    classesCmpMock = createMockAndAddToDefault(IClassesCompositorComponent.class);
    listener.classesCompositor = classesCmpMock;
  }

  @Test
  public void testGetName() {
    assertEquals("celements.classes.ApplicationReadyEventListener", listener.getName());
  }

  @Test
  public void testGetEvents() {
    assertEquals(1, listener.getEvents().size());
    assertSame(ApplicationReadyEvent.class, listener.getEvents().get(0).getClass());
  }

  @Test
  public void testOnEvent() throws Exception {
    List<String> virtWikis = Arrays.asList("db1", "db2");
    
    expect(remoteObsMngContextMock.isRemoteState()).andReturn(false).atLeastOnce();
    expect(getWikiMock().ParamAsLong(eq("celements.classCollections.checkOnStart"), 
        eq(1L))).andReturn(1L).atLeastOnce();
    expect(getWikiMock().isVirtualMode()).andReturn(true).once();
    expect(getWikiMock().getVirtualWikisDatabaseNames(same(getContext()))).andReturn(
        virtWikis).once();

    classesCmpMock.checkAllClassCollections();
    expectLastCall().andDelegateTo(new TestClassesCompositor(getContext().getMainXWiki())
        ).once();
    classesCmpMock.checkAllClassCollections();
    expectLastCall().andDelegateTo(new TestClassesCompositor(virtWikis.get(0))).once();
    classesCmpMock.checkAllClassCollections();
    expectLastCall().andDelegateTo(new TestClassesCompositor(virtWikis.get(1))).once();
    
    String db = getContext().getDatabase();
    replayDefault();
    listener.onEvent(new ApplicationReadyEvent(), null, null);
    verifyDefault();
    assertEquals(db, getContext().getDatabase());
  }

  @Test
  public void testOnEvent_notCheckOnStart() {
    expect(remoteObsMngContextMock.isRemoteState()).andReturn(false).atLeastOnce();
    expect(getWikiMock().ParamAsLong(eq("celements.classCollections.checkOnStart"), 
        eq(1L))).andReturn(0L).atLeastOnce();
    
    replayDefault();
    listener.onEvent(new ApplicationReadyEvent(), null, null);
    verifyDefault();
  }

  @Test
  public void testOnEvent_remote() {
    expect(remoteObsMngContextMock.isRemoteState()).andReturn(true).atLeastOnce();
    expect(getWikiMock().ParamAsLong(eq("celements.classCollections.checkOnStart"), 
        eq(1L))).andReturn(1L).atLeastOnce();
    
    replayDefault();
    listener.onEvent(new ApplicationReadyEvent(), null, null);
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
    
  }

}
