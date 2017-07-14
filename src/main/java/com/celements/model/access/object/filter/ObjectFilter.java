package com.celements.model.access.object.filter;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.ObjectHandler;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;

/**
 * "Type Safe Heterogeneous Container" for information on object filtering: {@link ClassReference}s
 * and {@link ClassField}s with values. used in {@link ObjectHandler}
 */
@Immutable
public class ObjectFilter {

  public static class Builder {

    private final ObjectFilterMap filterMap = new ObjectFilterMap();

    public Builder add(@NotNull ObjectFilter filter) {
      filterMap.add(filter.map);
      return this;
    }

    public Builder add(@NotNull ClassReference classRef) {
      filterMap.add(checkNotNull(classRef));
      return this;
    }

    public <T> Builder add(@NotNull ClassField<T> field, @NotNull T value) {
      filterMap.add(checkNotNull(field), checkNotNull(value));
      return this;
    }

    public <T> Builder add(@NotNull ClassField<T> field, @NotNull Collection<T> values) {
      for (T value : checkNotNull(values)) {
        add(checkNotNull(field), value);
      }
      return this;
    }

    public <T> Builder addAbsent(@NotNull ClassField<T> field) {
      filterMap.addAbsent(checkNotNull(field));
      return this;
    }

    public @NotNull ObjectFilter build() {
      return new ObjectFilter(new ObjectFilterMap(filterMap));
    }

  }

  private final ObjectFilterMap map;

  private ObjectFilter(ObjectFilterMap map) {
    this.map = map;
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
    Optional<ObjectFilterEntry<T>> entry = map.getEntry(field);
    return entry.isPresent() ? Collections.unmodifiableSet(entry.get().getValues())
        : Collections.<T>emptySet();
  }

  public <T> boolean isAbsent(@NotNull ClassField<T> field) {
    Optional<ObjectFilterEntry<T>> entry = map.getEntry(field);
    return entry.isPresent() && entry.get().isAbsent();
  }

  public @NotNull Builder newBuilder() {
    return new Builder().add(this);
  }

}
