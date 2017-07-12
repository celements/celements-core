package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.filter.ObjectFilterView;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

@Immutable
public final class ObjectEditor<D, O> {

  private final D doc;
  private final ObjectFilterView filter;
  private final ObjectBridge<D, O> bridge;
  private final ObjectFetcher<D, O> fetcher;

  ObjectEditor(@NotNull D doc, @NotNull ObjectFilterView filter,
      @NotNull ObjectBridge<D, O> bridge) {
    this.doc = checkNotNull(doc);
    this.filter = checkNotNull(filter);
    this.bridge = checkNotNull(bridge);
    this.fetcher = new ObjectFetcher<>(doc, filter, bridge, false);
  }

  public Map<ClassReference, O> create() {
    return FluentIterable.from(filter.getClassRefs()).toMap(new ObjectCreateFunction(false));
  }

  public Map<ClassReference, O> createIfNotExists() {
    return FluentIterable.from(filter.getClassRefs()).toMap(new ObjectCreateFunction(true));
  }

  private class ObjectCreateFunction implements Function<ClassReference, O> {

    private final boolean ifNotExists;

    ObjectCreateFunction(boolean ifNotExists) {
      this.ifNotExists = ifNotExists;
    }

    @Override
    public O apply(ClassReference classRef) {
      Optional<O> obj = Optional.absent();
      if (ifNotExists) {
        obj = handle().filter(classRef).edit().fetch().first();
      }
      if (!obj.isPresent()) {
        obj = Optional.of(updateFields(bridge.createObject(classRef)));
      }
      return obj.get();
    }

  }

  private O updateFields(O obj) {
    ClassReference classRef = bridge.getObjectClassRef(obj);
    for (ClassField<?> field : filter.getFields(classRef)) {
      setFirstValue(obj, field);
    }
    return obj;
  }

  private <T> void setFirstValue(O obj, ClassField<T> field) {
    Optional<T> value = FluentIterable.from(filter.getValues(field)).first();
    if (value.isPresent()) {
      bridge.setObjectField(obj, field, value.get());
    }
  }

  public List<O> remove() {
    return FluentIterable.from(fetch().list()).filter(new ObjectRemovePredicate()).toList();
  }

  private class ObjectRemovePredicate implements Predicate<O> {

    @Override
    public boolean apply(O obj) {
      return bridge.removeObject(obj);
    }

  }

  public ObjectHandler<D, O> handle() {
    return new ObjectHandler<>(doc, bridge, filter);
  }

  public ObjectFetcher<D, O> fetch() {
    return fetcher;
  }

}
