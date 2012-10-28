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
package com.celements.pagetype;

import java.util.ArrayList;
import java.util.List;

public class PageTypeReference {

  private String configName;

  private String providerHint;

  private List<String> categories;

  public PageTypeReference(String configName, String providerHint,
      List<String> categories) {
    this.configName = configName;
    this.providerHint = providerHint;
    this.categories = new ArrayList<String>(categories);
  }

  public String getConfigName() {
    return configName;
  }

  public List<String> getCategories() {
    return new ArrayList<String>(categories);
  }

  public String getProviderHint() {
    return providerHint;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PageTypeReference)) {
      return false;
    }
    PageTypeReference thePageTypeRef = (PageTypeReference) obj;
    return thePageTypeRef.getConfigName().equals(this.getConfigName());
  }

  @Override
  public int hashCode() {
    return this.getConfigName().hashCode();
  }

}
