package com.celements.common.classes.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.classes.IClassesCompositorComponent;
import com.celements.common.test.AbstractComponentTest;
import com.celements.wiki.event.WikiCreatedEvent;

public class WikiCreatedEventListenerTest extends AbstractComponentTest {

  private WikiCreatedEventListener listener;

  private IClassesCompositorComponent classesCmpMock;

  @Before
  public void prepare() throws Exception {
    classesCmpMock = registerComponentMock(IClassesCompositorComponent.class);
    listener = getBeanFactory().getBean(WikiCreatedEventListener.class);
  }

  @Test
  public void test_getOrder() {
    assertEquals(-200, listener.getOrder());
  }

  @Test
  public void test_onApplicationEvent() {
    String database = "db";
    WikiCreatedEvent event = new WikiCreatedEvent(new WikiReference(database));

    classesCmpMock.checkClasses();
    expectLastCall().andDelegateTo(new TestClassesCompositor(database)).once();

    String db = getXContext().getDatabase();
    replayDefault();
    listener.onApplicationEvent(event);
    verifyDefault();
    assertEquals(db, getXContext().getDatabase());
  }

  private class TestClassesCompositor implements IClassesCompositorComponent {

    private final String database;

    TestClassesCompositor(String database) {
      this.database = database;
    }

    @Override
    public void checkClasses() {
      assertEquals(database, getXContext().getDatabase());
    }

    @Override
    public void checkAllClassCollections() {
      assertEquals(database, getXContext().getDatabase());
    }

    @Override
    public boolean isActivated(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void checkClasses(WikiReference wikiRef) {
      assertEquals(database, wikiRef.getName());
    }
  }

}
