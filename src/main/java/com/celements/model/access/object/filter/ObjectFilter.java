package com.celements.model.access.object.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.ObjectHandler;
import com.celements.model.classes.fields.ClassField;

/**
 * "Type Safe Heterogeneous Container" collecting information for object filtering used in
 * {@link ObjectHandler}
 */
@NotThreadSafe
public class ObjectFilter {

  public ObjectFilter() {
  }

  // type safe inner map
  private final Map<ClassReference, Map<ClassField<?>, Entry<?>>> map = new LinkedHashMap<>();

  public ObjectFilterView createView() {
    return new ObjectFilterView(this);
  }

  public void add(ClassReference classRef) {
    Map<ClassField<?>, Entry<?>> fieldMap = map.get(classRef);
    if (fieldMap == null) {
      map.put(classRef, fieldMap = new HashMap<>());
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

  Set<ClassReference> getClassRefs() {
    return Collections.unmodifiableSet(map.keySet());
  }

  Set<ClassField<?>> getFields(ClassReference classRef) {
    Set<ClassField<?>> ret = Collections.emptySet();
    if (map.get(classRef) != null) {
      ret = Collections.unmodifiableSet(map.get(classRef).keySet());
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  <T> Entry<T> getEntry(ClassField<T> field) {
    Entry<?> valueSet = new Entry<>();
    Map<ClassField<?>, Entry<?>> fieldMap = map.get(field.getClassDef().getClassReference());
    if (fieldMap != null) {
      if (fieldMap.containsKey(field)) {
        valueSet = fieldMap.get(field);
      } else {
        fieldMap.put(field, valueSet);
      }
    }
    // cast is fine because of ensured type safety
    return (Entry<T>) valueSet;
  }

  @Override
  public ObjectFilter clone() {
    ObjectFilter clone = new ObjectFilter();
    for (ClassReference classRef : map.keySet()) {
      clone.add(classRef);
      for (ClassField<?> field : map.get(classRef).keySet()) {
        Entry<?> entryClone = map.get(classRef).get(field).clone();
        clone.map.get(classRef).put(field, entryClone);
      }
    }
    return clone;
  }

}
