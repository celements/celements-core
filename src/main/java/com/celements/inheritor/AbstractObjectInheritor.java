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

import java.util.Date;
import java.util.List;

import com.celements.iterator.IIteratorFactory;
import com.celements.iterator.XObjectIterator;
import com.xpn.xwiki.objects.BaseCollection;

public abstract class AbstractObjectInheritor implements IObjectInheritor {

  private IIteratorFactory<XObjectIterator> _iteratorFactory;
  public abstract BaseCollection getObject(String key);
  
  public void setIteratorFactory(IIteratorFactory<XObjectIterator> iteratorFactory) {
    _iteratorFactory = iteratorFactory;
  }

  protected IIteratorFactory<XObjectIterator> getIteratorFactory() {
    return _iteratorFactory;
  }
  
  public String getStringValue(String key) {
    return getStringValue(key, "");
  }

  public String getStringValue(String key, String defaultValue) {
    if (getObject(key) == null)
      return defaultValue;
    else
      return getObject(key).getStringValue(key);
  }

  public String getLargeStringValue(String key) {
    return getLargeStringValue(key, "");
  }
  
  public String getLargeStringValue(String key, String defaultValue) {
    if (getObject(key) == null)
      return defaultValue;
    else
      return getObject(key).getLargeStringValue(key);
  }

  public int getIntValue(String key) {
    return getIntValue(key, 0);
  }
  
  public int getIntValue(String key, int defaultValue) {
    if (getObject(key) == null)
      return defaultValue;
    else
      return getObject(key).getIntValue(key);
  }

  public long getLongValue(String key) {
    return getLongValue(key, 0);
  }
  
  public long getLongValue(String key, long defaultValue) {
    if (getObject(key) == null)
      return defaultValue;
    else
      return getObject(key).getLongValue(key);
  }

  public float getFloatValue(String key) {
    return getFloatValue(key, 0);
  }
  
  public float getFloatValue(String key, float defaultValue) {
    if (getObject(key) == null)
      return defaultValue;
    else
      return getObject(key).getFloatValue(key);
  }

  public double getDoubleValue(String key) {
    return getDoubleValue(key, 0);
  }
  
  public double getDoubleValue(String key, double defaultValue) {
    if (getObject(key) == null)
      return defaultValue;
    else
      return getObject(key).getDoubleValue(key);
  }

  public Date getDateValue(String key) {
    return getDateValue(key, null);
  }
  
  public Date getDateValue(String key, Date defaultValue) {
    if (getObject(key) == null)
      return defaultValue;
    else
      return getObject(key).getDateValue(key);
  }

  @SuppressWarnings("unchecked")
  public List getListValue(String key) {
    return getListValue(key, null);
  }
  
  @SuppressWarnings("unchecked")
  public List getListValue(String key, List defaultValue) {
    if (getObject(key) == null)
      return defaultValue;
    else
      return getObject(key).getListValue(key);
  }
}
