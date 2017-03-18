package com.celements.model.classes.fields;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.model.classes.ClassDefinition;
import com.google.common.base.MoreObjects;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

/**
 * Subclasses are expected to be immutable
 */
public abstract class AbstractClassField<T> extends AbstractParentClassField<T> {

  private final String classDefName;
  private final String prettyName;
  private final String validationRegExp;
  private final String validationMessage;

  public abstract static class Builder<B extends Builder<B, T>, T> extends
      AbstractParentClassField.Builder<B, T> {

    private final String classDefName;
    private String prettyName;
    private String validationRegExp;
    private String validationMessage;

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(name);
      this.classDefName = Objects.requireNonNull(classDefName);
    }

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

  }

  protected AbstractClassField(@NotNull Builder<?, T> builder) {
    super(builder);
    this.classDefName = builder.classDefName;
    this.prettyName = MoreObjects.firstNonNull(builder.prettyName, getName());
    this.validationRegExp = builder.validationRegExp;
    this.validationMessage = builder.validationMessage;
  }

  @Override
  public ClassDefinition getClassDef() {
    return Utils.getComponent(ClassDefinition.class, classDefName);
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
    element.setName(getName());
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

}
