package com.celements.model.util;

import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.model.access.exception.ClassDocumentLoadException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IXObjectUpdateRole {

  /**
   * updates the document from the field map given
   * 
   * @param doc
   * @param fieldMap
   *          key is of format "{ClassFullName}.{FieldName}"
   * @return true if doc has changed
   * @throws ClassDocumentLoadException
   *           if encountered invalid class name
   */
  public boolean updateFromMap(XWikiDocument doc, Map<String, Object> fieldMap)
      throws ClassDocumentLoadException;

}
