package com.celements.cells.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.cells.HtmlDoctype;
import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.DisplayType;
import com.celements.model.classes.fields.list.single.ComponentSingleListField;
import com.celements.model.classes.fields.list.single.StringSingleListField;
import com.celements.rendering.head.HtmlHeadConfiguratorRole;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;

@Singleton
@Immutable
@Component(PageLayoutPropertiesClass.CLASS_DEF_HINT)
public class PageLayoutPropertiesClass extends AbstractClassDefinition
    implements CellsClassDefinition {

  public static final String DOC_NAME = "PageLayoutPropertiesClass";
  public static final String SPACE_NAME = "Celements";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final String EDITOR_LAYOUT_VALUE = "editorLayout";
  public static final String PAGE_LAYOUT_VALUE = "pageLayout";

  public static final ClassField<String> FIELD_PRETTYNAME = new StringField.Builder(CLASS_REF,
      "prettyname")
          .prettyName("Layout Pretty Name")
          .size(30)
          .build();

  public static final ClassField<Boolean> FIELD_IS_ACTIVE = new BooleanField.Builder(CLASS_REF,
      "isActive")
          .displayType("yesno")
          .prettyName("is active")
          .build();

  public static final ClassField<String> FIELD_AUTHORS = new StringField.Builder(CLASS_REF,
      "authors")
          .prettyName("Authors")
          .size(30)
          .build();

  public static final ClassField<String> FIELD_LICENSE = new LargeStringField.Builder(CLASS_REF,
      "license")
          .rows(30)
          .size(15)
          .prettyName("License")
          .build();

  public static final ClassField<String> FIELD_VERSION = new StringField.Builder(CLASS_REF,
      "version")
          .prettyName("Version")
          .size(30)
          .build();

  public static final ClassField<String> FIELD_LAYOUT_DOCTYPE = new StringSingleListField.Builder(
      CLASS_REF, "doctype")
          .size(1)
          .displayType(DisplayType.select)
          .values(ImmutableList.of(HtmlDoctype.XHTML.toString(), HtmlDoctype.HTML5.toString()))
          .prettyName("Doctype")
          /* .relationalStorage(true) */
          .build();

  public static final ClassField<String> FIELD_LAYOUT_TYPE = new StringSingleListField.Builder(
      CLASS_REF, "layout_type")
          .size(1)
          .displayType(DisplayType.select)
          .values(ImmutableList.of(PAGE_LAYOUT_VALUE, EDITOR_LAYOUT_VALUE))
          .prettyName("Layout Type")
          /* .relationalStorage(true) */
          .build();

  public static final ClassField<HtmlHeadConfiguratorRole> FIELD_HTML_HEAD_CONFIGURATOR = new ComponentSingleListField.Builder<>(
      CLASS_REF, "html_head_configurator", HtmlHeadConfiguratorRole.class)
          .prettyName("HTML head Configurator")
          .build();

  public PageLayoutPropertiesClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
