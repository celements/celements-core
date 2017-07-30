package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.restriction.FieldRestriction;
import com.celements.model.access.object.restriction.ObjectQuery;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

@NotThreadSafe
public abstract class AbstractObjectEditor<D, O> implements ObjectEditor<D, O> {

  protected final ObjectBridge<D, O> bridge;
  protected final D doc;
  protected final ObjectQuery<O> query;

  protected AbstractObjectEditor(@NotNull ObjectBridge<D, O> bridge, @NotNull D doc,
      @NotNull ObjectQuery<O> query) {
    this.doc = checkNotNull(doc);
    this.query = new ObjectQuery<>(query);
    this.bridge = checkNotNull(bridge);
  }

  @Override
  public Map<ClassReference, O> create() {
    return FluentIterable.from(query.getClassRefs()).toMap(new ObjectCreateFunction(false));
  }

  @Override
  public Map<ClassReference, O> createIfNotExists() {
    return FluentIterable.from(query.getClassRefs()).toMap(new ObjectCreateFunction(true));
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
        ret = handle().filter(classRef).edit().fetch().first();
      }
      if (!ret.isPresent()) {
        ret = Optional.of(createObject(classRef));
      }
      return ret.get();
    }

    private O createObject(ClassReference classRef) {
      final O obj = bridge.createObject(doc, classRef);
      query.getFieldRestrictions(classRef).forEach(new Consumer<FieldRestriction<O, ?>>() {

        @Override
        public void accept(FieldRestriction<O, ?> restr) {
          updateField(obj, restr);
        }

        private <T> void updateField(O obj, FieldRestriction<O, T> restr) {
          bridge.setObjectField(obj, restr.getField(), FluentIterable.from(
              restr.getValues()).first().get());
        }
      });
      return obj;
    }

  }

  @Override
  public List<O> remove() {
    return FluentIterable.from(fetch().list()).filter(new ObjectRemovePredicate()).toList();
  }

  private class ObjectRemovePredicate implements Predicate<O> {

    @Override
    public boolean apply(O obj) {
      return bridge.removeObject(doc, obj);
    }

  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [doc=" + bridge.getDocRef(doc) + ", query=" + query
        + "]";
  }

}
