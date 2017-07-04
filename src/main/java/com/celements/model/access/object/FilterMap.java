package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.fields.ClassField;

/**
 * Type Safe Heterogenous Container for ClassField values
 */
class FilterMap {

  // type safe inner map
  private final Map<ClassReference, Map<ClassField<?>, FieldValues<?>>> map = new LinkedHashMap<>();

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public Set<ClassReference> getClassRefs() {
    return Collections.unmodifiableSet(map.keySet());
  }

  public void add(ClassReference classRef) {
    Map<ClassField<?>, FieldValues<?>> fieldMap = map.get(classRef);
    if (fieldMap == null) {
      map.put(classRef, fieldMap = new HashMap<>());
    }
  }

  public <T> void add(ClassField<T> field, T value) {
    add(field.getClassDef().getClassReference());
    getFieldValues(field).add(value);
  }

  public void addAbsent(ClassField<?> field) {
    add(field.getClassDef().getClassReference());
    getFieldValues(field).absent();
  }

  public boolean hasFields(ClassReference classRef) {
    Map<ClassField<?>, FieldValues<?>> fieldMap = map.get(classRef);
    return (fieldMap != null) && !fieldMap.isEmpty();
  }

  public Set<ClassField<?>> getFields(ClassReference classRef) {
    Set<ClassField<?>> ret = Collections.emptySet();
    if (hasFields(classRef)) {
      ret = Collections.unmodifiableSet(map.get(classRef).keySet());
    }
    return ret;
  }

  public <T> boolean hasValue(ClassField<T> field, T value) {
    return getFieldValues(field).values.contains(value);
  }

  public <T> Set<T> getValues(ClassField<T> field) {
    return Collections.unmodifiableSet(getFieldValues(field).values);
  }

  public boolean isAbsent(ClassField<?> field) {
    return getFieldValues(field).absent;
  }

  @SuppressWarnings("unchecked")
  private <T> FieldValues<T> getFieldValues(ClassField<T> field) {
    FieldValues<?> valueSet = new FieldValues<>();
    Map<ClassField<?>, FieldValues<?>> fieldMap = map.get(field.getClassDef().getClassReference());
    if (fieldMap != null) {
      if (fieldMap.containsKey(field)) {
        valueSet = fieldMap.get(field);
      } else {
        fieldMap.put(field, valueSet);
      }
    }
    // cast is fine because of ensured type safety
    return (FieldValues<T>) valueSet;
  }

  private class FieldValues<T> {

    private final Set<T> values = new HashSet<>();
    private boolean absent = false;

    void add(T value) {
      checkState(!absent, "filter field already absent");
      values.add(value);
    }

    void absent() {
      checkState(values.isEmpty(), "filter field already present");
      absent = true;
    }

  }

}
