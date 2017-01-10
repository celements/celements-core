package com.celements.pagetype.classes;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Component(PageTypeClass.CLASS_DEF_HINT)
public class PageTypeClass extends AbstractClassDefinition {

  public static final String SPACE_NAME = "Celements2";
  public static final String DOC_NAME = "PageType";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public static final ClassField<String> FIELD_PAGE_TYPE = new StringField.Builder(CLASS_DEF_HINT,
      "page_type").size(30).prettyName("Page Type").build();
  public static final ClassField<String> PAGE_LAYOUT = new StringField.Builder(CLASS_DEF_HINT,
      "page_layout").size(30).prettyName("Page Layout").build();

  @Override
  public String getName() {
    return CLASS_DEF_HINT;
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

  @Override
  protected String getClassSpaceName() {
    return SPACE_NAME;
  }

  @Override
  protected String getClassDocName() {
    return DOC_NAME;
  }
}
