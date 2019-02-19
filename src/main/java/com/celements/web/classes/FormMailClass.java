package com.celements.web.classes;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Component(FormMailClass.CLASS_DEF_HINT)
public class FormMailClass extends AbstractClassDefinition implements CelementsClassDefinition {

  public static final String SPACE_NAME = "Celements2";
  public static final String DOC_NAME = "FormMailClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_NAME = new StringField.Builder(CLASS_DEF_HINT,
      "name").size(30).build();
  public static final ClassField<String> FIELD_EMAIL_FROM = new StringField.Builder(CLASS_DEF_HINT,
      "emailFrom").size(30).build();
  public static final ClassField<String> FIELD_EMAIL_FIELDS = new StringField.Builder(
      CLASS_DEF_HINT, "emailFields").size(30).build();
  public static final ClassField<String> FIELD_USER_EMAIL_FIELDS = new StringField.Builder(
      CLASS_DEF_HINT, "userEmailFields").size(30).build();

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
