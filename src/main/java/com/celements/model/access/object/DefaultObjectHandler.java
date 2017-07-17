package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.restriction.ClassRestriction;
import com.celements.model.access.object.restriction.FieldAbsentRestriction;
import com.celements.model.access.object.restriction.FieldRestriction;
import com.celements.model.access.object.restriction.ObjectQuery;
import com.celements.model.access.object.restriction.ObjectRestriction;
import com.celements.model.classes.fields.ClassField;

@NotThreadSafe
public class DefaultObjectHandler<D, O> implements ObjectHandler<D, O> {

  private final D doc;
  private final ObjectBridge<D, O> bridge;
  private final ObjectQuery<O> query;

  protected DefaultObjectHandler(D doc, ObjectBridge<D, O> bridge, ObjectQuery<O> query) {
    this.doc = checkNotNull(doc);
    this.bridge = checkNotNull(bridge);
    this.query = new ObjectQuery<>(query);
  }

  protected DefaultObjectHandler(@NotNull D doc, @NotNull ObjectBridge<D, O> bridge) {
    this(doc, bridge, new ObjectQuery<O>());
  }

  protected final D getDoc() {
    return doc;
  }

  protected final ObjectBridge<D, O> getBridge() {
    return bridge;
  }

  @Override
  public final ObjectQuery<O> getQuery() {
    return new ObjectQuery<>(query);
  }

  @Override
  public final ObjectHandler<D, O> with(ObjectQuery<O> query) {
    query.addAll(query);
    return this;
  }

  @Override
  public final ObjectHandler<D, O> filter(ObjectRestriction<O> restriction) {
    query.add(checkNotNull(restriction));
    return this;
  }

  @Override
  public final ObjectHandler<D, O> filter(ClassReference classRef) {
    filter(new ClassRestriction<>(bridge, classRef));
    return this;
  }

  @Override
  public final <T> ObjectHandler<D, O> filter(ClassField<T> field, T value) {
    filter(new FieldRestriction<>(bridge, field, value));
    return this;
  }

  @Override
  public final <T> ObjectHandler<D, O> filter(ClassField<T> field, Collection<T> values) {
    filter(new FieldRestriction<>(bridge, field, values));
    return this;
  }

  @Override
  public final ObjectHandler<D, O> filterAbsent(ClassField<?> field) {
    filter(new FieldAbsentRestriction<>(bridge, field));
    return this;
  }

  @Override
  public final ObjectFetcher<D, O> fetch() {
    return new DefaultObjectFetcher<>(doc, query, bridge);
  }

  @Override
  public final ObjectEditor<D, O> edit() {
    return new DefaultObjectEditor<>(doc, query, bridge);
  }

}
