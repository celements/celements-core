package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.fields.ClassField;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Type Safe Heterogenous Container for ClassField values
 */
class ClassFieldValues {

  // type safe inner map
  private final Map<ClassReference, Map<ClassField<?>, Set<?>>> map = new LinkedHashMap<>();

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public @NotNull List<ClassReference> getClassRefs() {
    return ImmutableList.copyOf(map.keySet());
  }

  public void add(@NotNull ClassReference classRef) {
    Map<ClassField<?>, Set<?>> fieldMap = map.get(checkNotNull(classRef));
    if (fieldMap == null) {
      map.put(classRef, fieldMap = new HashMap<>());
    }
  }

  public <T> void add(@NotNull ClassField<T> field, @Nullable T value) {
    add(field, ImmutableList.of(value));
  }

  public <T> void add(@NotNull ClassField<T> field, @NotNull Collection<T> values) {
    checkArgument(!checkNotNull(values).isEmpty(), "cannot filter for empty value list");
    add(checkNotNull(field).getClassDef().getClassReference());
    getValueSet(field).addAll(values);
  }

  public boolean hasFields(ClassReference classRef) {
    Map<ClassField<?>, Set<?>> fieldMap = map.get(classRef);
    return (fieldMap != null) && !fieldMap.isEmpty();
  }

  public Set<ClassField<?>> getFields(ClassReference classRef) {
    Set<ClassField<?>> ret = ImmutableSet.of();
    if (hasFields(classRef)) {
      ret = ImmutableSet.copyOf(map.get(classRef).keySet());
    }
    return ret;
  }

  public <T> boolean hasValue(@NotNull ClassField<T> field, @Nullable T value) {
    return getValueSet(field).contains(value);
  }

  public @NotNull <T> Set<T> getValues(@NotNull ClassField<T> field) {
    return ImmutableSet.copyOf(getValueSet(field));
  }

  @SuppressWarnings("unchecked")
  private <T> Set<T> getValueSet(ClassField<T> field) {
    Set<?> valueSet = ImmutableSet.of();
    Map<ClassField<?>, Set<?>> fieldMap = map.get(checkNotNull(
        field).getClassDef().getClassReference());
    if (fieldMap != null) {
      valueSet = fieldMap.get(field);
      if (valueSet == null) {
        fieldMap.put(field, valueSet = new HashSet<>());
      }
    }
    // cast is ok because of ensured type safety
    return (Set<T>) valueSet;
  }

}
