package com.celements.model.classes.fields.ref;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.classes.fields.AbstractClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.NumberClass;

public abstract class EntityReferenceField<T extends EntityReference> extends AbstractClassField<T>
    implements CustomClassField<T> {

  private String prettyName;
  private Integer size;
  private String validationRegExp;
  private String validationMessage;

  public EntityReferenceField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  public String getPrettyName() {
    return prettyName;
  }

  public EntityReferenceField<T> setPrettyName(String prettyName) {
    this.prettyName = prettyName;
    return this;
  }

  public Integer getSize() {
    return size;
  }

  public EntityReferenceField<T> setSize(Integer size) {
    this.size = size;
    return this;
  }

  public String getValidationRegExp() {
    return validationRegExp;
  }

  public EntityReferenceField<T> setValidationRegExp(String validationRegExp) {
    this.validationRegExp = validationRegExp;
    return this;
  }

  public String getValidationMessage() {
    return validationMessage;
  }

  public EntityReferenceField<T> setValidationMessage(String validationMessage) {
    this.validationMessage = validationMessage;
    return this;
  }

  @Override
  public PropertyInterface getXField() {
    NumberClass element = new NumberClass();
    element.setName(getName());
    if (prettyName != null) {
      element.setPrettyName(prettyName);
    }
    if (size != null) {
      element.setSize(size);
    }
    if (validationRegExp != null) {
      element.setValidationRegExp(validationRegExp);
    }
    if (validationMessage != null) {
      element.setValidationMessage(validationMessage);
    }
    return element;
  }

  @Override
  public T resolve(Object obj) {
    return getWebUtils().resolveReference(obj.toString(), getType());
  }

  @Override
  public Object serialize(T value) {
    return getWebUtils().serializeRef(value);
  }

}
