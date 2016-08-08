package com.celements.model.util;

import static com.google.common.base.Preconditions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.ObjectPropertyReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.context.IModelContext;
import com.google.common.base.MoreObjects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

@Component
public class DefaultModelUtils implements IModelUtils {

  private final BiMap<Class<? extends EntityReference>, EntityType> ENTITY_TYPE_MAP;
  private final Map<Class<? extends EntityReference>, String> REGEX_MAP;

  public static final String REGEX_WORD = "[a-zA-Z0-9]+";
  public static final String REGEX_SPACE = "(" + REGEX_WORD + "\\:)?" + REGEX_WORD;
  public static final String REGEX_DOC = REGEX_SPACE + "\\." + REGEX_WORD;
  public static final String REGEX_ATT = REGEX_DOC + "\\@" + ".+";

  {
    Map<Class<? extends EntityReference>, EntityType> map = new HashMap<>();
    map.put(WikiReference.class, EntityType.WIKI);
    map.put(SpaceReference.class, EntityType.SPACE);
    map.put(DocumentReference.class, EntityType.DOCUMENT);
    map.put(AttachmentReference.class, EntityType.ATTACHMENT);
    map.put(ObjectReference.class, EntityType.OBJECT);
    map.put(ObjectPropertyReference.class, EntityType.OBJECT_PROPERTY);
    ENTITY_TYPE_MAP = ImmutableBiMap.copyOf(map);
    Map<Class<? extends EntityReference>, String> regexMap = new LinkedHashMap<>();
    regexMap.put(WikiReference.class, REGEX_WORD);
    regexMap.put(SpaceReference.class, REGEX_SPACE);
    regexMap.put(DocumentReference.class, REGEX_DOC);
    regexMap.put(AttachmentReference.class, REGEX_ATT);
    REGEX_MAP = Collections.unmodifiableMap(regexMap);
  }

  @Requirement
  private IModelContext context;

  @Requirement("explicit")
  private EntityReferenceResolver<String> resolver;

  @Requirement
  private EntityReferenceSerializer<String> serializer;

  @Requirement("local")
  private EntityReferenceSerializer<String> serializerLocal;

  @Override
  public BiMap<Class<? extends EntityReference>, EntityType> getEntityTypeMap() {
    return ENTITY_TYPE_MAP;
  }

  @Override
  public boolean isAbsoluteRef(EntityReference ref) {
    checkNotNull(ref);
    return ref.extractReference(EntityType.values()[0]) != null;
  }

  @Override
  public EntityReference cloneRef(EntityReference ref) {
    Class<? extends EntityReference> token = EntityReference.class;
    if (isAbsoluteRef(ref)) {
      token = getEntityTypeMap().inverse().get(ref.getType());
    }
    return cloneRef(ref, token);
  }

  @Override
  public <T extends EntityReference> T cloneRef(EntityReference ref, Class<T> token) {
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

  @Override
  public Class<? extends EntityReference> resolveRefClass(String name) {
    if (!checkNotNull(name).isEmpty()) {
      Set<Class<? extends EntityReference>> tokens = new LinkedHashSet<>(); // keeps insertion order
      tokens.add(getRootRefClass());
      tokens.addAll(REGEX_MAP.keySet());
      for (Class<? extends EntityReference> token : tokens) {
        if (name.matches(REGEX_MAP.get(token))) {
          return token;
        }
      }
    }
    throw new IllegalArgumentException("No valid reference class found for '" + name + "'");
  }

  @Override
  public EntityReference resolveRef(String name) {
    return resolveRef(name, (EntityReference) null);
  }

  @Override
  public EntityReference resolveRef(String name, EntityReference baseRef) {
    return resolveRef(name, resolveRefClass(name), baseRef);
  }

  @Override
  public <T extends EntityReference> T resolveRef(String name, Class<T> token) {
    return resolveRef(name, token, null);
  }

  @Override
  public <T extends EntityReference> T resolveRef(String name, Class<T> token,
      EntityReference baseRef) {
    if (checkNotNull(name).isEmpty()) {
      throw new IllegalArgumentException("name may not be empty");
    }
    EntityType type = getEntityTypeForToken(token);
    baseRef = MoreObjects.firstNonNull(baseRef, context.getCurrentWiki());
    EntityReference resolvedRef;
    if (type.ordinal() == 0) {
      // resolver cannot handle root reference
      resolvedRef = new WikiReference(name);
    } else {
      resolvedRef = resolver.resolve(name, type, baseRef);
    }
    return cloneRef(resolvedRef, token); // effective immutability
  }

  Class<? extends EntityReference> getRootRefClass() {
    return getEntityTypeMap().inverse().get(EntityType.values()[0]);
  }

  @Override
  public String serializeRef(EntityReference ref) {
    checkNotNull(ref);
    return serializer.serialize(ref);
  }

  @Override
  public String serializeRefLocal(EntityReference ref) {
    checkNotNull(ref);
    return serializerLocal.serialize(ref);
  }

  @Override
  public <T extends EntityReference> T extractRef(EntityReference fromRef, T defaultRef,
      Class<T> token) {
    return MoreObjects.firstNonNull(extractRef(fromRef, token), checkNotNull(defaultRef));
  }

  @Override
  public <T extends EntityReference> T extractRef(EntityReference fromRef, Class<T> token) {
    EntityReference extractedRef = null;
    if (fromRef != null) {
      extractedRef = fromRef.extractReference(getEntityTypeForToken(token));
    }
    if (extractedRef != null) {
      return cloneRef(extractedRef, token);
    } else {
      return null;
    }
  }

  @Override
  public <T extends EntityReference> T adjustRef(T ref, Class<T> token, EntityReference toRef) {
    toRef = MoreObjects.firstNonNull(toRef, context.getCurrentWiki());
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

  private EntityType getEntityTypeForToken(Class<? extends EntityReference> token) {
    EntityType type = getEntityTypeMap().get(checkNotNull(token));
    if (type != null) {
      return type;
    } else {
      throw new IllegalArgumentException("No entity type for class: " + token);
    }
  }

}
