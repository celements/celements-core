package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.filter.ObjectFilter;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;

@Immutable
public final class DefaultObjectFetcher<D, O> implements ObjectFetcher<D, O> {

  private final D doc;
  private final ObjectFilter filter;
  private final ObjectBridge<D, O> bridge;
  private final boolean clone;

  DefaultObjectFetcher(@NotNull D doc, @NotNull ObjectFilter filter,
      @NotNull ObjectBridge<D, O> bridge) {
    this(doc, filter, bridge, true);
  }

  DefaultObjectFetcher(@NotNull D doc, @NotNull ObjectFilter filter,
      @NotNull ObjectBridge<D, O> bridge, boolean clone) {
    this.doc = checkNotNull(doc);
    this.filter = checkNotNull(filter);
    this.bridge = checkNotNull(bridge);
    this.clone = clone;
  }

  D getDoc() {
    return this.doc;
  }

  @Override
  public boolean isEmpty() {
    return list().isEmpty();
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
        return bridge.getObjectNumber(obj) == objNb;
      }
    });
  }

  @Override
  public List<O> list() {
    return ImmutableList.copyOf(Iterables.concat(map().values()));
  }

  @Override
  public Map<ClassReference, List<O>> map() {
    Builder<ClassReference, List<O>> builder = ImmutableMap.builder();
    for (ClassReference classRef : getClassRefs()) {
      builder.put(classRef, FluentIterable.from(bridge.getObjects(classRef)).filter(
          new ObjectFetchPredicate()).transform(new ObjectCloner()).toList());
    }
    return builder.build();
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

  private class ObjectCloner implements Function<O, O> {

    @Override
    public O apply(O obj) {
      return clone ? bridge.cloneObject(obj) : obj;
    }
  };

  private List<ClassReference> getClassRefs() {
    List<ClassReference> ret = new ArrayList<>();
    if (!filter.isEmpty()) {
      ret.addAll(filter.getClassRefs());
    } else {
      ret.addAll(bridge.getDocClassRefs());
    }
    return ret;
  }

  @Override
  public ObjectHandler<D, O> handle() {
    return new DefaultObjectHandler<>(doc, bridge, filter);
  }

}
