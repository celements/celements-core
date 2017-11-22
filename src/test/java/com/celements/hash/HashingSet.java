package com.celements.hash;

import java.util.Arrays;

import gnu.trove.set.hash.TLongHashSet;

public class HashingSet {

  private final TLongHashSet set;
  private final int[] collisionCount;

  final int bitOffset;
  final int bitCollHandling;

  public HashingSet(long initCapacity, int bitOffset, int bitCollHandling) {
    initCapacity += 1000;
    if (initCapacity < Integer.MAX_VALUE) {
      System.out.println("Initial Set capacity: " + initCapacity);
      set = new TLongHashSet((int) initCapacity, 1);
      collisionCount = new int[(1 << bitCollHandling)];
      this.bitOffset = bitOffset;
      this.bitCollHandling = bitCollHandling;
    } else {
      throw new RuntimeException(initCapacity + " too big for initial capacity");
    }
  }

  public synchronized long addWithCollisionHandling(long id) {
    int count = 0;
    while (set.contains(id)) {
      if (count++ < (1 << bitCollHandling)) {
        id = ((id >> bitOffset) + 1) << bitOffset;
        collisionCount[Math.min(count - 1, 3)]++;
      } else {
        throw new RuntimeException("Out of Collision handling range " + id);
      }
    }
    set.add(id);
    return id;
  }

  public synchronized int[] getCollisionCount() {
    return Arrays.copyOf(collisionCount, collisionCount.length);
  }

  public synchronized int size() {
    return set.size();
  }

  @Override
  public String toString() {
    return "Hash count: " + size() + " - Collisions: " + Arrays.toString(getCollisionCount());
  }

}
