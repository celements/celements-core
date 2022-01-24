package com.celements.cells.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.google.errorprone.annotations.Immutable;

@Singleton
@Immutable
@Component(PageDepCellConfigClass.CLASS_DEF_HINT)
public class PageDepCellConfigClass extends AbstractClassDefinition
    implements CellsClassDefinition {

  public static final String DOC_NAME = "PageDepCellConfigClass";
  public static final String SPACE_NAME = "Celements";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_SPACE_NAME = new StringField.Builder(CLASS_REF,
      "space_name")
          .prettyName("Space Name")
          .size(30)
          .build();

  public static final ClassField<Boolean> FIELD_IS_ACTIVE = new BooleanField.Builder(CLASS_REF,
      "is_inheritable")
          .displayType("yesno")
          .prettyName("is inheritable")
          .build();

  public PageDepCellConfigClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

}
