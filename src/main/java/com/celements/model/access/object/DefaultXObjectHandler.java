package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@NotThreadSafe
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultXObjectHandler implements XObjectHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultXObjectHandler.class);

  // do not use for XObject operations, could lead to endless loops
  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelContext context;

  private XWikiDocument doc;

  private final ClassFieldValues filter = new ClassFieldValues();

  private XWikiDocument getDoc() {
    return checkNotNull(doc);
  }

  @Override
  public XObjectHandler onDoc(XWikiDocument doc) {
    checkState(!modelAccess.isTranslation(doc), MessageFormat.format(
        "XObjectHandler cannot be used on translation ''{0}'' of doc ''{1}''", doc.getLanguage(),
        doc.getDocumentReference()));
    this.doc = doc;
    return this;
  }

  private List<ClassReference> getClassRefs() {
    return filter.isEmpty() ? getClassRefsFromDoc() : filter.getClassRefs();
  }

  private List<ClassReference> getClassRefsFromDoc() {
    Set<DocumentReference> docRefs = getDoc().getXObjects().keySet();
    return FluentIterable.from(docRefs).transform(ClassReference.FUNC_DOC_TO_CLASS_REF).toList();
  }

  @Override
  public XObjectHandler filter(ClassReference classRef) {
    filter.add(classRef);
    return this;
  }

  @Override
  public <T> XObjectHandler filter(ClassField<T> field, T value) {
    filter.add(field, value);
    return this;
  }

  @Override
  public <T> XObjectHandler filter(ClassField<T> field, Collection<T> values) {
    filter.add(field, values);
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
    for (ClassReference classRef : getClassRefs()) {
      ret.addAll(getXObjects(classRef));
    }
    return FluentIterable.from(ret).filter(new ObjectFetchingPredicate()).toList();
  }

  @Override
  public Map<ClassReference, List<BaseObject>> fetchMap() {
    Map<ClassReference, List<BaseObject>> ret = new LinkedHashMap<>();
    for (ClassReference classRef : getClassRefs()) {
      List<BaseObject> objs = FluentIterable.from(getXObjects(classRef)).filter(
          new ObjectFetchingPredicate()).toList();
      if (!objs.isEmpty()) {
        ret.put(classRef, objs);
      }
    }
    return ImmutableMap.copyOf(ret);
  }

  private class ObjectFetchingPredicate implements Predicate<BaseObject> {

    @Override
    public boolean apply(BaseObject obj) {
      ClassReference classRef = new ClassReference(obj.getXClassReference());
      return !filter.hasFields(classRef) || FluentIterable.from(filter.getFields(
          classRef)).anyMatch(getClassFieldPrediate(obj));
    }

    private Predicate<ClassField<?>> getClassFieldPrediate(final BaseObject obj) {
      return new Predicate<ClassField<?>>() {

        @Override
        public boolean apply(ClassField<?> field) {
          return hasValue(field);
        }

        private <T> boolean hasValue(ClassField<T> field) {
          Optional<T> fieldValue = modelAccess.getFieldValue(obj, field);
          return fieldValue.isPresent() && filter.hasValue(field, fieldValue.get());
        }
      };
    }
  }

  @Override
  public List<BaseObject> create() {
    return FluentIterable.from(getClassRefs()).transform(new ObjectCreateFunction(false)).filter(
        Predicates.notNull()).toList();
  }

  @Override
  public List<BaseObject> createIfNotExists() {
    return FluentIterable.from(getClassRefs()).transform(new ObjectCreateFunction(true)).filter(
        Predicates.notNull()).toList();
  }

  private class ObjectCreateFunction implements Function<ClassReference, BaseObject> {

    private final boolean ifNotExists;

    ObjectCreateFunction(boolean ifNotExists) {
      this.ifNotExists = ifNotExists;
    }

    @Override
    public BaseObject apply(ClassReference classRef) {
      if (!ifNotExists || getXObjects(classRef).isEmpty()) {
        try {
          BaseObject obj = getDoc().newXObject(getClassDocRef(classRef), context.getXWikiContext());
          for (ClassField<?> field : filter.getFields(classRef)) {
            setFirstValueFromFilter(obj, field);
          }
          return obj;
        } catch (XWikiException xwe) {
          throw new ClassDocumentLoadException(getClassDocRef(classRef), xwe);
        }
      } else {
        return null;
      }
    }

    private <T> void setFirstValueFromFilter(BaseObject obj, ClassField<T> field) {
      Optional<T> value = FluentIterable.from(filter.getValues(field)).first();
      if (value.isPresent()) {
        modelAccess.setProperty(obj, field, value.get());
      }
    }
  }

  @Override
  public List<BaseObject> remove() {
    return FluentIterable.from(fetchList()).filter(new Predicate<BaseObject>() {

      @Override
      public boolean apply(BaseObject obj) {
        return getDoc().removeXObject(obj);
      }
    }).toList();
  }

  private List<BaseObject> getXObjects(ClassReference classRef) {
    List<BaseObject> ret = getDoc().getXObjects(getClassDocRef(classRef));
    if (ret != null) {
      ret = FluentIterable.from(ret).filter(Predicates.notNull()).toList();
    } else {
      ret = ImmutableList.of();
    }
    return ret;
  }

  private DocumentReference getClassDocRef(ClassReference classRef) {
    return classRef.getDocumentReference(getDoc().getDocumentReference().getWikiReference());
  }

}
