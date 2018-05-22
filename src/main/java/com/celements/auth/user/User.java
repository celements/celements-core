package com.celements.auth.user;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.google.common.base.Optional;
import com.xpn.xwiki.user.api.XWikiUser;

@Immutable
@ComponentRole
public interface User {

  public void initialize(@NotNull DocumentReference userDocRef) throws UserInstantiationException;

  @NotNull
  DocumentReference getDocRef();

  @NotNull
  Optional<String> getEmail();

  @NotNull
  Optional<String> getAdminLanguage();

  boolean isActive();

  @NotNull
  XWikiUser asXWikiUser();

}
