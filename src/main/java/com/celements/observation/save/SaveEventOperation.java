package com.celements.observation.save;

import static com.google.common.base.Preconditions.*;

import java.util.Arrays;
import java.util.Optional;

import org.xwiki.observation.event.Event;

public enum SaveEventOperation {

  UPDATING,
  CREATING,
  DELETING,
  UPDATED,
  CREATED,
  DELETED;

  public boolean isBeforeSave() {
    return ordinal() < 3;
  }

  public boolean isUpdate() {
    return (ordinal() % 3) == 0;
  }

  public boolean isCreate() {
    return (ordinal() % 3) == 1;
  }

  public boolean isDelete() {
    return (ordinal() % 3) == 2;
  }

  /**
   * @return the appropriate operation for the given information:
   *         A) if an original and new entity (doc, object, ...) exists
   *         B) if the operation is before or after save.
   */
  public static Optional<SaveEventOperation> from(boolean origExists, boolean newExists, boolean beforeSave) {
    int val = ((newExists ? 0 : 1) << 1) | (origExists ? 0 : 1);
    if (val < 3) {
      return Optional.of(values()[val + (beforeSave ? 0 : 3)]);
    } else {
      return Optional.empty();
    }
  }

  public static SaveEventOperation from(Event event) {
    String eventName = checkNotNull(event).getClass().getSimpleName().toUpperCase();
    return Arrays.stream(values()).filter(ops -> eventName.contains(ops.name()))
        .findAny().orElseThrow(() -> new IllegalArgumentException("illegal save event: " + event));
  }
}
