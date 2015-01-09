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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.docform.DocFormRequestKey;
import com.celements.docform.DocFormRequestKeyParser;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.XWikiRequest;

public class DocFormCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private DocFormCommand docFormCmd;

  @Before
  public void setUp_DocFormCommandTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    docFormCmd = new DocFormCommand();
  }

  @Test
  public void testCollapse_nullElems() {
    assertEquals("abf|cdf", docFormCmd.collapse(new String[]{"abf", null, "cdf", null}));
  }

  @Test
  public void testCollapse_emptyStrings() {
    assertEquals("", docFormCmd.collapse(new String[]{"",""}));
  }

  @Test
  public void testCollapse_someEmptyStrings() {
    assertEquals("abc|dfg", docFormCmd.collapse(new String[]{"abc","","","dfg"}));
  }

  @Test
  public void testCollapse_trailingEmptyStrings() {
    assertEquals("abc3|df2g", docFormCmd.collapse(new String[]{"","abc3","df2g",""}));
  }

  @Test
  public void testGetUpdateDoc() throws XWikiException {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Full",
      "Name");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    replayDefault();
    XWikiDocument updateDoc = docFormCmd.getUpdateDoc(docRef, context);
    assertNotNull(updateDoc);
    assertEquals(doc, updateDoc);
    verifyDefault();
  }
  
  @Test
  public void setOrRemoveObj_fromCache() throws XWikiException {
    DocumentReference docRef = new DocumentReference("W", "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key = new DocFormRequestKeyParser().parse("A.B_0_hi", docRef);
    String value = "value";
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    docFormCmd.getChangedObjects().put("W:Sp.Doc_W:A.B_0", objMock);
    objMock.set(eq("hi"), eq(value), same(context));
    expectLastCall().once();
    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key, value, context);
    verifyDefault();
  }
  
  @Test
  public void setOrRemoveObj_get() throws XWikiException {
    DocumentReference docRef = new DocumentReference("W", "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key = new DocFormRequestKeyParser().parse("A.B_0_hi", docRef);
    DocumentReference classRef = new DocumentReference("W", "A", "B");
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    expect(docMock.getXObject(eq(classRef), eq(0))).andReturn(objMock).once();
    String value = "value";
    objMock.set(eq("hi"), eq(value), same(context));
    expectLastCall().once();
    
    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key, value, context);
    verifyDefault();
    
    assertEquals(objMock, docFormCmd.getChangedObjects().get("W:Sp.Doc_W:A.B_0"));
  }
  
  // TODO do nothing in this case?
  @Test
  public void setOrRemoveObj_get_notExists() throws XWikiException {
    DocumentReference docRef = new DocumentReference("W", "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key = new DocFormRequestKeyParser().parse("A.B_0_hi", docRef);
    DocumentReference classRef = new DocumentReference("W", "A", "B");
    expect(docMock.getXObject(eq(classRef), eq(0))).andReturn(null).once();
    String value = "value";
    
    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key, value, context);
    verifyDefault();
    
    assertNull(docFormCmd.getChangedObjects().get("W:Sp.Doc_W:A.B_0"));
  }
  
  @Test
  public void setOrRemoveObj_create() throws XWikiException {
    DocumentReference docRef = new DocumentReference("W", "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key1 = new DocFormRequestKeyParser().parse("A.B_-1_hi", docRef);
    DocumentReference classRef1 = new DocumentReference("W", "A", "B");
    BaseObject objMock1 = createMockAndAddToDefault(BaseObject.class);
    expect(docMock.getXObject(eq(classRef1), eq(-1))).andReturn(null).once();
    expect(docMock.newXObject(eq(classRef1), same(context))).andReturn(objMock1).once();
    String value1 = "value1";
    objMock1.set(eq("hi"), eq(value1), same(context));
    expectLastCall();
    DocFormRequestKey key2 = new DocFormRequestKeyParser().parse("B.C_0_bla", docRef);
    String value2 = "value2";
    BaseObject objMock2 = createMockAndAddToDefault(BaseObject.class);
    docFormCmd.getChangedObjects().put("W:Sp.Doc_W:B.C_0", objMock2);
    objMock2.set(eq("bla"), eq(value2), same(context));
    expectLastCall();
    
    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key1, value1, context);
    docFormCmd.setOrRemoveObj(docMock, key2, value2, context);
    verifyDefault();
    
    assertEquals(objMock1, docFormCmd.getChangedObjects().get("W:Sp.Doc_W:A.B_-1"));
    assertEquals(objMock2, docFormCmd.getChangedObjects().get("W:Sp.Doc_W:B.C_0"));
  }
  
  @Test
  public void setOrRemoveObj_remove_fromCache() throws XWikiException {
    DocumentReference docRef = new DocumentReference("W", "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key = new DocFormRequestKeyParser().parse("A.B_^0", docRef);
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    docFormCmd.getChangedObjects().put("W:Sp.Doc_W:A.B_0", objMock);
    expect(docMock.removeXObject(same(objMock))).andReturn(true).once();
    
    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key, "", context);
    verifyDefault();
    
    assertEquals(objMock, docFormCmd.getChangedObjects().get("W:Sp.Doc_W:A.B_0"));
  }
  
  @Test
  public void setOrRemoveObj_remove() throws XWikiException {
    DocumentReference docRef = new DocumentReference("W", "Sp", "Doc");
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocFormRequestKey key = new DocFormRequestKeyParser().parse("A.B_^0", docRef);
    DocumentReference classRef = new DocumentReference("W", "A", "B");
    BaseObject objMock = createMockAndAddToDefault(BaseObject.class);
    expect(docMock.getXObject(eq(classRef), eq(0))).andReturn(objMock).once();
    expect(docMock.removeXObject(same(objMock))).andReturn(true).once();
    
    replayDefault();
    docFormCmd.setOrRemoveObj(docMock, key, "", context);
    verifyDefault();
    
    assertEquals(objMock, docFormCmd.getChangedObjects().get("W:Sp.Doc_W:A.B_0"));
  }

  @Test
  public void testValidateField() throws XWikiException {
    BaseProperty regexProp = new StringProperty();
    regexProp.setValue("/.{1,5}/");
    BaseProperty msgProp = new StringProperty();
    msgProp.setValue("msg");
    Map<String, BaseProperty> propfields = new HashMap<String, BaseProperty>();
    PropertyClass property = new PropertyClass();
    propfields.put("validationRegExp", regexProp);
    propfields.put("validationMessage", msgProp);
    property.setFields(propfields);
    Map<String, PropertyClass> fields = new HashMap<String, PropertyClass>();
    fields.put("testField", property);
    BaseClass bclass = new BaseClass();
    bclass.setFields(fields);
    DocumentReference bclassDocRef = new DocumentReference(context.getDatabase(), "Test",
        "TestClass");
    XWikiDocument doc = new XWikiDocument(bclassDocRef);
    doc.setXClass(bclass);
    expect(xwiki.getDocument(eq(bclassDocRef), same(context))).andReturn(doc
        ).atLeastOnce();
    replayDefault();
    String result = docFormCmd.validateField("Test.TestClass", "testField", "value",
        context);
    assertNull("Successful validation should result in answoer null", result);
    result = docFormCmd.validateField("Test.TestClass", "testField", "", context);
    assertEquals("msg", result);
    verifyDefault();
  }
  
  @Test
  public void testGetFieldFromProperty() throws XWikiException{
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
    assertTrue("Space.Doc_0_field_one".matches(
        docFormCmd.getFindObjectFieldInRequestRegex()));
    assertTrue("Space.Doc_Class.Name_3_field".matches(
        docFormCmd.getFindObjectFieldInRequestRegex()));
    assertTrue("Space.Doc_Class.Name_-3_field".matches(
        docFormCmd.getFindObjectFieldInRequestRegex()));
    assertTrue("Space.Doc_Class.Name_^3_field".matches(
        docFormCmd.getFindObjectFieldInRequestRegex()));
  }
  
  @Test
  public void testApplyCreationDateFix() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext(
        ).getDatabase(), "Test", "Doc"));
    doc.setCreationDate(new Date(0));
    doc.setCreator("");
    getContext().setUser("Hans.Wurscht");
    docFormCmd.applyCreationDateFix(doc, getContext());
    assertTrue(doc.getCreationDate().getTime() > 0);
    assertEquals("Hans.Wurscht", doc.getCreator());
  }

  @Test
  public void testUpdateDocFromMap() throws XWikiException {
    Map<String, String[]> data = new HashMap<String, String[]>();
    data.put("Oh.Noes_A.B_0_blabla", new String[]{"Blabla Value"});
    data.put("C.D_1_blabla2", new String[]{"Another Blabla Value"});
    BaseObject obj = createMock(BaseObject.class);
    obj.setNumber(1);
    expectLastCall().once();
    obj.set(eq("blabla2"), eq("Another Blabla Value"), same(context));
    expectLastCall();
    DocumentReference fullNameRef = new DocumentReference(context.getDatabase(), "Full",
      "Name");
    obj.setDocumentReference(fullNameRef);
    expectLastCall().atLeastOnce();
    BaseObject obj2 = createMock(BaseObject.class);
    obj2.setNumber(0);
    expectLastCall().once();
    obj2.set(eq("blabla"), eq("Blabla Value"), same(context));
    expectLastCall().once();
    DocumentReference specificDocRef = new DocumentReference(context.getDatabase(), "Oh",
      "Noes");
    obj2.setDocumentReference(specificDocRef);
    expectLastCall().atLeastOnce();
    replay(obj, obj2);
    XWikiDocument defaultDoc = new XWikiDocument(fullNameRef);
    Vector<BaseObject> cDobjects = new Vector<BaseObject>();
    cDobjects.addAll(Arrays.asList(new BaseObject(), obj));
    DocumentReference cdClassRef = new DocumentReference(context.getDatabase(), "C", "D");
    defaultDoc.setXObjects(cdClassRef, cDobjects);
    expect(xwiki.getDocument(eq(fullNameRef), same(context))).andReturn(defaultDoc
        ).times(2);
    XWikiDocument specificDoc = new XWikiDocument(specificDocRef);
    Vector<BaseObject> cDobjects2 = new Vector<BaseObject>();
    cDobjects2.add(obj2);
    DocumentReference abClassRef = new DocumentReference(context.getDatabase(), "A", "B");
    specificDoc.setXObjects(abClassRef, cDobjects2);
    DocumentReference doc2Ref = new DocumentReference(context.getDatabase(), "Oh",
      "Noes");
    expect(xwiki.getDocument(eq(doc2Ref), same(context))).andReturn(specificDoc
        ).once();
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.getParameter(eq("template"))).andReturn("");
    replayDefault();
    Set<XWikiDocument> changedDocs = docFormCmd.updateDocFromMap("Full.Name", 
        data, context);
    verifyDefault(obj, obj2);
    assertTrue(changedDocs.contains(defaultDoc));
    assertEquals(2, defaultDoc.getXObjects(cdClassRef).size());
    assertTrue(changedDocs.contains(specificDoc));
    assertEquals(1, specificDoc.getXObjects(abClassRef).size());
  }

  @Test
  public void testUpdateDocFromMap_newFromTemplate_titleAndContent() 
      throws XWikiException {
    context.setLanguage("de");
    Map<String, String[]> data = new HashMap<String, String[]>();
    data.put("Oh.Noes_title", new String[]{"Blabla Value"});
    data.put("content", new String[]{"Another Blabla Value"});
    DocumentReference fullNameRef = new DocumentReference(context.getDatabase(), "Full",
      "Name");
    XWikiDocument defaultDoc = new XWikiDocument(fullNameRef);
    DocumentReference specificDocRef = new DocumentReference(context.getDatabase(), "Oh",
      "Noes");
    expect(xwiki.getDocument(eq(fullNameRef), same(context))).andReturn(defaultDoc
        ).times(2);
    XWikiDocument specificDoc = new XWikiDocument(specificDocRef);
    DocumentReference doc2Ref = new DocumentReference(context.getDatabase(), "Oh",
      "Noes");
    expect(xwiki.getDocument(eq(doc2Ref), same(context))).andReturn(specificDoc
        ).once();
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.getParameter(eq("template"))).andReturn("Templates.MyTempl");
    DocumentReference templRef = new DocumentReference(context.getDatabase(), "Templates",
        "MyTempl");
    XWikiDocument templDoc = new XWikiDocument(templRef);
    expect(xwiki.getDocument(eq(templRef), same(context))).andReturn(templDoc);
    XWikiStoreInterface store = createMockAndAddToDefault(XWikiStoreInterface.class);
//    //TODO is there a better method to match the parameter? -> eq and same do not work
//    //     since loadXWikiDoc create a new XWikiDocument
//    expect(store.loadXWikiDoc(isA(XWikiDocument.class), same(context))).andReturn(defaultDoc);
//    expect(store.loadXWikiDoc(isA(XWikiDocument.class), same(context))).andReturn(specificDoc);
    expect(xwiki.getStore()).andReturn(store).anyTimes();
    replayDefault();
    Set<XWikiDocument> changedDocs = docFormCmd.updateDocFromMap("Full.Name", data,
        context);
    verifyDefault();
    //TODO Check how it works - getTranslatedDoc seems to create a new object which leads
    //     to getContent() and getTitle() to be empty -> does it work correctly anyways?
    assertTrue(changedDocs.contains(defaultDoc));
    assertEquals("de", defaultDoc.getDefaultLanguage());
    assertTrue(changedDocs.contains(specificDoc));
    assertEquals("de", specificDoc.getDefaultLanguage());
  }

  @Test
  public void testUpdateDocFromMap_newFromTemplate() throws XWikiException {
    Map<String, String[]> data = new HashMap<String, String[]>();
    data.put("Oh.Noes_A.B_0_blabla", new String[]{"Blabla Value"});
    data.put("C.D_1_blabla2", new String[]{"Another Blabla Value"});
    BaseObject obj = createMock(BaseObject.class);
    obj.setNumber(1);
    expectLastCall().once();
    obj.set(eq("blabla2"), eq("Another Blabla Value"), same(context));
    expectLastCall();
    DocumentReference fullNameRef = new DocumentReference(context.getDatabase(), "Full",
      "Name");
    obj.setDocumentReference(fullNameRef);
    expectLastCall().atLeastOnce();
    BaseObject obj2 = createMock(BaseObject.class);
    obj2.setNumber(0);
    expectLastCall().once();
    obj2.set(eq("blabla"), eq("Blabla Value"), same(context));
    expectLastCall().once();
    DocumentReference specificDocRef = new DocumentReference(context.getDatabase(), "Oh",
      "Noes");
    obj2.setDocumentReference(specificDocRef);
    expectLastCall().atLeastOnce();
    replay(obj, obj2);
    XWikiDocument defaultDoc = new XWikiDocument(fullNameRef);
    Vector<BaseObject> cDobjects = new Vector<BaseObject>();
    cDobjects.addAll(Arrays.asList(new BaseObject(), obj));
    DocumentReference cdClassRef = new DocumentReference(context.getDatabase(), "C", "D");
    defaultDoc.setXObjects(cdClassRef, cDobjects);
    DocumentReference doc1Ref = new DocumentReference(context.getDatabase(), "Full",
      "Name");
    expect(xwiki.getDocument(eq(doc1Ref), same(context))).andReturn(defaultDoc
        ).times(2);
    XWikiDocument specificDoc = new XWikiDocument(specificDocRef);
    Vector<BaseObject> cDobjects2 = new Vector<BaseObject>();
    cDobjects2.add(obj2);
    DocumentReference abClassRef = new DocumentReference(context.getDatabase(), "A", "B");
    specificDoc.setXObjects(abClassRef, cDobjects2);
    DocumentReference doc2Ref = new DocumentReference(context.getDatabase(), "Oh",
      "Noes");
    expect(xwiki.getDocument(eq(doc2Ref), same(context))).andReturn(specificDoc
        ).once();
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.getParameter(eq("template"))).andReturn("Templates.MyTempl");
    DocumentReference templRef = new DocumentReference(context.getDatabase(), "Templates",
        "MyTempl");
    XWikiDocument templDoc = new XWikiDocument(templRef);
    expect(xwiki.getDocument(eq(templRef), same(context))).andReturn(templDoc);
    replayDefault();
    Set<XWikiDocument> changedDocs = docFormCmd.updateDocFromMap("Full.Name", 
        data, context);
    verifyDefault(obj, obj2);
    assertTrue(changedDocs.contains(defaultDoc));
    assertEquals(2, defaultDoc.getXObjects(cdClassRef).size());
    assertTrue(changedDocs.contains(specificDoc));
    assertEquals(1, specificDoc.getXObjects(abClassRef).size());
  }

}
