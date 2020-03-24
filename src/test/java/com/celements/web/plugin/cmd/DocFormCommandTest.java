/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.plugin.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.docform.DocFormRequestKey;
import com.celements.docform.DocFormRequestKeyParser;
import com.celements.model.access.XWikiDocumentCreator;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.XWikiRequest;
import com.xpn.xwiki.web.XWikiServletRequestStub;

public class DocFormCommandTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private DocFormCommand docFormCmd;
  private String db;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(XWikiDocumentCreator.class);
    context = getContext();
    xwiki = getWikiMock();
    docFormCmd = new DocFormCommand();
    db = "db";
    context.setDatabase(db);
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
  }

  @Test
  public void testCollapse_nullElems() {
    assertEquals("abf|cdf", docFormCmd.collapse(new String[] { "abf", null, "cdf", null }));
  }

  @Test
  public void testCollapse_emptyStrings() {
    assertEquals("", docFormCmd.collapse(new String[] { "", "" }));
  }

  @Test
  public void testCollapse_someEmptyStrings() {
    assertEquals("abc|dfg", docFormCmd.collapse(new String[] { "abc", "", "", "dfg" }));
  }

  @Test
  public void testCollapse_trailingEmptyStrings() {
    assertEquals("abc3|df2g", docFormCmd.collapse(new String[] { "", "abc3", "df2g", "" }));
  }

  @Test
  public void testGetUpdateDoc() throws XWikiException {
    DocumentReference docRef = new DocumentReference(db, "Full", "Name");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    replayDefault();
    XWikiDocument updateDoc = docFormCmd.getUpdateDoc(docRef);
    assertNotNull(updateDoc);
    assertEquals(doc, updateDoc);
    verifyDefault();
  }

  @Test
  public void setOrRemoveObj_fromCache() throws XWikiException {
    DocumentReference docRef = new DocumentReference(db, "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key = new DocFormRequestKeyParser().parse("A.B_0_hi", docRef);
    String value = "value";
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    docFormCmd.getChangedObjects().put("db:Sp.Doc_db:A.B_0", objMock);
    objMock.set(eq("hi"), eq(value), same(context));
    expectLastCall().once();
    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key, value);
    verifyDefault();
  }

  @Test
  public void setOrRemoveObj_get() throws XWikiException {
    DocumentReference docRef = new DocumentReference(db, "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key = new DocFormRequestKeyParser().parse("A.B_0_hi", docRef);
    DocumentReference classRef = new DocumentReference(db, "A", "B");
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    expect(docMock.getXObject(eq(classRef), eq(0))).andReturn(objMock).once();
    String value = "value";
    objMock.set(eq("hi"), eq(value), same(context));
    expectLastCall().once();

    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key, value);
    verifyDefault();

    assertEquals(objMock, docFormCmd.getChangedObjects().get("db:Sp.Doc_db:A.B_0"));
  }

  @Test
  public void setOrRemoveObj_get_notExists() throws XWikiException {
    DocumentReference docRef = new DocumentReference(db, "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key = new DocFormRequestKeyParser().parse("A.B_0_hi", docRef);
    DocumentReference classRef = new DocumentReference(db, "A", "B");
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    expect(docMock.getXObject(eq(classRef), eq(0))).andReturn(null).once();
    expect(docMock.newXObject(eq(classRef), same(context))).andReturn(objMock).once();
    String value = "value";
    objMock.set(eq("hi"), eq(value), same(context));
    expectLastCall().once();

    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key, value);
    verifyDefault();

    assertEquals(objMock, docFormCmd.getChangedObjects().get("db:Sp.Doc_db:A.B_0"));
  }

  @Test
  public void setOrRemoveObj_create() throws XWikiException {
    DocumentReference docRef = new DocumentReference(db, "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key1 = new DocFormRequestKeyParser().parse("A.B_-1_hi", docRef);
    DocumentReference classRef1 = new DocumentReference(db, "A", "B");
    BaseObject objMock1 = createMockAndAddToDefault(BaseObject.class);
    expect(docMock.getXObject(eq(classRef1), eq(-1))).andReturn(null).once();
    expect(docMock.newXObject(eq(classRef1), same(context))).andReturn(objMock1).once();
    String value1 = "value1";
    objMock1.set(eq("hi"), eq(value1), same(context));
    expectLastCall();
    DocFormRequestKey key2 = new DocFormRequestKeyParser().parse("B.C_0_bla", docRef);
    String value2 = "value2";
    BaseObject objMock2 = createMockAndAddToDefault(BaseObject.class);
    docFormCmd.getChangedObjects().put("db:Sp.Doc_db:B.C_0", objMock2);
    objMock2.set(eq("bla"), eq(value2), same(context));
    expectLastCall();

    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key1, value1);
    docFormCmd.setOrRemoveObj(docMock, key2, value2);
    verifyDefault();

    assertEquals(objMock1, docFormCmd.getChangedObjects().get("db:Sp.Doc_db:A.B_-1"));
    assertEquals(objMock2, docFormCmd.getChangedObjects().get("db:Sp.Doc_db:B.C_0"));
  }

  @Test
  public void setOrRemoveObj_remove_fromCache() throws XWikiException {
    DocumentReference docRef = new DocumentReference(db, "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key = new DocFormRequestKeyParser().parse("A.B_^0", docRef);
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    docFormCmd.getChangedObjects().put("db:Sp.Doc_db:A.B_0", objMock);
    expect(docMock.removeXObject(same(objMock))).andReturn(true).once();

    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key, "");
    verifyDefault();

    assertEquals(objMock, docFormCmd.getChangedObjects().get("db:Sp.Doc_db:A.B_0"));
  }

  @Test
  public void setOrRemoveObj_remove() throws XWikiException {
    DocumentReference docRef = new DocumentReference(db, "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key = new DocFormRequestKeyParser().parse("A.B_^0", docRef);
    DocumentReference classRef = new DocumentReference(db, "A", "B");
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    expect(docMock.getXObject(eq(classRef), eq(0))).andReturn(objMock).once();
    expect(docMock.removeXObject(same(objMock))).andReturn(true).once();

    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key, "");
    verifyDefault();

    assertEquals(objMock, docFormCmd.getChangedObjects().get("db:Sp.Doc_db:A.B_0"));
  }

  @Test
  public void testValidateField() throws XWikiException {
    BaseProperty regexProp = new StringProperty();
    regexProp.setValue("/.{1,5}/");
    BaseProperty msgProp = new StringProperty();
    msgProp.setValue("msg");
    Map<String, BaseProperty> propfields = new HashMap<>();
    PropertyClass property = new PropertyClass();
    propfields.put("validationRegExp", regexProp);
    propfields.put("validationMessage", msgProp);
    property.setFields(propfields);
    Map<String, PropertyClass> fields = new HashMap<>();
    fields.put("testField", property);
    BaseClass bclass = new BaseClass();
    bclass.setFields(fields);
    DocumentReference bclassDocRef = new DocumentReference(db, "Test", "TestClass");
    XWikiDocument doc = new XWikiDocument(bclassDocRef);
    doc.setXClass(bclass);
    expect(xwiki.getDocument(eq(bclassDocRef), same(context))).andReturn(doc).atLeastOnce();
    expect(xwiki.exists(eq(bclassDocRef), same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    String result = docFormCmd.validateField("Test.TestClass", "testField", "value", context);
    assertNull("Successful validation should result in answoer null", result);
    result = docFormCmd.validateField("Test.TestClass", "testField", "", context);
    assertEquals("msg", result);
    verifyDefault();
  }

  @Test
  public void testGetFieldFromProperty() throws XWikiException {
    BaseProperty prop = new StringProperty();
    prop.setValue("value");
    PropertyClass propclass = new PropertyClass();
    propclass.put("test", prop);
    assertEquals("value", docFormCmd.getFieldFromProperty(propclass, "test"));
  }

  @Test
  public void testGetFindObjectFieldInRequestRegex_noMatch() {
    assertFalse("".matches(docFormCmd.getFindObjectFieldInRequestRegex()));
    assertFalse("abcd".matches(docFormCmd.getFindObjectFieldInRequestRegex()));
    assertFalse("Hi_0_there".matches(docFormCmd.getFindObjectFieldInRequestRegex()));
    assertFalse("Space.Doc_Class.Name_+3_field".matches(
        docFormCmd.getFindObjectFieldInRequestRegex()));
  }

  @Test
  public void testGetFindObjectFieldInRequestRegex_match() {
    assertTrue("Space.Doc_0_field_one".matches(docFormCmd.getFindObjectFieldInRequestRegex()));
    assertTrue("Space.Doc_Class.Name_3_field".matches(
        docFormCmd.getFindObjectFieldInRequestRegex()));
    assertTrue("Space.Doc_Class.Name_-3_field".matches(
        docFormCmd.getFindObjectFieldInRequestRegex()));
    assertTrue("Space.Doc_Class.Name_^3_field".matches(
        docFormCmd.getFindObjectFieldInRequestRegex()));
  }

  @Test
  public void testUpdateDocFromMap() throws XWikiException {
    Map<String, String[]> data = new HashMap<>();
    data.put("Oh.Noes_A.B_0_blabla", new String[] { "Blabla Value" });
    data.put("C.D_1_blabla2", new String[] { "Another Blabla Value" });
    BaseObject obj = createMock(BaseObject.class);
    obj.setNumber(1);
    expectLastCall().once();
    obj.set(eq("blabla2"), eq("Another Blabla Value"), same(context));
    expectLastCall();
    DocumentReference fullNameRef = new DocumentReference(db, "Full", "Name");
    obj.setDocumentReference(fullNameRef);
    expectLastCall().atLeastOnce();
    BaseObject obj2 = createMock(BaseObject.class);
    obj2.setNumber(0);
    expectLastCall().once();
    obj2.set(eq("blabla"), eq("Blabla Value"), same(context));
    expectLastCall().once();
    DocumentReference specificDocRef = new DocumentReference(db, "Oh", "Noes");
    obj2.setDocumentReference(specificDocRef);
    expectLastCall().atLeastOnce();
    replay(obj, obj2);
    XWikiDocument defaultDoc = new XWikiDocument(fullNameRef);
    Vector<BaseObject> cDobjects = new Vector<>();
    cDobjects.addAll(Arrays.asList(new BaseObject(), obj));
    DocumentReference cdClassRef = new DocumentReference(db, "C", "D");
    defaultDoc.setXObjects(cdClassRef, cDobjects);
    expect(xwiki.exists(eq(fullNameRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(fullNameRef), same(context))).andReturn(defaultDoc).once();
    XWikiDocument specificDoc = new XWikiDocument(specificDocRef);
    Vector<BaseObject> cDobjects2 = new Vector<>();
    cDobjects2.add(obj2);
    DocumentReference abClassRef = new DocumentReference(db, "A", "B");
    specificDoc.setXObjects(abClassRef, cDobjects2);
    DocumentReference doc2Ref = new DocumentReference(db, "Oh", "Noes");
    expect(xwiki.exists(eq(doc2Ref), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(doc2Ref), same(context))).andReturn(specificDoc).once();
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("");
    replayDefault();
    Set<XWikiDocument> changedDocs = docFormCmd.updateDocFromMap(fullNameRef, data);
    verifyDefault(obj, obj2);
    assertTrue(changedDocs.contains(defaultDoc));
    assertEquals(2, defaultDoc.getXObjects(cdClassRef).size());
    assertTrue(changedDocs.contains(specificDoc));
    assertEquals(1, specificDoc.getXObjects(abClassRef).size());
  }

  @Test
  public void testUpdateDocFromMap_newFromTemplate_titleAndContent() throws XWikiException {
    context.setLanguage("de");
    Map<String, String[]> data = new HashMap<>();
    data.put("Oh.Noes_title", new String[] { "Blabla Value" });
    data.put("content", new String[] { "Another Blabla Value" });
    DocumentReference fullNameRef = new DocumentReference(db, "Full", "Name");
    XWikiDocument defaultDoc = new XWikiDocument(fullNameRef);
    DocumentReference specificDocRef = new DocumentReference(db, "Oh", "Noes");
    expect(xwiki.getDocument(eq(fullNameRef), same(context))).andReturn(defaultDoc).once();
    expect(xwiki.exists(eq(fullNameRef), same(context))).andReturn(true).atLeastOnce();
    XWikiDocument specificDoc = new XWikiDocument(specificDocRef);
    DocumentReference doc2Ref = new DocumentReference(db, "Oh", "Noes");
    expect(xwiki.getDocument(eq(doc2Ref), same(context))).andReturn(specificDoc).once();
    expect(xwiki.exists(eq(doc2Ref), same(context))).andReturn(true).atLeastOnce();
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("Templates.MyTempl");
    DocumentReference templRef = new DocumentReference(db, "Templates", "MyTempl");
    XWikiDocument templDoc = new XWikiDocument(templRef);
    expect(xwiki.getDocument(eq(templRef), same(context))).andReturn(templDoc);
    // //TODO is there a better method to match the parameter? -> eq and same do not work
    // // since loadXWikiDoc create a new XWikiDocument
    // expect(store.loadXWikiDoc(isA(XWikiDocument.class),
    // same(context))).andReturn(defaultDoc);
    // expect(store.loadXWikiDoc(isA(XWikiDocument.class),
    // same(context))).andReturn(specificDoc);
    replayDefault();
    Set<XWikiDocument> changedDocs = docFormCmd.updateDocFromMap(fullNameRef, data);
    verifyDefault();
    // TODO Check how it works - getTranslatedDoc seems to create a new object which leads
    // to getContent() and getTitle() to be empty -> does it work correctly anyways?
    assertTrue(changedDocs.contains(defaultDoc));
    assertEquals("de", defaultDoc.getDefaultLanguage());
    assertTrue(changedDocs.contains(specificDoc));
    assertEquals("de", specificDoc.getDefaultLanguage());
  }

  @Test
  public void testUpdateDocFromMap_newFromTemplate() throws XWikiException {
    Map<String, String[]> data = new HashMap<>();
    data.put("Oh.Noes_A.B_0_blabla", new String[] { "Blabla Value" });
    data.put("C.D_1_blabla2", new String[] { "Another Blabla Value" });
    BaseObject obj = createMock(BaseObject.class);
    obj.setNumber(1);
    expectLastCall().once();
    obj.set(eq("blabla2"), eq("Another Blabla Value"), same(context));
    expectLastCall();
    DocumentReference fullNameRef = new DocumentReference(db, "Full", "Name");
    obj.setDocumentReference(fullNameRef);
    expectLastCall().atLeastOnce();
    BaseObject obj2 = createMock(BaseObject.class);
    obj2.setNumber(0);
    expectLastCall().once();
    obj2.set(eq("blabla"), eq("Blabla Value"), same(context));
    expectLastCall().once();
    DocumentReference specificDocRef = new DocumentReference(db, "Oh", "Noes");
    obj2.setDocumentReference(specificDocRef);
    expectLastCall().atLeastOnce();
    replay(obj, obj2);
    XWikiDocument defaultDoc = new XWikiDocument(fullNameRef);
    Vector<BaseObject> cDobjects = new Vector<>();
    cDobjects.addAll(Arrays.asList(new BaseObject(), obj));
    DocumentReference cdClassRef = new DocumentReference(db, "C", "D");
    defaultDoc.setXObjects(cdClassRef, cDobjects);
    DocumentReference doc1Ref = new DocumentReference(db, "Full", "Name");
    expect(xwiki.exists(eq(doc1Ref), same(context))).andReturn(false).atLeastOnce();
    expect(getMock(XWikiDocumentCreator.class).create(eq(doc1Ref), eq("")))
        .andReturn(defaultDoc).once();
    XWikiDocument specificDoc = new XWikiDocument(specificDocRef);
    Vector<BaseObject> cDobjects2 = new Vector<>();
    cDobjects2.add(obj2);
    DocumentReference abClassRef = new DocumentReference(db, "A", "B");
    specificDoc.setXObjects(abClassRef, cDobjects2);
    DocumentReference doc2Ref = new DocumentReference(db, "Oh", "Noes");
    expect(xwiki.getDocument(eq(doc2Ref), same(context))).andReturn(specificDoc).once();
    expect(xwiki.exists(eq(doc2Ref), same(context))).andReturn(true).atLeastOnce();
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("Templates.MyTempl");
    DocumentReference templRef = new DocumentReference(db, "Templates", "MyTempl");
    XWikiDocument templDoc = new XWikiDocument(templRef);
    expect(xwiki.getDocument(eq(templRef), same(context))).andReturn(templDoc);
    replayDefault();
    Set<XWikiDocument> changedDocs = docFormCmd.updateDocFromMap(fullNameRef, data);
    verifyDefault(obj, obj2);
    assertTrue(changedDocs.contains(defaultDoc));
    assertEquals(2, defaultDoc.getXObjects(cdClassRef).size());
    assertTrue(changedDocs.contains(specificDoc));
    assertEquals(1, specificDoc.getXObjects(abClassRef).size());
  }

  @Test
  public void testUpdateDocFromMap_content() throws XWikiException {
    context.setRequest(new XWikiServletRequestStub());
    DocumentReference docRef = new DocumentReference(db, "Sp", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    docFormCmd.getChangedObjects().put("db:Sp.Doc_db:A.B_0", objMock);
    String value = "value";
    Map<String, String[]> data = new HashMap<>();
    data.put("content", new String[] { value });

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).atLeastOnce();

    replayDefault();
    docFormCmd.updateDocFromMap(docRef, data);
    verifyDefault();

    assertEquals(value, doc.getContent());
  }

  @Test
  public void testUpdateDocFromMap_objWithContentField() throws XWikiException {
    context.setRequest(new XWikiServletRequestStub());
    DocumentReference docRef = new DocumentReference(db, "Sp", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    docFormCmd.getChangedObjects().put("db:Sp.Doc_db:A.B_0", objMock);
    String value = "value";
    Map<String, String[]> data = new HashMap<>();
    data.put("A.B_0_content", new String[] { value });

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).atLeastOnce();
    objMock.set(eq("content"), eq(value), same(context));
    expectLastCall().once();

    replayDefault();
    docFormCmd.updateDocFromMap(docRef, data);
    verifyDefault();
  }

  @Test
  public void testUpdateDocFromMap_title() throws XWikiException {
    context.setRequest(new XWikiServletRequestStub());
    DocumentReference docRef = new DocumentReference(db, "Sp", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    docFormCmd.getChangedObjects().put("db:Sp.Doc_db:A.B_0", objMock);
    String value = "value";
    Map<String, String[]> data = new HashMap<>();
    data.put("title", new String[] { value });

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).atLeastOnce();

    replayDefault();
    docFormCmd.updateDocFromMap(docRef, data);
    verifyDefault();

    assertEquals(value, doc.getTitle());
  }

  @Test
  public void testUpdateDocFromMap_objWithTitleField() throws XWikiException {
    context.setRequest(new XWikiServletRequestStub());
    DocumentReference docRef = new DocumentReference(db, "Sp", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    docFormCmd.getChangedObjects().put("db:Sp.Doc_db:A.B_0", objMock);
    String value = "value";
    Map<String, String[]> data = new HashMap<>();
    data.put("A.B_0_title", new String[] { value });

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).anyTimes();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).atLeastOnce();
    objMock.set(eq("title"), eq(value), same(context));
    expectLastCall().once();

    replayDefault();
    docFormCmd.updateDocFromMap(docRef, data);
    verifyDefault();
  }

  @Test
  public void testPrepareMapForDocUpdate() {
    Map<String, Object> inMap = new HashMap<>();
    String key1 = "key1";
    String key2 = "key2";
    String val1 = "Value1";
    String val2 = "Value2";
    inMap.put(key1, val1);
    inMap.put(key2, new String[] { val1, val2 });
    inMap.put("key3", new Object());
    Map<String, String[]> result = docFormCmd.prepareMapForDocUpdate(inMap);
    assertEquals(2, result.size());
    assertEquals(val1, result.get(key1)[0]);
    assertEquals(val1, result.get(key2)[0]);
    assertEquals(val2, result.get(key2)[1]);
  }
}
