package com.celements.observation.save;

import org.xwiki.bridge.event.AbstractDocumentEvent;

public enum SaveEventOperation {

  CREATING,
  UPDATING,
  DELETING,
  CREATED,
  UPDATED,
  DELETED;

  public boolean isBeforeSave() {
    return ordinal() < 3;
  }

  public boolean isAfterSave() {
    return ordinal() >= 3;
  }

  public boolean isCreate() {
    return (ordinal() % 3) == 0;
  }

  public boolean isUpdate() {
    return (ordinal() % 3) == 1;
  }

  public boolean isDelete() {
    return (ordinal() % 3) == 2;
  }

  public static SaveEventOperation fromDocumentEvent(AbstractDocumentEvent event) {
    String operationName = event.getClass().getSimpleName()
        .replace("Document", "")
        .replace("Event", "");
    return valueOf(operationName.toUpperCase());
  }
}
