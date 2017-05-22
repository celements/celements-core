package com.celements.model.classes.fields.list;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.marshalling.XWikiUserMarshaller;
import com.xpn.xwiki.objects.classes.UsersClass;
import com.xpn.xwiki.user.api.XWikiUser;

@Immutable
public final class ListOfUsersField extends ListField<XWikiUser> {

  private final Boolean usesList;

  public static class Builder extends ListField.Builder<Builder, XWikiUser> {

    private Boolean usesList;

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name, new XWikiUserMarshaller());
      separator(",");
    }

    @Override
    public Builder getThis() {
      return this;
    }

    public Builder usesList(@NotNull Boolean usesList) {
      this.usesList = usesList;
      return getThis();
    }

    @Override
    public ListOfUsersField build() {
      return new ListOfUsersField(getThis());
    }

  }

  protected ListOfUsersField(@NotNull ListOfUsersField.Builder builder) {
    super(builder);
    this.usesList = builder.usesList;
  }

  public Boolean getUsesList() {
    return usesList;
  }

  @Override
  protected UsersClass getListClass() {
    UsersClass element = new UsersClass();
    if (usesList != null) {
      element.setUsesList(usesList);
    }
    return element;
  }

}
