package com.celements.pagetype.service;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.pagetype.PageTypeReference;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface IPageTypeResolverRole {

  public PageTypeReference getPageTypeRefForCurrentDoc();

  /**
   * getPageTypeObject returns the page-type object attached to the given checkDoc.
   * If the checkDoc isNew and thus has no objects it falls back to the wikiTemplateDoc
   * in case one is defined in the request.
   * 
   * @param checkDocRef
   * @return page-type object or null if non is present
   */
  public BaseObject getPageTypeObject(XWikiDocument checkDocRef);

  /**
   * getPageTypeRefForDoc gets the PageTypeRef defined by the PageType-xobject attached
   * to the given document. It returns null if no valid PageType-xobject is found.
   * 
   * @param checkDoc
   * @return
   */
  public PageTypeReference getPageTypeRefForDoc(XWikiDocument checkDoc);

  /**
   * getDefaultPageTypeRefForDoc returns the PageTypeReference which is defined as
   * default PageType in the current location (docRef).
   * 
   * @param docRef
   * @return
   */
  public PageTypeReference getDefaultPageTypeRefForDoc(DocumentReference docRef);

  /**
   * getPageTypeRefForDocWithDefault returns the PageTypeReference defined for the
   * given document reference. If no explicit page-type is defined it returns the default
   * PageTypeReference defined for the given document location.
   * 
   * @param docRef
   * @return
   */
  public PageTypeReference getPageTypeRefForDocWithDefault(DocumentReference docRef);

  /**
   * getPageTypeRefForDocWithDefault returns the PageTypeReference defined for the
   * given document. If no explicit page-type is defined it returns the default
   * PageTypeReference defined for the given document location.
   * 
   * @param doc
   * @return
   */
  public PageTypeReference getPageTypeRefForDocWithDefault(XWikiDocument doc);

  /**
   * getPageTypeRefForDocWithDefault returns the PageTypeReference defined for the
   * given document. If no explicit page-type is defined it returns the default
   * PageTypeReference given by the caller.
   * 
   * @param doc
   * @param defaultPTRef
   * @return
   */
  public PageTypeReference getPageTypeRefForDocWithDefault(XWikiDocument doc,
      PageTypeReference defaultPTRef);

}
