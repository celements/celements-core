package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.restriction.FieldRestriction;
import com.celements.model.access.object.restriction.ObjectQuery;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

@Immutable
public final class DefaultObjectEditor<D, O> implements ObjectEditor<D, O> {

  private final D doc;
  private final ObjectQuery<O> query;
  private final ObjectBridge<D, O> bridge;
  private final ObjectFetcher<D, O> fetcher;

  DefaultObjectEditor(@NotNull D doc, @NotNull ObjectQuery<O> query,
      @NotNull ObjectBridge<D, O> bridge) {
    this.doc = checkNotNull(doc);
    this.query = new ObjectQuery<>(query);
    this.bridge = checkNotNull(bridge);
    this.fetcher = new DefaultObjectFetcher<>(doc, query, bridge, false);
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
      final O obj = bridge.createObject(classRef);
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
      return bridge.removeObject(obj);
    }

  }

  @Override
  public ObjectHandler<D, O> handle() {
    return new DefaultObjectHandler<>(doc, bridge, query);
  }

  @Override
  public ObjectFetcher<D, O> fetch() {
    return fetcher;
  }

}
