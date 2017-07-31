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
import com.celements.model.access.object.restriction.ObjectQueryBuilder;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

@NotThreadSafe
public abstract class AbstractObjectEditor<R extends AbstractObjectEditor<R, D, O>, D, O> extends
    ObjectQueryBuilder<R, O> implements ObjectEditor<D, O> {

  protected final D doc;

  protected AbstractObjectEditor(@NotNull D doc) {
    this.doc = checkNotNull(doc);
    getBridge().checkDoc(doc);
  }

  @Override
  public DocumentReference getDocRef() {
    return getBridge().getDocRef(doc);
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
    return from(getQuery().getClassRefs()).toMap(new ObjectCreateFunction(ifNotExists));
  }

  @Override
  public O createFirst() {
    return createFirst(false);
  }

  @Override
  public O createFirstIfNotExists() {
    return createFirst(true);
  }

  private O createFirst(boolean ifNotExists) {
    Optional<ClassReference> classRef = from(getQuery().getClassRefs()).first();
    if (classRef.isPresent()) {
      return new ObjectCreateFunction(ifNotExists).apply(classRef.get());
    } else {
      throw new IllegalArgumentException("no class defined");
    }
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
      getQuery().getFieldRestrictions(classRef).forEach(new Consumer<FieldRestriction<O, ?>>() {

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
        + getQuery() + "]";
  }

  @Override
  protected abstract @NotNull ObjectBridge<D, O> getBridge();

}
