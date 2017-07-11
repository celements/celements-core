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

public final class ObjectFetcher<D, O> {

  private final D doc;
  private final ObjectFilterView filter;
  private final ObjectBridge<D, O> bridge;

  protected ObjectFetcher(@NotNull D doc, @NotNull ObjectFilterView filter,
      @NotNull ObjectBridge<D, O> bridge) {
    this.doc = checkNotNull(doc);
    this.filter = checkNotNull(filter);
    this.bridge = checkNotNull(bridge);
  }

  protected @NotNull D getDoc() {
    return this.doc;
  }

  public boolean hasValues() {
    return !list().isEmpty();
  }

  public Optional<O> first() {
    return FluentIterable.from(list()).first();
  }

  public Optional<O> number(final int objNb) {
    return FluentIterable.from(list()).firstMatch(new Predicate<O>() {

      @Override
      public boolean apply(O obj) {
        return bridge.getObjectNumber(obj) == objNb;
      }
    });
  }

  private List<O> cacheList;

  public List<O> list() {
    if (cacheList == null) {
      cacheList = FluentIterable.from(getClassRefs()).transformAndConcat(
          new ObjectFetchFunction()).filter(new ObjectFetchPredicate()).toList();
    }
    return cacheList;
  }

  private class ObjectFetchFunction implements Function<ClassReference, List<O>> {

    @Override
    public List<O> apply(ClassReference classRef) {
      return FluentIterable.from(bridge.getObjects(classRef)).transform(new Function<O, O>() {

        @Override
        public O apply(O obj) {
          return bridge.cloneObject(obj);
        }
      }).toList();
    }

  }

  private Map<ClassReference, List<O>> cacheMap;

  public Map<ClassReference, List<O>> map() {
    if (cacheMap == null) {
      Map<ClassReference, List<O>> map = new LinkedHashMap<>();
      Predicate<O> predicate = new ObjectFetchPredicate();
      for (ClassReference classRef : getClassRefs()) {
        // TODO clone
        map.put(classRef, FluentIterable.from(bridge.getObjects(classRef)).filter(
            predicate).toList());
      }
      cacheMap = ImmutableMap.copyOf(map);
    }
    return cacheMap;
  }

  private class ObjectFetchPredicate implements Predicate<O> {

    @Override
    public boolean apply(O obj) {
      Set<ClassField<?>> fields = filter.getFields(bridge.getObjectClassRef(obj));
      return fields.isEmpty() || FluentIterable.from(fields).allMatch(getClassFieldPrediate(obj));
    }

    private Predicate<ClassField<?>> getClassFieldPrediate(final O obj) {
      return new Predicate<ClassField<?>>() {

        @Override
        public boolean apply(ClassField<?> field) {
          return applyFilter(field);
        }

        private <T> boolean applyFilter(ClassField<T> field) {
          Optional<T> value = bridge.getObjectField(obj, field);
          if (value.isPresent()) {
            return filter.getValues(field).contains(value.get());
          } else {
            return filter.isAbsent(field);
          }
        }
      };
    }
  }

  private List<ClassReference> getClassRefs() {
    List<ClassReference> ret = new ArrayList<>();
    if (!filter.isEmpty()) {
      ret.addAll(filter.getClassRefs());
    } else {
      ret.addAll(bridge.getDocClassRefs());
    }
    return ret;
  }

}
