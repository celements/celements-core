package com.celements.pagetype;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface IPageTypeClassConfig {

  public static final String PAGE_TYPE_PROPERTIES_CLASS_SPACE = "Celements2";
  public static final String PAGE_TYPE_PROPERTIES_CLASS_DOC = "PageTypeProperties";
  public static final String PAGE_TYPE_PROPERTIES_CLASS = 
    PAGE_TYPE_PROPERTIES_CLASS_SPACE + "." + PAGE_TYPE_PROPERTIES_CLASS_DOC;

  public static final String PAGE_TYPE_CLASS_SPACE = "Celements2";
  public static final String PAGE_TYPE_CLASS_DOC = "PageType";
  public static final String PAGE_TYPE_CLASS = PAGE_TYPE_CLASS_SPACE + "."
      + PAGE_TYPE_CLASS_DOC;
  public static final String PAGE_TYPE_FIELD = "page_type";

  public DocumentReference getPageTypePropertiesClassRef(String wikiName);
  public DocumentReference getPageTypeClassRef(String wikiName);

}
