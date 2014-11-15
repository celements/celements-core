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
  public static final String PAGETYPE_PROP_HASPAGETITLE = "haspagetitle";
  public static final String PAGETYPE_PROP_RTE_HEIGHT = "rte_height";
  public static final String PAGETYPE_PROP_RTE_WIDTH = "rte_width";
  public static final String PAGETYPE_PROP_LOAD_RICHTEXT = "load_richtext";
  public static final String PAGETYPE_PROP_SHOW_FRAME = "show_frame";
  public static final String PAGETYPE_PROP_VISIBLE = "visible";
  public static final String PAGETYPE_PROP_PAGE_VIEW = "page_view";
  public static final String PAGETYPE_PROP_PAGE_EDIT = "page_edit";
  public static final String PAGETYPE_PROP_CATEGORY = "category";
  public static final String PAGETYPE_PROP_TYPE_NAME = "type_name";

  public DocumentReference getPageTypePropertiesClassRef(String wikiName);
  public DocumentReference getPageTypeClassRef(String wikiName);

}
