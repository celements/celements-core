package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.restriction.ObjectQuery;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;

@Immutable
public final class DefaultObjectFetcher<D, O> implements ObjectFetcher<D, O> {

  private final D doc;
  private final ObjectQuery<O> query;
  private final ObjectBridge<D, O> bridge;
  private final boolean clone;

  DefaultObjectFetcher(@NotNull D doc, @NotNull ObjectQuery<O> query,
      @NotNull ObjectBridge<D, O> bridge) {
    this(doc, query, bridge, true);
  }

  DefaultObjectFetcher(@NotNull D doc, @NotNull ObjectQuery<O> query,
      @NotNull ObjectBridge<D, O> bridge, boolean clone) {
    this.doc = checkNotNull(doc);
    this.query = new ObjectQuery<>(query);
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
      // TODO more tests fail with or?
      builder.put(classRef, FluentIterable.from(bridge.getObjects(classRef)).filter(Predicates.and(
          query)).transform(new ObjectCloner()).toList());
    }
    return builder.build();
  }

  private class ObjectCloner implements Function<O, O> {

    @Override
    public O apply(O obj) {
      return clone ? bridge.cloneObject(obj) : obj;
    }
  };

  private Set<ClassReference> getClassRefs() {
    Set<ClassReference> ret = new LinkedHashSet<>();
    if (!query.isEmpty()) {
      ret.addAll(query.getClassRefs());
    } else {
      ret.addAll(bridge.getDocClassRefs());
    }
    return ret;
  }

  @Override
  public ObjectHandler<D, O> handle() {
    return new DefaultObjectHandler<>(doc, bridge, query);
  }

}
