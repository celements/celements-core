package com.celements.model.access;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.ModelMock.DocRecord;
import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentSaveException;
import com.xpn.xwiki.web.Utils;

public class ModelAccessMockTest extends AbstractComponentTest {

  private ModelMock modelAccess;

  private DocumentReference docRef = new DocumentReference("wiki", "space", "doc");

  @Before
  public void prepareTest() throws Exception {
    assertFalse(Utils.getComponent(ModelAccessStrategy.class) instanceof ModelMock);
    modelAccess = ModelMock.init();
    assertTrue(Utils.getComponent(ModelAccessStrategy.class) instanceof ModelMock);
  }

  @Test
  public void test_get() throws Exception {
    DocRecord doc = modelAccess.registerDoc(docRef);
    assertSame(doc.doc(), modelAccess.getDocRecord(docRef).doc());
  }

  @Test
  public void test_get_notMocked() throws Exception {
    try {
      modelAccess.getDocRecord(docRef);
      fail("expecting ISE");
    } catch (IllegalStateException exc) {
      // expected
    }
  }

  @Test
  public void test_save() throws Exception {
    DocRecord doc = modelAccess.registerDoc(docRef);

    assertFalse(doc.wasSaved());
    assertEquals(0, doc.getSavedCount());

    modelAccess.saveDocument(doc.doc(), "", false);
    assertTrue(doc.wasSaved());
    assertEquals(1, doc.getSavedCount());

    modelAccess.saveDocument(doc.doc(), "", false);
    assertTrue(doc.wasSaved());
    assertEquals(2, doc.getSavedCount());

    modelAccess.getDocRecord(docRef).setThrowSaveException(true);
    try {
      modelAccess.saveDocument(doc.doc(), "", false);
      fail("expecting DocumentSaveException");
    } catch (DocumentSaveException exc) {
      // expected
    }
  }

  @Test
  public void test_delete() throws Exception {
    DocRecord doc = modelAccess.registerDoc(docRef);

    assertFalse(doc.wasDeleted());
    assertEquals(0, doc.getDeletedCount());

    modelAccess.deleteDocument(doc.doc(), true);
    assertTrue(doc.wasDeleted());
    assertEquals(1, doc.getDeletedCount());

    modelAccess.deleteDocument(doc.doc(), false);
    assertTrue(doc.wasDeleted());
    assertEquals(2, doc.getDeletedCount());

    modelAccess.getDocRecord(docRef).setThrowDeleteException(true);
    try {
      modelAccess.deleteDocument(doc.doc(), false);
      fail("expecting DocumentDeleteException");
    } catch (DocumentDeleteException exc) {
      // expected
    }
  }

}
