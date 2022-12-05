package com.celements.web.comparators;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class ObjectComparator implements Comparator<Object> {

  private final CollectionComparator<Object> collectionCmp = new CollectionComparator<>(this);

  @Override
  @SuppressWarnings("unchecked")
  public int compare(Object o1, Object o2) {
    if (o1 instanceof Optional<?>) {
      o1 = ((Optional<?>) o1).orElse(null);
    }
    if (o2 instanceof Optional<?>) {
      o2 = ((Optional<?>) o2).orElse(null);
    }
    boolean isAssignable = (o1 != null) && (o2 != null)
        && (o1.getClass().isAssignableFrom(o2.getClass())
            || o2.getClass().isAssignableFrom(o1.getClass()));
    if ((o1 instanceof Collection<?>) && isAssignable) {
      return collectionCmp.compare((Collection<Object>) o1, (Collection<Object>) o2);
    } else if ((o1 instanceof Comparable<?>) && isAssignable) {
      return ((Comparable<Object>) o1).compareTo(o2);
    } else {
      return Objects.toString(o1, "").compareTo(Objects.toString(o2, ""));
    }
  }
}
