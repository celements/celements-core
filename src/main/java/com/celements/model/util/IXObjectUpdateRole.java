package com.celements.model.util;

import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

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
   * @deprecated instead use {@link #update(XWikiDocument, Set)}
   */
  @Deprecated
  public boolean updateFromMap(XWikiDocument doc, Map<String, Object> fieldMap);

  /**
   * updates the document from the field map given
   *
   * @param doc
   * @param fieldValues
   * @return true if doc has changed
   */
  public boolean update(XWikiDocument doc, Set<ClassFieldValue<?>> fieldValues);

}
