package com.celements.model.access.object.filter;

import static com.google.common.base.Preconditions.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class Entry<T> {

  private final Set<T> values = new HashSet<>();
  private boolean absent = false;

  void add(T value) {
    checkState(!absent, "filter entry already absent");
    values.add(value);
  }

  boolean contains(T value) {
    return values.contains(value);
  }

  Set<T> getValues() {
    return Collections.unmodifiableSet(values);
  }

  boolean isAbsent() {
    return absent;
  }

  void setAbsent() {
    checkState(values.isEmpty(), "filter entry already present");
    absent = true;
  }

  @Override
  public Entry<T> clone() {
    Entry<T> clone = new Entry<>();
    if (absent) {
      clone.setAbsent();
    } else {
      for (T value : values) {
        clone.add(value);
      }
    }
    return clone;
  }

}
