package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.filter.ObjectFilterView;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

public abstract class AbstractObjectFetcher<D, O> implements ObjectFetcher<D, O> {

  private final D doc;
  private final ObjectFilterView filter;

  protected AbstractObjectFetcher(@NotNull D doc, @NotNull ObjectFilterView filter) {
    this.doc = checkNotNull(doc);
    this.filter = checkNotNull(filter);
  }

  protected @NotNull D getDoc() {
    return this.doc;
  }

  @Override
  public boolean hasValues() {
    return !list().isEmpty();
  }

  @Override
  public Optional<O> first() {
    return FluentIterable.from(list()).first();
  }

  @Override
  public Optional<O> number(final int objNb) {
    return FluentIterable.from(list()).firstMatch(new Predicate<O>() {

      @Override
      public boolean apply(O obj) {
        return getObjectNumber(obj) == objNb;
      }
    });
  }

  protected abstract int getObjectNumber(@NotNull O obj);

  private List<O> cacheList;

  @Override
  public List<O> list() {
    if (cacheList == null) {
      cacheList = FluentIterable.from(getClassRefs()).transformAndConcat(
          new ObjectFetchFunction()).filter(new ObjectFetchPredicate()).toList();
    }
    return cacheList;
  }

  private Map<ClassReference, List<O>> cacheMap;

  @Override
  public Map<ClassReference, List<O>> map() {
    if (cacheMap == null) {
      Map<ClassReference, List<O>> map = new LinkedHashMap<>();
      Predicate<O> predicate = new ObjectFetchPredicate();
      for (ClassReference classRef : getClassRefs()) {
        map.put(classRef, FluentIterable.from(getXObjects(classRef)).filter(predicate).toList());
      }
      cacheMap = ImmutableMap.copyOf(map);
    }
    return cacheMap;
  }

  private class ObjectFetchFunction implements Function<ClassReference, List<O>> {

    @Override
    public List<O> apply(ClassReference classRef) {
      return getXObjects(classRef);
    }

  }

  protected abstract @NotNull List<O> getXObjects(@NotNull ClassReference classRef);

  private class ObjectFetchPredicate implements Predicate<O> {

    @Override
    public boolean apply(O obj) {
      Set<ClassField<?>> fields = filter.getFields(getObjectClassRef(obj));
      return fields.isEmpty() || FluentIterable.from(fields).allMatch(getClassFieldPrediate(obj));
    }

    private Predicate<ClassField<?>> getClassFieldPrediate(final O obj) {
      return new Predicate<ClassField<?>>() {

        @Override
        public boolean apply(ClassField<?> field) {
          return applyFilter(field);
        }

        private <T> boolean applyFilter(ClassField<T> field) {
          Optional<T> value = getObjectField(obj, field);
          if (value.isPresent()) {
            return filter.getValues(field).contains(value.get());
          } else {
            return filter.isAbsent(field);
          }
        }
      };
    }
  }

  protected abstract @NotNull ClassReference getObjectClassRef(@NotNull O obj);

  protected abstract @NotNull <T> Optional<T> getObjectField(@NotNull O obj,
      @NotNull ClassField<T> field);

  private List<ClassReference> getClassRefs() {
    List<ClassReference> ret = new ArrayList<>();
    if (!filter.isEmpty()) {
      ret.addAll(filter.getClassRefs());
    } else {
      ret.addAll(getDocClassRefs());
    }
    return ret;
  }

  protected abstract @NotNull List<ClassReference> getDocClassRefs();

}
