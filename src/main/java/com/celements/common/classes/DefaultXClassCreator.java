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

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.ClassPackage;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;

@Singleton
@Component
public class DefaultXClassCreator implements XClassCreator {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultXClassCreator.class);

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected ModelContext context;

  @Requirement
  private List<ClassPackage> classPackages;

  @Override
  public void createXClasses() {
    LOGGER.info("create classes for database '{}'", context.getWikiRef());
    for (ClassPackage classPackage : classPackages) {
      if (classPackage.isActivated()) {
        try {
          createXClasses(classPackage);
        } catch (XClassCreateException exc) {
          LOGGER.error("failed to create classes for package '{}'", classPackage.getName(), exc);
        }
      } else {
        LOGGER.info("skipping package '{}'", classPackage.getName());
      }
    }
  }

  @Override
  public void createXClasses(ClassPackage classPackage) throws XClassCreateException {
    LOGGER.debug("creating package '{}'", classPackage.getName());
    for (ClassDefinition classDef : classPackage.getClassDefinitions()) {
      if (!classDef.isBlacklisted()) {
        createXClass(classDef);
      } else {
        LOGGER.info("skipping blacklisted class '{}'", classDef.getName());
      }
    }
  }

  @Override
  public void createXClass(ClassDefinition classDef) throws XClassCreateException {
    LOGGER.debug("creating class '{}'", classDef.getName());
    XWikiDocument classDoc = modelAccess.getOrCreateDocument(
        classDef.getClassReference().getDocRef());
    BaseClass bClass = generateXClass(classDef);
    if (!classDoc.getXClass().equals(bClass)) {
      try {
        classDoc.setXClass(bClass);
        modelAccess.saveDocument(classDoc, "created/updated XClass");
      } catch (DocumentSaveException exc) {
        throw new XClassCreateException(exc);
      }
    }
  }

  @Override
  public BaseClass generateXClass(ClassDefinition classDef) {
    BaseClass bClass = new BaseClass();
    DocumentReference classDocRef = classDef.getClassReference().getDocRef();
    bClass.setDocumentReference(classDocRef);
    bClass.setXClassReference(classDocRef);
    if (classDef.isInternalMapping() && !bClass.hasInternalCustomMapping()) {
      bClass.setCustomMapping("internal");
    }
    for (ClassField<?> field : classDef.getFields()) {
      if (bClass.get(field.getName()) == null) {
        PropertyInterface xField = field.getXField();
        xField.setObject(bClass);
        bClass.addField(field.getName(), xField);
      }
    }
    return bClass;
  }

}
