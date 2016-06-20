package com.celements.model.util;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.ObjectPropertyReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class ModelUtils {

  public static final BiMap<Class<? extends EntityReference>, EntityType> ENTITY_TYPE_MAP;

  static {
    Map<Class<? extends EntityReference>, EntityType> map = new HashMap<>();
    map.put(WikiReference.class, EntityType.WIKI);
    map.put(SpaceReference.class, EntityType.SPACE);
    map.put(DocumentReference.class, EntityType.DOCUMENT);
    map.put(AttachmentReference.class, EntityType.ATTACHMENT);
    map.put(ObjectPropertyReference.class, EntityType.OBJECT_PROPERTY);
    map.put(ObjectReference.class, EntityType.OBJECT);
    ENTITY_TYPE_MAP = ImmutableBiMap.copyOf(map);
  }

  public static EntityReference cloneReference(@NotNull EntityReference reference) {
    return cloneReference(reference, ENTITY_TYPE_MAP.inverse().get(reference.getType()));
  }

  /**
   * @param reference
   *          the reference to be copied
   * @param token
   *          type of the reference
   * @return a copied instance of the reference
   */
  @NotNull
  public static <T extends EntityReference> T cloneReference(@NotNull EntityReference reference,
      @NotNull Class<T> token) {
    try {
      reference = reference.clone();
      T ret;
      if (token != EntityReference.class) {
        ret = token.getConstructor(EntityReference.class).newInstance(reference.clone());
      } else {
        // T == EntityReference
        @SuppressWarnings("unchecked")
        T castedReference = (T) reference;
        ret = castedReference;
      }
      return ret;
    } catch (ReflectiveOperationException | SecurityException exc) {
      throw new IllegalArgumentException("Unsupported entity class: " + token, exc);
    }
  }

}
