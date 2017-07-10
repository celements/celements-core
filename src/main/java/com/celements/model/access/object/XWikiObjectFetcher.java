package com.celements.model.access.object;

import static com.google.common.base.MoreObjects.*;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.object.ObjectFilter.ObjectFilterView;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class XWikiObjectFetcher extends AbstractObjectFetcher<XWikiDocument, BaseObject> {

  XWikiObjectFetcher(XWikiDocument doc, ObjectFilterView filter) {
    super(doc, filter);
  }

  @Override
  protected List<ClassReference> getDocClassRefs() {
    return FluentIterable.from(getDoc().getXObjects().keySet()).transform(
        ClassReference.FUNC_DOC_TO_CLASS_REF).toList();
  }

  @Override
  protected List<BaseObject> getXObjects(ClassReference classRef) {
    List<BaseObject> ret = new ArrayList<>();
    WikiReference docWiki = getDoc().getDocumentReference().getWikiReference();
    List<BaseObject> objs = firstNonNull(getDoc().getXObjects(classRef.getDocRef(docWiki)),
        ImmutableList.<BaseObject>of());
    return FluentIterable.from(objs).filter(Predicates.notNull()).copyInto(ret);
  }

  @Override
  protected int getObjectNumber(BaseObject obj) {
    return obj.getNumber();
  }

  @Override
  protected ClassReference getObjectClassRef(BaseObject obj) {
    return new ClassReference(obj.getXClassReference());
  }

  @Override
  protected <T> Optional<T> getObjectField(BaseObject obj, ClassField<T> field) {
    return getModelAccess().getFieldValue(obj, field);
  }

  /**
   * IMPORTANT: do not use for XObject operations, could lead to endless loops
   */
  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

}
