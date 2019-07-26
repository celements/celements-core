package com.celements.observation.event;

public enum EventOperation {

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
}
