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

import java.util.List;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAccessException;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * Can be implemented to create custom {@link BaseClass} definitions CelementsClassCollection and
 * make the implementor a named component.
 * Celements then will call your initClasses method on system start once or if it
 * is explicitly asked for.
 *
 * @author Marc Sladek
 */
public abstract class AbstractClassCreator implements IClassCreatorRole {

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected IWebUtilsService webUtils;

  @Requirement
  protected ConfigurationSource configSrc;

  @Override
  public void createClasses() throws DocumentAccessException {
    if (isActivated()) {
      getLogger().debug("{} - define classes for database '{}'", getName(), webUtils.getWikiRef());
      for (ClassDefinition classDef : getClassDefinitions()) {
        createClassDefinition(classDef);
        getLogger().debug("{} - defined class '{}'", getName(), classDef.getClassRef());
      }
    } else {
      getLogger().info("{} - skipping not activated class definition", getName());
    }
  }

  @Override
  public boolean isActivated() {
    String key = "celements.classcollections";
    return configSrc.containsKey(key) && configSrc.<List<?>>getProperty(key).contains(getName());
  }

  abstract protected List<ClassDefinition> getClassDefinitions();

  private void createClassDefinition(ClassDefinition classDef) throws DocumentAccessException {
    XWikiDocument classDoc = modelAccess.getOrCreateDocument(classDef.getClassRef());
    BaseClass bClass = classDoc.getXClass();
    boolean needsUpdate = classDoc.isNew();
    if (classDef.isInternalMapping() && !bClass.hasInternalCustomMapping()) {
      bClass.setCustomMapping("internal");
      needsUpdate = true;
    }
    needsUpdate |= classDef.defineProperties(bClass);
    if (needsUpdate) {
      modelAccess.saveDocument(classDoc);
    }
  }

  protected abstract Logger getLogger();

}
