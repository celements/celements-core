package com.celements.hash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gnu.trove.set.hash.TLongHashSet;

public class HashingSet {

  private final TLongHashSet set;
  private final int[] collisionCount;

  public HashingSet(long initCapacity) {
    initCapacity += 1000;
    if (initCapacity < Integer.MAX_VALUE) {
      System.out.println("Init HashingSet with capacity " + initCapacity);
      set = new TLongHashSet((int) initCapacity, 1);
      collisionCount = new int[(1 << HashingTest.BIT_COLL_HANDLING)];
    } else {
      throw new RuntimeException(initCapacity + " too big for initial capacity");
    }
  }

  public synchronized long addWithCollisionHandling(long id) {
    int count = 0;
    while (set.contains(id)) {
      if (count++ < (1 << HashingTest.BIT_COLL_HANDLING)) {
        id = ((id >> HashingTest.BIT_OFFSET) + 1) << HashingTest.BIT_OFFSET;
        collisionCount[Math.min(count - 1, 3)]++;
      } else {
        throw new RuntimeException("Out of Collision handling range " + id);
      }
    }
    set.add(id);
    return id;
  }

  public synchronized List<Long> addWithCollisionHandling(List<Long> ids) {
    List<Long> ret = new ArrayList<>(ids);
    for (long id : ids) {
      ret.add(this.addWithCollisionHandling(id));
    }
    return ret;
  }

  public synchronized int[] getCollisionCount() {
    return Arrays.copyOf(collisionCount, collisionCount.length);
  }

  public synchronized int size() {
    return set.size();
  }

}
