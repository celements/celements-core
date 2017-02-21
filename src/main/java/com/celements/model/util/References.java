package com.celements.model.util;

import static com.celements.model.util.EntityTypeUtil.*;
import static com.google.common.base.Preconditions.*;

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
    int ordinal = checkNotNull(ref).getType().ordinal();
    while (ref.getParent() != null) {
      ref = ref.getParent();
      ordinal -= ((ordinal == EntityType.OBJECT.ordinal()) ? 2 : 1); // skip attachment type
      if (ref.getType().ordinal() != ordinal) {
        return false; // wrong type order
      }
    }
    return ordinal == 0; // root has to be type root
  }

  /**
   * @param ref
   *          the reference to be cloned
   * @return a cloned instance of the reference
   */
  @NotNull
  public static EntityReference cloneRef(@NotNull EntityReference ref) {
    Class<? extends EntityReference> token = isAbsoluteRef(ref) ? getClassForEntityType(
        ref.getType()) : EntityReference.class;
    return cloneRef(ref, token);
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
    EntityReference extractedRef = null;
    Optional<EntityType> type = getEntityTypeForClass(token);
    if ((fromRef != null) && type.isPresent()) {
      extractedRef = fromRef.extractReference(type.get());
    }
    if (extractedRef != null) {
      return Optional.of(cloneRef(extractedRef, token));
    }
    return Optional.absent();
  }

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
  public static <T extends EntityReference> T adjustRef(@NotNull T ref, @NotNull Class<T> token,
      @Nullable EntityReference toRef) {
    checkNotNull(toRef);
    EntityReference adjustedRef = cloneRef(ref); // avoid modifying argument
    EntityReference current = adjustedRef;
    while (current != null) {
      if (current.getType() != toRef.getType()) {
        current = current.getParent();
      } else {
        if (current.getChild() != null) {
          current.getChild().setParent(cloneRef(toRef)); // set parent modifies child of param
        } else {
          adjustedRef = toRef;
        }
        break;
      }
    }
    return cloneRef(adjustedRef, token); // effective immutability
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
    Optional<EntityType> type = getEntityTypeForClass(token);
    if (type.isPresent()) {
      return createInternal(token, type.get(), name, parent);
    } else {
      throw new IllegalArgumentException("No entity type for class: " + token);
    }
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

}
