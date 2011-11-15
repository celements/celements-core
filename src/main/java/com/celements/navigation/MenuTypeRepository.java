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
package com.celements.navigation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class MenuTypeRepository {
  
  private static MenuTypeRepository repositoryInstance;

  private HashMap<String, IMenuTypeRepository> repository;
  
  private MenuTypeRepository() {
    repository = new HashMap<String, IMenuTypeRepository>();
  }

  public static MenuTypeRepository getInstance() {
    if (repositoryInstance == null) {
      repositoryInstance = new MenuTypeRepository();
    }
    return repositoryInstance;
  }

  public IMenuTypeRepository get(String menuTypeName) {
    IMenuTypeRepository writerType = null;
    if ((menuTypeName != null) && (!"".equals(menuTypeName))) {
      writerType  = repository.get(menuTypeName);
    }
    return writerType;
  }

  public boolean put(String menuTypeName, IMenuTypeRepository menuType) {
    if ((menuTypeName != null) && (!"".equals(menuTypeName))
        && !repository.containsKey(menuTypeName)) {
      repository.put(menuTypeName, menuType);
      return true;
    }
    return false;
  }

  public Set<String> getMenuTypeNames() {
    return new HashSet<String>(repository.keySet());
  }

}
