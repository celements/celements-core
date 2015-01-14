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
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

public class DefaultEmptyFieldCheckerTest extends AbstractBridgedComponentTestCase{
  
  private IEmptyFieldChecker _emptyFieldChecker;
  
  @Before
  public void setUp_DefaultEmptyFieldCheckerTest() throws Exception {
    _emptyFieldChecker = new DefaultEmptyFieldChecker();
  }
  
  @Test
  public void testIsEmpty_fieldNotExist(){
    BaseObject bo = new BaseObject();    
    assertTrue(_emptyFieldChecker.isEmpty(getProperty(bo, "myField")));
  }

  @Test
  public void testIsEmpty_stringField_notEmpty(){
    BaseObject bo = new BaseObject();
    bo.setStringValue("myField", "myStringValue");
    assertFalse(_emptyFieldChecker.isEmpty(getProperty(bo, "myField")));
  }
  
  @Test
  public void testIsEmpty_stringField_empty(){
    BaseObject bo = new BaseObject();
    bo.setStringValue("myField", "");
    assertTrue(_emptyFieldChecker.isEmpty(getProperty(bo, "myField")));
  }
  
  @Test
  public void testIsEmpty_intField_notEmpty(){
    BaseObject bo = new BaseObject();
    bo.setIntValue("myField", 123);
    assertFalse(_emptyFieldChecker.isEmpty(getProperty(bo, "myField")));
  }
  
  @Test
  public void testIsEmpty_intField_empty(){
    BaseObject bo = new BaseObject();
    bo.setIntValue("myField", 0);
    assertTrue(_emptyFieldChecker.isEmpty(getProperty(bo, "myField")));
  }
  
  @Test
  public void testIsEmpty_doubleField_notEmpty(){
    BaseObject bo = new BaseObject();
    bo.setDoubleValue("myField", 123.45);
    assertFalse(_emptyFieldChecker.isEmpty(getProperty(bo, "myField")));
  }
  
  @Test
  public void testIsEmpty_doubleField_empty(){
    BaseObject bo = new BaseObject();
    bo.setDoubleValue("myField", 0.00);
    assertTrue(_emptyFieldChecker.isEmpty(getProperty(bo, "myField")));
  }
  
  @Test
  public void testIsEmpty_DateField_notEmpty(){
    BaseObject bo = new BaseObject();
    bo.setDateValue("myField", new Date());
    assertFalse(_emptyFieldChecker.isEmpty(getProperty(bo, "myField")));
  }
  
  @Test
  public void testIsEmpty_DateField_empty(){
    BaseObject bo = new BaseObject();
    bo.setDateValue("myField", null);
    assertTrue(_emptyFieldChecker.isEmpty(getProperty(bo, "myField")));
  }
  
//*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/
  
  private BaseProperty getProperty(BaseObject bo, String key) {
    return (BaseProperty) bo.getField(key);
  }
}
