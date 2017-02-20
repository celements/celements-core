package com.celements.model.util;

import static com.celements.model.util.EntityTypeUtil.*;
import static com.google.common.base.Preconditions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

public class References {

  /**
   * @param ref
   * @return false if the given reference is relative
   */
  public static boolean isAbsoluteRef(@NotNull EntityReference ref) {
    EntityTypeIterator iter = new EntityTypeIterator(checkNotNull(ref).getType());
    while (ref.getParent() != null) {
      ref = ref.getParent();
      if (!iter.hasNext() || (ref.getType() != iter.next())) {
        return false; // wrong type order
      }
    }
    return !iter.hasNext(); // root has to be type root
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
   * adjust a relative or absolute reference to another one of higher order, e.g. a docRef to
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
  public static <T extends EntityReference> T adjustRef(@NotNull T ref, @NotNull Class<T> token,
      @Nullable EntityReference toRef) {
    EntityType type = (token != EntityReference.class) ? getEntityTypeForClass(token) : null;
    return cloneRef(combineRef(type, toRef, checkNotNull(ref)).get(), token);
  }

  /**
   * builds an absolute reference of the given class with the provided references
   *
   * @param token
   *          for the reference type
   * @param refs
   * @return a new instance of the combined references
   */
  /**
   * @param token
   * @param refs
   * @return
   */
  @NotNull
  public static <T extends EntityReference> Optional<T> completeRef(@NotNull Class<T> token,
      EntityReference... refs) {
    // TODO use combineRef and check for absolute
    EntityType retType = getEntityTypeForClass(token);
    Optional<? extends EntityReference> ret = extractSimpleRef(retType, refs);
    for (EntityTypeIterator iter = new EntityTypeIterator(retType); ret.isPresent()
        && iter.hasNext();) {
      Optional<? extends EntityReference> extrRef = extractSimpleRef(iter.next(), refs);
      if (extrRef.isPresent()) {
        ret.get().getRoot().setParent(extrRef.get());
      } else {
        ret = Optional.absent();
      }
    }
    if (ret.isPresent()) {
      return Optional.of(cloneRef(ret.get(), token)); // effective immutability
    } else {
      return Optional.absent();
    }
  }

  @NotNull
  public static Optional<EntityReference> combineRef(EntityReference... refs) {
    return combineRef(null, refs);
  }

  @NotNull
  public static Optional<EntityReference> combineRef(@Nullable EntityType type,
      EntityReference... refs) {
    EntityReference ret = null;
    for (EntityTypeIterator iter = new EntityTypeIterator(type); iter.hasNext();) {
      Optional<? extends EntityReference> extrRef = extractSimpleRef(iter.next(), refs);
      if (extrRef.isPresent()) {
        if (ret == null) {
          ret = extrRef.get();
        } else {
          ret.getRoot().setParent(extrRef.get());
        }
      }
    }
    if (ret != null) {
      return Optional.of(cloneRef(ret)); // effective immutability
    } else {
      return Optional.absent();
    }
  }

  private static Optional<? extends EntityReference> extractSimpleRef(EntityType type,
      EntityReference... fromRefs) {
    for (EntityReference fromRef : fromRefs) {
      Optional<? extends EntityReference> ref = extractRef(fromRef, getClassForEntityType(type));
      if (ref.isPresent()) {
        return Optional.of(create(type, ref.get().getName()));
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

class EntityTypeIterator implements Iterator<EntityType> {

  private EntityType type;

  EntityTypeIterator(@Nullable EntityType type) {
    this.type = MoreObjects.firstNonNull(type, getTopEntityType());
  }

  private static EntityType getTopEntityType() {
    return EntityType.values()[EntityType.values().length - 1];
  }

  @Override
  public boolean hasNext() {
    return type.ordinal() > 0;
  }

  @Override
  public EntityType next() {
    int ordinal = type.ordinal() - ((type == EntityType.OBJECT) ? 2 : 1); // skip attachment type
    return type = EntityType.values()[ordinal];
  }

}
