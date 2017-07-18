package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.object.restriction.ClassRestriction;
import com.celements.model.access.object.restriction.FieldAbsentRestriction;
import com.celements.model.access.object.restriction.FieldRestriction;
import com.celements.model.access.object.restriction.ObjectQuery;
import com.celements.model.access.object.restriction.ObjectRestriction;
import com.celements.model.classes.fields.ClassField;

@NotThreadSafe
public class DefaultObjectHandler<D, O> implements ObjectHandler<D, O> {

  protected final ObjectBridge<D, O> bridge;
  protected final D doc;
  protected final ObjectQuery<O> query;

  protected DefaultObjectHandler(@NotNull ObjectBridge<D, O> bridge, @NotNull D doc,
      @NotNull ObjectQuery<O> query) {
    this.bridge = checkNotNull(bridge);
    this.doc = checkNotNull(doc);
    this.query = new ObjectQuery<>(query);
    bridge.checkDoc(doc);
  }

  protected DefaultObjectHandler(@NotNull ObjectBridge<D, O> bridge, @NotNull D doc) {
    this(bridge, doc, new ObjectQuery<O>());
  }

  @Override
  public DocumentReference getDocRef() {
    return bridge.getDocRef(doc);
  }

  @Override
  public final ObjectBridge<D, O> getBridge() {
    return bridge;
  }

  @Override
  public final ObjectQuery<O> getQuery() {
    return new ObjectQuery<>(query);
  }

  @Override
  public final ObjectHandler<D, O> with(ObjectQuery<O> query) {
    this.query.addAll(query);
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
  public ObjectFetcher<D, O> fetch() {
    return new DefaultObjectFetcher<>(bridge, doc, query);
  }

  @Override
  public ObjectEditor<D, O> edit() {
    return new DefaultObjectEditor<>(bridge, doc, query);
  }

}
