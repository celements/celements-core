package com.celements.observation.save;

import static com.celements.observation.save.SaveEventOperation.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;

public class SaveEventFilterTest extends AbstractComponentTest {

  @Test
  public void test_matches() {
    for (SaveEventOperation ops1 : values()) {
      for (SaveEventOperation ops2 : values()) {
        assertEquals(ops1 == ops2, filter(ops1).matches(filter(ops2)));
        assertEquals(ops1 == ops2, filter(ops2).matches(filter(ops1)));
        assertEquals(ops1 == ops2, filter(ops1).matches(filter(ops2, "identity")));
        assertEquals(ops1 == ops2, filter(ops1, "identity").matches(filter(ops2)));
        assertEquals(ops1 == ops2, filter(ops1, "identity").matches(filter(ops2, "identity")));
        assertEquals(false, filter(ops1, "identity1").matches(filter(ops2, "identity2")));
        assertEquals(false, filter(ops1, "identity2").matches(filter(ops2, "identity1")));
      }
      assertTrue(filter("identity").matches(filter("identity")));
      assertFalse(filter("identity1").matches(filter("identity2")));
      assertFalse(filter("identity2").matches(filter("identity1")));
      assertTrue(filter("identity").matches(filter(ops1, "identity")));
      assertTrue(filter(ops1, "identity").matches(filter("identity")));
    }
  }

  @Test
  public void test_getFilter() {
    assertEquals("CREATED:identity", filter(CREATED, "identity").getFilter());
    assertEquals("DELETING:identity2", filter(DELETING, "identity2").getFilter());
    assertEquals(".*:identity", filter("identity").getFilter());
    assertEquals("CREATED:.*", filter(CREATED).getFilter());
  }

  public static SaveEventFilter<String> filter(SaveEventOperation operation) {
    return new SaveEventFilter<>(operation, null);
  }

  public static SaveEventFilter<String> filter(String identity) {
    return new SaveEventFilter<>(null, identity);
  }

  public static SaveEventFilter<String> filter(SaveEventOperation operation, String identity) {
    return new SaveEventFilter<>(operation, identity);
  }

}
