package com.celements.filebase.references;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;

public class FileReferenceTest extends AbstractComponentTest {

  @Before
  public void setUp_FileReferenceTest() throws Exception {}

  @Test
  public void test_isAttachmentLink_null() {
    assertThrows(IllegalArgumentException.class, () -> FileReference.of(null));
  }

  @Test
  public void test_isAttachmentLink_empty() {
    assertThrows(IllegalArgumentException.class, () -> FileReference.of(""));
  }

  @Test
  public void test_isAttachmentLink_url() {
    assertFalse(
        FileReference.of("/download/Space/Page/attachment.jpg").isAttachmentReference());
  }

  @Test
  public void test_isAttachmentLink_is() {
    assertTrue(FileReference.of("Space.Page;attachment.jpg").isAttachmentReference());
  }

  @Test
  public void test_isAttachmentLink_isSpecialChars() {
    assertTrue(FileReference.of("Teilnehmer.f8Nx9vyPOX8O2;Hans-002-Bearbeitet-2.jpg")
        .isAttachmentReference());
  }

  @Test
  public void test_isAttachmentLink_isWithDb() {
    assertTrue(FileReference.of("db:Space.Page;attachment.jpg").isAttachmentReference());
  }

  @Test
  public void test_isOnDiskLink_true() {
    assertTrue(FileReference.of(":bla.js").isOnDiskReference());
    assertTrue(FileReference.of(" :celJS/bla.js").isOnDiskReference());
  }

  @Test
  public void test_isOnDiskLink_false() {
    assertFalse(FileReference.of("bla.js").isOnDiskReference());
    assertFalse(FileReference.of("x:celJS/bla.js").isOnDiskReference());
    assertFalse(FileReference.of("x:A.B;bla.js").isOnDiskReference());
  }

}
