/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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

  public void put(K key, L key2, E value, Map<K, Map<L, Set<E>>> toMap) {
    Map<L, Set<E>> map = toMap.get(key);
    if (map == null) {
      map = new HashMap<L, Set<E>>();
      toMap.put(key, map);
    }
    put(key2, value, map);
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

  public void put(L key, E value, Map<L, Set<E>> toMap) {
    if (value != null) {
      Set<E> set = toMap.get(key);
      if (set == null) {
        set = new HashSet<E>();
        toMap.put(key, set);
      }
      set.add(value);
    }
  }

}
