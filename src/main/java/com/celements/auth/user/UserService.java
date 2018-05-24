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

  public static final String DEFAULT_LOGIN_FIELD = "loginname";

  /**
   * @return the default user space reference for the current wiki
   */
  @NotNull
  SpaceReference getUserSpaceRef();

  /**
   * @param wikiRef
   * @return the default user space reference for the given wikiRef
   */
  @NotNull
  SpaceReference getUserSpaceRef(@Nullable WikiReference wikiRef);

  /**
   * @param accountName
   * @return the resolved user doc reference, enforces the user space from
   *         {@link #getUserSpaceRef()}
   */
  @NotNull
  DocumentReference resolveUserDocRef(@NotNull String accountName);

  /**
   * @param userDocRef
   * @return a {@link User} instance for the given user doc
   * @throws UserInstantiationException
   *           if the given user doc is invalid
   */
  @NotNull
  User getUser(@NotNull DocumentReference userDocRef) throws UserInstantiationException;

  /**
   * @return list of user fields for which one can login, e.g. 'name', 'email' or 'validkey'
   */
  @NotNull
  Set<String> getPossibleLoginFields();

  /**
   * creates a new user with the provided userData. generates a random name if given accountName is
   * already taken.
   *
   * @param accountName
   * @param userData
   * @param validate
   *          sends a validation mail if true
   * @return the new {@link User} instance
   * @throws UserCreateException
   *           if the user creation process failed
   */
  @NotNull
  User createNewUser(@NotNull String accountName, @NotNull Map<String, String> userData,
      boolean validate) throws UserCreateException;

  /**
   * creates a new user with the provided userData. generates a random name if no account name is
   * provided.
   *
   * @param userData
   * @param validate
   *          sends a validation mail if true
   * @return the new {@link User} instance
   * @throws UserCreateException
   *           if the user creation process failed
   */
  @NotNull
  User createNewUser(@NotNull Map<String, String> userData, boolean validate)
      throws UserCreateException;

  /**
   * looks up the user by the given login value with {@link #getPossibleLoginFields()}
   *
   * @param login
   *          the login value
   * @return the matched {@link User} instance or absent if the given login doesn't match or isn't
   *         unique
   */
  @NotNull
  Optional<User> getUserForLoginField(@NotNull String login);

  /**
   * looks up the user by the given login value with the possible login fields.
   *
   * @param login
   *          the login value
   * @param possibleLoginFields
   *          list of user fields for which one can login, e.g. 'name', 'email' or 'validkey'
   * @return the matched {@link User} instance or absent if the given login doesn't match or isn't
   *         unique
   */
  @NotNull
  Optional<User> getUserForLoginField(@NotNull String login,
      @Nullable Collection<String> possibleLoginFields);

}
