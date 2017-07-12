package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.filter.ObjectFilterView;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

public final class ObjectEditor<D, O> {

  private final ObjectFilterView filter;
  private final ObjectBridge<D, O> bridge;
  private final ObjectFetcher<D, O> fetcher;

  ObjectEditor(@NotNull D doc, @NotNull ObjectFilterView filter,
      @NotNull ObjectBridge<D, O> bridge) {
    this.filter = checkNotNull(filter);
    this.bridge = checkNotNull(bridge);
    this.fetcher = new ObjectFetcher<>(doc, filter, bridge, false);
  }

  public ObjectFetcher<D, O> fetch() {
    return fetcher;
  }

  public List<O> create() {
    return createInternal(new ObjectCreateFunction(false));
  }

  public List<O> createIfNotExists() {
    return createInternal(new ObjectCreateFunction(true));
  }

  private List<O> createInternal(ObjectCreateFunction function) {
    List<O> ret = new ArrayList<>();
    return FluentIterable.from(filter.getClassRefs()).transform(function).filter(
        Predicates.notNull()).copyInto(ret);

  }

  private class ObjectCreateFunction implements Function<ClassReference, O> {

    private final boolean ifNotExists;

    ObjectCreateFunction(boolean ifNotExists) {
      this.ifNotExists = ifNotExists;
    }

    @Override
    public O apply(ClassReference classRef) {
      O obj = null;
      if (!ifNotExists || hasObj(classRef)) {
        obj = bridge.createObject(classRef);
        updateFields(obj);
      }
      return obj;
    }

    private boolean hasObj(ClassReference classRef) {
      List<O> objs = fetch().map().get(classRef);
      return (objs == null) || objs.isEmpty();
    }

  }

  private void updateFields(O obj) {
    ClassReference classRef = bridge.getObjectClassRef(obj);
    for (ClassField<?> field : filter.getFields(classRef)) {
      setFirstValue(obj, field);
    }
  }

  private <T> void setFirstValue(O obj, ClassField<T> field) {
    Optional<T> value = FluentIterable.from(filter.getValues(field)).first();
    if (value.isPresent()) {
      bridge.setObjectField(obj, field, value.get());
    }
  }

  public List<O> fetchOrCreate() {
    List<O> ret = new ArrayList<>();
    return FluentIterable.from(filter.getClassRefs()).transformAndConcat(
        new ObjectFetchOrCreateFunction()).copyInto(ret);
  }

  private class ObjectFetchOrCreateFunction implements Function<ClassReference, List<O>> {

    private ObjectCreateFunction createFunction = new ObjectCreateFunction(false);

    @Override
    public List<O> apply(ClassReference classRef) {
      List<O> ret = fetch().map().get(classRef);
      if (ret.isEmpty()) {
        ret = ImmutableList.of(createFunction.apply(classRef));
      }
      return ret;
    }

  }

  public List<O> remove() {
    List<O> ret = new ArrayList<>();
    return FluentIterable.from(fetch().list()).filter(new ObjectRemovePredicate()).copyInto(ret);
  }

  private class ObjectRemovePredicate implements Predicate<O> {

    @Override
    public boolean apply(O obj) {
      return bridge.removeObject(obj);
    }

  }

}
