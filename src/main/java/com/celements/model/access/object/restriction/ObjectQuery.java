package com.celements.model.access.object.restriction;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import com.celements.model.classes.ClassIdentity;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

@NotThreadSafe
public class ObjectQuery<O> extends LinkedHashSet<ObjectRestriction<O>> {

  private static final long serialVersionUID = 7825122021132999318L;

  public ObjectQuery() {
    super();
  }

  public ObjectQuery(Collection<? extends ObjectRestriction<O>> coll) {
    super(coll);
  }

  public FluentIterable<ObjectRestriction<O>> getRestrictions(ClassIdentity classId) {
    return FluentIterable.from(this).filter(new ClassPredicate(classId));
  }

  public Set<ClassIdentity> getObjectClasses() {
    return FluentIterable.from(this).filter(getClassRestrictionClass()).transform(
        new Function<ClassRestriction<O>, ClassIdentity>() {

          @Override
          public ClassIdentity apply(ClassRestriction<O> restr) {
            return restr.getClassIdentity();
          }
        }).toSet();
  }

  @SuppressWarnings("unchecked")
  private Class<ClassRestriction<O>> getClassRestrictionClass() {
    return (Class<ClassRestriction<O>>) (Class<?>) ClassRestriction.class;
  }

  public FluentIterable<FieldRestriction<O, ?>> getFieldRestrictions(ClassIdentity classId) {
    return FluentIterable.from(this).filter(getFieldRestrictionClass()).filter(new ClassPredicate(
        classId));
  }

  @SuppressWarnings("unchecked")
  private Class<FieldRestriction<O, ?>> getFieldRestrictionClass() {
    return (Class<FieldRestriction<O, ?>>) (Class<?>) FieldRestriction.class;
  }

  private class ClassPredicate implements Predicate<ObjectRestriction<?>> {

    private final ClassIdentity classId;

    ClassPredicate(ClassIdentity classId) {
      this.classId = classId;
    }

    @Override
    public boolean apply(ObjectRestriction<?> restr) {
      if (restr instanceof ClassRestriction) {
        return ((ClassRestriction<?>) restr).getClassIdentity().equals(classId);
      }
      return true;
    }

  }

}
