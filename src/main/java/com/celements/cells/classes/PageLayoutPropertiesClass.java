package com.celements.cells.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.google.errorprone.annotations.Immutable;

@Singleton
@Immutable
@Component(PageLayoutPropertiesClass.CLASS_DEF_HINT)
public class PageLayoutPropertiesClass extends AbstractClassDefinition
    implements CellsClassDefinition {

  public static final String DOC_NAME = "PageLayoutPropertiesClass";
  private static final String SPACE_NAME = "Celements";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_PRETTYNAME = new LargeStringField.Builder(CLASS_REF,
      "prettyname").prettyName("Layout Pretty Name").size(30).build();
  public static final ClassField<Boolean> FIELD_IS_ACTIVE = new BooleanField.Builder(CLASS_REF,
      "isActive").prettyName("is active").displayType("yesno").build();
  // TODO

  public PageLayoutPropertiesClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
