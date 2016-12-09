package com.celements.model.classes.fields.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.xpn.xwiki.objects.classes.UsersClass;
import com.xpn.xwiki.user.api.XWikiUser;

@Immutable
public final class ListOfUsersField extends ListField<XWikiUser> {

  private final Boolean usesList;

  public static class Builder extends ListField.Builder<Builder, XWikiUser> {

    private Boolean usesList;

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name);
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
  public Object serialize(List<XWikiUser> value) {
    Object ret = null;
    if (value != null) {
      StringBuilder sb = new StringBuilder();
      for (XWikiUser val : value) {
        if (sb.length() > 0) {
          sb.append(getSeparator());
        }
        sb.append(val.getUser());
      }
      ret = sb.toString();
    }
    return ret;
  }

  @Override
  protected List<XWikiUser> resolveList(List<?> list) {
    List<XWikiUser> ret = new ArrayList<>();
    for (Object elem : (Collection<?>) list) {
      ret.add(new XWikiUser(elem.toString()));
    }
    return Collections.unmodifiableList(ret);
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
