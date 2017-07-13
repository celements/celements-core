package com.celements.model.access.object.filter;

import java.util.Collections;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.ObjectHandler;
import com.celements.model.classes.fields.ClassField;

/**
 * "Type Safe Heterogeneous Container" for information on object filtering: {@link ClassReference}s
 * and {@link ClassField}s with values. used in {@link ObjectHandler}
 */
@Immutable
public class ObjectFilter {

  public static class Builder extends ObjectFilterMap {

    private static final long serialVersionUID = 1L;

    public Builder() {
    }

    public Builder(ObjectFilterMap map) {
      putAll(map.clone());
    }

    public ObjectFilter build() {
      return new ObjectFilter(clone());
    }

  }

  private final ObjectFilterMap map;

  private ObjectFilter(@NotNull ObjectFilterMap map) {
    this.map = map;
  }

  public @NotNull Builder newBuilder() {
    return new Builder(map);
  }

  public boolean isEmpty() {
    return getClassRefs().isEmpty();
  }

  public @NotNull Set<ClassReference> getClassRefs() {
    return Collections.unmodifiableSet(map.keySet());
  }

  public @NotNull Set<ClassField<?>> getFields(@NotNull ClassReference classRef) {
    Set<ClassField<?>> ret = Collections.emptySet();
    if (map.get(classRef) != null) {
      ret = Collections.unmodifiableSet(map.get(classRef).keySet());
    }
    return ret;
  }

  public @NotNull <T> Set<T> getValues(@NotNull ClassField<T> field) {
    return Collections.unmodifiableSet(map.getEntry(field).getValues());
  }

  public boolean isAbsent(@NotNull ClassField<?> field) {
    return map.getEntry(field).isAbsent();
  }

}
