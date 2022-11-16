package com.celements.web.comparators;

import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.DateProperty;
import com.xpn.xwiki.objects.IntegerProperty;
import com.xpn.xwiki.objects.LongProperty;
import com.xpn.xwiki.objects.StringProperty;

public class BaseObjectComparatorTest extends AbstractComponentTest {

  Comparator<BaseObject> compAsc;
  Comparator<BaseObject> compDesc;

  @Before
  public void setUp_BaseObjectComparatorTest() throws Exception {
    compAsc = BaseObjectComparator.create("x")
        .thenComparing(BaseObjectComparator.create("y"));
    compDesc = BaseObjectComparator.reversed("x")
        .thenComparing(BaseObjectComparator.reversed("y"));
  }

  @Test
  public void testCompare_onlyOneSort() {
    Comparator<BaseObject> comp = compAsc;
    BaseObject obj1 = new BaseObject();
    obj1.setStringValue("x", "f");
    BaseObject obj2 = new BaseObject();
    obj2.setStringValue("x", "b");
    assertTrue(comp.compare(obj1, obj2) > 0);
  }

  @Test
  public void testCompare_onlyOneSort_inverted() {
    Comparator<BaseObject> comp = BaseObjectComparator.reversed("x")
        .thenComparing(BaseObjectComparator.reversed("y"));
    BaseObject obj1 = new BaseObject();
    obj1.setStringValue("x", "f");
    BaseObject obj2 = new BaseObject();
    obj2.setStringValue("x", "b");
    assertTrue(comp.compare(obj1, obj2) < 0);
  }

  @Test
  public void testCompare_firstSortBigger() {
    Comparator<BaseObject> comp = compAsc;
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
    Comparator<BaseObject> comp = compDesc;
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
    Comparator<BaseObject> comp = compAsc;
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
    Comparator<BaseObject> comp = compDesc;
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
    Comparator<BaseObject> comp = compAsc;
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
    Comparator<BaseObject> comp = compDesc;
    BaseObject obj1 = new BaseObject();
    obj1.setIntValue("x", 1);
    obj1.setLongValue("y", 1);
    BaseObject obj2 = new BaseObject();
    obj2.setIntValue("x", 1);
    obj2.setLongValue("y", 3);
    assertTrue(comp.compare(obj1, obj2) > 0);
  }

  @Test
  public void testCompare_null() {
    Comparator<BaseObject> comp = compAsc;
    BaseObject obj1 = new BaseObject();
    BaseObject obj2 = new BaseObject();
    obj1.setDateValue("x", new Date(1000000));
    obj2.setDateValue("x", null);
    assertTrue(0 < comp.compare(obj1, obj2));
    obj1.setDateValue("x", null);
    assertEquals(0, comp.compare(obj1, obj2));
    obj2.setDateValue("x", new Date(541342131));
    assertTrue(0 > comp.compare(obj1, obj2));
  }

  @Test
  public void testGetValue() {
    BaseObjectComparator comp = new BaseObjectComparator(null);
    BaseObject obj = new BaseObject();
    obj.setStringValue("str", "s");
    obj.setIntValue("int", 12);
    obj.setLongValue("long", 123l);
    obj.setDateValue("date", new Date(1234));
    assertTrue(comp.getProperty(obj, "str") instanceof StringProperty);
    assertTrue(comp.getProperty(obj, "int") instanceof IntegerProperty);
    assertTrue(comp.getProperty(obj, "long") instanceof LongProperty);
    assertTrue(comp.getProperty(obj, "date") instanceof DateProperty);
  }

}
