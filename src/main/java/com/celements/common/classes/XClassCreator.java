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
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAccessException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class XClassCreator implements XClassCreatorRole {

  private static Logger LOGGER = LoggerFactory.getLogger(XClassCreator.class);

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected IWebUtilsService webUtils;

  @Requirement
  protected ConfigurationSource configSrc;

  @Override
  public void createXClasses() throws DocumentAccessException {
    LOGGER.info("create classes for database '{}'", webUtils.getWikiRef());
    for (ClassDefinition classDef : Utils.getComponentList(ClassDefinition.class)) {
      if (!isBlacklisted(classDef)) {
        createXClass(classDef);
        LOGGER.debug("created class '{}'", classDef.getName());
      } else {
        LOGGER.info("skipping blacklisted class '{}'", classDef.getName());
      }
    }
  }

  private boolean isBlacklisted(ClassDefinition classDef) {
    String key = "celements.classdefinition.blacklist";
    return configSrc.containsKey(key) && configSrc.<List<?>>getProperty(key).contains(
        classDef.getName());
  }

  private void createXClass(ClassDefinition classDef) throws DocumentAccessException {
    XWikiDocument classDoc = modelAccess.getOrCreateDocument(classDef.getClassRef());
    BaseClass bClass = classDoc.getXClass();
    boolean needsUpdate = classDoc.isNew();
    if (classDef.isInternalMapping() && !bClass.hasInternalCustomMapping()) {
      bClass.setCustomMapping("internal");
      needsUpdate = true;
    }
    for (ClassField<?> field : classDef.getFields()) {
      if (bClass.get(field.getName()) == null) {
        PropertyInterface xField = field.getXField();
        xField.setObject(bClass);
        bClass.addField(field.getName(), xField);
        needsUpdate = true;
      }
    }
    if (needsUpdate) {
      modelAccess.saveDocument(classDoc);
    }
  }

}
