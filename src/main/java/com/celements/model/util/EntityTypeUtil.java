package com.celements.model.util;

import static com.google.common.base.Preconditions.*;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.ImmutableDocumentReference;
import org.xwiki.model.reference.ObjectPropertyReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

public class EntityTypeUtil {

  private static final BiMap<Class<? extends EntityReference>, EntityType> ENTITY_TYPE_MAP;
  private static final Map<EntityType, String> REGEX_MAP;
  private static final Map<Class<? extends EntityReference>, Class<? extends EntityReference>> OVERRIDE_MAP;

  public static final String REGEX_WORD = "[a-zA-Z0-9_-]+";
  public static final String REGEX_WIKINAME = "[a-zA-Z0-9]+";
  public static final String REGEX_SPACE = "(" + REGEX_WIKINAME + "\\:)?" + REGEX_WORD;
  public static final String REGEX_DOC = REGEX_SPACE + "\\." + REGEX_WORD;
  public static final String REGEX_ATT = REGEX_DOC + "\\@" + ".+";

  static {
    ImmutableBiMap.Builder<Class<? extends EntityReference>, EntityType> builder;
    builder = ImmutableBiMap.builder();
    builder.put(WikiReference.class, EntityType.WIKI);
    builder.put(SpaceReference.class, EntityType.SPACE);
    builder.put(ImmutableDocumentReference.class, EntityType.DOCUMENT);
    builder.put(AttachmentReference.class, EntityType.ATTACHMENT);
    builder.put(ObjectReference.class, EntityType.OBJECT);
    builder.put(ObjectPropertyReference.class, EntityType.OBJECT_PROPERTY);
    ENTITY_TYPE_MAP = builder.build();

    ImmutableMap.Builder<EntityType, String> regexBuilder = ImmutableMap.builder();
    regexBuilder.put(EntityType.WIKI, REGEX_WIKINAME);
    regexBuilder.put(EntityType.SPACE, REGEX_SPACE);
    regexBuilder.put(EntityType.DOCUMENT, REGEX_DOC);
    regexBuilder.put(EntityType.ATTACHMENT, REGEX_ATT);
    REGEX_MAP = regexBuilder.build();

    ImmutableMap.Builder<Class<? extends EntityReference>, Class<? extends EntityReference>> overrideBuilder;
    overrideBuilder = ImmutableMap.builder();
    overrideBuilder.put(DocumentReference.class, ImmutableDocumentReference.class);
    // XXX add new (immutable) sub classes here
    OVERRIDE_MAP = overrideBuilder.build();
  }

  @NotNull
  public static Optional<EntityType> getEntityTypeForClass(
      @NotNull Class<? extends EntityReference> token) {
    token = checkClassOverride(checkNotNull(token));
    return Optional.fromNullable(ENTITY_TYPE_MAP.get(token));
  }

  @NotNull
  public static EntityType getEntityTypeForClassOrThrow(
      @NotNull Class<? extends EntityReference> token) {
    Optional<EntityType> type = getEntityTypeForClass(token);
    if (!type.isPresent()) {
      throw new IllegalArgumentException("No entity type for class: " + token);
    }
    return type.get();
  }

  @NotNull
  public static Class<? extends EntityReference> getClassForEntityType(@NotNull EntityType type) {
    Class<? extends EntityReference> token = ENTITY_TYPE_MAP.inverse().get(checkNotNull(type));
    if (token != null) {
      return token;
    } else {
      throw new IllegalStateException("No class for entity type: " + type);
    }
  }

  @NotNull
  public static EntityType getRootEntityType() {
    return EntityType.values()[0];
  }

  @NotNull
  public static EntityType getLastEntityType() {
    return EntityType.values()[EntityType.values().length - 1];
  }

  /**
   * @return the class for the root entity type
   */
  @NotNull
  public static Class<? extends EntityReference> getRootClass() {
    return checkNotNull(ENTITY_TYPE_MAP.inverse().get(getRootEntityType()));
  }

  /**
   * @param token
   * @return the override class (sub class of the given one) if there is one
   */
  @NotNull
  @SuppressWarnings("unchecked")
  public static <T extends EntityReference> Class<T> checkClassOverride(Class<T> token) {
    if (OVERRIDE_MAP.containsKey(token)) {
      return (Class<T>) OVERRIDE_MAP.get(token);
    }
    return token;
  }

  /**
   * identifies the reference class for the given absolute name (root type may be missing).<br>
   * <br>
   * simple names default to the root entity type.
   *
   * @param name
   *          the string representation
   * @return the identified reference class
   * @throws IllegalArgumentException
   *           for illegal strings
   */
  @NotNull
  public static Optional<EntityType> identifyEntityTypeFromName(@NotNull String name) {
    if (!checkNotNull(name).isEmpty()) {
      for (EntityType type : REGEX_MAP.keySet()) {
        if (name.matches(REGEX_MAP.get(type))) {
          return Optional.of(type);
        }
      }
    }
    return Optional.absent();
  }

  @NotNull
  public static Iterator<EntityType> createIterator() {
    return createIteratorAt(null);
  }

  @NotNull
  public static Iterator<EntityType> createIteratorFrom(@Nullable final EntityType startType) {
    Iterator<EntityType> ret = createIteratorAt(checkNotNull(startType));
    if ((startType != null) && ret.hasNext()) {
      ret.next();
    }
    return ret;
  }

  @NotNull
  public static Iterator<EntityType> createIteratorAt(@Nullable final EntityType startType) {
    return new Iterator<EntityType>() {

      private int ordinal = MoreObjects.firstNonNull(startType, getLastEntityType()).ordinal();

      @Override
      public boolean hasNext() {
        return ordinal >= 0;
      }

      @Override
      public EntityType next() {
        EntityType ret = EntityType.values()[ordinal];
        decrease();
        return ret;
      }

      private int decrease() {
        // skip type attachment for type OBJECT or OBJECT_PROPERTY
        return ordinal -= ((ordinal == EntityType.OBJECT.ordinal()) ? 2 : 1);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

}
