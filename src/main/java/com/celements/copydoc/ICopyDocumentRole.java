package com.celements.copydoc;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.model.access.exception.DocumentSaveException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface ICopyDocumentRole {

  /**
   * @param doc1
   *          may not be null
   * @param doc2
   *          may not be null
   * @return true if the given docs are different
   */
  public boolean check(XWikiDocument doc1, XWikiDocument doc2);

  public boolean check(XWikiDocument doc1, XWikiDocument doc2, Collection<BaseObject> toIgnore);

  public boolean check(XWikiDocument doc1, XWikiDocument doc2, Predicate<BaseObject> xObjFilter);

  /**
   * Copies given source doc to given target doc and saves target if it has changed
   *
   * @param srcDoc
   *          source doc, may not be null
   * @param trgDoc
   *          target doc, may not be null
   * @return whether target doc has changed or not
   * @throws DocumentSaveException
   */
  public boolean copyAndSave(XWikiDocument srcDoc, XWikiDocument trgDoc)
      throws DocumentSaveException;

  public boolean copyAndSave(XWikiDocument srcDoc, XWikiDocument trgDoc, Set<BaseObject> toIgnore)
      throws DocumentSaveException;

  /**
   * Copies given source doc to given target doc
   *
   * @param srcDoc
   *          source doc, may not be null
   * @param trgDoc
   *          target doc, may not be null
   * @return whether target doc has changed or not
   */
  public boolean copy(XWikiDocument srcDoc, XWikiDocument trgDoc);

  public boolean copy(XWikiDocument srcDoc, XWikiDocument trgDoc, Collection<BaseObject> toIgnore);

  public boolean copy(XWikiDocument srcDoc, XWikiDocument trgDoc, Predicate<BaseObject> xObjFilter);

  public boolean checkObject(BaseObject obj1, BaseObject obj2);

  public boolean copyObject(BaseObject srcObj, BaseObject trgObj);

}
