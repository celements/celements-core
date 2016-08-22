package com.celements.model.util;

import static com.google.common.base.Preconditions.*;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.context.ModelContext;
import com.google.common.base.MoreObjects;

@Component
public class DefaultModelUtils implements ModelUtils {

  @Requirement
  private ModelContext context;

  @Requirement("explicit")
  private EntityReferenceResolver<String> resolver;

  @Requirement
  private EntityReferenceSerializer<String> serializer;

  @Requirement("local")
  private EntityReferenceSerializer<String> serializerLocal;

  @Override
  public boolean isAbsoluteRef(EntityReference ref) {
    return References.isAbsoluteRef(ref);
  }

  @Override
  public EntityReference cloneRef(EntityReference ref) {
    return References.cloneRef(ref);
  }

  @Override
  public <T extends EntityReference> T cloneRef(EntityReference ref, Class<T> token) {
    return References.cloneRef(ref, token);
  }

  @Override
  public <T extends EntityReference> T extractRef(EntityReference fromRef, T defaultRef,
      Class<T> token) {
    return References.extractRef(fromRef, defaultRef, token);
  }

  @Override
  public <T extends EntityReference> T extractRef(EntityReference fromRef, Class<T> token) {
    return References.extractRef(fromRef, token);
  }

  @Override
  public <T extends EntityReference> T adjustRef(T ref, Class<T> token, EntityReference toRef) {
    return References.adjustRef(ref, token, MoreObjects.firstNonNull(toRef, context.getWiki()));
  }

  @Override
  public Class<? extends EntityReference> identifyClassFromName(String name) {
    return References.identifyClassFromName(name);
  }

  @Override
  public EntityReference resolveRef(String name) {
    return resolveRef(name, (EntityReference) null);
  }

  @Override
  public EntityReference resolveRef(String name, EntityReference baseRef) {
    return resolveRef(name, identifyClassFromName(name), baseRef);
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
    EntityType type = References.getEntityTypeForClass(token);
    baseRef = MoreObjects.firstNonNull(baseRef, context.getWiki());
    EntityReference resolvedRef;
    if (type.ordinal() == 0) {
      // resolver cannot handle root reference
      resolvedRef = new WikiReference(name);
    } else {
      resolvedRef = resolver.resolve(name, type, baseRef);
    }
    return cloneRef(resolvedRef, token); // effective immutability
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

}
