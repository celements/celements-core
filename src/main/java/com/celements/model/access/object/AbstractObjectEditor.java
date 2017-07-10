package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.ObjectFilter.ObjectFilterView;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

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
  public List<O> create() {
    return createInternal(false);
  }

  @Override
  public List<O> createIfNotExists() {
    return createInternal(true);
  }

  private List<O> createInternal(boolean ifNotExists) {
    List<O> ret = new ArrayList<>();
    return FluentIterable.from(fetcher.getClassRefs()).transform(new ObjectCreateFunction(
        ifNotExists)).filter(Predicates.notNull()).copyInto(ret);

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
      List<O> objs = fetcher.map().get(classRef);
      return (objs == null) || objs.isEmpty();
    }

    // TODO why only first and not all?
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
  public List<O> remove() {
    List<O> ret = new ArrayList<>();
    return FluentIterable.from(fetcher.list()).filter(new ObjectRemovePredicate()).copyInto(ret);
  }

  protected abstract boolean removeObject(@NotNull O obj);

  private class ObjectRemovePredicate implements Predicate<O> {

    @Override
    public boolean apply(O obj) {
      return removeObject(obj);
    }

  }

}
