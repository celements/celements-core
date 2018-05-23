package com.celements.auth.user;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.google.common.base.Optional;

@ComponentRole
public interface UserService {

  public static final String DEFAULT_LOGIN_FIELD = "name";

  @NotNull
  SpaceReference getUserSpaceRef();

  @NotNull
  SpaceReference getUserSpaceRef(@Nullable WikiReference wikiRef);

  @NotNull
  DocumentReference completeUserDocRef(@NotNull String accountName);

  @NotNull
  User getUser(@NotNull DocumentReference userDocRef) throws UserInstantiationException;

  boolean isGuestUser(@NotNull DocumentReference userDocRef);

  @NotNull
  Set<String> getPossibleLoginFields();

  @NotNull
  User createNewUser(@NotNull String accountName, @NotNull Map<String, String> userData,
      boolean validate) throws UserCreateException;

  @NotNull
  User createNewUser(@NotNull Map<String, String> userData, boolean validate)
      throws UserCreateException;

  @NotNull
  Optional<User> getUserForLoginField(@NotNull String login);

  @NotNull
  Optional<User> getUserForLoginField(@NotNull String login,
      @NotNull Collection<String> possibleLoginFields);

}
