package com.celements.form.classes;

import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Immutable
@Singleton
@Component(FormConfigClass.CLASS_DEF_HINT)
public class FormConfigClass extends AbstractClassDefinition implements FormClass {

  public static final String CLASS_NAME = "FormConfigClass";
  public static final String CLASS_DEF_HINT = CLASS_SPACE + "." + CLASS_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(CLASS_SPACE, CLASS_NAME);

  public static final ClassField<String> FIELD_FORM_LAYOUT = new StringField.Builder(
      CLASS_REF, "formLayout").size(30).prettyName("Formular Layout Name").build();

  public static final ClassField<String> FIELD_SUCCESSFULPAGE = new StringField.Builder(
      CLASS_REF, "successfulpage").size(30).build();

  public static final ClassField<String> FIELD_FAILEDPAGE = new StringField.Builder(
      CLASS_REF, "failedpage").size(30).build();

  public static final ClassField<String> FIELD_EXCLUDE_FORM_IS_FILLED = new StringField.Builder(
      CLASS_REF, "excludeFromIsFilledCheck").size(30)
          .prettyName("Exclude fields from 'isFilled' check. (separator: ',')")
          .build();

  public FormConfigClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }
}
