package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
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

public abstract class AbstractObjectEditor<D, O> implements ObjectEditor<D, O> {

  private final D doc;
  private final ObjectFilterView filter;
  private final ObjectFetcher<D, O> fetcher;

  protected AbstractObjectEditor(@NotNull D doc, @NotNull ObjectFilterView filter,
      @NotNull ObjectFetcher<D, O> fetcher) {
    this.doc = checkNotNull(doc);
    this.filter = checkNotNull(filter);
    this.fetcher = checkNotNull(fetcher);
  }

  protected @NotNull D getDoc() {
    return this.doc;
  }

  @Override
  public ObjectFetcher<D, O> fetch() {
    return fetcher;
  }

  @Override
  public List<O> create() {
    return createInternal(new ObjectCreateFunction(false));
  }

  @Override
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
        obj = createObject(classRef);
        for (ClassField<?> field : filter.getFields(classRef)) {
          setFirstValue(obj, field);
        }
      }
      return obj;
    }

    private boolean hasObj(ClassReference classRef) {
      List<O> objs = fetch().map().get(classRef);
      return (objs == null) || objs.isEmpty();
    }

    private <T> void setFirstValue(O obj, ClassField<T> field) {
      Optional<T> value = FluentIterable.from(filter.getValues(field)).first();
      if (value.isPresent()) {
        setObjectField(obj, field, value.get());
      }
    }

  }

  protected abstract @NotNull O createObject(@NotNull ClassReference classRef);

  protected abstract <T> boolean setObjectField(@NotNull O obj, @NotNull ClassField<T> field,
      @Nullable T value);

  @Override
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

  @Override
  public List<O> remove() {
    List<O> ret = new ArrayList<>();
    return FluentIterable.from(fetch().list()).filter(new ObjectRemovePredicate()).copyInto(ret);
  }

  protected abstract boolean removeObject(@NotNull O obj);

  private class ObjectRemovePredicate implements Predicate<O> {

    @Override
    public boolean apply(O obj) {
      return removeObject(obj);
    }

  }

}
