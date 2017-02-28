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

import org.apache.commons.logging.Log;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.AbstractClassPackage;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.BooleanClass;
import com.xpn.xwiki.objects.classes.DBListClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.NumberClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;

/**
 * Extend CelementsClassCollection and make the implementor a named component. Celements
 * then will call your initClasses method on system start once or if it is explicitly
 * asked for.
 *
 * @author fabian pichler since 2.18.0
 * @deprecated since 2.82 instead use {@link AbstractClassDefinition} and
 *             {@link AbstractClassPackage}}
 */
@Deprecated
public abstract class AbstractClassCollection implements IClassCollectionRole {

  @Requirement
  protected Execution execution;

  @Requirement
  protected IModelAccessFacade modelAccess;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  final public void runUpdate() throws XWikiException {
    if (isActivated()) {
      getLogger().debug("calling initClasses for database: " + getContext().getDatabase());
      initClasses();
    } else {
      getLogger().info("skipping not activated class collection: " + getConfigName());
    }
  }

  @Override
  public boolean isActivated() {
    return ("," + getContext().getWiki().getXWikiPreference("activated_classcollections",
        getContext()) + "," + getContext().getWiki().Param("celements.classcollections", "")
        + ",").contains("," + getConfigName() + ",");
  }

  protected XWikiDocument getClassDoc(DocumentReference classRef) {
    try {
      return modelAccess.getOrCreateDocument(classRef);
    } catch (DocumentLoadException docLoadExp) {
      getLogger().error("Failed getting classDoc for classRef '" + classRef + "'", docLoadExp);
      return new XWikiDocument(classRef);
    }
  }

  protected void setContentAndSaveClassDocument(XWikiDocument doc, boolean needsUpdate)
      throws XWikiException {
    if (Strings.nullToEmpty(doc.getContent()).isEmpty()) {
      needsUpdate = true;
      doc.setContent(" ");
    }
    if (needsUpdate) {
      try {
        modelAccess.saveDocument(doc, "update class definition");
      } catch (DocumentSaveException exp) {
        throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
            XWikiException.ERROR_XWIKI_UNKNOWN, "failed to save class update", exp);
      }
    }
  }

  abstract protected void initClasses() throws XWikiException;

  abstract protected Log getLogger();

  protected final boolean addBooleanField(BaseClass bclass, String name, String prettyName,
      String displayType, int defaultValue) {
    if (bclass.get(name) == null) {
      BooleanClass element = new BooleanClass();
      element.setObject(bclass);
      element.setName(name);
      element.setPrettyName(prettyName);
      element.setDisplayType(displayType);
      element.setDefaultValue(defaultValue);
      bclass.addField(name, element);
      return true;
    }
    return false;
  }

  protected final boolean addTextField(BaseClass bclass, String name, String prettyName, int size,
      String validationRegExp, String validationMessage) {
    if (bclass.get(name) == null) {
      StringClass element = new StringClass();
      element.setObject(bclass);
      element.setName(name);
      element.setPrettyName(prettyName);
      element.setSize(size);
      element.setValidationRegExp(validationRegExp);
      element.setValidationMessage(validationMessage);
      bclass.addField(name, element);
      return true;
    }
    return false;
  }

  protected final boolean addTextAreaField(BaseClass bclass, String name, String prettyName,
      int cols, int rows, String validationRegExp, String validationMessage) {
    if (bclass.get(name) == null) {
      TextAreaClass element = new TextAreaClass();
      element.setObject(bclass);
      element.setName(name);
      element.setPrettyName(prettyName);
      element.setSize(cols);
      element.setRows(rows);
      element.setValidationRegExp(validationRegExp);
      element.setValidationMessage(validationMessage);
      bclass.addField(name, element);
      return true;
    }
    return false;
  }

  protected final boolean addNumberField(BaseClass bclass, String name, String prettyName, int size,
      String ntype, String validationRegExp, String validationMessage) {
    if (bclass.get(name) == null) {
      NumberClass element = new NumberClass();
      element.setObject(bclass);
      element.setName(name);
      element.setPrettyName(prettyName);
      element.setSize(size);
      element.setNumberType(ntype);
      element.setValidationRegExp(validationRegExp);
      element.setValidationMessage(validationMessage);
      bclass.addField(name, element);
    }
    return false;
  }

  protected final boolean addDateField(BaseClass bclass, String name, String prettyName,
      String dateFormat, int size, int emptyIsToday, String validationRegExp,
      String validationMessage) {
    if (bclass.get(name) == null) {
      DateClass element = new DateClass();
      element.setObject(bclass);
      element.setName(name);
      element.setPrettyName(prettyName);
      if (dateFormat != null) {
        element.setDateFormat(dateFormat);
      }
      element.setSize(size);
      element.setEmptyIsToday(emptyIsToday);
      element.setDateFormat(dateFormat);
      element.setValidationRegExp(validationRegExp);
      element.setValidationMessage(validationMessage);
      bclass.addField(name, element);
      return true;
    }
    return false;
  }

  protected final boolean addDBListField(BaseClass bclass, String name, String prettyName, int size,
      boolean multiSelect, boolean useSuggest, String sql, String validationRegExp,
      String validationMessage) {
    if (bclass.get(name) == null) {
      DBListClass element = new DBListClass();
      element.setObject(bclass);
      element.setName(name);
      element.setPrettyName(prettyName);
      element.setSize(size);
      element.setMultiSelect(multiSelect);
      element.setDisplayType("select");
      element.setPicker(useSuggest);
      element.setSql(sql);
      element.setValidationRegExp(validationRegExp);
      element.setValidationMessage(validationMessage);
      bclass.addField(name, element);
      return true;
    }
    return false;
  }

}
