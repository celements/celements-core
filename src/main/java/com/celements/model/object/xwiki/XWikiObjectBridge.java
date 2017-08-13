package com.celements.model.object.xwiki;

import static com.celements.model.util.References.*;
import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.classes.ClassIdentity;
import com.celements.model.context.ModelContext;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.XObjectFieldAccessor;
import com.celements.model.object.ObjectBridge;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Immutable
@Component(XWikiObjectBridge.NAME)
public class XWikiObjectBridge implements ObjectBridge<XWikiDocument, BaseObject> {

  public static final String NAME = "xwiki";

  @Requirement(XObjectFieldAccessor.NAME)
  private FieldAccessor<BaseObject> xObjAccessor;

  @Requirement
  private ModelContext context;

  @Override
  public Class<XWikiDocument> getDocumentType() {
    return XWikiDocument.class;
  }

  @Override
  public Class<BaseObject> getObjectType() {
    return BaseObject.class;
  }

  @Override
  public void checkDoc(XWikiDocument doc) throws IllegalArgumentException {
    checkArgument(doc.getTranslation() == 0, MessageFormat.format("XWikiObjectBridge "
        + "cannot be used  on translation ''{0}'' of doc ''{1}''", doc.getLanguage(),
        doc.getDocumentReference()));
  }

  @Override
  public DocumentReference getDocRef(XWikiDocument doc) {
    return cloneRef(doc.getDocumentReference(), DocumentReference.class);
  }

  @Override
  public List<? extends ClassIdentity> getDocClasses(XWikiDocument doc) {
    return FluentIterable.from(doc.getXObjects().keySet()).transform(
        ClassReference.FUNC_DOC_TO_CLASS_REF).toList();
  }

  @Override
  public List<BaseObject> getObjects(XWikiDocument doc, ClassIdentity classId) {
    List<BaseObject> ret = new ArrayList<>();
    WikiReference docWiki = doc.getDocumentReference().getWikiReference();
    List<BaseObject> objs = firstNonNull(doc.getXObjects(classId.getDocRef(docWiki)),
        ImmutableList.<BaseObject>of());
    return FluentIterable.from(objs).filter(Predicates.notNull()).copyInto(ret);
  }

  @Override
  public int getObjectNumber(BaseObject obj) {
    return obj.getNumber();
  }

  @Override
  public ClassIdentity getObjectClass(BaseObject obj) {
    return new ClassReference(obj.getXClassReference());
  }

  @Override
  public BaseObject cloneObject(BaseObject obj) {
    BaseObject clone = (BaseObject) obj.clone();
    // BaseObject.clone does not properly clone document reference
    clone.setDocumentReference(cloneRef(obj.getDocumentReference(), DocumentReference.class));
    return clone;
  }

  @Override
  public BaseObject createObject(XWikiDocument doc, ClassIdentity classId) {
    WikiReference docWiki = doc.getDocumentReference().getWikiReference();
    try {
      return doc.newXObject(classId.getDocRef(docWiki), context.getXWikiContext());
    } catch (XWikiException xwe) {
      throw new ClassDocumentLoadException(classId.getDocRef(docWiki), xwe);
    }
  }

  @Override
  public boolean deleteObject(XWikiDocument doc, BaseObject obj) {
    return doc.removeXObject(obj);
  }

  @Override
  public FieldAccessor<BaseObject> getFieldAccessor() {
    return xObjAccessor;
  }

}
