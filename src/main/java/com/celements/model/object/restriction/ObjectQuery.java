package com.celements.model.object.restriction;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import com.celements.model.classes.ClassIdentity;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

@NotThreadSafe
public class ObjectQuery<O> {

  private Set<ObjectRestriction<O>> restrictions = new LinkedHashSet<>();

  public ObjectQuery() {
  }

  public ObjectQuery(Iterable<? extends ObjectRestriction<O>> iter) {
    this.addAll(iter);
  }

  public void add(ObjectRestriction<O> restr) {
    restrictions.add(restr);
  }

  public void addAll(Iterable<? extends ObjectRestriction<O>> iter) {
    for (ObjectRestriction<O> restr : iter) {
      this.add(restr);
    }
  }

  public FluentIterable<ObjectRestriction<O>> getRestrictions() {
    return FluentIterable.from(restrictions);
  }

  public FluentIterable<ObjectRestriction<O>> getRestrictions(ClassIdentity classId) {
    return getRestrictions().filter(new ClassPredicate(classId));
  }

  public Set<ClassIdentity> getObjectClasses() {
    return getRestrictions().filter(getClassRestrictionClass()).transform(
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
    return getRestrictions().filter(getFieldRestrictionClass()).filter(new ClassPredicate(classId));
  }

  @SuppressWarnings("unchecked")
  private Class<FieldRestriction<O, ?>> getFieldRestrictionClass() {
    return (Class<FieldRestriction<O, ?>>) (Class<?>) FieldRestriction.class;
  }

  @Override
  public int hashCode() {
    return Objects.hash(restrictions);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ObjectQuery) {
      ObjectQuery<?> other = (ObjectQuery<?>) obj;
      return Objects.equals(this.restrictions, other.restrictions);
    }
    return false;
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
