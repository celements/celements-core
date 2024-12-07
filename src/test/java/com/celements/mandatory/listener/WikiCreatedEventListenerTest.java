package com.celements.mandatory.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.mandatory.IMandatoryDocumentCompositorRole;
import com.celements.wiki.event.WikiCreatedEvent;

public class WikiCreatedEventListenerTest extends AbstractComponentTest {

  private WikiCreatedEventListener listener;

  private IMandatoryDocumentCompositorRole mandatoryDocCmpMock;

  @Before
  public void prepare() throws Exception {
    mandatoryDocCmpMock = registerComponentMock(IMandatoryDocumentCompositorRole.class);
    listener = getBeanFactory().getBean(WikiCreatedEventListener.class);
  }

  @Test
  public void test_getOrder() {
    assertEquals(-100, listener.getOrder());
  }

  @Test
  public void test_onApplicationEvent() {
    String database = "db";
    WikiCreatedEvent event = new WikiCreatedEvent(new WikiReference(database));

    mandatoryDocCmpMock.checkAllMandatoryDocuments();
    expectLastCall().andDelegateTo(new TestMandatoryDocumentCompositor(database)).once();

    String db = getXContext().getDatabase();
    replayDefault();
    listener.onApplicationEvent(event);
    verifyDefault();
    assertEquals(db, getXContext().getDatabase());
  }

  private class TestMandatoryDocumentCompositor implements IMandatoryDocumentCompositorRole {

    private final String database;

    TestMandatoryDocumentCompositor(String database) {
      this.database = database;
    }

    @Override
    public void checkAllMandatoryDocuments() {
      assertEquals(database, getXContext().getDatabase());
    }

    @Override
    public void checkAllMandatoryDocuments(WikiReference wikiRef) {
      assertEquals(database, wikiRef.getName());
    }
  }

}
