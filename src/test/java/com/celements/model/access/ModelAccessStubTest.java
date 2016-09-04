package com.celements.model.access;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.ModelAccessStub.InjectedDoc;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class ModelAccessStubTest extends AbstractComponentTest {

  private ModelAccessStub modelAccess;

  private DocumentReference docRef = new DocumentReference("wiki", "space", "doc");

  @Before
  public void prepareTest() throws Exception {
    assertFalse(Utils.getComponent(IModelAccessFacade.class) instanceof ModelAccessStub);
    modelAccess = ModelAccessStub.init();
    assertTrue(Utils.getComponent(IModelAccessFacade.class) instanceof ModelAccessStub);
  }

  @Test
  public void test_get() throws Exception {
    XWikiDocument doc = modelAccess.createDocument(docRef);
    InjectedDoc injDoc = modelAccess.getInjectedDoc(docRef);
    assertSame(doc, injDoc.doc());
  }

  @Test
  public void test_get_notInjected() throws Exception {
    try {
      modelAccess.getInjectedDoc(docRef);
      fail("expecting ISE");
    } catch (IllegalStateException exc) {
      // expected
    }
  }

  @Test
  public void test_save() throws Exception {
    XWikiDocument doc = modelAccess.createDocument(docRef);
    InjectedDoc injDoc = modelAccess.getInjectedDoc(docRef);

    assertFalse(injDoc.wasSaved());
    assertEquals(0, injDoc.getSavedCount());

    modelAccess.saveDocument(doc);
    assertTrue(injDoc.wasSaved());
    assertEquals(1, injDoc.getSavedCount());

    modelAccess.saveDocument(doc);
    assertTrue(injDoc.wasSaved());
    assertEquals(2, injDoc.getSavedCount());
  }

  @Test
  public void test_delete() throws Exception {
    XWikiDocument doc = modelAccess.createDocument(docRef);
    InjectedDoc injDoc = modelAccess.getInjectedDoc(docRef);

    assertFalse(injDoc.wasDeleted());
    assertEquals(0, injDoc.getDeletedCount());

    modelAccess.deleteDocument(doc, true);
    assertTrue(injDoc.wasDeleted());
    assertEquals(1, injDoc.getDeletedCount());
    modelAccess.deleteDocument(doc, false);
    assertEquals(2, injDoc.getDeletedCount());
  }

}
