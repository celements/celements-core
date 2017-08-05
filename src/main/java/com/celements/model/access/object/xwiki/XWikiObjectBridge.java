package com.celements.model.access.object.xwiki;

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

@Immutable
@Component(XWikiObjectBridge.NAME)
public class XWikiObjectBridge implements ObjectBridge<XWikiDocument, BaseObject> {

  public static final String NAME = "xwiki";

  /**
   * IMPORTANT: do not use for XObject operations, could lead to endless loops
   */
  @Requirement
  private IModelAccessFacade modelAccess;

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
  public List<ClassReference> getDocClassRefs(XWikiDocument doc) {
    return FluentIterable.from(doc.getXObjects().keySet()).transform(
        ClassReference.FUNC_DOC_TO_CLASS_REF).toList();
  }

  @Override
  public List<BaseObject> getObjects(XWikiDocument doc, ClassReference classRef) {
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
    BaseObject clone = (BaseObject) obj.clone();
    // BaseObject.clone does not property clone references
    clone.setDocumentReference(cloneRef(obj.getDocumentReference(), DocumentReference.class));
    clone.setXClassReference(cloneRef(obj.getXClassReference(), DocumentReference.class));
    return clone;
  }

  @Override
  public BaseObject createObject(XWikiDocument doc, ClassReference classRef) {
    WikiReference docWiki = doc.getDocumentReference().getWikiReference();
    try {
      return doc.newXObject(classRef.getDocRef(docWiki), context.getXWikiContext());
    } catch (XWikiException xwe) {
      throw new ClassDocumentLoadException(classRef.getDocRef(docWiki), xwe);
    }
  }

  @Override
  public boolean deleteObject(XWikiDocument doc, BaseObject obj) {
    return doc.removeXObject(obj);
  }

  @Override
  public <T> Optional<T> getObjectField(BaseObject obj, ClassField<T> field) {
    return modelAccess.getFieldValue(obj, field);
  }

  @Override
  public <T> boolean setObjectField(BaseObject obj, ClassField<T> field, T value) {
    return modelAccess.setProperty(obj, field, value);
  }

}
