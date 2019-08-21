package com.celements.observation.save.object;

import org.xwiki.model.reference.ClassReference;

import com.celements.common.observation.converter.Remote;
import com.celements.observation.save.SaveEvent;
import com.celements.observation.save.SaveEventFilter;
import com.celements.observation.save.SaveEventOperation;

@Remote
public class ObjectEvent extends SaveEvent<ClassReference> {

  private static final long serialVersionUID = -6666255714705030039L;

  public ObjectEvent() {
    super(new SaveEventFilter<>(null, null));
  }

  public ObjectEvent(SaveEventOperation operation) {
    super(new SaveEventFilter<>(operation, null));
  }

  public ObjectEvent(ClassReference classRef) {
    super(new SaveEventFilter<>(null, classRef));
  }

  public ObjectEvent(SaveEventOperation operation, ClassReference classRef) {
    super(new SaveEventFilter<>(operation, classRef));
  }

}
