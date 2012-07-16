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
package com.celements.web.comparators;

import java.util.Comparator;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.DateProperty;
import com.xpn.xwiki.objects.IntegerProperty;
import com.xpn.xwiki.objects.LongProperty;
import com.xpn.xwiki.objects.StringProperty;

public class BaseObjectComparator implements Comparator<BaseObject> {
  private static Log LOGGER = LogFactory.getFactory().getInstance(
      BaseObjectComparator.class);
  
  private String orderField1 = null;
  private boolean asc1 = true;
  private String orderField2 = null;
  private boolean asc2 = true;
  
  public BaseObjectComparator(String orderField1, boolean asc1, String orderField2, 
      boolean asc2) {
    this.orderField1 = orderField1;
    this.asc1 = asc1;
    this.orderField2 = orderField2;
    this.asc2 = asc2;
  }

  public int compare(BaseObject obj1, BaseObject obj2) {
    Object val1 = getValue(obj1, orderField1);
    Object val2 = getValue(obj2, orderField1);
    int firstLarger = 0;
    if(val1 instanceof StringProperty) {
      firstLarger = compareField((StringProperty)val1, (StringProperty)val2);
    } else if(val1 instanceof IntegerProperty) {
      firstLarger = compareField((IntegerProperty)val1, (IntegerProperty)val2);
    } else if(val1 instanceof LongProperty) {
      firstLarger = compareField((LongProperty)val1, (LongProperty)val2);
    } else if(val1 instanceof DateProperty) {
      firstLarger = compareField((DateProperty)val1, (DateProperty)val2);
    }
    if((firstLarger == 0) && (orderField2 != null) && (orderField2.trim().length() > 0)
        ) {
      val1 = getValue(obj1, orderField2);
      val2 = getValue(obj2, orderField2);
      if(val1 instanceof StringProperty) {
        firstLarger = compareField((StringProperty)val1, (StringProperty)val2);
      } else if(val1 instanceof IntegerProperty) {
        firstLarger = compareField((IntegerProperty)val1, (IntegerProperty)val2);
      } else if(val1 instanceof LongProperty) {
        firstLarger = compareField((LongProperty)val1, (LongProperty)val2);
      } else if(val1 instanceof DateProperty) {
        firstLarger = compareField((DateProperty)val1, (DateProperty)val2);
      }
      firstLarger *= asc2?1:-1;
    } else {
      firstLarger *= asc1?1:-1;
    }
    return firstLarger;
  }

  int compareField(StringProperty value, StringProperty value2) {
    return ((String)value.getValue()).compareTo((String)value2.getValue()) ;
  }

  short compareField(IntegerProperty value, IntegerProperty value2) {
    if((Integer)value.getValue() > (Integer)value2.getValue()) {
      return 1;
    } else if((Integer)value.getValue() < (Integer)value2.getValue()) {
      return -1;
    }
    return 0;
  }

  short compareField(LongProperty value, LongProperty value2) {
    if((Long)value.getValue() > (Long)value2.getValue()) {
      return 1;
    } else if((Long)value.getValue() < (Long)value2.getValue()) {
      return -1;
    }
    return 0;
  }

  short compareField(DateProperty value, DateProperty value2) {
    if(((Date)value.getValue()).getTime() > ((Date)value2.getValue()).getTime()) {
      return 1;
    } else if(((Date)value.getValue()).getTime() < ((Date)value2.getValue()).getTime()
        ) {
      return -1;
    }
    return 0;
  }

  Object getValue(BaseObject obj, String field) {
    try {
      return obj.get(field);
    } catch (XWikiException e) {
      LOGGER.error("Could not get field '" + field + "' from object '" + obj + "'", e);
    }
    return null;
  }
}
