package com.celements.web.comparators;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;
import java.util.Comparator;

import one.util.streamex.StreamEx;

public class CollectionComparator<T> implements Comparator<Collection<T>> {

  private final Comparator<T> comparator;

  public CollectionComparator(Comparator<T> comparator) {
    this.comparator = checkNotNull(comparator);
  }

  @Override
  public int compare(Collection<T> l1, Collection<T> l2) {
    return StreamEx.of(l1).zipWith(l2.stream())
        .mapKeyValue(comparator::compare)
        .filter(c -> c != 0)
        .findFirst()
        .orElseGet(() -> Integer.compare(l1.size(), l2.size()));
  }
}
