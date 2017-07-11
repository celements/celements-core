package com.celements.model.access.object.xwiki;

import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.object.ObjectBridge;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class XWikiObjectBridge implements ObjectBridge<XWikiDocument, BaseObject> {

  private final XWikiDocument doc;

  XWikiObjectBridge(@NotNull XWikiDocument doc) {
    this.doc = checkNotNull(doc);
    checkState(doc.getTranslation() == 0, MessageFormat.format("XWikiObjectAdapter cannot be used"
        + " on translation ''{0}'' of doc ''{1}''", doc.getLanguage(), doc.getDocumentReference()));
  }

  @Override
  public List<ClassReference> getDocClassRefs() {
    return FluentIterable.from(doc.getXObjects().keySet()).transform(
        ClassReference.FUNC_DOC_TO_CLASS_REF).toList();
  }

  @Override
  public List<BaseObject> getObjects(ClassReference classRef) {
    List<BaseObject> ret = new ArrayList<>();
    WikiReference docWiki = doc.getDocumentReference().getWikiReference();
    List<BaseObject> objs = firstNonNull(doc.getXObjects(classRef.getDocRef(docWiki)),
        ImmutableList.<BaseObject>of());
    return FluentIterable.from(objs).filter(Predicates.notNull()).copyInto(ret);
  }

  @Override
  public int getObjectNumber(BaseObject obj) {
    return obj.getNumber();
  }

  @Override
  public ClassReference getObjectClassRef(BaseObject obj) {
    return new ClassReference(obj.getXClassReference());
  }

  @Override
  public BaseObject cloneObject(BaseObject obj) {
    return (BaseObject) obj.clone();
  }

  @Override
  public BaseObject createObject(ClassReference classRef) {
    WikiReference docWiki = doc.getDocumentReference().getWikiReference();
    try {
      return doc.newXObject(classRef.getDocRef(docWiki), getContext().getXWikiContext());
    } catch (XWikiException xwe) {
      throw new ClassDocumentLoadException(classRef.getDocRef(docWiki), xwe);
    }
  }

  @Override
  public boolean removeObject(BaseObject obj) {
    return doc.removeXObject(obj);
  }

  @Override
  public <T> Optional<T> getObjectField(BaseObject obj, ClassField<T> field) {
    return getModelAccess().getFieldValue(obj, field);
  }

  @Override
  public <T> boolean setObjectField(BaseObject obj, ClassField<T> field, T value) {
    return getModelAccess().setProperty(obj, field, value);
  }

  private ModelContext getContext() {
    return Utils.getComponent(ModelContext.class);
  }

  /**
   * IMPORTANT: do not use for XObject operations, could lead to endless loops
   */
  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

}
