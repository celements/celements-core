package com.celements.rights.access.internal;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.EntityReference;

@ComponentRole
public interface IEntityReferenceRandomCompleterRole {

  public EntityReference randomCompleteSpaceRef(EntityReference entityRef);

}
