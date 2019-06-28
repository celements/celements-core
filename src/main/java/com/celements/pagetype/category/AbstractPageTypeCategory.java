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
package com.celements.pagetype.category;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractPageTypeCategory implements IPageTypeCategoryRole {

  private ImmutableSet<String> allTypeNames;

  @Override
  public Set<String> getAllTypeNames() {
    if (allTypeNames == null) {
      Set<String> allTypes = new HashSet<>();
      allTypes.add(getTypeName());
      allTypes.addAll(getDeprecatedNames());
      allTypeNames = ImmutableSet.copyOf(allTypes);
    }
    return allTypeNames;
  }

  @Override
  public Set<String> getDeprecatedNames() {
    return Collections.emptySet();
  }

}
