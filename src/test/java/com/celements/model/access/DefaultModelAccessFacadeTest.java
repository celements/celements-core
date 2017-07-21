package com.celements.model.access;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.syntax.Syntax;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.exception.DocumentAlreadyExistsException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.TestClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.DateField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ClassFieldValue;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.NumberClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class DefaultModelAccessFacadeTest extends AbstractComponentTest {

  private DefaultModelAccessFacade modelAccess;
  private XWikiDocument doc;
  private DocumentReference classRef;
  private DocumentReference classRef2;
  private XWikiStoreInterface storeMock;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(XWikiDocumentCreator.class, "default", new TestXWikiDocumentCreator());
    modelAccess = (DefaultModelAccessFacade) Utils.getComponent(IModelAccessFacade.class);
    doc = new XWikiDocument(new DocumentReference("db", "space", "doc"));
    doc.setSyntax(Syntax.XWIKI_1_0);
    doc.setMetaDataDirty(false);
    storeMock = createMockAndAddToDefault(XWikiStoreInterface.class);
    doc.setStore(storeMock);
    doc.setNew(false);
    expect(getWikiMock().getStore()).andReturn(storeMock).anyTimes();
    classRef = new DocumentReference("db", "class", "any");
    classRef2 = new DocumentReference("db", "class", "other");
    // important for unstable-2.0 set database because class references are checked for db
    getContext().setDatabase("db");
  }

  @Test
  public void test_getDocument() throws Exception {
    doc.setNew(true);
    doc.setFromCache(false);
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        doc).once();
    replayDefault();
    XWikiDocument ret = modelAccess.getDocument(doc.getDocumentReference());
    verifyDefault();
    assertEquals(doc, ret);
    assertTrue(ret.isNew());
    assertFalse(ret.isFromCache());
    assertSame("do not clone if isNew", doc, ret);
  }

  @Test
  public void test_getDocument_failed() throws Exception {
    Throwable cause = new XWikiException();
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))).andThrow(
        cause).once();
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
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        false).once();
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
  public void test_getDocument_cloneFromCache() throws Exception {
    modelAccess.strategy = registerComponentMock(ModelAccessStrategy.class);
    String lang = "";
    doc.setDefaultLanguage("");
    doc.setLanguage("");
    doc.setFromCache(true);
    expect(modelAccess.strategy.exists(eq(doc.getDocumentReference()), eq(""))).andReturn(
        true).once();
    expect(modelAccess.strategy.getDocument(eq(doc.getDocumentReference()), eq(""))).andReturn(
        doc).once();
    replayDefault();
    XWikiDocument theDoc = modelAccess.getDocument(doc.getDocumentReference(), lang);
    verifyDefault();
    assertNotSame(doc, theDoc);
    assertEquals(doc.getDocumentReference(), theDoc.getDocumentReference());
  }

  @Test
  public void test_getDocument_defaultLanguage() throws Exception {
    modelAccess.strategy = registerComponentMock(ModelAccessStrategy.class);
    String lang = "default";
    doc.setDefaultLanguage("");
    doc.setLanguage("");
    expect(modelAccess.strategy.exists(eq(doc.getDocumentReference()), eq(""))).andReturn(
        true).once(); // empty lang instead of 'default'
    expect(modelAccess.strategy.getDocument(eq(doc.getDocumentReference()), eq(""))).andReturn(
        doc).once(); // empty lang instead of 'default'
    replayDefault();
    XWikiDocument theDoc = modelAccess.getDocument(doc.getDocumentReference(), lang);
    verifyDefault();
    assertSame(doc, theDoc);
  }

  @Test
  public void test_getDocument_translatedDocument_defaultLanguage_empty() throws Exception {
    modelAccess.strategy = registerComponentMock(ModelAccessStrategy.class);
    String lang = "de";
    doc.setDefaultLanguage("");
    doc.setLanguage(lang);
    getConfigurationSource().setProperty(ModelContext.CFG_KEY_DEFAULT_LANG, lang);
    expect(modelAccess.strategy.exists(eq(doc.getDocumentReference()), eq(lang))).andReturn(
        true).once();
    expect(modelAccess.strategy.getDocument(eq(doc.getDocumentReference()), eq(lang))).andReturn(
        doc).once();
    replayDefault();
    XWikiDocument theDoc = modelAccess.getDocument(doc.getDocumentReference(), lang);
    verifyDefault();
    assertSame(doc, theDoc);
  }

  @Test
  public void test_getDocument_translatedDocument_noTranslation() throws Exception {
    modelAccess.strategy = registerComponentMock(ModelAccessStrategy.class);
    String defaultLang = "de";
    String lang = "en";
    doc.setDefaultLanguage(defaultLang);
    doc.setLanguage("");
    getConfigurationSource().setProperty(ModelContext.CFG_KEY_DEFAULT_LANG, defaultLang);
    expect(modelAccess.strategy.exists(eq(doc.getDocumentReference()), eq(lang))).andReturn(
        false).once();
    replayDefault();
    try {
      modelAccess.getDocument(doc.getDocumentReference(), lang);
      fail("expecting DocumentNotExistsException");
    } catch (DocumentNotExistsException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void test_getDocument_translatedDocument() throws Exception {
    modelAccess.strategy = registerComponentMock(ModelAccessStrategy.class);
    String defaultLang = "de";
    String lang = "en";
    doc.setDefaultLanguage(defaultLang);
    doc.setLanguage(lang);
    getConfigurationSource().setProperty(ModelContext.CFG_KEY_DEFAULT_LANG, defaultLang);
    expect(modelAccess.strategy.exists(eq(doc.getDocumentReference()), eq(lang))).andReturn(
        true).once();
    expect(modelAccess.strategy.getDocument(eq(doc.getDocumentReference()), eq(lang))).andReturn(
        doc).once();
    replayDefault();
    XWikiDocument theDoc = modelAccess.getDocument(doc.getDocumentReference(), lang);
    verifyDefault();
    assertSame(doc, theDoc);
  }

  @Test
  public void test_createDocument() throws Exception {
    String lang = "de";
    getConfigurationSource().setProperty(ModelContext.CFG_KEY_DEFAULT_LANG, lang);
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        false).once();
    expect(getWikiMock().isVirtualMode()).andReturn(true).anyTimes();
    replayDefault();
    // important only call setUser after replayDefault. In unstable-2.0 branch setUser
    // calls xwiki.isVirtualMode
    String userName = "XWiki.TestUser";
    getContext().setUser(userName);
    XWikiDocument ret = modelAccess.createDocument(doc.getDocumentReference());
    verifyDefault();
    assertEquals(doc.getDocumentReference(), ret.getDocumentReference());
    assertTrue(ret.isNew());
    assertFalse(ret.isFromCache());
  }

  @Test
  public void test_createDocument_alreadyExists() throws Exception {
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        true).once();
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
    doc.setNew(true);
    doc.setFromCache(false);
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        doc).once();
    replayDefault();
    XWikiDocument ret = modelAccess.getOrCreateDocument(doc.getDocumentReference());
    verifyDefault();
    assertEquals(doc, ret);
    assertTrue(ret.isNew());
    assertFalse(ret.isFromCache());
    assertSame("do not clone if isNew", doc, ret);
    assertFalse(doc.isMetaDataDirty());
  }

  @Test
  public void test_getOrCreateDocument_get_failed() throws Exception {
    Throwable cause = new XWikiException();
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))).andThrow(
        cause).once();
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
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        false).once();
    replayDefault();
    XWikiDocument ret = modelAccess.getOrCreateDocument(doc.getDocumentReference());
    verifyDefault();
    assertEquals(doc.getDocumentReference(), ret.getDocumentReference());
    assertTrue(ret.isNew());
    assertFalse(ret.isFromCache());
    assertTrue(ret.isMetaDataDirty());
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
  public void test_exists_true() throws Exception {
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        true).once();
    replayDefault();
    boolean ret = modelAccess.exists(doc.getDocumentReference());
    verifyDefault();
    assertTrue(ret);
  }

  @Test
  public void test_exists_false() throws Exception {
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        false).once();
    replayDefault();
    boolean ret = modelAccess.exists(doc.getDocumentReference());
    verifyDefault();
    assertFalse(ret);
  }

  @Test
  public void test_exists_null() throws Exception {
    replayDefault();
    boolean ret = modelAccess.exists(null);
    verifyDefault();
    assertFalse(ret);
  }

  @Test
  public void test_exists_lang() throws Exception {
    String lang = "en";
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        true).once();
    replayDefault();
    boolean ret = modelAccess.exists(doc.getDocumentReference(), lang);
    verifyDefault();
    assertTrue(ret);
  }

  @Test
  public void test_exists_lang_none() throws Exception {
    modelAccess.strategy = registerComponentMock(ModelAccessStrategy.class);
    expect(modelAccess.strategy.exists(eq(doc.getDocumentReference()), eq(""))).andReturn(
        false).once(); // empty lang instead of null

    replayDefault();
    boolean ret = modelAccess.exists(doc.getDocumentReference(), null);
    verifyDefault();
    assertFalse(ret);
  }

  @Test
  public void test_exists_lang_null() throws Exception {
    replayDefault();
    boolean ret = modelAccess.exists(null, null);
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
  public void test_saveDocument_checkAuthor() throws Exception {
    String username = "XWiki.TestUser";
    getContext().setUser(username);
    doc.setAuthor("XWiki.OldAuthor");
    String oldCreator = "XWiki.OldCreator";
    doc.setCreator(oldCreator);
    Capture<XWikiDocument> docCapture = new Capture<>();
    getWikiMock().saveDocument(capture(docCapture), eq(""), eq(false), same(getContext()));
    expectLastCall().once();
    doc.setMetaDataDirty(false);
    replayDefault();
    modelAccess.saveDocument(doc);
    verifyDefault();
    XWikiDocument docSaved = docCapture.getValue();
    assertEquals(username, docSaved.getAuthor());
    assertEquals(oldCreator, docSaved.getCreator());
    assertTrue(docSaved.isMetaDataDirty());
  }

  @Test
  public void test_saveDocument_checkAuthor_Creator_isNew() throws Exception {
    String username = "XWiki.TestUser";
    getContext().setUser(username);
    doc.setNew(true);
    doc.setAuthor("XWiki.OldAuthor");
    String oldCreator = "XWiki.OldCreator";
    doc.setCreator(oldCreator);
    Capture<XWikiDocument> docCapture = new Capture<>();
    getWikiMock().saveDocument(capture(docCapture), eq(""), eq(false), same(getContext()));
    expectLastCall().once();
    doc.setMetaDataDirty(false);
    replayDefault();
    modelAccess.saveDocument(doc);
    verifyDefault();
    XWikiDocument docSaved = docCapture.getValue();
    assertEquals(username, docSaved.getAuthor());
    assertEquals(username, docSaved.getCreator());
    assertTrue(docSaved.isMetaDataDirty());
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
    getWikiMock().saveDocument(same(doc), eq(comment), eq(isMinorEdit), same(getContext()));
    expectLastCall().once();
    replayDefault();
    modelAccess.saveDocument(doc, comment, isMinorEdit);
    verifyDefault();
  }

  @Test
  public void test_isTranslation() throws Exception {
    assertFalse(modelAccess.isTranslation(doc));
  }

  @Test
  public void test_isTranslation_false() throws Exception {
    doc.setTranslation(1);
    assertTrue(modelAccess.isTranslation(doc));
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
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    classRef = new DocumentReference("otherWiki", classRef.getLastSpaceReference().getName(),
        classRef.getName());
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
  public void test_getXObjects_isTranslation() throws Exception {
    doc.setTranslation(1);
    replayDefault();
    try {
      modelAccess.getXObjects(doc, classRef);
      fail("expecting IllegalStateException");
    } catch (IllegalStateException ise) {
      // expected
    }
    verifyDefault();
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
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        doc).once();
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
  public void test_getXObjects_map_isTranslation() throws Exception {
    doc.setTranslation(1);
    replayDefault();
    try {
      modelAccess.getXObjects(doc);
      fail("expecting IllegalStateException");
    } catch (IllegalStateException ise) {
      // expected
    }
    verifyDefault();
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
    classRef = new DocumentReference("otherWiki", classRef.getLastSpaceReference().getName(),
        classRef.getName());
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
  public void test_getOrgCreateXObject_get() throws Exception {
    BaseObject obj = addObj(classRef, null, null);

    replayDefault();
    BaseObject ret = modelAccess.getOrCreateXObject(doc, classRef);
    verifyDefault();

    assertSame(obj, ret);
  }

  @Test
  public void test_getOrgCreateXObject_create() throws Exception {
    expectNewBaseObject(classRef);

    replayDefault();
    BaseObject ret = modelAccess.getOrCreateXObject(doc, classRef);
    verifyDefault();

    assertEquals(classRef, ret.getXClassReference());
  }

  @Test
  public void test_getOrgCreateXObject_create_set() throws Exception {
    String key = "field";
    String val = "val";
    BaseClass bClassMock = expectNewBaseObject(classRef);
    expect(bClassMock.get(eq(key))).andReturn(new StringClass()).anyTimes();

    replayDefault();
    BaseObject ret = modelAccess.getOrCreateXObject(doc, classRef, key, val);
    verifyDefault();

    assertEquals(classRef, ret.getXClassReference());
    assertEquals(val, ret.getStringValue(key));
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
  public void test_getProperty_String() throws Exception {
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
  public void test_getProperty_String_emptyString() throws Exception {
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
  public void test_getProperty_Number() throws Exception {
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
  public void test_getProperty_Date() throws Exception {
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
  public void test_getProperty_Date_Timestamp() throws Exception {
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
  public void test_getProperty_ClassField() throws Exception {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    String val = "val";
    addObj(field.getClassDef().getClassRef(), field.getName(), val);

    replayDefault();
    String ret = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertEquals(val, ret);
  }

  @Test
  public void test_getProperty_ClassField_illegalField() throws Exception {
    ClassField<Date> field = new DateField.Builder(TestClassDefinition.NAME, "name").build();
    addObj(field.getClassDef().getClassRef(), field.getName(), "val");

    replayDefault();
    try {
      modelAccess.getProperty(doc, field);
      fail("expecting IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      assertTrue(iae.getMessage().contains(field.toString()));
      assertTrue(iae.getCause().getClass().equals(ClassCastException.class));
    } finally {
      verifyDefault();
    }

  }

  @Test
  public void test_getProperty_ClassField_docRef() throws Exception {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    String val = "val";
    addObj(field.getClassDef().getClassRef(), field.getName(), val);

    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))).andReturn(
        doc).once();

    replayDefault();
    String ret = modelAccess.getProperty(doc.getDocumentReference(), field);
    verifyDefault();

    assertEquals(val, ret);
  }

  public void test_getFieldValue_doc() {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    String val = "val";
    addObj(field.getClassDef().getClassRef(), field.getName(), val);
    replayDefault();
    Optional<String> ret = modelAccess.getFieldValue(doc, field);
    verifyDefault();
    assertNotNull(ret);
    assertTrue(ret.isPresent());
    assertEquals(val, ret.get());
  }

  public void test_getFieldValue_doc_null() {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    addObj(field.getClassDef().getClassRef(), field.getName(), null);
    replayDefault();
    Optional<String> ret = modelAccess.getFieldValue(doc, field);
    verifyDefault();
    assertNotNull(ret);
    assertFalse(ret.isPresent());
  }

  public void test_getFieldValue_docRef() throws Exception {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    String val = "val";
    addObj(field.getClassDef().getClassRef(), field.getName(), null);
    replayDefault();
    Optional<String> ret = modelAccess.getFieldValue(doc.getDocumentReference(), field);
    verifyDefault();
    assertNotNull(ret);
    assertTrue(ret.isPresent());
    assertEquals(val, ret.get());
  }

  public void test_getFieldValue_docRef_null() throws Exception {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    addObj(field.getClassDef().getClassRef(), field.getName(), null);
    replayDefault();
    Optional<String> ret = modelAccess.getFieldValue(doc.getDocumentReference(), field);
    verifyDefault();
    assertNotNull(ret);
    assertFalse(ret.isPresent());
  }

  public void test_getFieldValue_doc_ignore_hasValue() {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    String val = "val";
    addObj(field.getClassDef().getClassRef(), field.getName(), val);
    replayDefault();
    Optional<String> ret = modelAccess.getFieldValue(doc, field, "test");
    verifyDefault();
    assertNotNull(ret);
    assertTrue(ret.isPresent());
    assertEquals(val, ret.get());
  }

  public void test_getFieldValue_doc_ignore_noValue() {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    addObj(field.getClassDef().getClassRef(), field.getName(), null);
    replayDefault();
    Optional<String> ret = modelAccess.getFieldValue(doc, field, "test");
    verifyDefault();
    assertNotNull(ret);
    assertFalse(ret.isPresent());
  }

  public void test_getFieldValue_doc_ignore_hasIgnoreValue() {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    String val = "test";
    addObj(field.getClassDef().getClassRef(), field.getName(), val);
    replayDefault();
    Optional<String> ret = modelAccess.getFieldValue(doc, field, "test");
    verifyDefault();
    assertNotNull(ret);
    assertFalse(ret.isPresent());
  }

  public void test_getFieldValue_docRef_ignore_hasValue() throws Exception {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    String val = "val";
    addObj(field.getClassDef().getClassRef(), field.getName(), val);
    replayDefault();
    Optional<String> ret = modelAccess.getFieldValue(doc.getDocumentReference(), field, "test");
    verifyDefault();
    assertNotNull(ret);
    assertTrue(ret.isPresent());
    assertEquals(val, ret.get());
  }

  public void test_getFieldValue_docRef_ignore_noValue() throws Exception {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    addObj(field.getClassDef().getClassRef(), field.getName(), null);
    replayDefault();
    Optional<String> ret = modelAccess.getFieldValue(doc.getDocumentReference(), field, "test");
    verifyDefault();
    assertNotNull(ret);
    assertFalse(ret.isPresent());
  }

  public void test_getFieldValue_docRef_ignore_hasIgnoreValue() throws Exception {
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    String val = "test";
    addObj(field.getClassDef().getClassRef(), field.getName(), val);
    replayDefault();
    Optional<String> ret = modelAccess.getFieldValue(doc.getDocumentReference(), field, "test");
    verifyDefault();
    assertNotNull(ret);
    assertFalse(ret.isPresent());
  }

  @Test
  public void test_getProperties() {
    ClassDefinition classDef = Utils.getComponent(ClassDefinition.class, TestClassDefinition.NAME);
    String val = "value";
    addObj(classDef.getClassRef(), TestClassDefinition.FIELD_MY_STRING.getName(), val);

    replayDefault();
    List<ClassFieldValue<?>> ret = modelAccess.getProperties(doc, classDef);
    verifyDefault();

    assertEquals(classDef.getFields().size(), ret.size());
    for (int i = 0; i < ret.size(); i++) {
      ClassField<?> field = classDef.getFields().get(i);
      assertEquals(field, ret.get(i).getField());
      if (field.equals(TestClassDefinition.FIELD_MY_STRING)) {
        assertEquals(val, ret.get(i).getValue());
      } else {
        assertNull(ret.get(i).getValue());
      }
    }
  }

  @Test
  public void test_setProperty_String() throws Exception {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    String name = "name";
    String val = "val";

    expectPropertyClass(classRef, name, new StringClass());

    replayDefault();
    modelAccess.setProperty(obj, name, val);
    verifyDefault();

    assertEquals(1, obj.getFieldList().size());
    assertEquals(val, ((BaseProperty) obj.get(name)).getValue());
  }

  @Test
  public void test_setProperty_Number() throws Exception {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    String name = "name";
    long val = 5;

    expectPropertyClass(classRef, name, new NumberClass());

    replayDefault();
    modelAccess.setProperty(obj, name, val);
    verifyDefault();

    assertEquals(1, obj.getFieldList().size());
    assertEquals(val, ((BaseProperty) obj.get(name)).getValue());
  }

  @Test
  public void test_setProperty_Date() throws Exception {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    String name = "name";
    Date val = new Date();

    expectPropertyClass(classRef, name, new DateClass());

    replayDefault();
    modelAccess.setProperty(obj, name, val);
    verifyDefault();

    assertEquals(1, obj.getFieldList().size());
    assertEquals(val, ((BaseProperty) obj.get(name)).getValue());
  }

  @Test
  public void test_setProperty_List() throws Exception {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    String name = "name";

    expectPropertyClass(classRef, name, new StringClass());

    replayDefault();
    modelAccess.setProperty(obj, name, Arrays.asList("A", "B"));
    verifyDefault();

    assertEquals(1, obj.getFieldList().size());
    assertEquals("A|B", ((BaseProperty) obj.get(name)).getValue());
  }

  @Test
  public void test_setProperty_ClassField() throws Exception {
    String val = "val";
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    ClassFieldValue<String> fieldValue = new ClassFieldValue<>(field, val);
    BaseObject obj = addObj(field.getClassDef().getClassRef(), field.getName(), "");

    expectPropertyClass(field.getClassDef().getClassRef(), field.getName(), new StringClass());

    replayDefault();
    modelAccess.setProperty(doc, fieldValue);
    verifyDefault();

    assertEquals(1, obj.getFieldList().size());
    assertEquals(val, obj.getStringValue(field.getName()));
  }

  @Test
  public void test_setProperty_ClassField_illegalField() throws Exception {
    ClassField<Date> field = new DateField.Builder(TestClassDefinition.NAME, "name").build();
    ClassFieldValue<Date> fieldValue = new ClassFieldValue<>(field, new Date());
    BaseClass bClass = expectNewBaseObject(field.getClassDef().getClassRef());

    expectPropertyClass(bClass, field.getName(), new StringClass());

    replayDefault();
    try {
      modelAccess.setProperty(doc, fieldValue);
      fail("expecting IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      assertTrue(iae.getMessage().contains(field.toString()));
      assertTrue(iae.getCause().getClass().equals(ClassCastException.class));
    } finally {
      verifyDefault();
    }
  }

  @Test
  public void test_setProperty_ClassField_newObj() throws Exception {
    String val = "val";
    ClassField<String> field = new StringField.Builder(TestClassDefinition.NAME, "name").build();
    ClassFieldValue<String> fieldValue = new ClassFieldValue<>(field, val);
    BaseClass bClass = expectNewBaseObject(field.getClassDef().getClassRef());
    expectPropertyClass(bClass, field.getName(), new StringClass());

    replayDefault();
    modelAccess.setProperty(doc, fieldValue);
    verifyDefault();

    BaseObject obj = doc.getXObject(field.getClassDef().getClassRef());
    assertEquals(1, obj.getFieldList().size());
    assertEquals(val, obj.getStringValue(field.getName()));
  }

  @Test
  public void test_setProperty_getProperty_customField() throws Exception {
    ClassField<DocumentReference> field = TestClassDefinition.FIELD_MY_DOCREF;
    DocumentReference toStoreRef = new DocumentReference("myDB", "mySpace", "myDoc");

    BaseClass bClass = expectNewBaseObject(field.getClassDef().getClassRef());
    expectPropertyClass(bClass, field.getName(), new StringClass());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, toStoreRef));
    DocumentReference ret = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertEquals(toStoreRef, ret);
    String objValue = modelAccess.getXObject(doc, field.getClassDef().getClassRef()).getStringValue(
        field.getName());
    assertEquals(modelAccess.modelUtils.serializeRef(toStoreRef), objValue);
  }

  @Test
  public void test_getAttachmentNameEqual() throws Exception {
    String filename = "image.jpg";
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment imageAtt = new XWikiAttachment(doc, filename);
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, imageAtt, lastAtt);
    doc.setAttachmentList(attList);
    replayDefault();
    XWikiAttachment att = modelAccess.getAttachmentNameEqual(doc, filename);
    verifyDefault();
    assertNotNull("Expected image.jpg attachment - not null", att);
    assertEquals(filename, att.getFilename());
  }

  @Test
  public void test_getAttachmentNameEqual_not_exists() {
    String filename = "image.jpg";
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, lastAtt);
    doc.setAttachmentList(attList);
    replayDefault();
    try {
      modelAccess.getAttachmentNameEqual(doc, filename);
      fail("AttachmentNotExistsException expected");
    } catch (AttachmentNotExistsException exp) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void test_getApiDocument_hasAccess() throws Exception {
    XWikiRightService mockRightSrv = createMockAndAddToDefault(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(mockRightSrv).anyTimes();
    expect(mockRightSrv.hasAccessLevel(eq("view"), eq(getContext().getUser()), eq("db:space.doc"),
        same(getContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    Document apiDoc = modelAccess.getApiDocument(doc);
    assertNotNull("Expected Attachment api object - not null", apiDoc);
    verifyDefault();
  }

  @Test
  public void test_getApiDocument_noAccess() throws Exception {
    XWikiRightService mockRightSrv = createMockAndAddToDefault(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(mockRightSrv).anyTimes();
    expect(mockRightSrv.hasAccessLevel(eq("view"), eq(getContext().getUser()), eq("db:space.doc"),
        same(getContext()))).andReturn(false).atLeastOnce();
    replayDefault();
    try {
      modelAccess.getApiDocument(doc);
      fail("NoAccessRightsException expected");
    } catch (NoAccessRightsException exp) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void test_checkNotTranslation() throws Exception {
    modelAccess.checkNotTranslation(doc);
    doc.setLanguage("en");
    doc.setTranslation(1);
    try {
      modelAccess.checkNotTranslation(doc);
      fail("expecting IllegalStateException");
    } catch (IllegalStateException ise) {
      assertTrue("format not replacing placeholder 0", ise.getMessage().contains("'en'"));
      assertTrue("format not replacing placeholder 1", ise.getMessage().contains("'"
          + doc.getDocumentReference() + "'"));
    }
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

  private void assertCapture(Capture<XWikiDocument> capt, DocumentReference docRef, String lang) {
    assertNotNull(capt.getValue());
    assertEquals(docRef, capt.getValue().getDocumentReference());
    assertEquals(lang, capt.getValue().getLanguage());
  }

  private class TestXWikiDocumentCreator implements XWikiDocumentCreator {

    @Override
    public XWikiDocument createWithoutDefaults(DocumentReference docRef, String lang) {
      XWikiDocument doc = new XWikiDocument(docRef);
      doc.setLanguage(lang);
      return doc;
    }

    @Override
    public XWikiDocument createWithoutDefaults(DocumentReference docRef) {
      return createWithoutDefaults(docRef, IModelAccessFacade.DEFAULT_LANG);
    }

    @Override
    public XWikiDocument create(DocumentReference docRef, String lang) {
      return createWithoutDefaults(docRef, lang);
    }

    @Override
    public XWikiDocument create(DocumentReference docRef) {
      return create(docRef, IModelAccessFacade.DEFAULT_LANG);
    }

  }

}
