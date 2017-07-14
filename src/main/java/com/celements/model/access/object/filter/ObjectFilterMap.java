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
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@NotThreadSafe
class ObjectFilterMap extends
    LinkedHashMap<ClassReference, Map<ClassField<?>, ObjectFilterEntry<?>>> {

  private static final long serialVersionUID = 1L;

  ObjectFilterMap() {
  }

  ObjectFilterMap(ObjectFilterMap map) {
    add(map);
  }

  void add(ClassReference classRef) {
    if (!containsKey(classRef)) {
      put(classRef, new HashMap<ClassField<?>, ObjectFilterEntry<?>>());
    }
  }

  <T> void add(ClassField<T> field, T value) {
    add(field.getClassDef().getClassReference());
    getOrCreateEntry(field).add(value);
  }

  <T> void addAbsent(ClassField<T> field) {
    add(field.getClassDef().getClassReference());
    getOrCreateEntry(field).setAbsent();
  }

  <T> void add(ObjectFilterMap map) {
    for (ClassReference classRef : map.keySet()) {
      add(classRef);
      for (ClassField<?> field : map.get(classRef).keySet()) {
        get(classRef).put(field, map.get(classRef).get(field).clone());
      }
    }
  }

  <T> Optional<ObjectFilterEntry<T>> getEntry(ClassField<T> field) {
    ObjectFilterEntry<T> entry = null;
    if (getFieldMap(field) != null) {
      entry = getFieldMap(field).get(field);
    }
    return Optional.fromNullable(entry);
  }

  private <T> ObjectFilterEntry<T> getOrCreateEntry(ClassField<T> field) {
    ObjectFilterEntry<T> entry = getFieldMap(field).get(field);
    if (entry == null) {
      getFieldMap(field).put(field, entry = new ObjectFilterEntry<>());
    }
    return entry;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private <T> Map<ClassField<T>, ObjectFilterEntry<T>> getFieldMap(ClassField<T> field) {
    ClassReference classRef = field.getClassDef().getClassReference();
    // unchecked cast is fine because of ensured type safety by add methods
    Map<ClassField<T>, ObjectFilterEntry<T>> map = (Map) get(classRef);
    if (map == null) {
      map = ImmutableMap.of();
    }
    return map;
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
