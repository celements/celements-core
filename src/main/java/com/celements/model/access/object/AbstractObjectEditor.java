package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.FluentIterable.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.object.restriction.FieldRestriction;
import com.celements.model.access.object.restriction.ObjectQuery;
import com.celements.model.access.object.restriction.ObjectQueryBuilder;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

@NotThreadSafe
public abstract class AbstractObjectEditor<D, O> implements ObjectEditor<D, O> {

  /**
   * Builder for {@link ObjectEditor}s. Use {@link #filter()} methods to construct the desired
   * query, then use {@link #edit()} to manipulate objects.
   */
  public static abstract class Builder<B extends Builder<B, D, O>, D, O> extends
      ObjectQueryBuilder<B, O> {

    protected final D doc;

    protected Builder(@NotNull ObjectBridge<D, O> bridge, D doc) {
      super(bridge);
      this.doc = doc;
    }

    /**
     * @return a new {@link ObjectEditor} for object manipulation
     */
    @NotNull
    public abstract ObjectEditor<D, O> edit();

    @Override
    protected abstract B getThis();

  }

  protected final D doc;
  protected final ObjectQuery<O> query;

  protected AbstractObjectEditor(@NotNull D doc, @NotNull ObjectQuery<O> query) {
    this.doc = checkNotNull(doc);
    this.query = new ObjectQuery<>(query);
    getBridge().checkDoc(doc);
  }

  @Override
  public DocumentReference getDocRef() {
    return getBridge().getDocRef(doc);
  }

  @Override
  public ObjectQuery<O> getQuery() {
    return new ObjectQuery<>(query);
  }

  @Override
  public Map<ClassReference, O> create() {
    return create(false);
  }

  @Override
  public Map<ClassReference, O> createIfNotExists() {
    return create(true);
  }

  private Map<ClassReference, O> create(boolean ifNotExists) {
    return from(query.getClassRefs()).toMap(new ObjectCreateFunction(ifNotExists));
  }

  @Override
  public Optional<O> createFirst() {
    return createFirst(false);
  }

  @Override
  public Optional<O> createFirstIfNotExists() {
    return createFirst(true);
  }

  private Optional<O> createFirst(boolean ifNotExists) {
    Optional<ClassReference> classRef = from(query.getClassRefs()).first();
    if (classRef.isPresent()) {
      return Optional.of(new ObjectCreateFunction(ifNotExists).apply(classRef.get()));
    }
    return Optional.absent();
  }

  private class ObjectCreateFunction implements Function<ClassReference, O> {

    private final boolean ifNotExists;

    ObjectCreateFunction(boolean ifNotExists) {
      this.ifNotExists = ifNotExists;
    }

    @Override
    public O apply(ClassReference classRef) {
      Optional<O> ret = Optional.absent();
      if (ifNotExists) {
        ret = from(fetch().map().get(classRef)).first();
      }
      if (!ret.isPresent()) {
        ret = Optional.of(createObject(classRef));
      }
      return ret.get();
    }

    private O createObject(ClassReference classRef) {
      final O obj = getBridge().createObject(doc, classRef);
      query.getFieldRestrictions(classRef).forEach(new Consumer<FieldRestriction<O, ?>>() {

        @Override
        public void accept(FieldRestriction<O, ?> restr) {
          updateField(obj, restr);
        }

        private <T> void updateField(O obj, FieldRestriction<O, T> restr) {
          getBridge().setObjectField(obj, restr.getField(), from(restr.getValues()).first().get());
        }
      });
      return obj;
    }

  }

  @Override
  public List<O> delete() {
    return from(fetch().list()).filter(new ObjectRemovePredicate()).toList();
  }

  @Override
  public Optional<O> deleteFirst() {
    Optional<O> obj = fetch().first();
    if (obj.isPresent() && new ObjectRemovePredicate().apply(obj.get())) {
      return obj;
    }
    return Optional.absent();
  }

  private class ObjectRemovePredicate implements Predicate<O> {

    @Override
    public boolean apply(O obj) {
      return getBridge().removeObject(doc, obj);
    }

  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [doc=" + getBridge().getDocRef(doc) + ", query="
        + query + "]";
  }

  protected abstract @NotNull ObjectBridge<D, O> getBridge();

}
