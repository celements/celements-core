package com.celements.model.access;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.exception.DocumentAlreadyExistsException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.access.exception.TranslationNotExistsException;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.NumberClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

public class DefaultModelAccessFacadeTest extends AbstractBridgedComponentTestCase {

  private DefaultModelAccessFacade modelAccess;
  private XWikiDocument doc;
  private DocumentReference classRef;
  private DocumentReference classRef2;
  private XWikiStoreInterface xwikiStoreMock;

  @Before
  public void setUp_DefaultModelAccessFacadeTest() {
    modelAccess = (DefaultModelAccessFacade) Utils.getComponent(IModelAccessFacade.class);
    doc = new XWikiDocument(new DocumentReference("db", "space", "doc"));
    doc.setMetaDataDirty(false);
    xwikiStoreMock = createMockAndAddToDefault(XWikiStoreInterface.class);
    doc.setStore(xwikiStoreMock);
    doc.setNew(false);
    expect(getWikiMock().getStore()).andReturn(xwikiStoreMock).anyTimes();
    classRef = new DocumentReference("db", "class", "any");
    classRef2 = new DocumentReference("db", "class", "other");
    //important for unstable-2.0 set database because class references are checked for db
    getContext().setDatabase("db");
  }

  @Test
  public void test_getDocument() throws Exception {
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    XWikiDocument ret = modelAccess.getDocument(doc.getDocumentReference());
    verifyDefault();
    assertSame(doc, ret);
  }

  @Test
  public void test_getDocument_failed() throws Exception {
    Throwable cause = new XWikiException();
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andThrow(cause).once();
    replayDefault();
    try {
      modelAccess.getDocument(doc.getDocumentReference());
      fail("expecting DocumentLoadException");
    } catch (DocumentLoadException exc) {
      assertSame(cause, exc.getCause());
    }
    verifyDefault();
  }

  @Test
  public void test_getDocument_notExists() throws Exception {
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(false).once();
    replayDefault();
    try {
      modelAccess.getDocument(doc.getDocumentReference());
      fail("expecting DocumentNotExistsException");
    } catch (DocumentNotExistsException exc) {
      // expected
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
  public void test_getDocument_translatedDocument_defaultLanguage_empty(
      ) throws Exception {
    doc.setDefaultLanguage("");
    doc.setLanguage("");
    IWebUtilsService webUtilsMock = createMockAndAddToDefault(IWebUtilsService.class);
    modelAccess.webUtilsService = webUtilsMock;
    expect(webUtilsMock.getDefaultLanguage(eq(doc.getDocumentReference(
        ).getLastSpaceReference()))).andReturn("de").anyTimes();
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    XWikiDocument theDoc = modelAccess.getDocument(doc.getDocumentReference(), "de");
    verifyDefault();
    assertSame(doc, theDoc);
  }

  @Test
  public void test_getDocument_translatedDocument_noTranslation() throws Exception {
    doc.setDefaultLanguage("de");
    doc.setLanguage("");
    IWebUtilsService webUtilsMock = createMockAndAddToDefault(IWebUtilsService.class);
    modelAccess.webUtilsService = webUtilsMock;
    expect(webUtilsMock.getDefaultLanguage(eq(doc.getDocumentReference(
        ).getLastSpaceReference()))).andReturn("de").anyTimes();
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(xwikiStoreMock.getTranslationList(same(doc), same(getContext()))).andReturn(
        Collections.<String>emptyList());
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    try {
      modelAccess.getDocument(doc.getDocumentReference(), "en");
      fail("expecting TranslationNotExistsException");
    } catch (TranslationNotExistsException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void test_getDocument_translatedDocument() throws Exception {
    doc.setDefaultLanguage("de");
    doc.setLanguage("");
    IWebUtilsService webUtilsMock = createMockAndAddToDefault(IWebUtilsService.class);
    modelAccess.webUtilsService = webUtilsMock;
    expect(webUtilsMock.getDefaultLanguage(eq(doc.getDocumentReference(
        ).getLastSpaceReference()))).andReturn("de").anyTimes();
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(xwikiStoreMock.getTranslationList(same(doc), same(getContext()))).andReturn(
        Arrays.asList("en", "fr"));
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    Capture<XWikiDocument> tdocCapture = new Capture<>();
    XWikiDocument theTdoc = new XWikiDocument(doc.getDocumentReference());
    theTdoc.setDefaultLanguage("de");
    theTdoc.setLanguage("en");
    theTdoc.setNew(false);
    expect(xwikiStoreMock.loadXWikiDoc(capture(tdocCapture), same(getContext()))
        ).andReturn(theTdoc).once();
    replayDefault();
    XWikiDocument theDoc = modelAccess.getDocument(doc.getDocumentReference(), "en");
    verifyDefault();
    assertSame(theTdoc, theDoc);
    XWikiDocument loadedTdoc = tdocCapture.getValue();
    assertEquals(doc.getDocumentReference(), loadedTdoc.getDocumentReference());
    assertEquals("en", loadedTdoc.getLanguage());
  }

  @Test
  public void test_createDocument() throws Exception {
    getConfigurationSource().setProperty("default_language", "de");
    Date beforeCreationDate = new Date(System.currentTimeMillis() - 1000); // doc drops ms
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(false).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    expect(getWikiMock().isVirtualMode()).andReturn(true).anyTimes();
    replayDefault();
    // important only call setUser after replayDefault. In unstable-2.0 branch setUser
    // calls xwiki.isVirtualMode
    String userName = "XWiki.TestUser";
    getContext().setUser(userName);
    XWikiDocument ret = modelAccess.createDocument(doc.getDocumentReference());
    verifyDefault();
    assertSame(doc, ret);
    assertEquals("de", doc.getDefaultLanguage());
    assertEquals("", doc.getLanguage());
    assertTrue(beforeCreationDate.before(doc.getCreationDate()));
    assertTrue(beforeCreationDate.before(doc.getContentUpdateDate()));
    assertTrue(beforeCreationDate.before(doc.getDate()));
    assertEquals(userName, doc.getCreator());
    assertEquals(userName, doc.getAuthor());
    assertEquals(0, doc.getTranslation());
    assertEquals("", doc.getContent());
    assertTrue(doc.isMetaDataDirty());
  }

  @Test
  public void test_createDocument_failed() throws Exception {
    Throwable cause = new XWikiException();
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(false).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andThrow(cause).once();
    replayDefault();
    try {
      modelAccess.createDocument(doc.getDocumentReference());
      fail("expecting DocumentLoadException");
    } catch (DocumentLoadException exc) {
      assertSame(cause, exc.getCause());
    }
    verifyDefault();
  }

  @Test
  public void test_createDocument_alreadyExists() throws Exception {
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    replayDefault();
    try {
      modelAccess.createDocument(doc.getDocumentReference());
      fail("expecting DocumentAlreadyExistsException");
    } catch (DocumentAlreadyExistsException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void test_createDocument_null() throws Exception {
    try {
      modelAccess.createDocument(null);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_getOrCreateDocument_get() throws Exception {
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    XWikiDocument ret = modelAccess.getOrCreateDocument(doc.getDocumentReference());
    verifyDefault();
    assertSame(doc, ret);
    assertFalse(doc.isMetaDataDirty());
  }

  @Test
  public void test_getOrCreateDocument_get_failed() throws Exception {
    Throwable cause = new XWikiException();
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andThrow(cause).once();
    replayDefault();
    try {
      modelAccess.getOrCreateDocument(doc.getDocumentReference());
      fail("expecting DocumentLoadException");
    } catch (DocumentLoadException exc) {
      assertSame(cause, exc.getCause());
    }
    verifyDefault();
  }

  @Test
  public void test_getOrCreateDocument_create() throws Exception {
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(false).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    XWikiDocument ret = modelAccess.getOrCreateDocument(doc.getDocumentReference());
    verifyDefault();
    assertSame(doc, ret);
    assertTrue(doc.isMetaDataDirty());
  }

  @Test
  public void test_getOrCreateDocument_create_failed() throws Exception {
    Throwable cause = new XWikiException();
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(false).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andThrow(cause).once();
    replayDefault();
    try {
      modelAccess.getOrCreateDocument(doc.getDocumentReference());
      fail("expecting DocumentLoadException");
    } catch (DocumentLoadException exc) {
      assertSame(cause, exc.getCause());
    }
    verifyDefault();
  }

  @Test
  public void test_getOrCreateDocument_null() throws Exception {
    try {
      modelAccess.getOrCreateDocument(null);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_exists_true() {
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    replayDefault();
    boolean ret = modelAccess.exists(doc.getDocumentReference());
    verifyDefault();
    assertTrue(ret);
  }

  @Test
  public void test_exists_false() {
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(false).once();
    replayDefault();
    boolean ret = modelAccess.exists(doc.getDocumentReference());
    verifyDefault();
    assertFalse(ret);
  }

  @Test
  public void test_exists_null() {
    replayDefault();
    boolean ret = modelAccess.exists(null);
    verifyDefault();
    assertFalse(ret);
  }

  @Test
  public void test_saveDocument() throws Exception {
    getWikiMock().saveDocument(same(doc), eq(""), eq(false), same(getContext()));
    expectLastCall().once();
    replayDefault();
    modelAccess.saveDocument(doc);
    verifyDefault();
  }

  @Test
  public void test_saveDocument_saveException() throws Exception {
    Throwable cause = new XWikiException();
    getWikiMock().saveDocument(same(doc), eq(""), eq(false), same(getContext()));
    expectLastCall().andThrow(cause).once();
    replayDefault();
    try {
      modelAccess.saveDocument(doc);
      fail("expecting DocumentSaveException");
    } catch (DocumentSaveException exc) {
      assertSame(cause, exc.getCause());
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
    getWikiMock().saveDocument(same(doc), eq(comment), eq(false), same(getContext()));
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
    try {
      modelAccess.getXObjects(doc, null);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
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
  public void test_getXObjects_otherWikiRef() {
    BaseObject obj = addObj(classRef, null, null);
    //IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    classRef = new DocumentReference("otherWiki", classRef.getLastSpaceReference(
        ).getName(), classRef.getName());
    List<BaseObject> ret = modelAccess.getXObjects(doc, classRef);
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(0));
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
  public void test_getXObjects_undmodifiable() throws Exception {
    addObj(classRef, null, null);
    List<BaseObject> ret = modelAccess.getXObjects(doc, classRef);
    assertEquals(1, ret.size());
    try {
      ret.remove(0);
      fail("expecting UnsupportedOperationException");
    } catch (UnsupportedOperationException exc) {
      // expected
    }
  }

  @Test
  public void test_getXObjects_docRef() throws Exception {
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    List<BaseObject> ret = modelAccess.getXObjects(doc.getDocumentReference(), classRef);
    verifyDefault();
    assertEquals(0, ret.size());
  }

  @Test
  public void test_getXObjects_map() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef, null, null);
    BaseObject obj3 = addObj(classRef, null, null);
    BaseObject obj4 = addObj(classRef2, null, null);
    Map<DocumentReference, List<BaseObject>> ret = modelAccess.getXObjects(doc);
    assertEquals(2, ret.size());
    assertTrue(ret.containsKey(classRef));
    assertEquals(3, ret.get(classRef).size());
    assertEquals(obj1, ret.get(classRef).get(0));
    assertEquals(obj2, ret.get(classRef).get(1));
    assertEquals(obj3, ret.get(classRef).get(2));
    assertTrue(ret.containsKey(classRef2));
    assertEquals(1, ret.get(classRef2).size());
    assertEquals(obj4, ret.get(classRef2).get(0));
    try {
      ret.remove(classRef);
      fail("expecting UnsupportedOperationException");
    } catch (UnsupportedOperationException exc) {
      // expected
    }
  }

  @Test
  public void test_newXObject() throws Exception {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(docMock.getDocumentReference()).andReturn(doc.getDocumentReference());
    BaseObject obj = new BaseObject();
    expect(docMock.newXObject(eq(classRef), same(getContext()))).andReturn(obj).once();
    replayDefault();
    BaseObject ret = modelAccess.newXObject(docMock, classRef);
    verifyDefault();
    assertEquals(obj, ret);
  }

  @Test
  public void test_newXObject_loadException() throws Exception {
    Throwable cause = new XWikiException();
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(docMock.getDocumentReference()).andReturn(doc.getDocumentReference());
    expect(docMock.newXObject(eq(classRef), same(getContext()))).andThrow(cause).once();
    replayDefault();
    try {
      modelAccess.newXObject(docMock, classRef);
      fail("expecting ClassDocumentLoadException");
    } catch (ClassDocumentLoadException exc) {
      assertSame(cause, exc.getCause());
    }
    verifyDefault();
  }

  @Test
  public void test_newXObject_otherWikiRef() throws Exception {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(docMock.getDocumentReference()).andReturn(doc.getDocumentReference());
    BaseObject obj = new BaseObject();
    expect(docMock.newXObject(eq(classRef), same(getContext()))).andReturn(obj).once();
    //IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    classRef = new DocumentReference("otherWiki", classRef.getLastSpaceReference(
        ).getName(), classRef.getName());
    replayDefault();
    BaseObject ret = modelAccess.newXObject(docMock, classRef);
    verifyDefault();
    assertEquals(obj, ret);
  }

  @Test
  public void test_newXObject_nullDoc() throws Exception {
    replayDefault();
    try {
      modelAccess.newXObject((XWikiDocument) null, classRef);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void test_newXObject_nullClassRef() throws Exception {
    replayDefault();
    try {
      modelAccess.newXObject(doc, null);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
    verifyDefault();
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

  private BaseObject addObj(DocumentReference classRef, String key, String value) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    if (key != null) {
      obj.setStringValue(key, value);
    }
    doc.addXObject(obj);
    return obj;
  }

  
  @Test
  public void testGetProperty_String() throws Exception {
    BaseObject obj = new BaseObject();
    String name = "name";
    String val = "val";
    obj.setStringValue(name, val);
    
    replayDefault();
    Object ret = modelAccess.getProperty(obj, name);
    verifyDefault();

    assertEquals(val, ret);
  }
  
  @Test
  public void testGetProperty_String_emptyString() throws Exception {
    BaseObject obj = new BaseObject();
    String name = "name";
    String val = "";
    obj.setStringValue(name, val);
    
    replayDefault();
    Object ret = modelAccess.getProperty(obj, name);
    verifyDefault();

    assertNull(ret);
  }
  
  @Test
  public void testGetProperty_Number() throws Exception {
    BaseObject obj = new BaseObject();
    String name = "name";
    int val = 5;
    obj.setIntValue(name, val);
    
    replayDefault();
    Object ret = modelAccess.getProperty(obj, name);
    verifyDefault();

    assertEquals(val, ret);
  }
  
  @Test
  public void testGetProperty_Date() throws Exception {
    BaseObject obj = new BaseObject();
    String name = "name";
    Date val = new Date();
    obj.setDateValue(name, val);
    
    replayDefault();
    Object ret = modelAccess.getProperty(obj, name);
    verifyDefault();

    assertEquals(val, ret);
  }
  
  @Test
  public void testGetProperty_Date_Timestamp() throws Exception {
    BaseObject obj = new BaseObject();
    String name = "name";
    Date date = new Date();
    Timestamp val = new Timestamp(date.getTime());
    obj.setDateValue(name, val);
    
    replayDefault();
    Object ret = modelAccess.getProperty(obj, name);
    verifyDefault();

    assertEquals(date, ret);
  }
  
  @Test
  public void testSetProperty_String() throws Exception {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    PropertyInterface propClass = new StringClass();
    String name = "name";
    String val = "val";
    
    expect(getBaseClass(classRef).get(eq(name))).andReturn(propClass).once();
    
    replayDefault();
    modelAccess.setProperty(obj, name, val);
    verifyDefault();
    
    assertEquals(1, obj.getFieldList().size());
    assertEquals(val, ((BaseProperty) obj.get(name)).getValue());
  }
  
  @Test
  public void testSetProperty_Number() throws Exception {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    PropertyInterface propClass = new NumberClass();
    String name = "name";
    long val = 5;
    
    expect(getBaseClass(classRef).get(eq(name))).andReturn(propClass).once();
    
    replayDefault();
    modelAccess.setProperty(obj, name, val);
    verifyDefault();
    
    assertEquals(1, obj.getFieldList().size());
    assertEquals(val, ((BaseProperty) obj.get(name)).getValue());
  }
  
  @Test
  public void testSetProperty_Date() throws Exception {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    PropertyInterface propClass = new DateClass();
    String name = "name";
    Date val = new Date();
    
    expect(getBaseClass(classRef).get(eq(name))).andReturn(propClass).once();
    
    replayDefault();
    modelAccess.setProperty(obj, name, val);
    verifyDefault();
    
    assertEquals(1, obj.getFieldList().size());
    assertEquals(val, ((BaseProperty) obj.get(name)).getValue());
  }
  
  @Test
  public void testSetProperty_List() throws Exception {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    PropertyInterface propClass = new StringClass();
    String name = "name";
    
    expect(getBaseClass(classRef).get(eq(name))).andReturn(propClass).once();
    
    replayDefault();
    modelAccess.setProperty(obj, name, Arrays.asList("A", "B"));
    verifyDefault();
    
    assertEquals(1, obj.getFieldList().size());
    assertEquals("A|B", ((BaseProperty) obj.get(name)).getValue());
  }

  private BaseClass getBaseClass(DocumentReference classRef) throws Exception {
    BaseClass bClass = createMockAndAddToDefault(BaseClass.class);
    expect(getWikiMock().getXClass(eq(classRef), same(getContext()))).andReturn(bClass);
    return bClass;
  }

}
