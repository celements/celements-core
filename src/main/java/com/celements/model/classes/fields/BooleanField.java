package com.celements.model.classes.fields;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.xpn.xwiki.objects.classes.BooleanClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

@Immutable
public final class BooleanField extends AbstractClassField<Boolean> {

  private final String displayType;
  private final Integer defaultValue;

  public static class Builder extends AbstractClassField.Builder<Builder, Boolean> {

    private String displayType;
    private Integer defaultValue;

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    public Builder displayType(@Nullable String val) {
      displayType = val;
      return getThis();
    }

    public Builder defaultValue(@Nullable Integer val) {
      defaultValue = val;
      return getThis();
    }

    @Override
    public BooleanField build() {
      return new BooleanField(getThis());
    }
  }

  protected BooleanField(@NotNull Builder builder) {
    super(builder);
    this.displayType = builder.displayType;
    this.defaultValue = builder.defaultValue;
  }

  @Override
  public Class<Boolean> getType() {
    return Boolean.class;
  }

  public String getDisplayType() {
    return displayType;
  }

  public Integer getDefaultValue() {
    return defaultValue;
  }

  @Override
  protected PropertyClass getPropertyClass() {
    BooleanClass element = new BooleanClass();
    if (displayType != null) {
      element.setDisplayType(displayType);
    }
    if (defaultValue != null) {
      element.setDefaultValue(defaultValue);
    }
    return element;
  }

}
