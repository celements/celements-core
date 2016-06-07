package com.celements.rights.access.internal;

import org.apache.commons.lang.RandomStringUtils;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

@Component
public class EntityReferenceRandomCompleter implements IEntityReferenceRandomCompleterRole {

  @Requirement
  private DocumentAccessBridge documentAccessBridge;

  public EntityReference randomCompleteSpaceRef(EntityReference entityRef) {
    if ((entityRef != null) && (entityRef.getType() == EntityType.SPACE)) {
      SpaceReference spaceRef = new SpaceReference(entityRef);
      DocumentReference randomDocRef;
      do {
        String randomDocName = RandomStringUtils.randomAlphanumeric(50);
        randomDocRef = new DocumentReference(randomDocName, spaceRef);
      } while (documentAccessBridge.exists(randomDocRef));
      entityRef = randomDocRef;
    }
    return entityRef;
  }

}
