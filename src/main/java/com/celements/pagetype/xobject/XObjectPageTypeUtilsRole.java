package com.celements.pagetype.xobject;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.pagetype.PageTypeReference;

@ComponentRole
public interface XObjectPageTypeUtilsRole {

  @NotNull
  public DocumentReference getDocRefForPageType(@NotNull PageTypeReference pageTypeRef);

  @NotNull
  public DocumentReference getDocRefForPageType(@NotNull String configName);

}
