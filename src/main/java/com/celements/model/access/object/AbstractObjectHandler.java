package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.filter.ObjectFilter;
import com.celements.model.access.object.filter.ObjectFilterView;
import com.celements.model.classes.fields.ClassField;

public abstract class AbstractObjectHandler<D, O> implements ObjectHandler<D, O> {

  private final D doc;
  private final ObjectFilter filter = new ObjectFilter();

  protected AbstractObjectHandler(@NotNull D doc) {
    this.doc = checkNotNull(doc);
  }

  protected @NotNull D getDoc() {
    return doc;
  }

  protected @NotNull ObjectFilterView getFilter() {
    return filter.createView();
  }

  @Override
  public ObjectHandler<D, O> filter(ClassReference classRef) {
    filter.add(checkNotNull(classRef));
    return this;
  }

  @Override
  public <T> ObjectHandler<D, O> filter(ClassField<T> field, T value) {
    filter.add(checkNotNull(field), checkNotNull(value));
    return this;
  }

  @Override
  public <T> ObjectHandler<D, O> filter(ClassField<T> field, Collection<T> values) {
    checkNotNull(field);
    checkArgument(!checkNotNull(values).isEmpty(), "cannot filter for empty value list");
    for (T value : values) {
      filter.add(field, value);
    }
    return this;
  }

  @Override
  public ObjectHandler<D, O> filterAbsent(ClassField<?> field) {
    filter.addAbsent(checkNotNull(field));
    return this;
  }

}
