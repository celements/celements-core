package com.celements.model.util;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.EntityReference;

import com.google.common.base.Optional;

@ComponentRole
public interface ModelUtils {

  /**
   * @param ref
   * @return false if the given reference is relative
   */
  public boolean isAbsoluteRef(@NotNull EntityReference ref);

  /**
   * @param ref
   *          the reference to be cloned
   * @return a cloned instance of the reference
   */
  @NotNull
  public EntityReference cloneRef(@NotNull EntityReference ref);

  /**
   * @param ref
   *          the reference to be cloned
   * @param token
   *          type of the reference
   * @return a cloned instance of the reference of type T
   * @throws IllegalArgumentException
   *           when relative references are being cloned as subtypes of {@link EntityReference}
   */
  @NotNull
  public <T extends EntityReference> T cloneRef(@NotNull EntityReference ref,
      @NotNull Class<T> token);

  /**
   * @param fromRef
   *          the reference to extract from
   * @param token
   *          reference class to extract
   * @return optional of the extracted reference
   */
  @Nullable
  public <T extends EntityReference> Optional<T> extractRef(@Nullable EntityReference fromRef,
      @NotNull Class<T> token);

  /**
   * adjust a reference to another one of higher order, e.g. a docRef to another wikiRef.
   *
   * @param ref
   *          to be adjusted
   * @param token
   *          for the reference type
   * @param toRef
   *          it is adjusted to
   * @return a new instance of the adjusted reference or ref if toRef was of lower order
   */
  @NotNull
  public <T extends EntityReference> T adjustRef(@NotNull T ref, @NotNull Class<T> token,
      @Nullable EntityReference toRef);

  /**
   * resolves an absolute reference from the given name
   *
   * @param name
   *          to be resolved, may not be empty
   * @return a resolved reference
   * @throws IllegalArgumentException
   *           if unable to resolve absolute reference from name
   */
  @NotNull
  public EntityReference resolveRef(@NotNull String name);

  /**
   * resolves an absolute reference from the given name and baseRef
   *
   * @param name
   *          to be resolved, may not be empty
   * @param baseRef
   *          a reference used as base for resolving
   * @return a resolved reference
   * @throws IllegalArgumentException
   *           if unable to resolve absolute reference from name and baseRef
   */
  @NotNull
  public EntityReference resolveRef(@NotNull String name, @Nullable EntityReference baseRef);

  /**
   * resolves an absolute reference from the given name and baseRef
   *
   * @param name
   *          to be resolved, may not be empty
   * @param token
   *          for the reference type
   * @param baseRef
   *          a reference used as base for resolving
   * @return a resolved reference
   * @throws IllegalArgumentException
   *           if unable to resolve absolute reference from name and baseRef
   */
  @NotNull
  public <T extends EntityReference> T resolveRef(@NotNull String name, @NotNull Class<T> token,
      @Nullable EntityReference baseRef);

  /**
   * resolves an absolute reference from the given name
   *
   * @param name
   *          to be resolved, may not be empty
   * @param token
   *          for the reference type
   * @return a resolved reference
   * @throws IllegalArgumentException
   *           if unable to resolve absolute reference from name
   */
  @NotNull
  public <T extends EntityReference> T resolveRef(@NotNull String name, @NotNull Class<T> token);

  /**
   * @param ref
   * @return serialised global string representation of the given reference (e.g. "wiki:space.doc")
   */
  @NotNull
  public String serializeRef(@NotNull EntityReference ref);

  /**
   * @param ref
   * @return serialised local string representation of the given reference (e.g. "space.doc")
   */
  @NotNull
  public String serializeRefLocal(@NotNull EntityReference ref);

}
