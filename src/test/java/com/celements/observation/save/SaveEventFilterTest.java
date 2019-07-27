package com.celements.observation.object;

import static com.celements.observation.event.EventOperation.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.xwiki.model.reference.ClassReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.observation.event.EventOperation;

public class ObjectEventFilterTest extends AbstractComponentTest {

  @Test
  public void test_matches() {
    for (EventOperation ops1 : values()) {
      for (EventOperation ops2 : values()) {
        assertEquals(ops1 == ops2, filter(ops1).matches(filter(ops2)));
        assertEquals(ops1 == ops2, filter(ops2).matches(filter(ops1)));
        assertEquals(ops1 == ops2, filter(ops1).matches(filter(ops2, "class")));
        assertEquals(ops1 == ops2, filter(ops1, "class").matches(filter(ops2)));
        assertEquals(ops1 == ops2, filter(ops1, "class").matches(filter(ops2, "class")));
        assertEquals(false, filter(ops1, "class1").matches(filter(ops2, "class2")));
        assertEquals(false, filter(ops1, "class2").matches(filter(ops2, "class1")));
      }
      assertTrue(filter("class").matches(filter("class")));
      assertFalse(filter("class1").matches(filter("class2")));
      assertFalse(filter("class2").matches(filter("class1")));
      assertTrue(filter("class").matches(filter(ops1, "class")));
      assertTrue(filter(ops1, "class").matches(filter("class")));
    }
  }

  @Test
  public void test_getFilter() {
    assertEquals("CREATED:space.class", filter(CREATED, "class").getFilter());
    assertEquals("DELETING:space.class2", filter(DELETING, "class2").getFilter());
    assertEquals(".*:space.class", filter("class").getFilter());
    assertEquals("CREATED:.*", filter(CREATED).getFilter());
  }

  public static ObjectEventFilter filter(EventOperation operation) {
    return new ObjectEventFilter(operation, null);
  }

  public static ObjectEventFilter filter(String className) {
    return new ObjectEventFilter(null, new ClassReference("space", className));
  }

  public static ObjectEventFilter filter(EventOperation operation, String className) {
    return new ObjectEventFilter(operation, new ClassReference("space", className));
  }

}
