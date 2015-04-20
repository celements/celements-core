package com.celements.mandatory.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.mandatory.IMandatoryDocumentCompositorRole;

public class ApplicationStartedEventListenerTest extends AbstractBridgedComponentTestCase {

  private ApplicationStartedEventListener listener;

  private RemoteObservationManagerContext remoteObsMngContextMock;
  private IMandatoryDocumentCompositorRole mandatoryDocCmpMock;

  @Before
  public void setUp_ApplicationStartedEventListenerTest() throws Exception {
    listener = (ApplicationStartedEventListener) getComponentManager().lookup(
        EventListener.class, "celements.mandatory.ApplicationStartedEventListener");
    remoteObsMngContextMock = createMockAndAddToDefault(
        RemoteObservationManagerContext.class);
    listener.remoteObservationManagerContext = remoteObsMngContextMock;
    mandatoryDocCmpMock = createMockAndAddToDefault(
        IMandatoryDocumentCompositorRole.class);
    listener.mandatoryDocCmp = mandatoryDocCmpMock;
  }

  @Test
  public void testGetName() {
    assertEquals("celements.mandatory.ApplicationStartedEventListener", listener.getName());
  }

  @Test
  public void testGetEvents() {
    assertEquals(1, listener.getEvents().size());
    assertSame(ApplicationStartedEvent.class, listener.getEvents().get(0).getClass());
  }

  @Test
  public void testOnEvent() throws Exception {
    List<String> virtWikis = Arrays.asList("db1", "db2");
    
    expect(remoteObsMngContextMock.isRemoteState()).andReturn(false).atLeastOnce();
    expect(getWikiMock().ParamAsLong(eq("celements.mandatory.checkOnStart"), eq(1L))
        ).andReturn(1L).atLeastOnce();
    expect(getWikiMock().isVirtualMode()).andReturn(true).once();
    expect(getWikiMock().getVirtualWikisDatabaseNames(same(getContext()))).andReturn(
        virtWikis).once();

    mandatoryDocCmpMock.checkAllMandatoryDocuments();
    expectLastCall().andDelegateTo(new TestMandatoryDocumentCompositor(
        getContext().getMainXWiki())).once();
    mandatoryDocCmpMock.checkAllMandatoryDocuments();
    expectLastCall().andDelegateTo(new TestMandatoryDocumentCompositor(virtWikis.get(0))
        ).once();
    expectLastCall().andDelegateTo(new TestMandatoryDocumentCompositor(virtWikis.get(1))
        ).once();
    
    String db = getContext().getDatabase();
    replayDefault();
    listener.onEvent(new ApplicationStartedEvent(), null, null);
    verifyDefault();
    assertEquals(db, getContext().getDatabase());
  }

  @Test
  public void testOnEvent_notCheckOnStart() {
    expect(remoteObsMngContextMock.isRemoteState()).andReturn(false).atLeastOnce();
    expect(getWikiMock().ParamAsLong(eq("celements.mandatory.checkOnStart"), eq(1L))
        ).andReturn(0L).atLeastOnce();
    
    replayDefault();
    listener.onEvent(new ApplicationStartedEvent(), null, null);
    verifyDefault();
  }

  @Test
  public void testOnEvent_remote() {
    expect(remoteObsMngContextMock.isRemoteState()).andReturn(true).atLeastOnce();
    expect(getWikiMock().ParamAsLong(eq("celements.mandatory.checkOnStart"), eq(1L))
        ).andReturn(1L).atLeastOnce();
    
    replayDefault();
    listener.onEvent(new ApplicationStartedEvent(), null, null);
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
