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
 * Type Safe Heterogenous Container for object filtering
 */
public class ObjectFilter {

  ObjectFilter() {
  }

  private class Entry<T> {

    private final Set<T> values = new HashSet<>();
    private boolean absent = false;

    void add(T value) {
      checkState(!absent, "filter entry already absent");
      values.add(value);
    }

    void absent() {
      checkState(values.isEmpty(), "filter entry already present");
      absent = true;
    }

  }

  // type safe inner map
  private final Map<ClassReference, Map<ClassField<?>, Entry<?>>> map = new LinkedHashMap<>();

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
    getEntry(field).absent();
  }

  @SuppressWarnings("unchecked")
  private <T> Entry<T> getEntry(ClassField<T> field) {
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

  public ObjectFilterView createView() {
    return new ObjectFilterView();
  }

  public class ObjectFilterView {

    private ObjectFilterView() {
    }

    public boolean isEmpty() {
      return map.isEmpty();
    }

    public Set<ClassReference> getClassRefs() {
      return Collections.unmodifiableSet(map.keySet());
    }

    public boolean hasFields(ClassReference classRef) {
      Map<ClassField<?>, Entry<?>> fieldMap = map.get(classRef);
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
      return getEntry(field).values.contains(value);
    }

    public <T> Set<T> getValues(ClassField<T> field) {
      return Collections.unmodifiableSet(getEntry(field).values);
    }

    public boolean isAbsent(ClassField<?> field) {
      return getEntry(field).absent;
    }

  }

}
