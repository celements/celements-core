package com.celements.web;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.auth.user.User;
import com.celements.auth.user.UserCreateException;
import com.celements.auth.user.UserInstantiationException;
import com.google.common.base.Optional;

@ComponentRole
public interface UserService {

  public static final String DEFAULT_LOGIN_FIELD = "name";

  @NotNull
  SpaceReference getUserSpaceRef();

  @NotNull
  DocumentReference completeUserDocRef(@NotNull String accountName);

  @NotNull
  User getUser(@NotNull DocumentReference userDocRef) throws UserInstantiationException;

  @NotNull
  Set<String> getPossibleLoginFields();

  @NotNull
  User createNewUser(@NotNull String accountName, @NotNull Map<String, String> userData,
      boolean validate) throws UserCreateException;

  @NotNull
  User createNewUser(@NotNull Map<String, String> userData, boolean validate)
      throws UserCreateException;

  @NotNull
  Optional<User> getUserForData(@NotNull String login);

  @NotNull
  Optional<User> getUserForData(@NotNull String login, @NotNull Collection<String> possibleLogins);

}
