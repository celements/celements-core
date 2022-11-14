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

import static com.celements.common.MoreObjectsCel.*;
import static com.google.common.base.Strings.*;

import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

public class BaseObjectComparator implements Comparator<BaseObject> {

  public static Comparator<BaseObject> create(String orderField) {
    return new BaseObjectComparator(orderField);
  }

  public static Comparator<BaseObject> reversed(String orderField) {
    return new BaseObjectComparator(orderField).reversed();
  }

  public static Comparator<BaseObject> create(String orderField, boolean asc) {
    return asc ? create(orderField) : reversed(orderField);
  }

  private String orderField = "";

  public BaseObjectComparator(String orderField) {
    this.orderField = nullToEmpty(orderField);
  }

  public String getOrderField() {
    return orderField;
  }

  @Override
  public int compare(BaseObject obj1, BaseObject obj2) {
    Object val1 = getProperty(obj1, orderField).getValue();
    Object val2 = getProperty(obj2, orderField).getValue();
    if ((val1 instanceof Integer) && (val2 instanceof Integer)) {
      return ((Integer) val1).compareTo((Integer) val2);
    } else if ((val1 instanceof Long) && (val2 instanceof Long)) {
      return ((Long) val1).compareTo((Long) val2);
    } else if ((val1 instanceof Float) && (val2 instanceof Float)) {
      return ((Float) val1).compareTo((Float) val2);
    } else if ((val1 instanceof Double) && (val2 instanceof Double)) {
      return ((Double) val1).compareTo((Double) val2);
    } else if ((val1 instanceof Date) && (val2 instanceof Date)) {
      return ((Date) val1).compareTo((Date) val2);
    } else {
      return Objects.toString(val1, "").compareTo(Objects.toString(val2, ""));
    }
  }

  BaseProperty getProperty(BaseObject obj, String field) {
    return Optional.ofNullable(obj.getField(field))
        .flatMap(prop -> tryCast(prop, BaseProperty.class))
        .orElseGet(BaseProperty::new);
  }
}
