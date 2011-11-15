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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.iterator.XObjectIterator;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.objects.BaseProperty;

public class FieldInheritor extends AbstractObjectInheritor{

  private IEmptyFieldChecker _emptyFieldChecker;
  private static Log mLogger = LogFactory.getFactory().getInstance(FieldInheritor.class);
  
  public FieldInheritor(){
  }
  
  public BaseCollection getObject(String key){
    if (getIteratorFactory() == null) {
      throw new IllegalStateException("No IteratorFactory given.");
    }
    XObjectIterator iterator = getIteratorFactory().createIterator();
    while (iterator.hasNext()){
      try {
        BaseProperty property = (BaseProperty) iterator.next().get(key);
        if (!getEmptyFieldChecker().isEmpty(property)) {
          return property.getObject();
        }
      } catch (XWikiException exp) {
        mLogger.warn("failed to get [" + key + "] field.", exp);
      }
    }
    return null;
  }
  
  public void setEmptyFieldChecker(IEmptyFieldChecker emptyFieldChecker){
    _emptyFieldChecker = emptyFieldChecker;
  }
  
  IEmptyFieldChecker getEmptyFieldChecker(){
    if (_emptyFieldChecker == null){
      _emptyFieldChecker = new DefaultEmptyFieldChecker();
    }
    return _emptyFieldChecker;
  }
}
