package com.celements.pagetype.classes;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.number.IntField;

@Component(PageTypePropertiesClass.CLASS_DEF_HINT)
public class PageTypePropertiesClass extends AbstractClassDefinition {

  public static final String SPACE_NAME = "Celements2";
  public static final String DOC_NAME = "PageType";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public static final ClassField<String> PAGETYPE_PROP_TYPE_NAME = new StringField.Builder(
      CLASS_DEF_HINT, "type_name").size(30).prettyName("Type Pretty Name").build();
  public static final ClassField<String> PAGETYPE_PROP_CATEGORY = new StringField.Builder(
      CLASS_DEF_HINT, "category").size(30).prettyName("Category").build();
  public static final ClassField<String> PAGETYPE_PROP_PAGE_EDIT = new StringField.Builder(
      CLASS_DEF_HINT, "page_edit").size(30).prettyName("Type Edit Template").build();
  public static final ClassField<String> PAGETYPE_PROP_PAGE_VIEW = new StringField.Builder(
      CLASS_DEF_HINT, "page_view").size(30).prettyName("Type View Template").build();
  public static final ClassField<Boolean> PAGETYPE_PROP_VISIBLE = new BooleanField.Builder(
      CLASS_DEF_HINT, "visible").prettyName("visible").displayType("yesno").defaultValue(0).build();
  public static final ClassField<Boolean> PAGETYPE_PROP_SHOW_FRAME = new BooleanField.Builder(
      CLASS_DEF_HINT, "visible").prettyName("Show Frame").displayType("yesno").defaultValue(
          0).build();
  public static final ClassField<Boolean> PAGETYPE_PROP_LOAD_RICHTEXT = new BooleanField.Builder(
      CLASS_DEF_HINT, "load_richtext").prettyName("Load Richtext Editor").displayType(
          "yesno").defaultValue(0).build();
  public static final ClassField<Integer> PAGETYPE_PROP_RTE_WIDTH = new IntField.Builder(
      CLASS_DEF_HINT, "rte_width").prettyName("Richtext Editor Width").size(30).prettyName(
          "integer").build();
  public static final ClassField<Integer> PAGETYPE_PROP_RTE_HEIGHT = new IntField.Builder(
      CLASS_DEF_HINT, "rte_height").prettyName("Richtext Editor Height").size(30).prettyName(
          "integer").build();
  public static final ClassField<Boolean> PAGETYPE_PROP_HASPAGETITLE = new BooleanField.Builder(
      CLASS_DEF_HINT, "haspagetitle").prettyName("Has Page Title").displayType(
          "yesno").defaultValue(0).build();
  public static final ClassField<Boolean> PAGETYPE_PROP_IS_UNCONNECTED_PARENT = new BooleanField.Builder(
      CLASS_DEF_HINT, "unconnected_parent").prettyName("Is Unconnected Parent").displayType(
          "yesno").defaultValue(0).build();
  public static final ClassField<String> PAGETYPE_PROP_TAG_NAME = new StringField.Builder(
      CLASS_DEF_HINT, "tag_name").size(30).prettyName("Tag Name").build();

  @Override
  public String getName() {
    return CLASS_DEF_HINT;
  }

  @Override
  public boolean isInternalMapping() {
    return true;
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
