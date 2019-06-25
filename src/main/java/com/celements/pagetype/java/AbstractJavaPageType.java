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
package com.celements.pagetype.java;

import java.util.HashSet;
import java.util.Set;

import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.google.common.base.Optional;

public abstract class AbstractJavaPageType implements IJavaPageTypeRole {

  @Override
  public Set<String> getCategoryNames() {
    Set<String> categories = new HashSet<>();
    for (IPageTypeCategoryRole ptCat : getCategories()) {
      categories.addAll(ptCat.getAllTypeNames());
    }
    return categories;
  }

  @Override
  public Optional<String> defaultTagName() {
    return Optional.absent();
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    // default implementation: do nothing
  }

  @Override
  public boolean useInlineEditorMode() {
    return false;
  }

}
