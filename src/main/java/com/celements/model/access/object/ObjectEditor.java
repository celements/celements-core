package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.object.ObjectFilter.ObjectFilterView;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@Immutable
public class ObjectEditor {

  private final XWikiDocument doc;
  private final ObjectFilterView filter;
  private final ObjectFetcher fetcher;

  ObjectEditor(XWikiDocument doc, ObjectFilterView filter) {
    this.doc = checkNotNull(doc);
    this.filter = checkNotNull(filter);
    this.fetcher = new ObjectFetcher(doc, filter);
  }

  public List<BaseObject> create() {
    return createInternal(false);
  }

  public List<BaseObject> createIfNotExists() {
    return createInternal(true);
  }

  private List<BaseObject> createInternal(boolean ifNotExists) {
    List<BaseObject> ret = new ArrayList<>();
    return FluentIterable.from(fetcher.getClassRefs()).transform(new ObjectCreateFunction(
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
          obj = doc.newXObject(fetcher.getClassDocRef(classRef), getContext().getXWikiContext());
          for (ClassField<?> field : filter.getFields(classRef)) {
            setFirstValue(obj, field);
          }
        } catch (XWikiException xwe) {
          throw new ClassDocumentLoadException(fetcher.getClassDocRef(classRef), xwe);
        }
      }
      return obj;
    }

    private boolean hasObj(ClassReference classRef) {
      List<BaseObject> objs = fetcher.map().get(classRef);
      return (objs == null) || objs.isEmpty();
    }

    private <T> void setFirstValue(BaseObject obj, ClassField<T> field) {
      Optional<T> value = FluentIterable.from(filter.getValues(field)).first();
      if (value.isPresent()) {
        fetcher.getModelAccess().setProperty(obj, field, value.get());
      }
    }

  }

  public List<BaseObject> remove() {
    List<BaseObject> ret = new ArrayList<>();
    return FluentIterable.from(fetcher.list()).filter(new ObjectRemovePredicate()).copyInto(ret);
  }

  private class ObjectRemovePredicate implements Predicate<BaseObject> {

    @Override
    public boolean apply(BaseObject obj) {
      return doc.removeXObject(obj);
    }

  }

  private ModelContext getContext() {
    return Utils.getComponent(ModelContext.class);
  }

}
