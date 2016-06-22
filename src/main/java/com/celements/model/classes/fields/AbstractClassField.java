package com.celements.model.classes.fields;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.util.ModelUtils;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

/**
 * Subclasses are expected to be thread safe
 */
public abstract class AbstractClassField<T> implements ClassField<T> {

  private final DocumentReference classRef;
  private final String name;

  private volatile String prettyName;
  private volatile String validationRegExp;
  private volatile String validationMessage;

  public AbstractClassField(@NotNull DocumentReference classRef, @NotNull String name) {
    Objects.requireNonNull(classRef);
    this.classRef = ModelUtils.cloneReference(classRef, DocumentReference.class);
    this.name = Objects.requireNonNull(Strings.emptyToNull(name));
  }

  @Override
  public DocumentReference getClassRef() {
    return ModelUtils.cloneReference(classRef, DocumentReference.class);
  }

  @Override
  public String getName() {
    return name;
  }

  public String getPrettyName() {
    return prettyName;
  }

  public AbstractClassField<T> setPrettyName(String prettyName) {
    this.prettyName = prettyName;
    return this;
  }

  public String getValidationRegExp() {
    return validationRegExp;
  }

  public AbstractClassField<T> setValidationRegExp(String validationRegExp) {
    this.validationRegExp = validationRegExp;
    return this;
  }

  public String getValidationMessage() {
    return validationMessage;
  }

  public AbstractClassField<T> setValidationMessage(String validationMessage) {
    this.validationMessage = validationMessage;
    return this;
  }

  @Override
  public PropertyInterface getXField() {
    PropertyClass element = getPropertyClass();
    element.setName(name);
    if (prettyName != null) {
      element.setPrettyName(prettyName);
    }
    if (validationRegExp != null) {
      element.setValidationRegExp(validationRegExp);
    }
    if (validationMessage != null) {
      element.setValidationMessage(validationMessage);
    }
    return element;
  }

  protected abstract PropertyClass getPropertyClass();

  @Override
  public int hashCode() {
    return Objects.hash(classRef, name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AbstractClassField) {
      AbstractClassField<?> other = (AbstractClassField<?>) obj;
      return Objects.equals(this.classRef, other.classRef) && Objects.equals(this.name, other.name);
    }
    return false;
  }

  @Override
  public String toString() {
    return toString(true);
  }

  @Override
  public String toString(boolean local) {
    return getWebUtils().serializeRef(classRef, local) + "." + name;
  }

  protected static IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
