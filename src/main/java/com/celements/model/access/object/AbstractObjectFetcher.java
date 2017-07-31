package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.object.restriction.ObjectQuery;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

@NotThreadSafe
public abstract class AbstractObjectFetcher<D, O> implements ObjectFetcher<D, O> {

  protected final D doc;
  protected final ObjectQuery<O> query;
  protected final boolean clone;

  protected AbstractObjectFetcher(@NotNull D doc, @NotNull ObjectQuery<O> query, boolean clone) {
    this.doc = checkNotNull(doc);
    this.query = new ObjectQuery<>(query);
    this.clone = clone;
    getBridge().checkDoc(doc);
  }

  @Override
  public DocumentReference getDocRef() {
    return getBridge().getDocRef(doc);
  }

  @Override
  public ObjectQuery<O> getQuery() {
    return new ObjectQuery<>(query);
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
        return getBridge().getObjectNumber(obj) == objNb;
      }
    });
  }

  @Override
  public List<O> list() {
    return ImmutableList.copyOf(Iterables.concat(map().values()));
  }

  @Override
  public Map<ClassReference, List<O>> map() {
    ImmutableMap.Builder<ClassReference, List<O>> builder = ImmutableMap.builder();
    for (ClassReference classRef : getClassRefs()) {
      builder.put(classRef, getObjects(classRef).toList());
    }
    return builder.build();
  }

  private Set<ClassReference> getClassRefs() {
    Set<ClassReference> ret = new LinkedHashSet<>();
    if (!query.isEmpty()) {
      ret.addAll(query.getClassRefs());
    } else {
      ret.addAll(getBridge().getDocClassRefs(doc));
    }
    return ret;
  }

  private FluentIterable<O> getObjects(ClassReference classRef) {
    FluentIterable<O> iter = FluentIterable.from(getBridge().getObjects(doc, classRef));
    iter = iter.filter(Predicates.and(query.getRestrictions(classRef)));
    if (clone) {
      iter = iter.transform(new ObjectCloner());
    }
    return iter;
  }

  private class ObjectCloner implements Function<O, O> {

    @Override
    public O apply(O obj) {
      return getBridge().cloneObject(obj);
    }
  };

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [doc=" + getBridge().getDocRef(doc) + ", query="
        + query + "]";
  }

  protected abstract @NotNull ObjectBridge<D, O> getBridge();

}
