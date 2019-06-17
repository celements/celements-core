package com.celements.metatag;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface MetaTagRole {

  boolean getOverridable();

  void setOverridable(@Nullable Boolean overridable);

  @Nullable
  String getKey();

  void setKey(@Nullable String attributeKey);

  @Nullable
  String getValue();

  void setValue(@Nullable String content);

  @Nullable
  String getLang();

  void setLang(@Nullable String language);

  @NotNull
  String display();
}
