package com.celements.cells.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.google.errorprone.annotations.Immutable;

@Singleton
@Immutable
@Component(GroupCellClass.CLASS_DEF_HINT)
public class GroupCellClass extends AbstractClassDefinition
    implements CellsClassDefinition {

  public static final String DOC_NAME = "GroupCellClass";
  public static final String SPACE_NAME = "Celements";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_RENDER_LAYOUT = new StringField.Builder(CLASS_REF,
      "render_layout")
          .prettyName("Render Layout")
          .size(30)
          .build();

  public GroupCellClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

}
