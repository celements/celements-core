package com.celements.model.access.object.restriction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.xwiki.model.reference.ClassReference;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class ObjectQuery<O> extends ArrayList<ObjectRestriction<O>> {

  private static final long serialVersionUID = 1L;

  public ObjectQuery() {
    super();
  }

  public ObjectQuery(Collection<? extends ObjectRestriction<O>> coll) {
    super(coll);
  }

  public FluentIterable<ObjectRestriction<O>> asIter() {
    return FluentIterable.from(this);
  }

  public FluentIterable<ClassRestriction<O>> getClassRestrictions() {
    return this.asIter().filter(getClassRestrictionClass());
  }

  public FluentIterable<ClassRestriction<O>> getClassRestrictions(ClassReference classRef) {
    return getClassRestrictions().filter(new ClassPredicate(classRef));
  }

  @SuppressWarnings("unchecked")
  private Class<ClassRestriction<O>> getClassRestrictionClass() {
    return (Class<ClassRestriction<O>>) (Class<?>) ClassRestriction.class;
  }

  public Set<ClassReference> getClassRefs() {
    return getClassRestrictions().transform(new Function<ClassRestriction<O>, ClassReference>() {

      @Override
      public ClassReference apply(ClassRestriction<O> restr) {
        return restr.getClassRef();
      }
    }).toSet();
  }

  public FluentIterable<FieldRestriction<O, ?>> getFieldRestrictions() {
    return this.asIter().filter(getFieldRestrictionClass());
  }

  public FluentIterable<FieldRestriction<O, ?>> getFieldRestrictions(ClassReference classRef) {
    return getFieldRestrictions().filter(new ClassPredicate(classRef));
  }

  @SuppressWarnings("unchecked")
  private Class<FieldRestriction<O, ?>> getFieldRestrictionClass() {
    return (Class<FieldRestriction<O, ?>>) (Class<?>) FieldRestriction.class;
  }

  private class ClassPredicate implements Predicate<ClassRestriction<O>> {

    private final ClassReference classRef;

    ClassPredicate(ClassReference classRef) {
      this.classRef = classRef;
    }

    @Override
    public boolean apply(ClassRestriction<O> restr) {
      return restr.getClassRef().equals(classRef);
    }

  }

}
