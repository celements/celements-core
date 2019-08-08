package com.celements.observation.save.object;

import com.celements.common.observation.converter.Remote;
import com.celements.model.classes.ClassIdentity;
import com.celements.observation.save.SaveEvent;
import com.celements.observation.save.SaveEventFilter;
import com.celements.observation.save.SaveEventOperation;

@Remote
public class ObjectEvent extends SaveEvent<ClassIdentity> {

  private static final long serialVersionUID = -6666255714705030039L;

  public ObjectEvent() {
    super(new SaveEventFilter<>(null, null));
  }

  public ObjectEvent(SaveEventOperation operation) {
    super(new SaveEventFilter<>(operation, null));
  }

  public ObjectEvent(ClassIdentity classId) {
    super(new SaveEventFilter<>(null, classId));
  }

  public ObjectEvent(SaveEventOperation operation, ClassIdentity classId) {
    super(new SaveEventFilter<>(operation, classId));
  }

}
