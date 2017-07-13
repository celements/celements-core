package com.celements.model.access.object.filter;

import static com.google.common.base.Preconditions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.fields.ClassField;

@NotThreadSafe
class ObjectFilterMap extends
    LinkedHashMap<ClassReference, Map<ClassField<?>, ObjectFilterEntry<?>>> {

  private static final long serialVersionUID = 1L;

  @Override
  public Map<ClassField<?>, ObjectFilterEntry<?>> put(ClassReference classRef,
      Map<ClassField<?>, ObjectFilterEntry<?>> fieldMap) {
    throw new UnsupportedOperationException();
  }

  public void add(ClassReference classRef) {
    if (!containsKey(classRef)) {
      super.put(classRef, new HashMap<ClassField<?>, ObjectFilterEntry<?>>());
    }
  }

  public <T> void add(ClassField<T> field, T value) {
    add(field.getClassDef().getClassReference());
    getEntry(field).add(value);
  }

  public void addAbsent(ClassField<?> field) {
    add(field.getClassDef().getClassReference());
    getEntry(field).setAbsent();
  }

  @SuppressWarnings("unchecked")
  public <T> ObjectFilterEntry<T> getEntry(ClassField<T> field) {
    ObjectFilterEntry<?> entry = new ObjectFilterEntry<>();
    ClassReference classRef = field.getClassDef().getClassReference();
    if (containsKey(classRef)) {
      Map<ClassField<?>, ObjectFilterEntry<?>> fieldMap = get(classRef);
      if (fieldMap.containsKey(field)) {
        entry = fieldMap.get(field);
      } else {
        fieldMap.put(field, entry);
      }
    }
    // cast is fine because of ensured type safety
    return (ObjectFilterEntry<T>) entry;
  }

  @Override
  public ObjectFilterMap clone() {
    ObjectFilterMap clone = new ObjectFilterMap();
    for (ClassReference classRef : keySet()) {
      clone.add(classRef);
      for (ClassField<?> field : get(classRef).keySet()) {
        ObjectFilterEntry<?> entryClone = get(classRef).get(field).clone();
        clone.get(classRef).put(field, entryClone);
      }
    }
    return clone;
  }

}

class ObjectFilterEntry<T> {

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
  public ObjectFilterEntry<T> clone() {
    ObjectFilterEntry<T> clone = new ObjectFilterEntry<>();
    clone.absent = absent;
    clone.values.addAll(values);
    return clone;
  }

}
