package com.celements.model.access;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;

public class ContextExecutorTest extends AbstractComponentTest {

  @Test
  public void test() throws Exception {
    WikiReference wiki = new WikiReference("wiki");
    getContext().setDatabase(wiki.getName());
    final WikiReference otherWiki = new WikiReference("other");
    final Integer i = 5;
    ContextExecutor<Integer, Exception> exec = new ContextExecutor<Integer, Exception>() {

      @Override
      protected Integer call() throws Exception {
        assertEquals(otherWiki.getName(), getContext().getDatabase());
        return i;
      }
    };
    assertEquals(wiki.getName(), getContext().getDatabase());
    assertEquals(i, exec.inWiki(otherWiki).execute());
    assertEquals(wiki.getName(), getContext().getDatabase());
  }

  @Test
  public void test_exc() throws Exception {
    final Exception e = new Exception();
    ContextExecutor<Void, Exception> exec = new ContextExecutor<Void, Exception>() {

      @Override
      protected Void call() throws Exception {
        throw e;
      }
    };
    try {
      exec.inWiki(new WikiReference("wiki")).execute();
      fail("expecting Exception");
    } catch (Exception exc) {
      assertSame(e, exc);
    }
  }

  @Test
  public void test_iae() throws Exception {
    ContextExecutor<Void, Exception> exec = new ContextExecutor<Void, Exception>() {

      @Override
      protected Void call() throws Exception {
        return null;
      }
    };
    try {
      exec.execute();
      fail("expecting ISE");
    } catch (IllegalStateException exc) {
      // expected
    }
  }

}
