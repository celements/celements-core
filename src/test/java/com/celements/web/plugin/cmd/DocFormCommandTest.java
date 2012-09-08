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
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    docFormCmd = new DocFormCommand();
  }

  @Test
  public void testGetDocFullname_getDefault() {
    assertArrayEquals(new String[]{"Full", "Name", "Oh.Noes_0_blabla"},
        docFormCmd.getDocFullname("Oh.Noes_0_blabla", "Full", "Name"));
  }
  
  @Test
  public void testGetDocFullname_title() {
    assertArrayEquals(new String[]{"Oh", "Noes", "title"}, 
        docFormCmd.getDocFullname("Oh.Noes_title", "Full", "Name"));
  }
  
  @Test
  public void testGetDocFullname_defaultDocContent() {
    assertArrayEquals(new String[]{"Full", "Name", "content"}, 
        docFormCmd.getDocFullname("content", "Full", "Name"));
  }
  
  @Test
  public void testGetDocFullname_content() {
    assertArrayEquals(new String[]{"Oh", "Noes", "content"}, 
        docFormCmd.getDocFullname("Oh.Noes_content", "Full", "Name"));
  }
  
  @Test
  public void testGetDocFullname_objField() {
    assertArrayEquals(new String[]{"Oh", "Noes", "A.B_52_bla_40bla"}, 
        docFormCmd.getDocFullname("Oh.Noes_A.B_52_bla_40bla", "Full", "Name"));
  }
  
  @Test
  public void testGetDocFullname_objFieldWithDoc() {
    assertArrayEquals(new String[]{"Full", "Name", "Oh.Noes_52_bla_40bla"},
        docFormCmd.getDocFullname("Oh.Noes_52_bla_40bla", "Full", "Name"));
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
    replayAll();
    XWikiDocument updateDoc = docFormCmd.getUpdateDoc(docRef, context);
    assertNotNull(updateDoc);
    assertEquals(doc, updateDoc);
    verifyAll();
  }
  
  @Test
  public void setObjValue_fillMap() throws XWikiException {
    DocumentReference docRef = new DocumentReference(context.getDatabase(),
        "Sp", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject obj = createMock(BaseObject.class);
    obj.setDocumentReference(eq(docRef));
    expectLastCall().atLeastOnce();
    obj.setNumber(eq(0));
    expectLastCall().once();
    expect(obj.getNumber()).andReturn(0).atLeastOnce();
    expect(obj.getXClassReference()).andReturn(new DocumentReference(
        context.getDatabase(), "A", "B")).atLeastOnce();
    obj.set(eq("hi"), eq("value"), same(context));
    expectLastCall().once();
    replay(obj);
    doc.addXObject(obj);
    replayAll();
    String key = "A.B_0_hi";
    String[] value = new String[]{ "value" };
    docFormCmd.setObjValue(doc, key, value, context);
    assertEquals(obj, docFormCmd.getChangedObjects().get("Sp.Doc_A.B_0"));
    verifyAll(obj);
  }
  
  @Test
  public void setObjValue_one_object() throws XWikiException {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Sp", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    String key = "A.B_0_hi";
    String[] value = new String[]{ "value" };
    BaseObject obj = createMock(BaseObject.class);
    docFormCmd.getChangedObjects().put("Sp.Doc_A.B_0", obj);
    obj.set(eq("hi"), eq(value[0]), same(context));
    expectLastCall();
    replayAll(obj);
    docFormCmd.setObjValue(doc, key, value, context);
    verifyAll(obj);
  }
  
  @Test
  public void setObjValue_two_objects() throws XWikiException {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Sp", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    String key = "B.C_0_bla";
    String[] value = new String[]{ "value" };
    BaseObject obj = createMock(BaseObject.class);
    docFormCmd.getChangedObjects().put("Sp.Doc_A.B_0", null);
    docFormCmd.getChangedObjects().put("Sp.Doc_B.C_0", obj);
    obj.set(eq("bla"), eq(value[0]), same(context));
    expectLastCall();
    replayAll(obj);
    docFormCmd.setObjValue(doc, key, value, context);
    verifyAll(obj);
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
    replayAll();
    String result = docFormCmd.validateField("Test.TestClass", "testField", "value",
        context);
    assertNull("Successful validation should result in answoer null", result);
    result = docFormCmd.validateField("Test.TestClass", "testField", "", context);
    assertEquals("msg", result);
    verifyAll();
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
  }
  
  @Test
  public void testGetFindObjectFieldInRequestRegex_match() {
    assertTrue("Space.Doc_0_field_one".matches(
        docFormCmd.getFindObjectFieldInRequestRegex()));
    assertTrue("Space.Doc_Class.Name_3_field".matches(
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
    expect(obj.getNumber()).andReturn(1).atLeastOnce();
    obj.set(eq("blabla2"), eq("Another Blabla Value"), same(context));
    expectLastCall();
    DocumentReference fullNameRef = new DocumentReference(context.getDatabase(), "Full",
      "Name");
    obj.setDocumentReference(fullNameRef);
    expectLastCall().atLeastOnce();
    BaseObject obj2 = createMock(BaseObject.class);
    obj2.setNumber(0);
    expectLastCall().once();
    expect(obj2.getNumber()).andReturn(0).atLeastOnce();
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
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    expect(request.getParameter(eq("template"))).andReturn("");
    replayAll(request);
    Set<XWikiDocument> changedDocs = docFormCmd.updateDocFromMap("Full.Name", 
        data, context);
    verifyAll(obj, obj2, request);
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
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    expect(request.getParameter(eq("template"))).andReturn("Templates.MyTempl");
    DocumentReference templRef = new DocumentReference(context.getDatabase(), "Templates",
        "MyTempl");
    XWikiDocument templDoc = new XWikiDocument(templRef);
    expect(xwiki.getDocument(eq(templRef), same(context))).andReturn(templDoc);
    XWikiStoreInterface store = createMock(XWikiStoreInterface.class);
//    //TODO is there a better method to match the parameter? -> eq and same do not work
//    //     since loadXWikiDoc create a new XWikiDocument
//    expect(store.loadXWikiDoc(isA(XWikiDocument.class), same(context))).andReturn(defaultDoc);
//    expect(store.loadXWikiDoc(isA(XWikiDocument.class), same(context))).andReturn(specificDoc);
    expect(xwiki.getStore()).andReturn(store).anyTimes();
    replayAll(store, request);
    Set<XWikiDocument> changedDocs = docFormCmd.updateDocFromMap("Full.Name", data,
        context);
    verifyAll(store, request);
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
    expect(obj.getNumber()).andReturn(1).atLeastOnce();
    obj.set(eq("blabla2"), eq("Another Blabla Value"), same(context));
    expectLastCall();
    DocumentReference fullNameRef = new DocumentReference(context.getDatabase(), "Full",
      "Name");
    obj.setDocumentReference(fullNameRef);
    expectLastCall().atLeastOnce();
    BaseObject obj2 = createMock(BaseObject.class);
    obj2.setNumber(0);
    expectLastCall().once();
    expect(obj2.getNumber()).andReturn(0).atLeastOnce();
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
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    expect(request.getParameter(eq("template"))).andReturn("Templates.MyTempl");
    DocumentReference templRef = new DocumentReference(context.getDatabase(), "Templates",
        "MyTempl");
    XWikiDocument templDoc = new XWikiDocument(templRef);
    expect(xwiki.getDocument(eq(templRef), same(context))).andReturn(templDoc);
    replayAll(request);
    Set<XWikiDocument> changedDocs = docFormCmd.updateDocFromMap("Full.Name", 
        data, context);
    verifyAll(obj, obj2, request);
    assertTrue(changedDocs.contains(defaultDoc));
    assertEquals(2, defaultDoc.getXObjects(cdClassRef).size());
    assertTrue(changedDocs.contains(specificDoc));
    assertEquals(1, specificDoc.getXObjects(abClassRef).size());
  }

  
  private void replayAll(Object ... mocks) {
    replay(xwiki);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki);
    verify(mocks);
  }
}
