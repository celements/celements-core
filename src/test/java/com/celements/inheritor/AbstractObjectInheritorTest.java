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
package com.celements.inheritor;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.iterator.IIteratorFactory;
import com.celements.iterator.XObjectIterator;
import com.xpn.xwiki.objects.BaseCollection;

public class AbstractObjectInheritorTest extends AbstractBridgedComponentTestCase {
    
  private TestInheritor _testInheritor;

  @Before
  public void setUp_AbstractObjectInheritorTest() throws Exception {
    _testInheritor = new TestInheritor();
  }
  
  @Test
  public void testGetIteratorFactory_notSame() {
    _testInheritor.setIteratorFactory(getTestIteratorFactory(new ArrayList<String>()));  
    assertNotSame(getTestIteratorFactory(new ArrayList<String>()), _testInheritor.getIteratorFactory());
  }
  
  @Test
  public void testGetStringValue_isNull() {
    assertEquals("", _testInheritor.getStringValue("myField"));
    assertEquals("myDefaultValue", _testInheritor.getStringValue("myField","myDefaultValue"));
  }

  @Test
  public void testGetStringValue() {
    BaseCollection testColl = createMock(BaseCollection.class);
    expect(testColl.getStringValue(eq("myField"))).andReturn("myValue").anyTimes();
    _testInheritor.injectBaseColl(testColl);
    replay(testColl);
    assertEquals("myValue", _testInheritor.getStringValue("myField"));
    assertEquals("myValue", _testInheritor.getStringValue("myField","myDefaultValue"));
    verify(testColl);
  }  

  @Test
  public void testGetLargeStringValue_isNull() {
    assertEquals("", _testInheritor.getLargeStringValue("myField"));
    assertEquals("myDefaultValue", _testInheritor.getLargeStringValue("myField","myDefaultValue"));
  }
  
  @Test
  public void testGetLargeStringValue() {
    BaseCollection testColl = createMock(BaseCollection.class);
    expect(testColl.getLargeStringValue(eq("myField"))).andReturn("myValue").anyTimes();
    _testInheritor.injectBaseColl(testColl);
    replay(testColl);
    assertEquals("myValue", _testInheritor.getLargeStringValue("myField"));
    assertEquals("myValue", _testInheritor.getLargeStringValue("myField","myDefaultValue"));
    verify(testColl);
  }

  @Test
  public void testGetIntValue_isNull() {
    assertEquals(0, _testInheritor.getIntValue("myField"));
    assertEquals(-1, _testInheritor.getIntValue("myField",-1));
  }
  
  @Test
  public void testGetIntValue() {
    BaseCollection testColl = createMock(BaseCollection.class);
    expect(testColl.getIntValue(eq("myField"))).andReturn(123).anyTimes();
    _testInheritor.injectBaseColl(testColl);
    replay(testColl);
    assertEquals(123, _testInheritor.getIntValue("myField"));
    assertEquals(123, _testInheritor.getIntValue("myField",321));
    verify(testColl);
  }

  @Test
  public void testGetLongValue_isNull() {
    assertEquals(0l, _testInheritor.getLongValue("myField"));
    assertEquals(-1l, _testInheritor.getLongValue("myField",-1l));
  }
  
  @Test
  public void testGetLongValue() {
    BaseCollection testColl = createMock(BaseCollection.class);
    expect(testColl.getLongValue(eq("myField"))).andReturn(123l).anyTimes();
    _testInheritor.injectBaseColl(testColl);
    replay(testColl);
    assertEquals(123l, _testInheritor.getLongValue("myField"));
    assertEquals(123l, _testInheritor.getLongValue("myField",321l));
    verify(testColl);
  }

  @Test
  public void testGetFloatValue_isNull() {
    assertEquals(0, _testInheritor.getFloatValue("myField"), 0.001);
    assertEquals(987.65f, _testInheritor.getFloatValue("myField",987.65f), 0.001);
  }
  
  @Test
  public void testGetFloatValue() {
    BaseCollection testColl = createMock(BaseCollection.class);
    expect(testColl.getFloatValue(eq("myField"))).andReturn(123.45f).anyTimes();
    _testInheritor.injectBaseColl(testColl);
    replay(testColl);
    assertEquals(123.45f, _testInheritor.getFloatValue("myField"), 0.001);
    assertEquals(123.45f, _testInheritor.getFloatValue("myField",987.65f), 0.001);
    verify(testColl);
  }

  @Test
  public void testGetDoubleValue_isNull() {
    assertEquals(0.00, _testInheritor.getDoubleValue("myField"), 0.001);
    assertEquals(987.65, _testInheritor.getDoubleValue("myField",987.65), 0.001);
  }
  
  @Test
  public void testGetDoubleValue() {
    BaseCollection testColl = createMock(BaseCollection.class);
    expect(testColl.getDoubleValue(eq("myField"))).andReturn(123.45).anyTimes();
    _testInheritor.injectBaseColl(testColl);
    replay(testColl);
    assertEquals(123.45, _testInheritor.getDoubleValue("myField"), 0.001);
    assertEquals(123.45, _testInheritor.getDoubleValue("myField",987.65), 0.001);
    verify(testColl);
  }

  @Test
  public void testGetDateValue_isNull() {
    Date date = new Date();
    assertEquals(null, _testInheritor.getDateValue("myField"));
    assertEquals(date, _testInheritor.getDateValue("myField", date));
  }
  
  @Test
  public void testGetDateValue() {
    BaseCollection testColl = createMock(BaseCollection.class);
    Date date = new Date();
    expect(testColl.getDateValue(eq("myField"))).andReturn(date).anyTimes();
    _testInheritor.injectBaseColl(testColl);
    replay(testColl);
    assertEquals(date, _testInheritor.getDateValue("myField"));
    assertEquals(date, _testInheritor.getDateValue("myField", new Date()));
    verify(testColl);
  }

  @Test
  public void testGetListValue_isNull() {
    List<String> mylist = new ArrayList<String>();
    assertEquals(null, _testInheritor.getListValue("myField"));
    assertEquals(mylist, _testInheritor.getListValue("myField", mylist));
  }
  
  @Test
  public void testGetListValue() {
    BaseCollection testColl = createMock(BaseCollection.class);
    List<String> mylist = new ArrayList<String>();
    expect(testColl.getListValue(eq("myField"))).andReturn(mylist).anyTimes();
    _testInheritor.injectBaseColl(testColl);
    replay(testColl);
    assertEquals(mylist, _testInheritor.getListValue("myField"));
    assertEquals(mylist, _testInheritor.getListValue("myField", new ArrayList<String>()));
    verify(testColl);
  }
 
  
  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/
  
  public final class TestInheritor extends AbstractObjectInheritor {

    private BaseCollection baseColl;

    @Override
    public BaseCollection getObject(String key) {
      return baseColl;
    }

    public void injectBaseColl(BaseCollection testBaseColl) {
      baseColl = testBaseColl;
    }
  }
  
  private IIteratorFactory<XObjectIterator> getTestIteratorFactory(final List<String> docList) {
    return new IIteratorFactory<XObjectIterator>() {
      public XObjectIterator createIterator() {
        XObjectIterator iterator = new XObjectIterator(getContext());
        iterator.setClassName("myTestClass");
        iterator.setDocList(docList);
        return iterator;
      }
    };
  }

}
