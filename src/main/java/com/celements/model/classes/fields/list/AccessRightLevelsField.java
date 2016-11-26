package com.celements.model.classes.fields.list;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.xpn.xwiki.objects.classes.LevelsClass;
import com.xpn.xwiki.objects.classes.ListClass;

@Immutable
public final class AccessRightLevelsField extends StringListField {

  public static class Builder extends ListField.Builder<Builder, String> {

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public AccessRightLevelsField build() {
      return new AccessRightLevelsField(this);
    }

  }

  protected AccessRightLevelsField(@NotNull Builder builder) {
    super(builder);
  }

  @Override
  protected ListClass getListClass() {
    return new LevelsClass();
  }

}
