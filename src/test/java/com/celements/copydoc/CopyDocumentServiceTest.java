package com.celements.copydoc;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.NumberClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.web.Utils;

public class CopyDocumentServiceTest extends AbstractBridgedComponentTestCase {

  private CopyDocumentService copyDocService;
  private XWikiContext context;
  private XWiki xwiki;
  
  private List<BaseObject> emptyList;
  private DocumentReference docRef;
  private XWikiDocument docMock;
  private DocumentReference classRef;
  private BaseClass bClass;

  @Before
  public void setUp_CopyDocumentServiceTest() throws Exception {
    copyDocService = (CopyDocumentService) Utils.getComponent(ICopyDocumentRole.class);
    context = getContext();
    xwiki = getWikiMock();
    emptyList = Collections.emptyList();
    docRef = new DocumentReference("wiki", "Space", "SomeDoc");
    docMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(docMock.getDocumentReference()).andReturn(docRef).anyTimes();
    classRef = new DocumentReference("wiki", "Classes", "SomeClass");
    bClass = createMockAndAddToDefault(BaseClass.class);
    expect(xwiki.getXClass(eq(classRef), same(context))).andReturn(bClass).anyTimes();
  }
  
  @Test
  public void testCopyObjects() throws Exception {
//    TODO
//    boolean set = false;
//    XWikiDocument srcDocMock = createMockAndAddToDefault(XWikiDocument.class);
//    
//    replayDefault();
//    boolean ret = copyDocService.copyObjects(srcDocMock, docMock, set);
//    verifyDefault();
//    
//    assertFalse(ret);
  }
  
  @Test
  public void testGetAsTargetClassRef_sameDB() {
    DocumentReference trgDocRef = new DocumentReference("wiki", "Space", "SomeDoc");
    XWikiDocument trgDoc = new XWikiDocument(trgDocRef);
    DocumentReference trgClassRef = copyDocService.getAsTargetClassRef(classRef, trgDoc);
    assertEquals(classRef, trgClassRef);
  }
  
  @Test
  public void testGetAsTargetClassRef_otherDB() {
    DocumentReference srcClassRef = new DocumentReference("wikiA", "Classes", "SomeClass");
    DocumentReference trgDocRef = new DocumentReference("wikiB", "Space", "SomeDoc");
    XWikiDocument trgDoc = new XWikiDocument(trgDocRef);
    DocumentReference trgClassRef = copyDocService.getAsTargetClassRef(srcClassRef, trgDoc);
    assertFalse(srcClassRef.equals(trgClassRef));
    assertEquals(srcClassRef.getName(), trgClassRef.getName());
    assertEquals(srcClassRef.getLastSpaceReference().getName(), 
        trgClassRef.getLastSpaceReference().getName());
    assertEquals("wikiA", srcClassRef.getLastSpaceReference().getParent().getName());
    assertEquals("wikiB", trgClassRef.getLastSpaceReference().getParent().getName());
  }
  
  @Test
  public void testCreateOrUpdateObjects_none() throws Exception {
    boolean set = false;
    BaseObject obj = new BaseObject();
    
    replayDefault();
    boolean ret = copyDocService.createOrUpdateObjects(docMock, classRef, 
        emptyList.iterator(), Arrays.asList(obj).iterator(), set);
    verifyDefault();
    
    assertFalse(ret);
  }
  
  @Test
  public void testCreateOrUpdateObjects_create() throws Exception {
    boolean set = false;
    BaseObject obj = new BaseObject();
    
    
    replayDefault();
    boolean ret = copyDocService.createOrUpdateObjects(docMock, classRef, Arrays.asList(
        obj).iterator(), emptyList.iterator(), set);
    verifyDefault();
    
    assertTrue(ret);
  }
  
  @Test
  public void testCreateOrUpdateObjects_update() throws Exception {
    boolean set = false;
    BaseObject srcObj = new BaseObject();
    String name = "name";
    String val = "val";
    srcObj.setStringValue(name, val);
    BaseObject trgObj = new BaseObject();
    
    replayDefault();
    boolean ret = copyDocService.createOrUpdateObjects(docMock, classRef, Arrays.asList(
        srcObj).iterator(), Arrays.asList(trgObj).iterator(), set);
    verifyDefault();
    
    assertTrue(ret);
    assertEquals("", trgObj.getStringValue(name));
  }
  
  @Test
  public void testCreateOrUpdateObjects_set_none() throws Exception {
    boolean set = true;
    BaseObject obj = new BaseObject();
    
    replayDefault();
    boolean ret = copyDocService.createOrUpdateObjects(docMock, classRef, 
        emptyList.iterator(), Arrays.asList(obj).iterator(), set);
    verifyDefault();
    
    assertFalse(ret);
  }
  
  @Test
  public void testCreateOrUpdateObjects_set_create() throws Exception {
    boolean set = true;
    BaseObject obj = new BaseObject();
    
    expect(docMock.newXObject(eq(classRef), same(context))).andReturn(obj).once();
    
    replayDefault();
    boolean ret = copyDocService.createOrUpdateObjects(docMock, classRef, Arrays.asList(
        obj).iterator(), emptyList.iterator(), set);
    verifyDefault();
    
    assertTrue(ret);
  }
  
  @Test
  public void testCreateOrUpdateObjects_set_update() throws Exception {
    boolean set = true;
    BaseObject srcObj = new BaseObject();
    srcObj.setXClassReference(classRef);
    String name = "name";
    String val = "val";
    srcObj.setStringValue(name, val);
    BaseObject trgObj = new BaseObject();
    trgObj.setXClassReference(classRef);
    PropertyInterface propClass = new StringClass();
    
    expect(bClass.get(eq(name))).andReturn(propClass).once();
    
    replayDefault();
    boolean ret = copyDocService.createOrUpdateObjects(docMock, classRef, Arrays.asList(
        srcObj).iterator(), Arrays.asList(trgObj).iterator(), set);
    verifyDefault();
    
    assertTrue(ret);
    assertEquals(val, trgObj.getStringValue(name));
  }
  
  @Test
  public void testRemoveObjects_noObjs() {
    boolean set = false;
    
    replayDefault();
    boolean ret = copyDocService.removeObjects(docMock, emptyList.iterator(), set);
    verifyDefault();
    
    assertFalse(ret);
  }
  
  @Test
  public void testRemoveObjects_notChanged_null() {
    boolean set = false;
    BaseObject obj1 = new BaseObject();
    obj1.setXClassReference(classRef);
    BaseObject obj2 = new BaseObject();
    obj2.setXClassReference(classRef);
    
    expect(docMock.getXObjects(eq(classRef))).andReturn(null).times(2);
    
    replayDefault();
    boolean ret = copyDocService.removeObjects(docMock, Arrays.asList(obj1, obj2
        ).iterator(), set);
    verifyDefault();
    
    assertFalse(ret);
  }
  
  @Test
  public void testRemoveObjects_notChanged_otherObj() {
    boolean set = false;
    BaseObject obj1 = new BaseObject();
    obj1.setXClassReference(classRef);
    BaseObject obj2 = new BaseObject();
    obj2.setXClassReference(classRef);
    
    expect(docMock.getXObjects(eq(classRef))).andReturn(Arrays.asList(new BaseObject())
        ).times(2);
    
    replayDefault();
    boolean ret = copyDocService.removeObjects(docMock, Arrays.asList(obj1, obj2
        ).iterator(), set);
    verifyDefault();
    
    assertFalse(ret);
  }
  
  @Test
  public void testRemoveObjects_changed() {
    boolean set = false;
    BaseObject obj1 = new BaseObject();
    obj1.setXClassReference(classRef);
    BaseObject obj2 = new BaseObject();
    obj2.setXClassReference(classRef);
    
    expect(docMock.getXObjects(eq(classRef))).andReturn(Arrays.asList(obj2)).times(2);
    
    replayDefault();
    boolean ret = copyDocService.removeObjects(docMock, Arrays.asList(obj1, obj2
        ).iterator(), set);
    verifyDefault();
    
    assertTrue(ret);
  }
  
  @Test
  public void testRemoveObjects_set_notChanged() {
    boolean set = true;
    BaseObject obj1 = new BaseObject();
    obj1.setXClassReference(classRef);
    BaseObject obj2 = new BaseObject();
    obj2.setXClassReference(classRef);
    
    expect(docMock.removeXObject(same(obj1))).andReturn(false).once();
    expect(docMock.removeXObject(same(obj2))).andReturn(false).once();
    
    replayDefault();
    boolean ret = copyDocService.removeObjects(docMock, Arrays.asList(obj1, obj2
        ).iterator(), set);
    verifyDefault();
    
    assertFalse(ret);
  }
  
  @Test
  public void testRemoveObjects_set_canged() {
    boolean set = true;
    BaseObject obj1 = new BaseObject();
    obj1.setXClassReference(classRef);
    BaseObject obj2 = new BaseObject();
    obj2.setXClassReference(classRef);
    
    expect(docMock.removeXObject(same(obj1))).andReturn(false).once();
    expect(docMock.removeXObject(same(obj2))).andReturn(true).once();
    
    replayDefault();
    boolean ret = copyDocService.removeObjects(docMock, Arrays.asList(obj1, obj2
        ).iterator(), set);
    verifyDefault();
    
    assertTrue(ret);
  }
  
  @Test
  public void testRemoveRemainingObjects_noClassRefs() {
    boolean set = false;
    
    replayDefault();
    boolean ret = copyDocService.removeRemainingObjects(docMock, 
        Collections.<DocumentReference>emptyList(), set);
    verifyDefault();
    
    assertFalse(ret);
  }
  
  @Test
  public void testRemoveRemainingObjects_notChanged() {
    boolean set = false;
    DocumentReference classRef1 = new DocumentReference("wiki", "Classes", "Class1");
    DocumentReference classRef2 = new DocumentReference("wiki", "Classes", "Class2");
    
    expect(docMock.getXObjects(eq(classRef1))).andReturn(null).once();
    expect(docMock.getXObjects(eq(classRef2))).andReturn(emptyList).once();
    
    replayDefault();
    boolean ret = copyDocService.removeRemainingObjects(docMock, Arrays.asList(classRef1, 
        classRef2), set);
    verifyDefault();
    
    assertFalse(ret);
  }
  
  @Test
  public void testRemoveRemainingObjects_changed() {
    boolean set = false;
    DocumentReference classRef1 = new DocumentReference("wiki", "Classes", "Class1");
    DocumentReference classRef2 = new DocumentReference("wiki", "Classes", "Class2");
    
    expect(docMock.getXObjects(eq(classRef1))).andReturn(null).once();
    expect(docMock.getXObjects(eq(classRef2))).andReturn(Arrays.asList(new BaseObject())
        ).once();
    
    replayDefault();
    boolean ret = copyDocService.removeRemainingObjects(docMock, Arrays.asList(classRef1, 
        classRef2), set);
    verifyDefault();
    
    assertTrue(ret);
  }
  
  @Test
  public void testRemoveRemainingObjects_set_notChanged() {
    boolean set = true;
    DocumentReference classRef1 = new DocumentReference("wiki", "Classes", "Class1");
    DocumentReference classRef2 = new DocumentReference("wiki", "Classes", "Class2");
    
    expect(docMock.removeXObjects(eq(classRef1))).andReturn(false).once();
    expect(docMock.removeXObjects(eq(classRef2))).andReturn(false).once();
    
    replayDefault();
    boolean ret = copyDocService.removeRemainingObjects(docMock, Arrays.asList(classRef1, 
        classRef2), set);
    verifyDefault();
    
    assertFalse(ret);
  }
  
  @Test
  public void testRemoveRemainingObjects_set_changed() {
    boolean set = true;
    DocumentReference classRef1 = new DocumentReference("wiki", "Classes", "Class1");
    DocumentReference classRef2 = new DocumentReference("wiki", "Classes", "Class2");
    
    expect(docMock.removeXObjects(eq(classRef1))).andReturn(false).once();
    expect(docMock.removeXObjects(eq(classRef2))).andReturn(true).once();
    
    replayDefault();
    boolean ret = copyDocService.removeRemainingObjects(docMock, Arrays.asList(classRef1, 
        classRef2), set);
    verifyDefault();
    
    assertTrue(ret);
  }
  
  @Test
  public void testCopyObject_added() throws Exception {
    boolean set = true;
    BaseObject srcObj = new BaseObject();
    srcObj.setXClassReference(classRef);
    String name1 = "name1";
    String val1 = "val1";
    srcObj.setStringValue(name1, val1);
    String name2 = "name2";
    Date val2 = new Date();
    srcObj.setDateValue(name2, val2);
    BaseObject trgObj = new BaseObject();
    trgObj.setXClassReference(classRef);
    
    expect(bClass.get(eq(name1))).andReturn(new StringClass()).once();
    expect(bClass.get(eq(name2))).andReturn(new DateClass()).once();
    
    replayDefault();
    boolean ret = copyDocService.copyObject(srcObj, trgObj, set);
    verifyDefault();
    
    assertTrue(ret);
    assertEquals(2, trgObj.getFieldList().size());
    assertEquals(val1, ((BaseProperty) trgObj.get(name1)).getValue());
    assertEquals(val2, ((BaseProperty) trgObj.get(name2)).getValue());
  }
  
  @Test
  public void testCopyObject_removed() throws Exception {
    boolean set = true;
    BaseObject srcObj = new BaseObject();
    srcObj.setXClassReference(classRef);
    String name1 = "name1";
    String val1 = "val1";
    srcObj.setStringValue(name1, val1);
    BaseObject trgObj = new BaseObject();
    trgObj.setXClassReference(classRef);
    trgObj.setStringValue(name1, val1);
    String name2 = "name2";
    String val2 = "val2";
    trgObj.setStringValue(name2, val2);
    
    assertEquals(2, trgObj.getFieldList().size());
    
    replayDefault();
    boolean ret = copyDocService.copyObject(srcObj, trgObj, set);
    verifyDefault();
    
    assertTrue(ret);
    assertEquals(1, trgObj.getFieldList().size());
    assertEquals(val1, ((BaseProperty) trgObj.get(name1)).getValue());
    assertNull(trgObj.get(name2));
  }
  
  @Test
  public void testCopyObject_noChange() throws Exception {
    boolean set = true;
    BaseObject srcObj = new BaseObject();
    srcObj.setXClassReference(classRef);
    String name = "name";
    String val = "val";
    srcObj.setStringValue(name, val);
    BaseObject trgObj = new BaseObject();
    trgObj.setXClassReference(classRef);
    trgObj.setStringValue(name, val);
    
    replayDefault();
    boolean ret = copyDocService.copyObject(srcObj, trgObj, set);
    verifyDefault();

    assertFalse(ret);
    assertEquals(1, trgObj.getFieldList().size());
    assertEquals(val, ((BaseProperty) trgObj.get(name)).getValue());
  }
  
  @Test
  public void testGetValue_String() throws Exception {
    BaseObject obj = new BaseObject();
    String name = "name";
    String val = "val";
    obj.setStringValue(name, val);
    
    replayDefault();
    Object ret = copyDocService.getValue(obj, name);
    verifyDefault();

    assertEquals(val, ret);
  }
  
  @Test
  public void testGetValue_String_emptyString() throws Exception {
    BaseObject obj = new BaseObject();
    String name = "name";
    String val = "";
    obj.setStringValue(name, val);
    
    replayDefault();
    Object ret = copyDocService.getValue(obj, name);
    verifyDefault();

    assertNull(ret);
  }
  
  @Test
  public void testGetValue_Number() throws Exception {
    BaseObject obj = new BaseObject();
    String name = "name";
    int val = 5;
    obj.setIntValue(name, val);
    
    replayDefault();
    Object ret = copyDocService.getValue(obj, name);
    verifyDefault();

    assertEquals(val, ret);
  }
  
  @Test
  public void testGetValue_Date() throws Exception {
    BaseObject obj = new BaseObject();
    String name = "name";
    Date val = new Date();
    obj.setDateValue(name, val);
    
    replayDefault();
    Object ret = copyDocService.getValue(obj, name);
    verifyDefault();

    assertEquals(val, ret);
  }
  
  @Test
  public void testGetValue_Date_Timestamp() throws Exception {
    BaseObject obj = new BaseObject();
    String name = "name";
    Date date = new Date();
    Timestamp val = new Timestamp(date.getTime());
    obj.setDateValue(name, val);
    
    replayDefault();
    Object ret = copyDocService.getValue(obj, name);
    verifyDefault();

    assertEquals(date, ret);
  }
  
  @Test
  public void testSetValue_noSet() throws Exception {
    boolean set = false;
    BaseObject obj = new BaseObject();
    String name = "name";
    String val = "val";
    
    replayDefault();
    copyDocService.setValue(obj, name, val, set);
    verifyDefault();

    assertEquals(0, obj.getFieldList().size());
  }
  
  @Test
  public void testSetValue_String() throws Exception {
    boolean set = true;
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    PropertyInterface propClass = new StringClass();
    String name = "name";
    String val = "val";
    
    expect(bClass.get(eq(name))).andReturn(propClass).once();
    
    replayDefault();
    copyDocService.setValue(obj, name, val, set);
    verifyDefault();
    
    assertEquals(1, obj.getFieldList().size());
    assertEquals(val, ((BaseProperty) obj.get(name)).getValue());
  }
  
  @Test
  public void testSetValue_Number() throws Exception {
    boolean set = true;
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    PropertyInterface propClass = new NumberClass();
    String name = "name";
    long val = 5;
    
    expect(bClass.get(eq(name))).andReturn(propClass).once();
    
    replayDefault();
    copyDocService.setValue(obj, name, val, set);
    verifyDefault();
    
    assertEquals(1, obj.getFieldList().size());
    assertEquals(val, ((BaseProperty) obj.get(name)).getValue());
  }
  
  @Test
  public void testSetValue_Date() throws Exception {
    boolean set = true;
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    PropertyInterface propClass = new DateClass();
    String name = "name";
    Date val = new Date();
    
    expect(bClass.get(eq(name))).andReturn(propClass).once();
    
    replayDefault();
    copyDocService.setValue(obj, name, val, set);
    verifyDefault();
    
    assertEquals(1, obj.getFieldList().size());
    assertEquals(val, ((BaseProperty) obj.get(name)).getValue());
  }
  
  @Test
  public void testSetValue_List() throws Exception {
    boolean set = true;
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    PropertyInterface propClass = new StringClass();
    String name = "name";
    List<String> val = Arrays.asList("A", "B");
    
    expect(bClass.get(eq(name))).andReturn(propClass).once();
    
    replayDefault();
    copyDocService.setValue(obj, name, val, set);
    verifyDefault();
    
    assertEquals(1, obj.getFieldList().size());
    assertEquals(StringUtils.join(val, "|"), ((BaseProperty) obj.get(name)).getValue());
  }

}
