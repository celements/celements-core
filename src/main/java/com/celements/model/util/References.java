package com.celements.model.util;

import static com.celements.model.util.EntityTypeUtil.*;
import static com.google.common.base.Preconditions.*;

import java.util.Iterator;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.google.common.base.Optional;

public class References {

  /**
   * @param ref
   * @return false if the given reference is relative
   */
  public static boolean isAbsoluteRef(@NotNull EntityReference ref) {
    Iterator<EntityType> iter = createIteratorFrom(checkNotNull(ref).getType());
    while (iter.hasNext()) {
      ref = ref.getParent();
      if ((ref == null) || (ref.getType() != iter.next())) {
        // incomplete or wrong type order
        return false;
      }
    }
    return ref.getParent() == null; // has to be iterated to root level
  }

  @NotNull
  public static Class<? extends EntityReference> determineClass(@NotNull EntityReference ref) {
    return isAbsoluteRef(ref) ? getClassForEntityType(ref.getType()) : EntityReference.class;
  }

  /**
   * @param ref
   *          the reference to be cloned
   * @return a cloned instance of the reference
   */
  @NotNull
  public static EntityReference cloneRef(@NotNull EntityReference ref) {
    return cloneRef(ref, determineClass(ref));
  }

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
  public static <T extends EntityReference> T cloneRef(@NotNull EntityReference ref,
      @NotNull Class<T> token) {
    checkNotNull(ref);
    checkNotNull(token);
    // clone as immutable subclass is preferable
    token = checkSubClassOverride(token);
    try {
      ref = ref.clone();
      T ret;
      if (token == EntityReference.class) {
        ret = token.cast(ref);
      } else if (isAbsoluteRef(ref)) {
        ret = token.getConstructor(EntityReference.class).newInstance(ref);
      } else {
        throw new IllegalArgumentException("Relative references can only be returned as "
            + "EntityReference");
      }
      return ret;
    } catch (ReflectiveOperationException | SecurityException exc) {
      throw new IllegalArgumentException("Unsupported entity class: " + token, exc);
    }
  }

  /**
   * @param fromRef
   *          the reference to extract from
   * @param token
   *          reference class to extract
   * @return optional of the extracted reference
   */
  public static <T extends EntityReference> Optional<T> extractRef(
      @Nullable EntityReference fromRef, @NotNull Class<T> token) {
    EntityReference ret = null;
    Optional<EntityType> type = getEntityTypeForClass(token);
    if (type.isPresent()) {
      ret = extractRef(fromRef, type.get()).orNull();
    }
    return castOrAbsent(ret, token);
  }

  public static Optional<EntityReference> extractRef(@Nullable EntityReference fromRef,
      @NotNull EntityType type) {
    EntityReference ret = null;
    if (fromRef != null) {
      ret = fromRef.extractReference(checkNotNull(type));
    }
    return cloneOrAbsent(ret);
  }

  /**
   * adjusts a relative or absolute reference to another one of higher order, e.g. a docRef to
   * another wikiRef.
   *
   * @param ref
   *          to be adjusted
   * @param token
   *          for the reference type
   * @param toRef
   *          it is adjusted to
   * @return a new instance of the adjusted reference
   */
  @NotNull
  public static <T extends EntityReference> T adjustRef(@NotNull T ref,
      @NotNull Class<? extends T> token, @Nullable EntityReference toRef) {
    EntityType type = getEntityTypeForClass(token).orNull();
    // combinedRef cannot be absent since ref is not null
    EntityReference combinedRef = combineRef(token, type, toRef, checkNotNull(ref)).get();
    // return value cannot be absent since ref is enforced to be of token class by signature
    return castOrAbsent(combinedRef, token).get();
  }

  /**
   * builds an absolute reference of the given token with the provided references (FIFO)
   *
   * @param token
   *          for the reference type
   * @param refs
   * @return a new, absolute instance of the combined references
   * @throws IllegalArgumentException
   *           if token is {@link EntityReference}, instead use
   *           {@link #combineRef(EntityReference...)} for relative references
   */
  @NotNull
  public static <T extends EntityReference> Optional<T> completeRef(@NotNull Class<T> token,
      EntityReference... refs) {
    EntityReference combinedRef = combineRef(token, getEntityTypeForClassOrThrow(token),
        refs).orNull();
    return castOrAbsent(combinedRef, token);
  }

  /**
   * builds a relative reference with the provided references (FIFO)
   *
   * @param refs
   * @return a new, relative instance of the combined references
   */
  @NotNull
  public static Optional<EntityReference> combineRef(EntityReference... refs) {
    return combineRef(EntityReference.class, null, refs);
  }

  /**
   * builds a relative reference from the given type (bottom-up) with the provided references (FIFO)
   *
   * @param type
   *          for the reference type
   * @param refs
   * @return a new, relative instance of the combined references
   */
  @NotNull
  public static Optional<EntityReference> combineRef(@Nullable EntityType type,
      EntityReference... refs) {
    return combineRef(EntityReference.class, type, refs);
  }

  @NotNull
  private static <T extends EntityReference> Optional<T> combineRef(@NotNull Class<T> token,
      @Nullable EntityType type, EntityReference... refs) {
    EntityReference ret = null;
    for (Iterator<EntityType> iter = createIteratorAt(type); iter.hasNext();) {
      Optional<EntityReference> extrRef = extractSimpleRef(iter.next(), refs);
      if (extrRef.isPresent()) {
        if (ret == null) {
          ret = extrRef.get();
        } else {
          ret.getRoot().setParent(extrRef.get());
        }
      }
    }
    return cloneOrAbsent(ret, token); // clone for effective immutability
  }

  private static Optional<EntityReference> extractSimpleRef(EntityType type,
      EntityReference... fromRefs) {
    if (fromRefs != null) {
      for (EntityReference fromRef : fromRefs) {
        Optional<? extends EntityReference> ref = extractRef(fromRef, type);
        if (ref.isPresent()) {
          return Optional.of(create(type, ref.get().getName())); // strip parent
        }
      }
    }
    return Optional.absent();
  }

  public static EntityReference create(@NotNull EntityType type, @NotNull String name) {
    return createInternal(EntityReference.class, type, name, null);
  }

  public static EntityReference create(@NotNull EntityType type, @NotNull String name,
      @Nullable EntityReference parent) {
    return createInternal(EntityReference.class, type, name, parent);
  }

  public static <T extends EntityReference> T create(@NotNull Class<T> token,
      @NotNull String name) {
    return create(token, name, null);
  }

  public static <T extends EntityReference> T create(@NotNull Class<T> token, @NotNull String name,
      @Nullable EntityReference parent) {
    return createInternal(token, getEntityTypeForClassOrThrow(token), name, parent);
  }

  private static <T extends EntityReference> T createInternal(@NotNull Class<T> token,
      @NotNull EntityType type, @NotNull String name, @Nullable EntityReference parent) {
    checkNotNull(name);
    checkNotNull(type);
    checkNotNull(token);
    if (parent != null) {
      parent = cloneRef(parent);
    }
    return cloneRef(new EntityReference(name, type, parent), token);
  }

  private static <T extends EntityReference> Optional<T> castOrAbsent(EntityReference ref,
      Class<T> token) {
    if ((ref != null) && (checkNotNull(token).isAssignableFrom(ref.getClass()))) {
      return Optional.of(token.cast(ref));
    } else {
      return Optional.absent();
    }
  }

  private static <T extends EntityReference> Optional<T> cloneOrAbsent(EntityReference ref,
      Class<T> token) {
    if ((ref != null) && checkNotNull(token).isAssignableFrom(determineClass(ref))) {
      return Optional.of(cloneRef(ref, token));
    } else {
      return Optional.absent();
    }
  }

  private static Optional<EntityReference> cloneOrAbsent(EntityReference ref) {
    if (ref != null) {
      return Optional.of(cloneRef(ref));
    } else {
      return Optional.absent();
    }
  }

}
