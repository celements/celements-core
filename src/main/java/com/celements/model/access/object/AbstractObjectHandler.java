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
public abstract class AbstractObjectHandler<D, O> implements ObjectHandler<D, O> {

  protected final D doc;
  protected final ObjectQuery<O> query;

  protected AbstractObjectHandler(@NotNull D doc) {
    this.doc = checkNotNull(doc);
    this.query = new ObjectQuery<>();
    getBridge().checkDoc(doc);
  }

  @Override
  public DocumentReference getDocRef() {
    return getBridge().getDocRef(doc);
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
    filter(new ClassRestriction<>(getBridge(), classRef));
    return this;
  }

  @Override
  public final <T> ObjectHandler<D, O> filter(ClassField<T> field, T value) {
    filter(new FieldRestriction<>(getBridge(), field, value));
    return this;
  }

  @Override
  public final <T> ObjectHandler<D, O> filter(ClassField<T> field, Collection<T> values) {
    filter(new FieldRestriction<>(getBridge(), field, values));
    return this;
  }

  @Override
  public final ObjectHandler<D, O> filterAbsent(ClassField<?> field) {
    filter(new FieldAbsentRestriction<>(getBridge(), field));
    return this;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [doc=" + getBridge().getDocRef(doc) + ", query="
        + query + "]";
  }

  protected abstract @NotNull ObjectBridge<D, O> getBridge();

}
