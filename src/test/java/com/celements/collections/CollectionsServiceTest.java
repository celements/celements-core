package com.celements.collections;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class CollectionsServiceTest extends AbstractBridgedComponentTestCase {
  private CollectionsService collectionsService;
  
  @Before
  public void setUp_CollectionsServiceTest() throws Exception {
    collectionsService = (CollectionsService) Utils.getComponent(ICollectionsService.class);
  }

  @Test
  public void testGetObjectsOrdered_docNull() {
    List<BaseObject> list = collectionsService.getObjectsOrdered(null, getBOClassRef(), "", 
        false);
    assertNotNull(list);
    assertEquals(0, list.size());
  }
  
  @Test
  public void testGetObjectsOrdered_noObjects() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext(
        ).getDatabase(), "S", "D"));
    List<BaseObject> list = collectionsService.getObjectsOrdered(doc, getBOClassRef(), "s1",
        true, "s2", false);
    assertNotNull(list);
    assertEquals(0, list.size());
  }
  
  @Test
  public void testGetObjectsOrdered_oneObject() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext(
        ).getDatabase(), "S", "D"));
    doc.addXObject(getSortTestBaseObjects().get(0));
    List<BaseObject> list = collectionsService.getObjectsOrdered(doc, getBOClassRef(), "s1",
        true, "s2", true);
    assertEquals(1, list.size());
  }

  @Test
  public void testGetObjectsOrdered_onlyOneFieldSort() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext(
        ).getDatabase(), "S", "D"));
    for (BaseObject obj : getSortTestBaseObjects()) {
      doc.addXObject(obj);
    }
    List<BaseObject> list = collectionsService.getObjectsOrdered(doc, getBOClassRef(), "s1",
        true);
    assertEquals(5, list.size());
    assertEquals("a", list.get(0).getStringValue("s1"));
    assertEquals("b", list.get(1).getStringValue("s1"));
    assertEquals("t", list.get(1).getStringValue("s2"));
    assertEquals("b", list.get(2).getStringValue("s1"));
    assertEquals("s", list.get(2).getStringValue("s2"));
    assertEquals("c", list.get(3).getStringValue("s1"));
    assertEquals("d", list.get(4).getStringValue("s1"));
  }
  
  @Test
  public void testGetObjectsOrdered_severalObjects_asc() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext(
        ).getDatabase(), "S", "D"));
    for (BaseObject obj : getSortTestBaseObjects()) {
      doc.addXObject(obj);
    }
    List<BaseObject> list = collectionsService.getObjectsOrdered(doc, getBOClassRef(),  "s1",
        true, "d", true);
    assertEquals(5, list.size());
    assertEquals("a", list.get(0).getStringValue("s1"));
    assertEquals("b", list.get(1).getStringValue("s1"));
    assertEquals(new Date(200l), list.get(1).getDateValue("d"));
    assertEquals("b", list.get(2).getStringValue("s1"));
    assertEquals(new Date(400l), list.get(2).getDateValue("d"));
    assertEquals("c", list.get(3).getStringValue("s1"));
    assertEquals("d", list.get(4).getStringValue("s1"));
  }
  
  @Test
  public void testGetObjectsOrdered_severalObjects_desc() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext(
        ).getDatabase(), "S", "D"));
    for (BaseObject obj : getSortTestBaseObjects()) {
      doc.addXObject(obj);
    }
    List<BaseObject> list = collectionsService.getObjectsOrdered(doc, getBOClassRef(), "i",
        false, "l", false);
    assertEquals(5, list.size());
    assertEquals(3, list.get(0).getIntValue("i"));
    assertEquals(2, list.get(1).getIntValue("i"));
    assertEquals(4, list.get(1).getIntValue("l"));
    assertEquals(2, list.get(2).getIntValue("i"));
    assertEquals(3, list.get(2).getIntValue("l"));
    assertEquals(2, list.get(3).getIntValue("i"));
    assertEquals(1, list.get(3).getIntValue("l"));
    assertEquals(1, list.get(4).getIntValue("i"));
  }
  
  private DocumentReference getBOClassRef() {
    return new DocumentReference(getContext().getDatabase(), "Classes", "TestClass");
  }
  
  private List<BaseObject> getSortTestBaseObjects() {
    List<BaseObject> objs = new ArrayList<BaseObject>();
    BaseObject obj = new BaseObject();
    obj.setXClassReference(getBOClassRef());
    obj.setStringValue("s1", "c");
    obj.setStringValue("s2", "u");
    obj.setIntValue("i", 1);
    obj.setDateValue("d", new Date(500l));
    obj.setLongValue("l", 5l);
    objs.add(obj);
    obj = new BaseObject();
    obj.setXClassReference(getBOClassRef());
    obj.setStringValue("s1", "b");
    obj.setStringValue("s2", "t");
    obj.setIntValue("i", 2);
    obj.setDateValue("d", new Date(400l));
    obj.setLongValue("l", 1l);
    objs.add(obj);
    obj = new BaseObject();
    obj.setXClassReference(getBOClassRef());
    obj.setStringValue("s1", "a");
    obj.setStringValue("s2", "r");
    obj.setIntValue("i", 3);
    obj.setDateValue("d", new Date(300l));
    obj.setLongValue("l", 2l);
    objs.add(obj);
    obj = new BaseObject();
    obj.setXClassReference(getBOClassRef());
    obj.setStringValue("s1", "b");
    obj.setStringValue("s2", "s");
    obj.setIntValue("i", 2);
    obj.setDateValue("d", new Date(200l));
    obj.setLongValue("l", 4l);
    objs.add(obj);
    obj = new BaseObject();
    obj.setXClassReference(getBOClassRef());
    obj.setStringValue("s1", "d");
    obj.setStringValue("s2", "v");
    obj.setIntValue("i", 2);
    obj.setDateValue("d", new Date(100l));
    obj.setLongValue("l", 3l);
    objs.add(obj);
    return objs;
  }
}
