package com.celements.mandatory;

import static org.easymock.EasyMock.expect;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.WikiCreatedEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class WikiCreatedEventListenerTest extends AbstractBridgedComponentTestCase {

  private WikiCreatedEventListener wikiCreatedEventListener;

  @Before
  public void setUp_WikiCreatedEventListenerTest() throws Exception {
    wikiCreatedEventListener = (WikiCreatedEventListener) getComponentManager().lookup(
        EventListener.class, "celements.mandatory.WikiCreatedEventListener");
  }

  @Test
  public void testOnEvent_remote() {
    // replace services with mocks to cache if anything is executed.
    // on a remote event NOTHING should be done!
    wikiCreatedEventListener.mandatoryDocCmp = createMockAndAddToDefault(
        IMandatoryDocumentCompositorRole.class);
    RemoteObservationManagerContext remoteObsMngContextMock = createMockAndAddToDefault(
        RemoteObservationManagerContext.class);
    wikiCreatedEventListener.remoteObservationManagerContext = remoteObsMngContextMock;
    expect(remoteObsMngContextMock.isRemoteState()).andReturn(true).atLeastOnce();
    replayDefault();
    wikiCreatedEventListener.onEvent(new WikiCreatedEvent(), null, null);
    verifyDefault();
  }

}
