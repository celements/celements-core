package com.celements.metatag;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface MetaTagRole {

  boolean getOverridable();

  @Nullable
  void setOverridable(Boolean overridable);

  @Nullable
  String getKey();

  @Nullable
  void setKey(String attributeKey);

  @Nullable
  String getValue();

  @Nullable
  void setValue(String content);

  @Nullable
  String getLang();

  @Nullable
  void setLang(String language);

  @NotNull
  String display();
}
