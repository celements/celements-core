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

import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.BaseStringProperty;
import com.xpn.xwiki.objects.DateProperty;
import com.xpn.xwiki.objects.ListProperty;
import com.xpn.xwiki.objects.NumberProperty;

public class DefaultEmptyFieldChecker implements IEmptyFieldChecker {
  
  final public boolean isEmpty(BaseProperty property) {
    if ((property != null) && (property.getValue() != null)) {
      if (property instanceof BaseStringProperty) {
        return isEmptyString((BaseStringProperty)property);
      }
      if (property instanceof NumberProperty){
        return isEmptyNumber((NumberProperty)property);
      }   
      if (property instanceof DateProperty) {
        return isEmptyDate((DateProperty)property);
      }
      if (property instanceof ListProperty) {
        return isEmptyList((ListProperty)property);
      }
    }
    return true;
  }
  
  public boolean isEmptyString(BaseStringProperty property){
    return property.getValue().equals("");
  }
  
  public boolean isEmptyNumber(NumberProperty property){
    return (property.getValue().toString().equals("0") ||
        property.getValue().toString().equals("0.0"));
  }
  
  public boolean isEmptyDate(DateProperty property){
    return (property.getValue() == null);
  }
  
  public boolean isEmptyList(ListProperty property){
    return (property.getList().size() == 0);
  }
}
