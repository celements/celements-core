package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.filter.ObjectFilter;
import com.celements.model.access.object.filter.ObjectFilterView;
import com.celements.model.classes.fields.ClassField;

@NotThreadSafe
public class ObjectHandler<D, O> {

  private final D doc;
  private final ObjectBridge<D, O> bridge;
  private final ObjectFilter filter;

  private ObjectHandler(D doc, ObjectBridge<D, O> bridge, ObjectFilter filter) {
    this.doc = checkNotNull(doc);
    this.bridge = checkNotNull(bridge);
    this.filter = checkNotNull(filter);
  }

  protected ObjectHandler(@NotNull D doc, @NotNull ObjectBridge<D, O> bridge) {
    this(doc, bridge, new ObjectFilter());
  }

  protected ObjectHandler(@NotNull D doc, @NotNull ObjectBridge<D, O> bridge,
      @NotNull ObjectFilterView filterView) {
    this(doc, bridge, checkNotNull(filterView).getFilter());
  }

  protected final D getDoc() {
    return doc;
  }

  protected final ObjectBridge<D, O> getBridge() {
    return bridge;
  }

  public final ObjectFilterView getFilter() {
    return filter.createView();
  }

  public final ObjectHandler<D, O> filter(ClassReference classRef) {
    filter.add(checkNotNull(classRef));
    return this;
  }

  public final <T> ObjectHandler<D, O> filter(ClassField<T> field, T value) {
    filter.add(checkNotNull(field), checkNotNull(value));
    return this;
  }

  public final <T> ObjectHandler<D, O> filter(ClassField<T> field, Collection<T> values) {
    checkNotNull(field);
    checkArgument(!checkNotNull(values).isEmpty(), "cannot filter for empty value list");
    for (T value : values) {
      filter.add(field, value);
    }
    return this;
  }

  public final ObjectHandler<D, O> filterAbsent(ClassField<?> field) {
    filter.addAbsent(checkNotNull(field));
    return this;
  }

  public final ObjectFetcher<D, O> fetch() {
    return new ObjectFetcher<>(doc, getFilter(), bridge);
  }

  public final ObjectEditor<D, O> edit() {
    return new ObjectEditor<>(doc, getFilter(), bridge);
  }

}
