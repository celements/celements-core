package com.celements.auth.classes;

import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.PasswordField;
import com.celements.model.classes.fields.PasswordField.StorageType;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.number.IntField;

@Immutable
@Singleton
@Component(RemoteLoginClass.CLASS_DEF_HINT)
public class RemoteLoginClass extends AbstractClassDefinition implements AuthClass {

  public static final String CLASS_NAME = "RemoteLoginClass";
  public static final String CLASS_DEF_HINT = AuthClass.CLASS_SPACE + "." + CLASS_NAME;

  public static final ClassField<String> FIELD_URL = new StringField.Builder(CLASS_DEF_HINT,
      "url").size(30).build();

  public static final ClassField<Integer> FIELD_TIMEOUT = new IntField.Builder(CLASS_DEF_HINT,
      "timeout").size(30).build();

  public static final ClassField<String> FIELD_USERNAME = new StringField.Builder(CLASS_DEF_HINT,
      "username").size(30).build();

  // TODO change to StorageType.Encrypted when available, see:
  // [CELDEV-733] PasswordField Encryption
  public static final ClassField<String> FIELD_PASSWORD = new PasswordField.Builder(CLASS_DEF_HINT,
      "password").storageType(StorageType.Clear).size(30).build();

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
    return CLASS_SPACE;
  }

  @Override
  protected String getClassDocName() {
    return CLASS_NAME;
  }
}
