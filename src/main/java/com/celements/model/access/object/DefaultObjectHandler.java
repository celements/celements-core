package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.filter.ObjectFilter;
import com.celements.model.classes.fields.ClassField;

@NotThreadSafe
public class DefaultObjectHandler<D, O> implements ObjectHandler<D, O> {

  private final D doc;
  private final ObjectBridge<D, O> bridge;
  private final ObjectFilter.Builder filterBuilder;

  private DefaultObjectHandler(D doc, ObjectBridge<D, O> bridge, ObjectFilter.Builder builder) {
    this.doc = checkNotNull(doc);
    this.bridge = checkNotNull(bridge);
    this.filterBuilder = checkNotNull(builder);
  }

  protected DefaultObjectHandler(@NotNull D doc, @NotNull ObjectBridge<D, O> bridge) {
    this(doc, bridge, new ObjectFilter.Builder());
  }

  protected DefaultObjectHandler(@NotNull D doc, @NotNull ObjectBridge<D, O> bridge,
      @NotNull ObjectFilter filter) {
    this(doc, bridge, checkNotNull(filter).newBuilder());
  }

  protected final D getDoc() {
    return doc;
  }

  protected final ObjectBridge<D, O> getBridge() {
    return bridge;
  }

  @Override
  public final ObjectFilter getFilter() {
    return filterBuilder.build();
  }

  @Override
  public final ObjectHandler<D, O> with(ObjectFilter filter) {
    filterBuilder.add(filter);
    return this;
  }

  @Override
  public final ObjectHandler<D, O> filter(ClassReference classRef) {
    filterBuilder.add(classRef);
    return this;
  }

  @Override
  public final <T> ObjectHandler<D, O> filter(ClassField<T> field, T value) {
    filterBuilder.add(field, value);
    return this;
  }

  @Override
  public final <T> ObjectHandler<D, O> filter(ClassField<T> field, Collection<T> values) {
    filterBuilder.add(field, values);
    return this;
  }

  @Override
  public final ObjectHandler<D, O> filterAbsent(ClassField<?> field) {
    filterBuilder.addAbsent(field);
    return this;
  }

  @Override
  public final ObjectFetcher<D, O> fetch() {
    return new DefaultObjectFetcher<>(doc, getFilter(), bridge);
  }

  @Override
  public final ObjectEditor<D, O> edit() {
    return new DefaultObjectEditor<>(doc, getFilter(), bridge);
  }

}
