package com.celements.rights.access;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.EntityReference;

@ComponentRole
interface IEntityReferenceRandomCompleterRole {

  public EntityReference randomCompleteSpaceRef(EntityReference entityRef);

}
