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
import static com.google.common.base.Predicates.*;
import static com.google.common.base.Strings.*;

import java.util.Collection;
import java.util.Comparator;
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

  public static Optional<Comparator<BaseObject>> create(Collection<String> orderFields) {
    return orderFields.stream()
        .map(String::trim).filter(not(String::isEmpty))
        .map(sort -> create(sort.replaceFirst("-", ""), !sort.startsWith("-")))
        .reduce((c1, c2) -> c1.thenComparing(c2));
  }

  private final Comparator<Object> valueComparator;

  private String orderField = "";

  public BaseObjectComparator(String orderField) {
    this.orderField = nullToEmpty(orderField);
    valueComparator = new ObjectComparator();
  }

  public String getOrderField() {
    return orderField;
  }

  @Override
  public int compare(BaseObject obj1, BaseObject obj2) {
    return valueComparator.compare(
        getProperty(obj1, orderField).getValue(),
        getProperty(obj2, orderField).getValue());
  }

  BaseProperty getProperty(BaseObject obj, String field) {
    return Optional.ofNullable(obj)
        .map(o -> o.getField(field))
        .flatMap(prop -> tryCast(prop, BaseProperty.class))
        .orElseGet(BaseProperty::new);
  }
}
