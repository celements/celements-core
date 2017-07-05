package com.celements.model.access.object;

import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@NotThreadSafe
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultXObjectHandler implements XObjectHandler {

  // TODO logging
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultXObjectHandler.class);

  // do not use for XObject operations, could lead to endless loops
  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelContext context;

  private XWikiDocument doc;

  private final FilterMap filter = new FilterMap();

  private XWikiDocument getDoc() {
    checkState(doc != null, "doc not initialised");
    return doc;
  }

  @Override
  public DefaultXObjectHandler onDoc(XWikiDocument doc) {
    checkState(!modelAccess.isTranslation(doc), MessageFormat.format("XObjectHandler cannot be used"
        + " on translation ''{0}'' of doc ''{1}''", doc.getLanguage(), doc.getDocumentReference()));
    this.doc = doc;
    return this;
  }

  private List<ClassReference> getClassRefs() {
    List<ClassReference> ret = new ArrayList<>();
    if (!filter.isEmpty()) {
      ret.addAll(filter.getClassRefs());
    } else {
      FluentIterable.from(getDoc().getXObjects().keySet()).transform(
          ClassReference.FUNC_DOC_TO_CLASS_REF).copyInto(ret);
    }
    return ret;
  }

  @Override
  public DefaultXObjectHandler filter(ClassReference classRef) {
    filter.add(checkNotNull(classRef));
    return this;
  }

  @Override
  public <T> DefaultXObjectHandler filter(ClassField<T> field, T value) {
    filter.add(checkNotNull(field), checkNotNull(value));
    return this;
  }

  @Override
  public <T> DefaultXObjectHandler filter(ClassField<T> field, Collection<T> values) {
    checkNotNull(field);
    checkArgument(!checkNotNull(values).isEmpty(), "cannot filter for empty value list");
    for (T value : values) {
      filter.add(field, value);
    }
    return this;
  }

  @Override
  public XObjectHandler filterAbsent(ClassField<?> field) {
    filter.addAbsent(checkNotNull(field));
    return this;
  }

  @Override
  public Optional<BaseObject> fetchFirst() {
    return FluentIterable.from(fetchList()).first();
  }

  @Override
  public Optional<BaseObject> fetchNumber(final int objNb) {
    return FluentIterable.from(fetchList()).firstMatch(new Predicate<BaseObject>() {

      @Override
      public boolean apply(BaseObject obj) {
        return obj.getNumber() == objNb;
      }
    });
  }

  @Override
  public List<BaseObject> fetchList() {
    List<BaseObject> ret = new ArrayList<>();
    return FluentIterable.from(getClassRefs()).transformAndConcat(getXObjectsFunction()).filter(
        new ObjectFetchingPredicate()).copyInto(ret);
  }

  @Override
  public Map<ClassReference, List<BaseObject>> fetchMap() {
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
          Optional<T> fieldValue = modelAccess.getFieldValue(obj, field);
          if (fieldValue.isPresent()) {
            return filter.hasValue(field, fieldValue.get());
          } else {
            return filter.isAbsent(field);
          }
        }
      };
    }
  }

  @Override
  public List<BaseObject> create() {
    return createInternal(false);
  }

  @Override
  public List<BaseObject> createIfNotExists() {
    return createInternal(true);
  }

  private List<BaseObject> createInternal(boolean ifNotExists) {
    List<BaseObject> ret = new ArrayList<>();
    return FluentIterable.from(getClassRefs()).transform(new ObjectCreateFunction(
        ifNotExists)).filter(Predicates.notNull()).copyInto(ret);

  }

  private class ObjectCreateFunction implements Function<ClassReference, BaseObject> {

    private final boolean ifNotExists;

    ObjectCreateFunction(boolean ifNotExists) {
      this.ifNotExists = ifNotExists;
    }

    @Override
    public BaseObject apply(ClassReference classRef) {
      BaseObject obj = null;
      if (!ifNotExists || hasObj(classRef)) {
        try {
          obj = getDoc().newXObject(getClassDocRef(classRef), context.getXWikiContext());
          for (ClassField<?> field : filter.getFields(classRef)) {
            setFirstValue(obj, field);
          }
        } catch (XWikiException xwe) {
          throw new ClassDocumentLoadException(getClassDocRef(classRef), xwe);
        }
      }
      return obj;
    }

    private boolean hasObj(ClassReference classRef) {
      List<BaseObject> objs = fetchMap().get(classRef);
      return (objs == null) || objs.isEmpty();
    }

    private <T> void setFirstValue(BaseObject obj, ClassField<T> field) {
      Optional<T> value = FluentIterable.from(filter.getValues(field)).first();
      if (value.isPresent()) {
        modelAccess.setProperty(obj, field, value.get());
      }
    }

  }

  @Override
  public List<BaseObject> remove() {
    List<BaseObject> ret = new ArrayList<>();
    return FluentIterable.from(fetchList()).filter(new ObjectRemovePredicate()).copyInto(ret);
  }

  private class ObjectRemovePredicate implements Predicate<BaseObject> {

    @Override
    public boolean apply(BaseObject obj) {
      return getDoc().removeXObject(obj);
    }

  }

  private List<BaseObject> getXObjects(ClassReference classRef) {
    List<BaseObject> ret = new ArrayList<>();
    List<BaseObject> objs = firstNonNull(getDoc().getXObjects(getClassDocRef(classRef)),
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

  private DocumentReference getClassDocRef(ClassReference classRef) {
    return classRef.getDocRef(getDoc().getDocumentReference().getWikiReference());
  }

}
