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
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

@Component
public class DefaultModelUtils implements IModelUtils {

  private final BiMap<Class<? extends EntityReference>, EntityType> ENTITY_TYPE_MAP;
  private final Map<Class<? extends EntityReference>, String> REGEX_MAP;

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
    String regexWord = "[a-zA-Z0-9]+";
    regexMap.put(WikiReference.class, regexWord);
    String regexSpace = "(" + regexWord + "\\:)?" + regexWord;
    regexMap.put(SpaceReference.class, regexSpace);
    String regexDoc = regexSpace + "\\." + regexWord;
    regexMap.put(DocumentReference.class, regexDoc);
    String regexAtt = regexDoc + "\\@" + regexWord;
    regexMap.put(AttachmentReference.class, regexAtt);
    REGEX_MAP = Collections.unmodifiableMap(regexMap);
  }

  @Requirement
  private IModelContext context;

  @Requirement
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
    EntityType type = getEntityTypeForToken(checkNotNull(token));
    baseRef = MoreObjects.firstNonNull(baseRef, context.getCurrentWiki());
    validateName(name, token);
    EntityReference ref;
    if (type.ordinal() > 0) {
      ref = resolver.resolve(name, type, baseRef);
    } else {
      // resolver cannot handle root reference
      ref = resolveRootRef(name, baseRef);
    }
    return cloneRef(ref, token); // ensure memory visibility
  }

  private void validateName(String name, Class<? extends EntityReference> token) {
    if (checkNotNull(name).isEmpty()) {
      throw new IllegalArgumentException("name may not be empty");
    } else if (!name.matches(REGEX_MAP.get(token))) {
      throw new IllegalArgumentException("name '" + name + "' is not valid for class " + token);
    }
  }

  private EntityReference resolveRootRef(String name, EntityReference baseRef) {
    EntityReference ref;
    if (!Strings.isNullOrEmpty(name)) {
      ref = new WikiReference(name);
    } else {
      ref = extractRef(baseRef, context.getCurrentWiki(), WikiReference.class);
    }
    return ref;
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
    EntityReference ref = null;
    if (fromRef != null) {
      ref = fromRef.extractReference(getEntityTypeForToken(token));
    }
    if (ref != null) {
      return cloneRef(ref, token);
    } else {
      return null;
    }
  }

  @Override
  public <T extends EntityReference> T adjustRef(T ref, Class<T> token, EntityReference toRef) {
    toRef = MoreObjects.firstNonNull(toRef, context.getCurrentWiki());
    EntityReference ret;
    ret = cloneRef(ref); // to not modify argument
    EntityReference current = ret;
    while (current != null) {
      if (current.getType() != toRef.getType()) {
        current = current.getParent();
      } else {
        if (current.getChild() != null) {
          current.getChild().setParent(toRef);
        } else {
          ret = toRef;
        }
        break;
      }
    }
    return cloneRef(ret, token); // ensure memory visibility
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
