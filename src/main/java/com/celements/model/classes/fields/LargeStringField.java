package com.celements.model.classes.fields;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;

@Immutable
public final class LargeStringField extends StringField {

  private final Integer rows;

  public static class Builder extends StringField.Builder {

    private Integer rows;

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name);
      this.rows(rows);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    public Builder rows(@Nullable Integer val) {
      rows = val;
      return getThis();
    }

    @Override
    public LargeStringField build() {
      return new LargeStringField(getThis());
    }

  }

  protected LargeStringField(@NotNull Builder builder) {
    super(builder);
    this.rows = builder.rows;
  }

  @Override
  public Class<String> getType() {
    return String.class;
  }

  public Integer getRows() {
    return rows;
  }

  @Override
  public StringClass getStringPropertyClass() {
    TextAreaClass textAreaClass = new TextAreaClass();
    textAreaClass.setRows(rows);
    return textAreaClass;
  }

}
