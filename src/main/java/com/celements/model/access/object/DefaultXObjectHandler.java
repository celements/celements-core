package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.util.AdjustWikiFunction;
import com.celements.model.util.References;
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
  private final Map<DocumentReference, Map<String, Set<Object>>> filterMap = new LinkedHashMap<>();

  private XWikiDocument getDoc() {
    return checkNotNull(doc);
  }

  @Override
  public XObjectHandler onDoc(XWikiDocument doc) {
    checkState(!modelAccess.isTranslation(doc),
        "XObjectHandler cannot be used on translation of doc: " + doc.getDocumentReference());
    this.doc = doc;
    return this;
  }

  private List<DocumentReference> getClassRefs() {
    if (filterMap.isEmpty()) {
      return ImmutableList.copyOf(getDoc().getXObjects().keySet());
    } else {
      final WikiReference wikiRef = References.extractRef(getDoc().getDocumentReference(),
          WikiReference.class).get();
      return FluentIterable.from(filterMap.keySet()).transform(new AdjustWikiFunction<>(
          DocumentReference.class, wikiRef)).toList();
    }
  }

  private Map<String, Set<Object>> getValueMap(DocumentReference classRef, boolean add) {
    classRef = References.adjustRef(checkNotNull(classRef), DocumentReference.class,
        context.getWikiRef());
    Map<String, Set<Object>> ret;
    if (filterMap.containsKey(classRef)) {
      ret = filterMap.get(classRef);
    } else {
      ret = new HashMap<>();
      if (add) {
        filterMap.put(classRef, ret);
      }
    }
    return ret;
  }

  @Override
  public XObjectHandler filter(DocumentReference classRef) {
    getValueMap(classRef, true);
    return this;
  }

  @Override
  public <T> XObjectHandler filter(ClassField<T> field, T value) {
    return filter(field, Arrays.asList(value));
  }

  @Override
  public <T> XObjectHandler filter(ClassField<T> field, Collection<T> values) {
    return filter(checkNotNull(field).getClassDef().getClassRef(), field.getName(), values);
  }

  @Override
  public XObjectHandler filter(DocumentReference classRef, String key, Object value) {
    return filter(classRef, key, Arrays.asList(value));
  }

  @Override
  public XObjectHandler filter(DocumentReference classRef, String key, Collection<?> values) {
    checkArgument(!checkNotNull(key).isEmpty(), "cannot filter for empty field");
    checkArgument(!checkNotNull(values).isEmpty(), "cannot filter for empty value list");
    Map<String, Set<Object>> map = getValueMap(classRef, true);
    Set<Object> valueSet = map.get(key);
    if (valueSet == null) {
      map.put(key, valueSet = new HashSet<>());
    }
    valueSet.addAll(values);
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
    for (DocumentReference classRef : getClassRefs()) {
      ret.addAll(getXObjects(classRef));
    }
    return FluentIterable.from(ret).filter(new ObjectFetchingPredicate()).toList();
  }

  @Override
  public Map<DocumentReference, List<BaseObject>> fetchMap() {
    Map<DocumentReference, List<BaseObject>> ret = new LinkedHashMap<>();
    for (DocumentReference classRef : getClassRefs()) {
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
    public boolean apply(final BaseObject obj) {
      final Map<String, Set<Object>> valueMap = getValueMap(obj.getXClassReference(), false);
      return valueMap.isEmpty() || FluentIterable.from(valueMap.keySet()).anyMatch(
          new Predicate<String>() {

            @Override
            public boolean apply(String key) {
              return valueMap.get(key).contains(modelAccess.getProperty(obj, key));
            }
          });
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

  private class ObjectCreateFunction implements Function<DocumentReference, BaseObject> {

    private final boolean ifNotExists;

    ObjectCreateFunction(boolean ifNotExists) {
      this.ifNotExists = ifNotExists;
    }

    @Override
    public BaseObject apply(DocumentReference classRef) {
      if (!ifNotExists || getXObjects(classRef).isEmpty()) {
        try {
          BaseObject obj = getDoc().newXObject(classRef, context.getXWikiContext());
          Map<String, Set<Object>> valueMap = getValueMap(classRef, false);
          for (String key : valueMap.keySet()) {
            Object value = FluentIterable.from(valueMap.get(key)).first().get();
            modelAccess.setProperty(obj, key, value);
          }
          return obj;
        } catch (XWikiException xwe) {
          throw new ClassDocumentLoadException(classRef, xwe);
        }
      } else {
        return null;
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

  private List<BaseObject> getXObjects(DocumentReference classRef) {
    List<BaseObject> ret = getDoc().getXObjects(classRef);
    if (ret != null) {
      ret = FluentIterable.from(ret).filter(Predicates.notNull()).toList();
    } else {
      ret = ImmutableList.of();
    }
    return ret;
  }

}
