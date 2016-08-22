package com.celements.model.util;

import static com.google.common.base.Preconditions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.ObjectPropertyReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.google.common.base.MoreObjects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class References {

  private static final BiMap<Class<? extends EntityReference>, EntityType> ENTITY_TYPE_MAP;
  private static final Map<Class<? extends EntityReference>, String> REGEX_MAP;

  public static final String REGEX_WORD = "[a-zA-Z0-9_-]+";
  public static final String REGEX_WIKINAME = "[a-zA-Z0-9]+";
  public static final String REGEX_SPACE = "(" + REGEX_WIKINAME + "\\:)?" + REGEX_WORD;
  public static final String REGEX_DOC = REGEX_SPACE + "\\." + REGEX_WORD;
  public static final String REGEX_ATT = REGEX_DOC + "\\@" + ".+";

  static {
    Map<Class<? extends EntityReference>, EntityType> map = new HashMap<>();
    map.put(WikiReference.class, EntityType.WIKI);
    map.put(SpaceReference.class, EntityType.SPACE);
    map.put(DocumentReference.class, EntityType.DOCUMENT);
    map.put(AttachmentReference.class, EntityType.ATTACHMENT);
    map.put(ObjectReference.class, EntityType.OBJECT);
    map.put(ObjectPropertyReference.class, EntityType.OBJECT_PROPERTY);
    ENTITY_TYPE_MAP = ImmutableBiMap.copyOf(map);
    Map<Class<? extends EntityReference>, String> regexMap = new LinkedHashMap<>();
    regexMap.put(WikiReference.class, REGEX_WIKINAME);
    regexMap.put(SpaceReference.class, REGEX_SPACE);
    regexMap.put(DocumentReference.class, REGEX_DOC);
    regexMap.put(AttachmentReference.class, REGEX_ATT);
    REGEX_MAP = Collections.unmodifiableMap(regexMap);
  }

  public static EntityType getEntityTypeForClass(Class<? extends EntityReference> token) {
    EntityType type = ENTITY_TYPE_MAP.get(checkNotNull(token));
    if (type != null) {
      return type;
    } else {
      throw new IllegalArgumentException("No entity type for class: " + token);
    }
  }

  public static Class<? extends EntityReference> getClassForEntityType(EntityType type) {
    Class<? extends EntityReference> token = ENTITY_TYPE_MAP.inverse().get(checkNotNull(type));
    if (token != null) {
      return token;
    } else {
      throw new IllegalArgumentException("No class for entity type: " + type);
    }
  }

  private static EntityType getRootEntityType() {
    return EntityType.values()[0]; // EntityType.WIKI
  }

  public static Class<? extends EntityReference> getRootClass() {
    return ENTITY_TYPE_MAP.inverse().get(getRootEntityType());
  }

  public static Class<? extends EntityReference> identifyClassFromName(String name) {
    if (!checkNotNull(name).isEmpty()) {
      Set<Class<? extends EntityReference>> tokens = new LinkedHashSet<>(); // keeps insertion order
      tokens.add(getRootClass());
      tokens.addAll(REGEX_MAP.keySet());
      for (Class<? extends EntityReference> token : tokens) {
        if (name.matches(REGEX_MAP.get(token))) {
          return token;
        }
      }
    }
    throw new IllegalArgumentException("No valid reference class found for '" + name + "'");
  }

  public static boolean isAbsoluteRef(EntityReference ref) {
    checkNotNull(ref);
    return ref.extractReference(getRootEntityType()) != null;
  }

  public static EntityReference cloneRef(EntityReference ref) {
    Class<? extends EntityReference> token = EntityReference.class;
    if (isAbsoluteRef(ref)) {
      token = ENTITY_TYPE_MAP.inverse().get(ref.getType());
    }
    return cloneRef(ref, token);
  }

  public static <T extends EntityReference> T cloneRef(EntityReference ref, Class<T> token) {
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

  public static <T extends EntityReference> T extractRef(EntityReference fromRef, T defaultRef,
      Class<T> token) {
    return MoreObjects.firstNonNull(extractRef(fromRef, token), checkNotNull(defaultRef));
  }

  public static <T extends EntityReference> T extractRef(EntityReference fromRef, Class<T> token) {
    EntityReference extractedRef = null;
    if (fromRef != null) {
      extractedRef = fromRef.extractReference(getEntityTypeForClass(token));
    }
    if (extractedRef != null) {
      return cloneRef(extractedRef, token);
    } else {
      return null;
    }
  }

  public static <T extends EntityReference> T adjustRef(T ref, Class<T> token,
      EntityReference toRef) {
    checkNotNull(toRef);
    EntityReference adjustedRef = cloneRef(ref); // avoid modifying argument
    EntityReference current = adjustedRef;
    while (current != null) {
      if (current.getType() != toRef.getType()) {
        current = current.getParent();
      } else {
        if (current.getChild() != null) {
          current.getChild().setParent(toRef);
        } else {
          adjustedRef = toRef;
        }
        break;
      }
    }
    return cloneRef(adjustedRef, token); // effective immutability
  }

}
