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
package com.celements.web.utils;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.DBListClass;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StringClass;

public class SuggestBaseClass {
  private static Log mLogger = LogFactory.getFactory().getInstance(SuggestBaseClass.class);
  private PropertyClass bc;
  
  public SuggestBaseClass(DocumentReference classreference, String fieldname, 
      XWikiContext context) {
    try {
      XWikiDocument doc = context.getWiki().getDocument(classreference, context);
      bc = (PropertyClass)doc.getXClass().get(fieldname);
    } catch (XWikiException e) {
      mLogger.error("Exception getting XWikiDocument for class " + classreference);
    }
  }
  

  public List<String> getListFromString(String value) {
    return ListClass.getListFromString(value);
  }
  
  public String getSeparators() {
    if(bc instanceof ListClass) {
      return ((ListClass)bc).getSeparators();
    }
    return null;
  }
  
  public String getSql() {
    if(bc instanceof DBListClass) {
      return ((DBListClass)bc).getSql();
    }
    return null;
  }
  
  public Boolean isMultiSelect() {
    if(bc instanceof ListClass) {
      return ((ListClass)bc).isMultiSelect();
    }
    return null;
  }
  
  public Boolean isRelationalStorage() {
    if(bc instanceof ListClass) {
      return ((ListClass)bc).isRelationalStorage();
    }
    return null;
  }
  
  /**
   * isPicker represents the class editor field 'Use Suggest'. Only ListClass and 
   * StringClass have the possibility of suggest.
   * @return if the edit view of the specified field should use suggest. Returns null for
   *          fields with no suggest possibility.
   */
  public Boolean isPicker() {
    if(bc instanceof ListClass) {
      return ((ListClass)bc).isPicker();
    } else if(bc instanceof StringClass) {
      return ((StringClass)bc).isPicker();
    }
    return null;
  }
  
  public void setPicker(boolean value) {
    if(bc instanceof ListClass) {
      ((ListClass)bc).setPicker(value);
    } else if(bc instanceof StringClass) {
      ((StringClass)bc).setPicker(value);
    }
  }
}
