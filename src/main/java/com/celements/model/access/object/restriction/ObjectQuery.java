package com.celements.model.access.object.restriction;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.xwiki.model.reference.ClassReference;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class ObjectQuery<O> extends LinkedHashSet<ObjectRestriction<O>> {

  private static final long serialVersionUID = 1L;

  public ObjectQuery() {
    super();
  }

  public ObjectQuery(Collection<? extends ObjectRestriction<O>> coll) {
    super(coll);
  }

  public FluentIterable<ObjectRestriction<O>> getRestrictions(ClassReference classRef) {
    return FluentIterable.from(this).filter(new ClassPredicate(classRef));
  }

  public Set<ClassReference> getClassRefs() {
    return FluentIterable.from(this).filter(getClassRestrictionClass()).transform(
        new Function<ClassRestriction<O>, ClassReference>() {

          @Override
          public ClassReference apply(ClassRestriction<O> restr) {
            return restr.getClassRef();
          }
        }).toSet();
  }

  @SuppressWarnings("unchecked")
  private Class<ClassRestriction<O>> getClassRestrictionClass() {
    return (Class<ClassRestriction<O>>) (Class<?>) ClassRestriction.class;
  }

  public FluentIterable<FieldRestriction<O, ?>> getFieldRestrictions(ClassReference classRef) {
    return FluentIterable.from(this).filter(getFieldRestrictionClass()).filter(new ClassPredicate(
        classRef));
  }

  @SuppressWarnings("unchecked")
  private Class<FieldRestriction<O, ?>> getFieldRestrictionClass() {
    return (Class<FieldRestriction<O, ?>>) (Class<?>) FieldRestriction.class;
  }

  private class ClassPredicate implements Predicate<ObjectRestriction<?>> {

    private final ClassReference classRef;

    ClassPredicate(ClassReference classRef) {
      this.classRef = classRef;
    }

    @Override
    public boolean apply(ObjectRestriction<?> restr) {
      if (restr instanceof ClassRestriction) {
        return ((ClassRestriction<?>) restr).getClassRef().equals(classRef);
      }
      return true;
    }

  }

}
