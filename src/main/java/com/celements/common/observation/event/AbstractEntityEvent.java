package com.celements.common.observation.event;

import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.event.AbstractFilterableEvent;

import com.xpn.xwiki.web.Utils;

public abstract class AbstractEntityEvent extends AbstractFilterableEvent {

  private static final long serialVersionUID = 1L;

  private EntityReference reference;

  public AbstractEntityEvent() {
    super();
  }

  public AbstractEntityEvent(EntityReference reference) {
    super(getSerializer().serialize(reference));
    this.reference = reference;
  }

  @SuppressWarnings("unchecked")
  public static EntityReferenceSerializer<String> getSerializer() {
    return Utils.getComponent(EntityReferenceSerializer.class);
  }

  public EntityReference getReference() {
    return reference.clone();
  }

}
