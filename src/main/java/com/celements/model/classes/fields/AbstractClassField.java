package com.celements.model.classes.fields;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.util.ModelUtils;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

/**
 * Subclasses are expected to be immutable
 */
@Immutable
public abstract class AbstractClassField<T> implements ClassField<T> {

  private final DocumentReference classRef;
  private final String name;
  private final String prettyName;
  private final String validationRegExp;
  private final String validationMessage;

  public abstract static class Builder<B extends Builder<B, T>, T> {

    private final DocumentReference classRef;
    private final String name;
    private String prettyName;
    private String validationRegExp;
    private String validationMessage;

    public Builder(@NotNull DocumentReference classRef, @NotNull String name) {
      Objects.requireNonNull(classRef);
      this.classRef = ModelUtils.cloneReference(classRef, DocumentReference.class);
      this.name = Objects.requireNonNull(Strings.emptyToNull(name));
    }

    public abstract B getThis();

    public B prettyName(@Nullable String val) {
      prettyName = val;
      return getThis();
    }

    public B validationRegExp(@Nullable String val) {
      validationRegExp = val;
      return getThis();
    }

    public B validationMessage(@Nullable String val) {
      validationMessage = val;
      return getThis();
    }

    public abstract AbstractClassField<T> build();

  }

  protected AbstractClassField(@NotNull Builder<?, T> builder) {
    this.classRef = builder.classRef;
    this.name = builder.name;
    this.prettyName = builder.prettyName;
    this.validationRegExp = builder.validationRegExp;
    this.validationMessage = builder.validationMessage;
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

  public String getValidationRegExp() {
    return validationRegExp;
  }

  public String getValidationMessage() {
    return validationMessage;
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
