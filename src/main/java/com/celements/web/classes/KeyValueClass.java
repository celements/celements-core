package com.celements.web.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Singleton
@Component(KeyValueClass.CLASS_DEF_HINT)
public class KeyValueClass extends AbstractClassDefinition implements CelementsClassDefinition {

  public static final String SPACE_NAME = "Classes";
  public static final String DOC_NAME = "KeyValueClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_LABEL = new StringField.Builder(
      CLASS_REF, "label").build();

  public static final ClassField<String> FIELD_KEY = new StringField.Builder(
      CLASS_REF, "key").build();

  public static final ClassField<String> FIELD_VALUE = new StringField.Builder(
      CLASS_REF, "value").build();

  public KeyValueClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
