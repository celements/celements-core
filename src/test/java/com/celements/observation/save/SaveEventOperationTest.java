package com.celements.observation.save;

import static com.celements.observation.save.SaveEventOperation.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;

public class SaveEventOperationTest {

  @Test
  public void test_isBeforeSave() {
    assertTrue(UPDATING.isBeforeSave());
    assertTrue(CREATING.isBeforeSave());
    assertTrue(DELETING.isBeforeSave());
    assertFalse(UPDATED.isBeforeSave());
    assertFalse(CREATED.isBeforeSave());
    assertFalse(DELETED.isBeforeSave());
  }

  @Test
  public void test_isUpdate() {
    assertTrue(UPDATING.isUpdate());
    assertFalse(CREATING.isUpdate());
    assertFalse(DELETING.isUpdate());
    assertTrue(UPDATED.isUpdate());
    assertFalse(CREATED.isUpdate());
    assertFalse(DELETED.isUpdate());
  }

  @Test
  public void test_isCreate() {
    assertFalse(UPDATING.isCreate());
    assertTrue(CREATING.isCreate());
    assertFalse(DELETING.isCreate());
    assertFalse(UPDATED.isCreate());
    assertTrue(CREATED.isCreate());
    assertFalse(DELETED.isCreate());
  }

  @Test
  public void test_isDelete() {
    assertFalse(UPDATING.isDelete());
    assertFalse(CREATING.isDelete());
    assertTrue(DELETING.isDelete());
    assertFalse(UPDATED.isDelete());
    assertFalse(CREATED.isDelete());
    assertTrue(DELETED.isDelete());
  }

  @Test
  public void test_from_origExists_newExists() {
    boolean beforeSave = true;
    assertFalse(from(false, false, beforeSave).isPresent());
    assertEquals(UPDATING, from(true, true, beforeSave).get());
    assertEquals(CREATING, from(false, true, beforeSave).get());
    assertEquals(DELETING, from(true, false, beforeSave).get());
    beforeSave = false;
    assertFalse(from(false, false, beforeSave).isPresent());
    assertEquals(UPDATED, from(true, true, beforeSave).get());
    assertEquals(CREATED, from(false, true, beforeSave).get());
    assertEquals(DELETED, from(true, false, beforeSave).get());
  }

  @Test
  public void test_from_DocumentEvent() {
    assertEquals(UPDATING, from(new DocumentUpdatingEvent()));
    assertEquals(CREATING, from(new DocumentCreatingEvent()));
    assertEquals(DELETING, from(new DocumentDeletingEvent()));
    assertEquals(UPDATED, from(new DocumentUpdatedEvent()));
    assertEquals(CREATED, from(new DocumentCreatedEvent()));
    assertEquals(DELETED, from(new DocumentDeletedEvent()));
  }

}
