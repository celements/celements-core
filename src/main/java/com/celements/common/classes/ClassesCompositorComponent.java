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
package com.celements.common.classes;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.model.access.exception.DocumentAccessException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component
public class ClassesCompositorComponent implements IClassesCompositorComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClassesCompositorComponent.class);

  @Requirement
  private XClassCreator classCreator;

  @Requirement
  private Map<String, IClassCollectionRole> classCollectionMap;

  @Requirement
  private Map<String, ICelementsClassCollection> classCollectionMap_old;

  @Requirement
  private Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public void checkClasses() {
    LOGGER.info("start checkClasses for wiki '{}'", getContext().getDatabase());
    try {
      classCreator.createXClasses();
    } catch (DocumentAccessException dae) {
      LOGGER.error("Exception creating classes", dae);
    }
    checkClassCollections();
    checkOldClassCollections();
    LOGGER.debug("finish checkClasses for wiki '{}'", getContext().getDatabase());
  }

  @Override
  @Deprecated
  public void checkAllClassCollections() {
    checkClasses();
  }

  @Deprecated
  void checkOldClassCollections() {
    for (ICelementsClassCollection classCollection : classCollectionMap_old.values()) {
      try {
        classCollection.runUpdate(getContext());
      } catch (XWikiException xwe) {
        LOGGER.error("Exception checking class collection '{}'", classCollection.getConfigName(),
            xwe);
      }
    }
  }

  @Deprecated
  void checkClassCollections() {
    for (IClassCollectionRole classCollection : classCollectionMap.values()) {
      try {
        classCollection.runUpdate();
      } catch (XWikiException xwe) {
        LOGGER.error("Exception checking class collection '{}'", classCollection.getConfigName(),
            xwe);
      }
    }
  }

  @Override
  public boolean isActivated(String name) {
    // TODO build map in initialize with key name and value hint to avoid loop here
    for (IClassCollectionRole classCollection : classCollectionMap.values()) {
      if (classCollection.getConfigName().equals(name)) {
        return classCollection.isActivated();
      }
    }
    return false;
  }

}
