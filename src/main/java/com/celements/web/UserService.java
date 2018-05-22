package com.celements.web;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.QueryException;

import com.google.common.base.Optional;
import com.xpn.xwiki.user.api.XWikiUser;

@ComponentRole
public interface UserService {

  public static final String DEFAULT_LOGIN_FIELD = "name";

  @NotNull
  SpaceReference getUserSpaceRef();

  @NotNull
  DocumentReference completeUserDocRef(@NotNull String accountName);

  @NotNull
  XWikiUser newXWikiUser(@NotNull DocumentReference userDocRef);

  @NotNull
  Set<String> getPossibleLoginFields();

  @NotNull
  DocumentReference createUser(@NotNull String accountName, @NotNull Map<String, String> userData,
      boolean validate) throws UserCreateException;

  @NotNull
  DocumentReference createUser(@NotNull Map<String, String> userData, boolean validate)
      throws UserCreateException;

  @NotNull
  Optional<XWikiUser> getUserForData(@NotNull String login) throws QueryException;

  @NotNull
  Optional<XWikiUser> getUserForData(@NotNull String login,
      @NotNull Collection<String> possibleLogins) throws QueryException;

  @NotNull
  Optional<String> getUserEmail(@NotNull DocumentReference userDocRef);

  @NotNull
  Optional<String> getUserAdminLanguage(@NotNull DocumentReference userDocRef);

  boolean isUserActive(@NotNull DocumentReference userDocRef);

}
