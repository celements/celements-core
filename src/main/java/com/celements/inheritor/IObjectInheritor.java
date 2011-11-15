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

public interface IObjectInheritor {

  public BaseCollection getObject(String key);
  public void setIteratorFactory(IIteratorFactory<XObjectIterator> iteratorFactory);
  
  public String getStringValue(String key);
  public String getStringValue(String key, String defaultValue);
  
  public String getLargeStringValue(String key);
  public String getLargeStringValue(String key, String defaultValue);
  
  public int getIntValue(String key);
  public int getIntValue(String key, int defaultValue);
  
  public long getLongValue(String key); 
  public long getLongValue(String key, long defaultValue);
  
  public float getFloatValue(String key);
  public float getFloatValue(String key, float defaultValue);
  
  public double getDoubleValue(String key);
  public double getDoubleValue(String key, double defaultValue);
  
  public Date getDateValue(String key);
  public Date getDateValue(String key, Date defaultValue);
  
  @SuppressWarnings("unchecked")
  public List getListValue(String key);
  @SuppressWarnings("unchecked")
  public List getListValue(String key, List defaultValue);
  
}
