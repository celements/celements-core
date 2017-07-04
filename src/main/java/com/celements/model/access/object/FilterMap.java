package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.fields.ClassField;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Type Safe Heterogenous Container for ClassField values
 */
class FilterMap {

  // type safe inner map
  private final Map<ClassReference, Map<ClassField<?>, FieldValues<?>>> map = new LinkedHashMap<>();

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public List<ClassReference> getClassRefs() {
    return ImmutableList.copyOf(map.keySet());
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
    Set<ClassField<?>> ret = ImmutableSet.of();
    if (hasFields(classRef)) {
      ret = ImmutableSet.copyOf(map.get(classRef).keySet());
    }
    return ret;
  }

  public boolean isAbsent(ClassField<?> field) {
    return getFieldValues(field).absent;
  }

  public <T> boolean hasValue(ClassField<T> field, T value) {
    return getFieldValues(field).values.contains(value);
  }

  public <T> Set<T> getValues(ClassField<T> field) {
    return ImmutableSet.copyOf(getFieldValues(field).values);
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
