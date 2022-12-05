package com.celements.web.comparators;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class ObjectComparator implements Comparator<Object> {

  private final CollectionComparator<Object> collComparator = new CollectionComparator<>(this);

  @Override
  @SuppressWarnings("unchecked")
  public int compare(Object o1, Object o2) {
    o1 = unwrapOptional(o1);
    o2 = unwrapOptional(o2);
    final boolean isAssignable = isAssignable(o1, o2);
    if ((o1 instanceof Collection<?>) && isAssignable) {
      return collComparator.compare((Collection<Object>) o1, (Collection<Object>) o2);
    } else if ((o1 instanceof Comparable<?>) && isAssignable) {
      return ((Comparable<Object>) o1).compareTo(o2);
    } else {
      return Objects.toString(o1, "").compareTo(Objects.toString(o2, ""));
    }
  }

  private Object unwrapOptional(Object obj) {
    if (obj instanceof Optional<?>) {
      return ((Optional<?>) obj).orElse(null);
    } else if (obj instanceof com.google.common.base.Optional<?>) {
      return ((com.google.common.base.Optional<?>) obj).orNull();
    } else {
      return obj;
    }
  }

  private boolean isAssignable(Object o1, Object o2) {
    return (o1 != null) && (o2 != null)
        && (o1.getClass().isAssignableFrom(o2.getClass())
            || o2.getClass().isAssignableFrom(o1.getClass()));
  }
}
