package com.celements.web.comparators;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.DateProperty;
import com.xpn.xwiki.objects.IntegerProperty;
import com.xpn.xwiki.objects.LongProperty;
import com.xpn.xwiki.objects.StringProperty;

public class BaseObjectComparatorTest extends AbstractBridgedComponentTestCase{
  
  @Before
  public void setUp_BaseObjectComparatorTest() throws Exception {
    
  }

  @Test
  public void testCompare_onlyOneSort() {
    BaseObjectComparator comp = new BaseObjectComparator("x", true, "y", true);
    BaseObject obj1 = new BaseObject();
    obj1.setStringValue("x", "f");
    BaseObject obj2 = new BaseObject();
    obj2.setStringValue("x", "b");
    assertTrue(comp.compare(obj1, obj2) > 0);
  }
  
  @Test
  public void testCompare_onlyOneSort_inverted() {
    BaseObjectComparator comp = new BaseObjectComparator("x", false, "y", false);
    BaseObject obj1 = new BaseObject();
    obj1.setStringValue("x", "f");
    BaseObject obj2 = new BaseObject();
    obj2.setStringValue("x", "b");
    assertTrue(comp.compare(obj1, obj2) < 0);
  }

  @Test
  public void testCompare_firstSortBigger() {
    BaseObjectComparator comp = new BaseObjectComparator("x", true, "y", true);
    BaseObject obj1 = new BaseObject();
    obj1.setIntValue("x", 3);
    obj1.setLongValue("y", 1);
    BaseObject obj2 = new BaseObject();
    obj2.setIntValue("x", 1);
    obj2.setLongValue("y", 2);
    assertTrue(comp.compare(obj1, obj2) > 0);
  }

  @Test
  public void testCompare_firstSortBigger_inverted() {
    BaseObjectComparator comp = new BaseObjectComparator("x", false, "y", false);
    BaseObject obj1 = new BaseObject();
    obj1.setIntValue("x", 3);
    obj1.setLongValue("y", 1);
    BaseObject obj2 = new BaseObject();
    obj2.setIntValue("x", 1);
    obj2.setLongValue("y", 2);
    assertTrue(comp.compare(obj1, obj2) < 0);
  }

  @Test
  public void testCompare_firstSortSmaller() {
    BaseObjectComparator comp = new BaseObjectComparator("x", true, "y", true);
    BaseObject obj1 = new BaseObject();
    obj1.setIntValue("x", 1);
    obj1.setLongValue("y", 3);
    BaseObject obj2 = new BaseObject();
    obj2.setIntValue("x", 2);
    obj2.setLongValue("y", 1);
    assertTrue(comp.compare(obj1, obj2) < 0);
  }

  @Test
  public void testCompare_firstSortSmaller_inverted() {
    BaseObjectComparator comp = new BaseObjectComparator("x", false, "y", false);
    BaseObject obj1 = new BaseObject();
    obj1.setIntValue("x", 1);
    obj1.setLongValue("y", 3);
    BaseObject obj2 = new BaseObject();
    obj2.setIntValue("x", 2);
    obj2.setLongValue("y", 1);
    assertTrue(comp.compare(obj1, obj2) > 0);
  }

  @Test
  public void testCompare_secondSort() {
    BaseObjectComparator comp = new BaseObjectComparator("x", true, "y", true);
    BaseObject obj1 = new BaseObject();
    obj1.setIntValue("x", 1);
    obj1.setLongValue("y", 1);
    BaseObject obj2 = new BaseObject();
    obj2.setIntValue("x", 1);
    obj2.setLongValue("y", 3);
    assertTrue(comp.compare(obj1, obj2) < 0);
  }
  
  @Test
  public void testCompare_secondSort_inverted() {
    BaseObjectComparator comp = new BaseObjectComparator("x", false, "y", false);
    BaseObject obj1 = new BaseObject();
    obj1.setIntValue("x", 1);
    obj1.setLongValue("y", 1);
    BaseObject obj2 = new BaseObject();
    obj2.setIntValue("x", 1);
    obj2.setLongValue("y", 3);
    assertTrue(comp.compare(obj1, obj2) > 0);
  }

  @Test
  public void testCompareFieldStringPropertyStringProperty() {
    BaseObjectComparator comp = new BaseObjectComparator("x", true, "y", true);
    StringProperty prop1 = new StringProperty();
    StringProperty prop2 = new StringProperty();
    prop1.setValue("b");
    prop2.setValue("a");
    assertTrue(comp.compareField(prop1, prop2) > 0);
    prop1.setValue("a b,c");
    prop2.setValue("a b,c");
    assertEquals(0, comp.compareField(prop1, prop2));
    prop1.setValue("max");
    prop2.setValue("x");
    assertTrue(comp.compareField(prop1, prop2) < 0);
  }

  @Test
  public void testCompareFieldIntegerPropertyIntegerProperty() {
    BaseObjectComparator comp = new BaseObjectComparator("x", true, "y", true);
    IntegerProperty prop1 = new IntegerProperty();
    IntegerProperty prop2 = new IntegerProperty();
    prop1.setValue(2);
    prop2.setValue(1);
    assertEquals(1, comp.compareField(prop1, prop2));
    prop1.setValue(4);
    prop2.setValue(4);
    assertEquals(0, comp.compareField(prop1, prop2));
    prop1.setValue(12);
    prop2.setValue(1512);
    assertEquals(-1, comp.compareField(prop1, prop2));
  }

  @Test
  public void testCompareFieldLongPropertyLongProperty() {
    BaseObjectComparator comp = new BaseObjectComparator("x", true, "y", true);
    LongProperty prop1 = new LongProperty();
    LongProperty prop2 = new LongProperty();
    prop1.setValue(231l);
    prop2.setValue(52l);
    assertEquals(1, comp.compareField(prop1, prop2));
    prop1.setValue(1234567890l);
    prop2.setValue(1234567890l);
    assertEquals(0, comp.compareField(prop1, prop2));
    prop1.setValue(41331l);
    prop2.setValue(1243123l);
    assertEquals(-1, comp.compareField(prop1, prop2));
  }

  @Test
  public void testCompareFieldDatePropertyDateProperty() {
    BaseObjectComparator comp = new BaseObjectComparator("x", true, "y", true);
    DateProperty prop1 = new DateProperty();
    DateProperty prop2 = new DateProperty();
    prop1.setValue(new Date(1000000));
    prop2.setValue(new Date(1));
    assertEquals(1, comp.compareField(prop1, prop2));
    prop1.setValue(new Date(123456789));
    prop2.setValue(new Date(123456789));
    assertEquals(0, comp.compareField(prop1, prop2));
    prop1.setValue(new Date(1345132));
    prop2.setValue(new Date(541342131));
    assertEquals(-1, comp.compareField(prop1, prop2));
  }

  @Test
  public void testGetValue() {
    BaseObjectComparator comp = new BaseObjectComparator("x", true, "y", true);
    BaseObject obj = new BaseObject();
    obj.setStringValue("str", "s");
    obj.setIntValue("int", 12);
    obj.setLongValue("long", 123l);
    obj.setDateValue("date", new Date(1234));
    assertTrue(comp.getValue(obj, "str") instanceof StringProperty);
    assertTrue(comp.getValue(obj, "int") instanceof IntegerProperty);
    assertTrue(comp.getValue(obj, "long") instanceof LongProperty);
    assertTrue(comp.getValue(obj, "date") instanceof DateProperty);
  }

}
