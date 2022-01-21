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
@Component(TranslationBoxCellConfigClass.CLASS_DEF_HINT)
public class TranslationBoxCellConfigClass extends AbstractClassDefinition
    implements CellsClassDefinition {

  public static final String DOC_NAME = "TranslationBoxCellConfigClass";
  public static final String SPACE_NAME = "Celements";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_PAGE_EXCEPTIONS = new StringField.Builder(CLASS_REF,
      "page_exceptions")
          .prettyName("Page Exceptions (FullNames comma separated)")
          .size(30)
          .build();
  public static final ClassField<String> FIELD_PAGETYPE_EXCEPTIONS = new StringField.Builder(
      CLASS_REF, "pagetype_exceptions")
          .prettyName("Page Type Exceptions (FullNames comma separated)")
          .size(30)
          .build();

  public TranslationBoxCellConfigClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

}
