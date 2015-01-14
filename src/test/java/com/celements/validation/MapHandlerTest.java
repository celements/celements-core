package com.celements.validation;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class MapHandlerTest {

  private MapHandler<String, Integer, String> mapHandler;

  @Before
  public void setUp_MapHandlerTest() throws Exception {
    mapHandler = new MapHandler<String, Integer, String>();
  }

  @Test
  public void testPut_Value() {
    int key = 0;
    Map<Integer, Set<String>> toMap = new HashMap<Integer, Set<String>>();
    Set<String> set = new HashSet<String>();
    set.add("old");
    toMap.put(key, set);

    mapHandler.put(key, "new1", toMap);
    mapHandler.put(key, "new2", toMap);

    assertEquals(1, toMap.size());
    set = toMap.get(key);
    assertNotNull(set);
    assertEquals(3, set.size());
    assertTrue(set.contains("old"));
    assertTrue(set.contains("new1"));
    assertTrue(set.contains("new2"));
  }

  @Test
  public void testPut_Set() {
    int key = 0;
    Map<Integer, Set<String>> toMap = new HashMap<Integer, Set<String>>();
    Set<String> set = new HashSet<String>();
    set.add("old");
    toMap.put(key, set);

    Set<String> putSet = new HashSet<String>();
    putSet.add("new1");
    putSet.add("new2");

    mapHandler.put(key, putSet, toMap);

    assertEquals(1, toMap.size());
    set = toMap.get(key);
    assertNotNull(set);
    assertEquals(3, set.size());
    assertTrue(set.contains("old"));
    assertTrue(set.contains("new1"));
    assertTrue(set.contains("new2"));
  }

  @Test
  public void testMergeMaps() {
    int key = 0;
    Map<Integer, Set<String>> toMap = new HashMap<Integer, Set<String>>();
    Set<String> set = new HashSet<String>();
    set.add("old");
    toMap.put(key, set);

    Map<Integer, Set<String>> mergeMap = new HashMap<Integer, Set<String>>();
    set = new HashSet<String>();
    set.add("new1");
    mergeMap.put(key, set);
    set = new HashSet<String>();
    set.add("new2");
    mergeMap.put(key + 1, set);
    mergeMap.put(key + 2, new HashSet<String>());

    mapHandler.mergeMaps(mergeMap, toMap);

    assertEquals(2, toMap.size());
    set = toMap.get(key);
    assertNotNull(set);
    assertEquals(2, set.size());
    assertTrue(set.contains("old"));
    assertTrue(set.contains("new1"));
    set = toMap.get(key + 1);
    assertNotNull(set);
    assertEquals(1, set.size());
    assertTrue(set.contains("new2"));
  }

  @Test
  public void testPut_Value_2() {
    String key1 = "asdf";
    String key2 = "fdsa";
    Map<String, Map<Integer, Set<String>>> toMap =
        new HashMap<String, Map<Integer,Set<String>>>();
    Map<Integer, Set<String>> innerMap = new HashMap<Integer, Set<String>>();
    Set<String> set = new HashSet<String>();
    set.add("old");
    innerMap.put(0, set);
    toMap.put(key1, innerMap);

    mapHandler.put(key1, 1, "new1", toMap);
    mapHandler.put(key2, 0, "new2", toMap);

    assertEquals(2, toMap.size());
    innerMap = toMap.get(key1);
    assertNotNull(innerMap);
    assertEquals(2, innerMap.size());
    set = innerMap.get(0);
    assertEquals(1, set.size());
    assertTrue(set.contains("old"));
    set = innerMap.get(1);
    assertEquals(1, set.size());
    assertTrue(set.contains("new1"));
    innerMap = toMap.get(key2);
    assertNotNull(innerMap);
    assertEquals(1, innerMap.size());
    set = innerMap.get(0);
    assertEquals(1, set.size());
    assertTrue(set.contains("new2"));
  }

  @Test
  public void testPut_Map() {
    String key1 = "asdf";
    String key2 = "fdsa";
    Map<String, Map<Integer, Set<String>>> toMap =
        new HashMap<String, Map<Integer,Set<String>>>();
    Map<Integer, Set<String>> innerMap = new HashMap<Integer, Set<String>>();
    Set<String> set = new HashSet<String>();
    set.add("old");
    innerMap.put(0, set);
    toMap.put(key1, innerMap);

    Map<Integer, Set<String>> putMap1 = new HashMap<Integer, Set<String>>();
    set = new HashSet<String>();
    set.add("new1");
    putMap1.put(1, set);
    Map<Integer, Set<String>> putMap2 = new HashMap<Integer, Set<String>>();
    set = new HashSet<String>();
    set.add("new2");
    putMap2.put(0, set);

    mapHandler.put(key1, putMap1, toMap);
    mapHandler.put(key2, putMap2, toMap);

    assertEquals(2, toMap.size());
    innerMap = toMap.get(key1);
    assertNotNull(innerMap);
    assertEquals(2, innerMap.size());
    set = innerMap.get(0);
    assertEquals(1, set.size());
    assertTrue(set.contains("old"));
    set = innerMap.get(1);
    assertEquals(1, set.size());
    assertTrue(set.contains("new1"));
    innerMap = toMap.get(key2);
    assertNotNull(innerMap);
    assertEquals(1, innerMap.size());
    set = innerMap.get(0);
    assertEquals(1, set.size());
    assertTrue(set.contains("new2"));
  }

  @Test
  public void testMergeMultiMaps() {
    String key1 = "asdf";
    String key2 = "fdsa";
    Map<String, Map<Integer, Set<String>>> toMap =
        new HashMap<String, Map<Integer,Set<String>>>();
    Map<Integer, Set<String>> innerMap = new HashMap<Integer, Set<String>>();
    Set<String> set = new HashSet<String>();
    set.add("old");
    innerMap.put(0, set);
    toMap.put(key1, innerMap);

    Map<String, Map<Integer, Set<String>>> mergeMap =
        new HashMap<String, Map<Integer,Set<String>>>();
    innerMap = new HashMap<Integer, Set<String>>();
    set = new HashSet<String>();
    set.add("new1");
    innerMap.put(1, set);
    mergeMap.put(key1, innerMap);
    innerMap = new HashMap<Integer, Set<String>>();
    set = new HashSet<String>();
    set.add("new2");
    innerMap.put(0, set);
    mergeMap.put(key2, innerMap);

    mapHandler.mergeMultiMaps(mergeMap, toMap);

    assertEquals(2, toMap.size());
    innerMap = toMap.get(key1);
    assertNotNull(innerMap);
    assertEquals(2, innerMap.size());
    set = innerMap.get(0);
    assertEquals(1, set.size());
    assertTrue(set.contains("old"));
    set = innerMap.get(1);
    assertEquals(1, set.size());
    assertTrue(set.contains("new1"));
    innerMap = toMap.get(key2);
    assertNotNull(innerMap);
    assertEquals(1, innerMap.size());
    set = innerMap.get(0);
    assertEquals(1, set.size());
    assertTrue(set.contains("new2"));
  }

}
