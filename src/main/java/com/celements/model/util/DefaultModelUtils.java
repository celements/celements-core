package com.celements.model.util;

import static com.google.common.base.Preconditions.*;

import java.util.HashMap;
import java.util.Map;

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

  {
    Map<Class<? extends EntityReference>, EntityType> map = new HashMap<>();
    map.put(WikiReference.class, EntityType.WIKI);
    map.put(SpaceReference.class, EntityType.SPACE);
    map.put(DocumentReference.class, EntityType.DOCUMENT);
    map.put(AttachmentReference.class, EntityType.ATTACHMENT);
    map.put(ObjectPropertyReference.class, EntityType.OBJECT_PROPERTY);
    map.put(ObjectReference.class, EntityType.OBJECT);
    ENTITY_TYPE_MAP = ImmutableBiMap.copyOf(map);
  }

  @Requirement
  private IModelContext context;

  @Requirement("relative") // TODO???
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
  public EntityReference cloneReference(EntityReference ref) {
    return cloneReference(ref, getEntityTypeMap().inverse().get(ref.getType()));
  }

  @Override
  public <T extends EntityReference> T cloneReference(EntityReference ref, Class<T> token) {
    try {
      ref = ref.clone();
      T ret;
      if (token != EntityReference.class) {
        ret = token.getConstructor(EntityReference.class).newInstance(ref);
      } else {
        ret = token.cast(ref);
      }
      return ret;
    } catch (ReflectiveOperationException | SecurityException exc) {
      throw new IllegalArgumentException("Unsupported entity class: " + token, exc);
    }
  }

  @Override
  public <T extends EntityReference> T resolveRef(String name, Class<T> token) {
    return resolveRef(name, token, null);
  }

  @Override
  public <T extends EntityReference> T resolveRef(String name, Class<T> token,
      EntityReference baseRef) {
    baseRef = MoreObjects.firstNonNull(baseRef, context.getCurrentWiki());
    EntityReference ref;
    EntityType type = getEntityTypeMap().get(token);
    if (type == null) {
      throw new IllegalArgumentException("Unsupported entity class: " + token);
    } else if (type == EntityType.WIKI) {
      // resolver cannot handle WikiReference
      if (!Strings.isNullOrEmpty(name)) {
        ref = new WikiReference(name);
      } else {
        ref = extractRef(baseRef, context.getCurrentWiki(), WikiReference.class);
      }
    } else {
      ref = resolver.resolve(name, type, baseRef);
    }
    return cloneReference(ref, token); // ensure memory visibility
  }

  @Override
  public String serializeRef(EntityReference ref) {
    return serializer.serialize(ref);
  }

  @Override
  public String serializeRefLocal(EntityReference ref) {
    return serializerLocal.serialize(ref);
  }

  @Override
  public <T extends EntityReference> T extractRef(EntityReference fromRef, T defaultRef,
      Class<T> token) {
    EntityReference extractedRef = fromRef.extractReference(getEntityTypeMap().get(token));
    if (extractedRef != null) {
      return cloneReference(extractedRef, token);
    } else {
      return checkNotNull(defaultRef);
    }
  }

  @Override
  public <T extends EntityReference> T adjustRef(T ref, Class<T> token, EntityReference toRef) {
    toRef = MoreObjects.firstNonNull(toRef, context.getCurrentWiki());
    EntityReference ret;
    ret = cloneReference(ref); // to not modify argument
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
    return cloneReference(ret, token); // ensure memory visibility
  }

}
