package com.celements.model.access;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class DefaultModelAccessFacadeTest extends AbstractBridgedComponentTestCase {

  private DefaultModelAccessFacade modelAccess;
  private XWikiDocument doc;
  private DocumentReference classRef;
  private DocumentReference classRef2;

  @Before
  public void setUp_DefaultModelAccessFacadeTest() {
    modelAccess = (DefaultModelAccessFacade) Utils.getComponent(IModelAccessFacade.class);
    doc = new XWikiDocument(new DocumentReference("db", "space", "doc"));
    classRef = new DocumentReference("db", "class", "any");
    classRef2 = new DocumentReference("db", "class", "other");
  }

  @Test
  public void test_getDocument() throws Exception {
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    XWikiDocument ret = modelAccess.getDocument(doc.getDocumentReference());
    verifyDefault();
    assertSame(doc, ret);
  }

  @Test
  public void test_getDocument_XWE() throws Exception {
    Throwable cause = new XWikiException();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andThrow(cause).once();
    replayDefault();
    try {
      modelAccess.getDocument(doc.getDocumentReference());
      fail("expecting XWikiException");
    } catch (XWikiException xwe) {
      assertSame(cause, xwe);
    }
    verifyDefault();
  }

  @Test
  public void test_getDocument_null() throws Exception {
    try {
      modelAccess.getDocument(null);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_saveDocument() throws Exception {
    getWikiMock().saveDocument(same(doc), same(getContext()));
    expectLastCall().once();
    replayDefault();
    modelAccess.saveDocument(doc);
    verifyDefault();
  }

  @Test
  public void test_saveDocument_XWE() throws Exception {
    Throwable cause = new XWikiException();
    getWikiMock().saveDocument(same(doc), same(getContext()));
    expectLastCall().andThrow(cause).once();
    replayDefault();
    try {
      modelAccess.saveDocument(doc);
      fail("expecting XWikiException");
    } catch (XWikiException xwe) {
      assertSame(cause, xwe);
    }
    verifyDefault();
  }

  @Test
  public void test_saveDocument_null() throws Exception {
    try {
      modelAccess.saveDocument(null);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_saveDocument_comment() throws Exception {
    String comment = "myComment";
    getWikiMock().saveDocument(same(doc), eq(comment), same(getContext()));
    expectLastCall().once();
    replayDefault();
    modelAccess.saveDocument(doc, comment);
    verifyDefault();
  }

  @Test
  public void test_saveDocument_comment_isMinorEdit() throws Exception {
    String comment = "myComment";
    boolean isMinorEdit = true;
    getWikiMock().saveDocument(same(doc), eq(comment), eq(isMinorEdit), 
        same(getContext()));
    expectLastCall().once();
    replayDefault();
    modelAccess.saveDocument(doc, comment, isMinorEdit);
    verifyDefault();
  }

  @Test
  public void test_getXObjects_nullDoc() {
    try {
      modelAccess.getXObjects((XWikiDocument) null, null);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_getXObjects_nullClassRef() {
    List<BaseObject> ret = modelAccess.getXObjects(doc, null);
    assertEquals(ret.size(), 0);
  }

  @Test
  public void test_getXObjects_emptyDoc() {
    List<BaseObject> ret = modelAccess.getXObjects(doc, classRef);
    assertEquals(ret.size(), 0);
  }

  @Test
  public void test_getXObjects_withObj() {
    BaseObject obj = addObj(classRef, null, null);
    List<BaseObject> ret = modelAccess.getXObjects(doc, classRef);
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(0));
  }

  @Test
  public void test_getXObjects_mutlipleObj() {
    BaseObject obj1 = addObj(classRef, null, null);
    addObj(classRef2, null, null);
    BaseObject obj2 = addObj(classRef, null, null);
    List<BaseObject> ret = modelAccess.getXObjects(doc, classRef);
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj2, ret.get(1));
  }

  @Test
  public void test_getXObjects_key() {
    String key = "field";
    String val = "val";
    addObj(classRef, null, null);
    addObj(classRef2, key, val);
    BaseObject obj1 = addObj(classRef, key, val);
    addObj(classRef, key, null);
    BaseObject obj2 = addObj(classRef, key, val);
    assertEquals(0, modelAccess.getXObjects(doc, classRef, key, null).size());
    assertEquals(0, modelAccess.getXObjects(doc, classRef, key, "").size());
    List<BaseObject> ret = modelAccess.getXObjects(doc, classRef, key, val);
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj2, ret.get(1));
  }

  @Test
  public void test_getXObjects_key_values() {
    String key = "field";
    List<String> vals = Arrays.asList("val1", "val2");
    addObj(classRef, null, null);
    addObj(classRef2, key, vals.get(0));
    BaseObject obj1 = addObj(classRef, key, vals.get(0));
    addObj(classRef, key, null);
    BaseObject obj2 = addObj(classRef, key, vals.get(1));
    List<BaseObject> ret = modelAccess.getXObjects(doc, classRef, key, vals);
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj2, ret.get(1));
  }

  @Test
  public void test_getXObjects_docRef() throws Exception {
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    List<BaseObject> ret = modelAccess.getXObjects(doc.getDocumentReference(), classRef);
    verifyDefault();
    assertEquals(ret.size(), 0);
  }

  @Test
  public void test_removeXObjects() {
    addObj(classRef, null, null);
    assertTrue(modelAccess.removeXObjects(doc, classRef));
    assertEquals(0, modelAccess.getXObjects(doc, classRef).size());
  }

  @Test
  public void test_removeXObjects_noChange() {
    addObj(classRef2, null, null);
    assertFalse(modelAccess.removeXObjects(doc, classRef));
    assertEquals(0, modelAccess.getXObjects(doc, classRef).size());
    assertEquals(1, modelAccess.getXObjects(doc, classRef2).size());
  }

  @Test
  public void test_removeXObjects_mutlipleObj() {
    addObj(classRef, null, null);
    addObj(classRef2, null, null);
    addObj(classRef, null, null);
    assertTrue(modelAccess.removeXObjects(doc, classRef));
    assertEquals(0, modelAccess.getXObjects(doc, classRef).size());
    assertEquals(1, modelAccess.getXObjects(doc, classRef2).size());
  }

  @Test
  public void test_removeXObjects_docRef() throws Exception {
    addObj(classRef, null, null);
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    assertTrue(modelAccess.removeXObjects(doc.getDocumentReference(), classRef));
    verifyDefault();
  }

  private BaseObject addObj(DocumentReference classRef, String key, String value) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    if (key != null) {
      obj.setStringValue(key, value);
    }
    doc.addXObject(obj);
    return obj;
  }

}
