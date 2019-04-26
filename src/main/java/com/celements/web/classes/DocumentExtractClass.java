package com.celements.web.classes;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Component(DocumentExtractClass.NAME)
public class DocumentExtractClass extends AbstractClassDefinition
    implements CelementsClassDefinition {

  public static final String SPACE_NAME = "Classes";
  public static final String DOC_NAME = "DocumentExtract";
  public static final String NAME = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_LANG = new StringField.Builder(NAME,
      "language").build();

  public static final ClassField<String> FIELD_EXTRACT = new StringField.Builder(NAME,
      "extract").build();

  @Override
  public String getName() {
    return NAME;
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
