package com.celements.model.access.object;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.object.ObjectFilter.ObjectFilterView;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class XWikiObjectEditor extends AbstractObjectEditor<XWikiDocument, BaseObject> {

  XWikiObjectEditor(XWikiDocument doc, ObjectFilterView filter) {
    super(doc, filter, new XWikiObjectFetcher(doc, filter));
  }

  @Override
  protected <T> boolean setObjectField(BaseObject obj, ClassField<T> field, T value) {
    return getModelAccess().setProperty(obj, field, value);
  }

  @Override
  protected BaseObject createObject(ClassReference classRef) {
    WikiReference docWiki = getDoc().getDocumentReference().getWikiReference();
    try {
      return getDoc().newXObject(classRef.getDocRef(docWiki), getContext().getXWikiContext());
    } catch (XWikiException xwe) {
      throw new ClassDocumentLoadException(classRef.getDocRef(docWiki), xwe);
    }
  }

  @Override
  protected boolean removeObject(BaseObject obj) {
    return getDoc().removeXObject(obj);
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
