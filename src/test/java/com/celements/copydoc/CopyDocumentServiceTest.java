package com.celements.copydoc;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.web.Utils;

public class CopyDocumentServiceTest extends AbstractComponentTest {

  private CopyDocumentService copyDocService;

  private List<BaseObject> emptyList;
  private DocumentReference docRef;
  private XWikiDocument docMock;
  private DocumentReference classRef;

  @Before
  public void setUp_CopyDocumentServiceTest() throws Exception {
    copyDocService = (CopyDocumentService) Utils.getComponent(ICopyDocumentRole.class);
    emptyList = new ArrayList<>();
    docRef = new DocumentReference("db", "Space", "SomeDoc");
    docMock = createDocMock(docRef);
    expect(docMock.getTranslation()).andReturn(0).anyTimes();
    expect(docMock.getLanguage()).andReturn("").anyTimes();
    classRef = new DocumentReference("db", "Classes", "SomeClass");
    // important for unstable-2.0 set database because class references are checked for db
    getContext().setDatabase("db");
  }

  @Test
  public void testCopyObjects_none() throws Exception {
    boolean set = true;
    DocumentReference srcDocRef = new DocumentReference("odb", "Space", "SomeSrcDoc");
    XWikiDocument srcDoc = new XWikiDocument(srcDocRef);
    XWikiDocument trgDoc = new XWikiDocument(docRef);
    Set<BaseObject> toIgnore = Sets.newHashSet();

    replayDefault();
    boolean ret = copyDocService.copyObjects(srcDoc, trgDoc, toIgnore, set);
    verifyDefault();

    assertFalse(ret);
  }

  @Test
  public void testCopyObjects_added() throws Exception {
    boolean set = true;
    String name = "name";
    String val = "val";
    DocumentReference srcDocRef = new DocumentReference("odb", "Space", "SomeSrcDoc");
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    DocumentReference srcClassRef = new DocumentReference("odb",
        classRef.getLastSpaceReference().getName(), classRef.getName());
    XWikiDocument srcDoc = new XWikiDocument(srcDocRef);
    srcDoc.addXObject(createObj(srcClassRef, name, val));
    XWikiDocument trgDoc = new XWikiDocument(docRef);
    Set<BaseObject> toIgnore = Sets.newHashSet();
    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, name, new StringClass());

    replayDefault();
    boolean ret = copyDocService.copyObjects(srcDoc, trgDoc, toIgnore, set);
    verifyDefault();

    assertTrue(ret);
    assertEquals(1, getModelAccess().getXObjects(trgDoc, classRef, name, val).size());
  }

  @Test
  public void testCopyObjects_added_notSet() throws Exception {
    boolean set = false;
    String name = "name";
    String val = "val";
    DocumentReference srcDocRef = new DocumentReference("odb", "Space", "SomeSrcDoc");
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    DocumentReference srcClassRef = new DocumentReference("odb",
        classRef.getLastSpaceReference().getName(), classRef.getName());
    XWikiDocument srcDoc = new XWikiDocument(srcDocRef);
    srcDoc.addXObject(createObj(srcClassRef, name, val));
    XWikiDocument trgDoc = new XWikiDocument(docRef);
    Set<BaseObject> toIgnore = Sets.newHashSet();
    expectNewBaseObject(classRef);

    replayDefault();
    boolean ret = copyDocService.copyObjects(srcDoc, trgDoc, toIgnore, set);
    verifyDefault();

    assertTrue(ret);
    assertEquals(0, getModelAccess().getXObjects(trgDoc, classRef).size());
  }

  @Test
  public void testCopyObjects_removed() throws Exception {
    boolean set = true;
    String name1 = "name1";
    String val1 = "val1";
    String name2 = "name2";
    String val2 = "val2";
    DocumentReference srcDocRef = new DocumentReference("odb", "Space", "SomeSrcDoc");
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    DocumentReference srcClassRef = new DocumentReference("odb",
        classRef.getLastSpaceReference().getName(), classRef.getName());
    XWikiDocument srcDoc = new XWikiDocument(srcDocRef);
    srcDoc.addXObject(createObj(srcClassRef, name1, val1));
    XWikiDocument trgDoc = new XWikiDocument(docRef);
    trgDoc.addXObject(createObj(classRef, name1, val1));
    trgDoc.addXObject(createObj(classRef, name2, val2));
    Set<BaseObject> toIgnore = Sets.newHashSet();

    replayDefault();
    boolean ret = copyDocService.copyObjects(srcDoc, trgDoc, toIgnore, set);
    verifyDefault();

    assertTrue(ret);
    assertEquals(1, getModelAccess().getXObjects(trgDoc, classRef).size());
    assertEquals(1, getModelAccess().getXObjects(trgDoc, classRef, name1, val1).size());
  }

  @Test
  public void testCopyObjects_removed_notSet() throws Exception {
    boolean set = false;
    DocumentReference srcDocRef = new DocumentReference("odb", "Space", "SomeSrcDoc");
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    DocumentReference srcClassRef = new DocumentReference("odb",
        classRef.getLastSpaceReference().getName(), classRef.getName());
    XWikiDocument srcDoc = new XWikiDocument(srcDocRef);
    srcDoc.addXObject(createObj(srcClassRef));
    XWikiDocument trgDoc = new XWikiDocument(docRef);
    trgDoc.addXObject(createObj(classRef));
    trgDoc.addXObject(createObj(classRef));
    Set<BaseObject> toIgnore = Sets.newHashSet();

    replayDefault();
    boolean ret = copyDocService.copyObjects(srcDoc, trgDoc, toIgnore, set);
    verifyDefault();

    assertTrue(ret);
    assertEquals(2, getModelAccess().getXObjects(trgDoc, classRef).size());
  }

  @Test
  public void testCopyObjects_removed_multiple() throws Exception {
    boolean set = true;
    DocumentReference srcDocRef = new DocumentReference("odb", "Space", "SomeSrcDoc");
    XWikiDocument srcDoc = new XWikiDocument(srcDocRef);
    XWikiDocument trgDoc = new XWikiDocument(docRef);
    trgDoc.addXObject(createObj(classRef));
    trgDoc.addXObject(createObj(classRef));
    trgDoc.addXObject(createObj(classRef));
    Set<BaseObject> toIgnore = Sets.newHashSet();

    replayDefault();
    boolean ret = copyDocService.copyObjects(srcDoc, trgDoc, toIgnore, set);
    verifyDefault();

    assertTrue(ret);
    assertEquals(0, getModelAccess().getXObjects(trgDoc, classRef).size());
  }

  @Test
  public void testCopyObjects_removed_multiple_notSet() throws Exception {
    boolean set = false;
    DocumentReference srcDocRef = new DocumentReference("odb", "Space", "SomeSrcDoc");
    XWikiDocument srcDoc = new XWikiDocument(srcDocRef);
    XWikiDocument trgDoc = new XWikiDocument(docRef);
    trgDoc.addXObject(createObj(classRef));
    trgDoc.addXObject(createObj(classRef));
    trgDoc.addXObject(createObj(classRef));
    Set<BaseObject> toIgnore = Sets.newHashSet();

    replayDefault();
    boolean ret = copyDocService.copyObjects(srcDoc, trgDoc, toIgnore, set);
    verifyDefault();

    assertTrue(ret);
    assertEquals(3, getModelAccess().getXObjects(trgDoc, classRef).size());
  }

  @Test
  public void testCopyObjects_noChange() throws Exception {
    boolean set = true;
    DocumentReference srcDocRef = new DocumentReference("odb", "Space", "SomeSrcDoc");
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    DocumentReference srcClassRef = new DocumentReference("odb",
        classRef.getLastSpaceReference().getName(), classRef.getName());
    XWikiDocument srcDoc = new XWikiDocument(srcDocRef);
    srcDoc.addXObject(createObj(srcClassRef));
    XWikiDocument trgDoc = new XWikiDocument(docRef);
    trgDoc.addXObject(createObj(classRef));
    Set<BaseObject> toIgnore = Sets.newHashSet();

    replayDefault();
    boolean ret = copyDocService.copyObjects(srcDoc, trgDoc, toIgnore, set);
    verifyDefault();

    assertFalse(ret);
    assertEquals(1, getModelAccess().getXObjects(srcDoc, srcClassRef).size());
    assertEquals(1, getModelAccess().getXObjects(trgDoc, classRef).size());
  }

  @Test
  public void testCopyObjects_toIgnore_add() throws Exception {
    boolean set = true;
    String name = "name";
    String val = "val";
    DocumentReference srcDocRef = new DocumentReference("odb", "Space", "SomeSrcDoc");
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    DocumentReference srcClassRef = new DocumentReference("odb",
        classRef.getLastSpaceReference().getName(), classRef.getName());
    XWikiDocument srcDoc = new XWikiDocument(srcDocRef);
    srcDoc.addXObject(createObj(srcClassRef, name, val));
    BaseObject ignoreObj = createObj(srcClassRef);
    srcDoc.addXObject(ignoreObj);
    XWikiDocument trgDoc = new XWikiDocument(docRef);
    trgDoc.addXObject(createObj(classRef, name, val));
    Set<BaseObject> toIgnore = Sets.newHashSet(ignoreObj);

    replayDefault();
    boolean ret = copyDocService.copyObjects(srcDoc, trgDoc, toIgnore, set);
    verifyDefault();

    assertFalse(ret);
    assertEquals(1, getModelAccess().getXObjects(trgDoc, classRef).size());
    assertFalse(getModelAccess().getXObjects(trgDoc, classRef).contains(ignoreObj));
  }

  @Test
  public void testCopyObjects_toIgnore_remove() throws Exception {
    boolean set = true;
    String name = "name";
    String val = "val";
    DocumentReference srcDocRef = new DocumentReference("odb", "Space", "SomeSrcDoc");
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    DocumentReference srcClassRef = new DocumentReference("odb",
        classRef.getLastSpaceReference().getName(), classRef.getName());
    XWikiDocument srcDoc = new XWikiDocument(srcDocRef);
    srcDoc.addXObject(createObj(srcClassRef, name, val));
    XWikiDocument trgDoc = new XWikiDocument(docRef);
    trgDoc.addXObject(createObj(classRef, name, val));
    BaseObject ignoreObj = createObj(classRef);
    trgDoc.addXObject(ignoreObj);
    Set<BaseObject> toIgnore = Sets.newHashSet(ignoreObj);

    replayDefault();
    boolean ret = copyDocService.copyObjects(srcDoc, trgDoc, toIgnore, set);
    verifyDefault();

    assertFalse(ret);
    assertEquals(2, getModelAccess().getXObjects(trgDoc, classRef).size());
    assertTrue(getModelAccess().getXObjects(trgDoc, classRef).contains(ignoreObj));
  }

  @Test
  public void testGetAllClassRefs() throws Exception {
    DocumentReference srcDocRef = new DocumentReference("odb", "Space", "SomeSrcDoc");
    XWikiDocument srcDoc = new XWikiDocument(srcDocRef);
    DocumentReference classRef1 = new DocumentReference("odb", "Classes", "SomeClass1");
    srcDoc.addXObject(createObj(classRef1));
    DocumentReference classRef2 = new DocumentReference("odb", "Classes", "SomeClass2");
    srcDoc.addXObject(createObj(classRef2));

    XWikiDocument trgDoc = new XWikiDocument(docRef);
    DocumentReference classRef3 = new DocumentReference("db", "Classes", "SomeClass3");
    trgDoc.addXObject(createObj(classRef3));
    DocumentReference classRef4 = new DocumentReference("db", "Classes", "SomeClass1");
    trgDoc.addXObject(createObj(classRef4));

    replayDefault();
    Set<DocumentReference> ret = copyDocService.getAllClassRefs(srcDoc, trgDoc);
    verifyDefault();

    assertEquals(3, ret.size());
    assertTrue(ret.contains(classRef3));
    assertTrue(ret.contains(classRef4));
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    classRef2 = new DocumentReference("db", classRef2.getLastSpaceReference().getName(),
        classRef2.getName());
    assertTrue(ret.contains(classRef2));
  }

  @Test
  public void testCreateOrUpdateObjects_none() throws Exception {
    boolean set = true;
    BaseObject obj = createObj(classRef);

    replayDefault();
    boolean ret = copyDocService.createOrUpdateObjects(docMock, emptyList, Lists.newArrayList(obj),
        set);
    verifyDefault();

    assertFalse(ret);
  }

  @Test
  public void testCreateOrUpdateObjects_create() throws Exception {
    boolean set = true;
    BaseObject obj = createObj(classRef);

    expect(docMock.newXObject(eq(classRef), same(getContext()))).andReturn(obj).once();

    replayDefault();
    boolean ret = copyDocService.createOrUpdateObjects(docMock, Lists.newArrayList(obj), emptyList,
        set);
    verifyDefault();

    assertTrue(ret);
  }

  @Test
  public void testCreateOrUpdateObjects_create_notSet() throws Exception {
    boolean set = false;
    BaseObject obj = createObj(classRef);

    replayDefault();
    boolean ret = copyDocService.createOrUpdateObjects(docMock, Lists.newArrayList(obj), emptyList,
        set);
    verifyDefault();

    assertTrue(ret);
  }

  @Test
  public void testCreateOrUpdateObjects_update() throws Exception {
    boolean set = true;
    String name = "name";
    String val = "val";
    BaseObject srcObj = createObj(classRef, name, val);
    List<BaseObject> srcObjs = Lists.newArrayList(srcObj);
    BaseObject trgObj = createObj(classRef);
    List<BaseObject> trgObjs = Lists.newArrayList(trgObj);
    expectPropertyClass(classRef, name, new StringClass());

    replayDefault();
    boolean ret = copyDocService.createOrUpdateObjects(docMock, srcObjs, trgObjs, set);
    verifyDefault();

    assertTrue(ret);
    assertEquals(val, trgObj.getStringValue(name));
    assertEquals(1, srcObjs.size());
    assertEquals(0, trgObjs.size());
  }

  @Test
  public void testCreateOrUpdateObjects_update_notSet() throws Exception {
    boolean set = false;
    String name = "name";
    String val = "val";
    BaseObject srcObj = createObj(classRef, name, val);
    BaseObject trgObj = createObj(classRef);

    replayDefault();
    boolean ret = copyDocService.createOrUpdateObjects(docMock, Lists.newArrayList(srcObj),
        Lists.newArrayList(trgObj), set);
    verifyDefault();

    assertTrue(ret);
    assertEquals("", trgObj.getStringValue(name));
  }

  @Test
  public void testCopyObject_added() throws Exception {
    boolean set = true;
    String name1 = "name1";
    String val1 = "val1";
    BaseObject srcObj = createObj(classRef, name1, val1);
    String name2 = "name2";
    Date val2 = new Date();
    srcObj.setDateValue(name2, val2);
    BaseObject trgObj = createObj(classRef);
    expectPropertyClasses(classRef, ImmutableMap.<String, PropertyClass>builder().put(name1,
        new StringClass()).put(name2, new DateClass()).build());

    replayDefault();
    boolean ret = copyDocService.copyObject(srcObj, trgObj, set);
    verifyDefault();

    assertTrue(ret);
    assertEquals(2, trgObj.getFieldList().size());
    assertEquals(val1, trgObj.getStringValue(name1));
    assertEquals(val2, trgObj.getDateValue(name2));
  }

  @Test
  public void testCopyObject_removed() throws Exception {
    boolean set = true;
    String name1 = "name1";
    String val1 = "val1";
    BaseObject srcObj = createObj(classRef, name1, val1);
    BaseObject trgObj = createObj(classRef, name1, val1);
    String name2 = "name2";
    String val2 = "val2";
    trgObj.setStringValue(name2, val2);

    assertEquals(2, trgObj.getFieldList().size());

    replayDefault();
    boolean ret = copyDocService.copyObject(srcObj, trgObj, set);
    verifyDefault();

    assertTrue(ret);
    assertEquals(1, trgObj.getFieldList().size());
    assertEquals(val1, trgObj.getStringValue(name1));
    assertNull(trgObj.get(name2));
  }

  @Test
  public void testCopyObject_noChange() throws Exception {
    boolean set = true;
    String name = "name";
    String val = "val";
    BaseObject srcObj = createObj(classRef, name, val);
    BaseObject trgObj = createObj(classRef, name, val);

    replayDefault();
    boolean ret = copyDocService.copyObject(srcObj, trgObj, set);
    verifyDefault();

    assertFalse(ret);
    assertEquals(1, trgObj.getFieldList().size());
    assertEquals(val, trgObj.getStringValue(name));
  }

  private BaseObject createObj(DocumentReference classRef, String name, Object val) {
    BaseObject obj = createObj(classRef);
    obj.setStringValue(name, val.toString());
    return obj;
  }

  private BaseObject createObj(DocumentReference classRef) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    return obj;
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

}
