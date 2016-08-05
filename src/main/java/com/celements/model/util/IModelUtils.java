package com.celements.model.util;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.EntityType;
import org.xwiki.model.internal.reference.DefaultStringEntityReferenceResolver;
import org.xwiki.model.reference.EntityReference;

import com.google.common.collect.BiMap;

@ComponentRole
public interface IModelUtils {

  public BiMap<Class<? extends EntityReference>, EntityType> getEntityTypeMap();

  /**
   * @param ref
   *          the reference to be cloned
   * @return a cloned instance of the reference
   */
  @NotNull
  public EntityReference cloneReference(@NotNull EntityReference ref);

  /**
   * @param ref
   *          the reference to be cloned
   * @param token
   *          type of the reference
   * @return a cloned instance of the reference of type T
   */
  @NotNull
  public <T extends EntityReference> T cloneReference(@NotNull EntityReference ref,
      @NotNull Class<T> token);

  /**
   * using {@link DefaultStringEntityReferenceResolver} but properly returning an instance of the
   * requested generic type
   *
   * @param name
   *          to be resolved
   * @param token
   *          for the reference type
   * @param baseRef
   *          a reference used as base for resolving
   * @return a resolved reference
   */
  @NotNull
  public <T extends EntityReference> T resolveRef(@NotNull String name, @NotNull Class<T> token,
      @Nullable EntityReference baseRef);

  /**
   * using {@link DefaultStringEntityReferenceResolver} but properly returning an instance of the
   * requested generic type
   *
   * @param name
   *          to be resolved
   * @param token
   *          for the reference type
   * @return a resolved reference
   */
  @NotNull
  public <T extends EntityReference> T resolveRef(@NotNull String name, @NotNull Class<T> token);

  @NotNull
  public String serializeRef(@NotNull EntityReference ref);

  @NotNull
  public String serializeRefLocal(@NotNull EntityReference ref);

  @NotNull
  public <T extends EntityReference> T extractRef(@NotNull EntityReference fromRef,
      @NotNull T defaultRef, @NotNull Class<T> token);

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

}
