package com.celements.model.classes.fields.list;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.xpn.xwiki.objects.classes.DBListClass;
import com.xpn.xwiki.objects.classes.ListClass;

@Immutable
public final class DBListField extends StringListField {

  private final String sql;

  public static class Builder extends ListField.Builder<Builder, String> {

    private String sql;

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    public Builder sql(@Nullable String val) {
      sql = val;
      return getThis();
    }

    @Override
    public DBListField build() {
      return new DBListField(getThis());
    }

  }

  protected DBListField(@NotNull Builder builder) {
    super(builder);
    this.sql = builder.sql;
  }

  public String getSql() {
    return sql;
  }

  @Override
  protected ListClass getListClass() {
    DBListClass element = new DBListClass();
    if (sql != null) {
      element.setSql(sql);
    }
    return element;
  }

}
