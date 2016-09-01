package com.celements.model.classes.fields;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StringClass;

@Immutable
public final class TestClassField extends AbstractClassField<TestClassField> {

  private final Integer size;

  public static class Builder extends AbstractClassField.Builder<Builder, TestClassField> {

    private Integer size;

    public Builder(@NotNull String className, @NotNull String name) {
      super(className, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    public Builder size(@Nullable Integer val) {
      size = val;
      return this;
    }

    @Override
    public TestClassField build() {
      return new TestClassField(this);
    }

  }

  protected TestClassField(@NotNull Builder builder) {
    super(builder);
    this.size = builder.size;
  }

  public Integer getSize() {
    return size;
  }

  @Override
  public Class<TestClassField> getType() {
    return TestClassField.class;
  }

  @Override
  protected PropertyClass getPropertyClass() {
    return new StringClass();
  }

}
