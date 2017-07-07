package com.celements.model.access.object;

import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.object.ObjectFilter.ObjectFilterView;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@Immutable
public class ObjectFetcher {

  private final XWikiDocument doc;
  private final ObjectFilterView filter;

  ObjectFetcher(XWikiDocument doc, ObjectFilterView filter) {
    this.doc = checkNotNull(doc);
    this.filter = checkNotNull(filter);
  }

  public boolean hasValue() {
    return !list().isEmpty();
  }

  public Optional<BaseObject> first() {
    return FluentIterable.from(list()).first();
  }

  public Optional<BaseObject> number(final int objNb) {
    return FluentIterable.from(list()).firstMatch(new Predicate<BaseObject>() {

      @Override
      public boolean apply(BaseObject obj) {
        return obj.getNumber() == objNb;
      }
    });
  }

  public List<BaseObject> list() {
    List<BaseObject> ret = new ArrayList<>();
    return FluentIterable.from(getClassRefs()).transformAndConcat(getXObjectsFunction()).filter(
        new ObjectFetchingPredicate()).copyInto(ret);
  }

  public Map<ClassReference, List<BaseObject>> map() {
    Map<ClassReference, List<BaseObject>> ret = new LinkedHashMap<>();
    Predicate<BaseObject> predicate = new ObjectFetchingPredicate();
    for (ClassReference classRef : getClassRefs()) {
      List<BaseObject> objs = new ArrayList<>();
      FluentIterable.from(getXObjects(classRef)).filter(predicate).copyInto(objs);
      if (!objs.isEmpty()) {
        ret.put(classRef, objs);
      }
    }
    return ret;
  }

  private class ObjectFetchingPredicate implements Predicate<BaseObject> {

    @Override
    public boolean apply(BaseObject obj) {
      ClassReference classRef = new ClassReference(obj.getXClassReference());
      return !filter.hasFields(classRef) || FluentIterable.from(filter.getFields(
          classRef)).allMatch(getClassFieldPrediate(obj));
    }

    private Predicate<ClassField<?>> getClassFieldPrediate(final BaseObject obj) {
      return new Predicate<ClassField<?>>() {

        @Override
        public boolean apply(ClassField<?> field) {
          return applyFilter(field);
        }

        private <T> boolean applyFilter(ClassField<T> field) {
          Optional<T> fieldValue = getModelAccess().getFieldValue(obj, field);
          if (fieldValue.isPresent()) {
            return filter.hasValue(field, fieldValue.get());
          } else {
            return filter.isAbsent(field);
          }
        }
      };
    }
  }

  private List<BaseObject> getXObjects(ClassReference classRef) {
    List<BaseObject> ret = new ArrayList<>();
    List<BaseObject> objs = firstNonNull(doc.getXObjects(getClassDocRef(classRef)),
        ImmutableList.<BaseObject>of());
    return FluentIterable.from(objs).filter(Predicates.notNull()).copyInto(ret);
  }

  private Function<ClassReference, List<BaseObject>> getXObjectsFunction() {
    return new Function<ClassReference, List<BaseObject>>() {

      @Override
      public List<BaseObject> apply(ClassReference classRef) {
        return getXObjects(classRef);
      }
    };
  }

  List<ClassReference> getClassRefs() {
    List<ClassReference> ret = new ArrayList<>();
    if (!filter.isEmpty()) {
      ret.addAll(filter.getClassRefs());
    } else {
      FluentIterable.from(doc.getXObjects().keySet()).transform(
          ClassReference.FUNC_DOC_TO_CLASS_REF).copyInto(ret);
    }
    return ret;
  }

  DocumentReference getClassDocRef(ClassReference classRef) {
    return classRef.getDocRef(doc.getDocumentReference().getWikiReference());
  }

  /**
   * IMPORTANT: do not use for XObject operations, could lead to endless loops
   */
  IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

}
