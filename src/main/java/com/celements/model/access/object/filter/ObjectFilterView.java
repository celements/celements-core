package com.celements.model.access.object.filter;

import java.util.Collections;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.fields.ClassField;

/**
 * View for an {@link ObjectFilter}
 *
 * @author Marc Sladek
 */
@Immutable
public class ObjectFilterView {

  private final ObjectFilter filter;

  ObjectFilterView(ObjectFilter filter) {
    this.filter = filter.clone();
  }

  public @NotNull ObjectFilter getFilter() {
    return filter.clone();
  }

  public boolean isEmpty() {
    return getClassRefs().isEmpty();
  }

  public @NotNull Set<ClassReference> getClassRefs() {
    return filter.getClassRefs();
  }

  public @NotNull Set<ClassField<?>> getFields(@NotNull ClassReference classRef) {
    return filter.getFields(classRef);
  }

  public @NotNull <T> Set<T> getValues(@NotNull ClassField<T> field) {
    return Collections.unmodifiableSet(filter.getEntry(field).getValues());
  }

  public boolean isAbsent(@NotNull ClassField<?> field) {
    return filter.getEntry(field).isAbsent();
  }

}
