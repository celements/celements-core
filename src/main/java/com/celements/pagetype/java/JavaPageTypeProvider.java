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

import static com.google.common.collect.ImmutableMap.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeReference;
import com.google.common.base.Suppliers;

@Component(JavaPageTypeProvider.PROVIDER_HINT)
public class JavaPageTypeProvider implements IPageTypeProviderRole {

  public static final String PROVIDER_HINT = "com.celements.JavaPageTypeProvider";

  @Requirement
  private List<IJavaPageTypeRole> javaPageTypes;

  private final Supplier<Map<PageTypeReference, IJavaPageTypeRole>> javaPageTypeRefs = Suppliers
      .memoize(this::buildTypeRefsMap);

  @Override
  public List<PageTypeReference> getPageTypes() {
    return new ArrayList<>(javaPageTypeRefs.get().keySet());
  }

  Map<PageTypeReference, IJavaPageTypeRole> buildTypeRefsMap() {
    return javaPageTypes.stream().collect(toImmutableMap(
        pt -> new PageTypeReference(pt.getName(), PROVIDER_HINT, pt.getCategoryNames()),
        pt -> pt));
  }

  @Override
  public IPageTypeConfig getPageTypeByReference(PageTypeReference pageTypeRef) {
    if (javaPageTypeRefs.get().containsKey(pageTypeRef)) {
      return new DefaultPageTypeConfig(javaPageTypeRefs.get().get(pageTypeRef));
    }
    return null;
  }

}
