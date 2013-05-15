package com.celements.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapHandler<K, L, E> {

  public void mergeMultiMaps(Map<K, Map<L, Set<E>>> mergeMap, Map<K, Map<L, Set<E>>> toMap) {
    if (mergeMap != null) {
      for (K key : mergeMap.keySet()) {
        put(key, mergeMap.get(key), toMap);
      }
    }
  }

  public void put(K key, Map<L, Set<E>> putMap, Map<K, Map<L, Set<E>>> toMap) {
    if ((putMap != null) && !putMap.isEmpty()) {
      Map<L, Set<E>> map = toMap.get(key);
      if (map == null) {
        map = new HashMap<L, Set<E>>();
        toMap.put(key, map);
      }
      mergeMaps(putMap, map);
    }
  }

  public void mergeMaps(Map<L, Set<E>> mergeMap, Map<L, Set<E>> toMap) {
    if (mergeMap != null) {
      for (L key : mergeMap.keySet()) {
        put(key, mergeMap.get(key), toMap);
      }
    }
  }

  public void put(L key, Set<E> putSet, Map<L, Set<E>> toMap) {
    if ((putSet != null) && !putSet.isEmpty()) {
      Set<E> set = toMap.get(key);
      if (set == null) {
        set = new HashSet<E>();
        toMap.put(key, set);
      }
      set.addAll(putSet);
    }
  }

}
