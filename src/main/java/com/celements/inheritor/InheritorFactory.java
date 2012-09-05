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
package com.celements.inheritor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.celements.iterator.DocumentIterator;
import com.celements.iterator.IIteratorFactory;
import com.celements.iterator.XObjectIterator;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;

public class InheritorFactory {

  private IWebUtils _injectedWebUtils;
  private PageLayoutCommand injectedPageLayoutCmd;

  public FieldInheritor getFieldInheritor(final String className,
      final List<String> docList, XWikiContext context) {
    final XWikiContext localContext = context;
    FieldInheritor inheritor = new FieldInheritor();
    inheritor.setIteratorFactory(new IIteratorFactory<XObjectIterator>() {
      public XObjectIterator createIterator() {
        XObjectIterator iterator = new XObjectIterator(localContext);
        iterator.setClassName(className);
        iterator.setDocList(docList);
        return iterator;
      }
    });
    return inheritor;
  }
  
  public ContentInheritor getContentInheritor(final List<String> docList, 
      XWikiContext context) {
    final XWikiContext localContext = context;
    ContentInheritor inheritor = new ContentInheritor();
    inheritor.setIteratorFactory(new IIteratorFactory<DocumentIterator>() {
      public DocumentIterator createIterator() {
        DocumentIterator iterator = new DocumentIterator(localContext);
        iterator.setDocList(docList);
        return iterator;
      }
    });
    return inheritor;
  }

  public FieldInheritor getNavigationFieldInheritor(String className, String fullName,
      XWikiContext context) {
    return getFieldInheritor(className, getWebUtils().getDocumentParentsList(fullName,
        true, context), context);
  }

  public FieldInheritor getConfigDocFieldInheritor(String className, String fullName,
      XWikiContext context) {
    List<String> inheritanceList = new ArrayList<String>();
    inheritanceList.add(fullName.split("\\.")[0] + ".WebPreferences");
    inheritanceList.add("XWiki.XWikiPreferences");
    String pageLayoutForDoc = getPageLayoutCmd().getPageLayoutForDoc(fullName, context);
    if (pageLayoutForDoc != null) {
      inheritanceList.add(pageLayoutForDoc + ".WebHome");
    }
    String skinDocName = context.getWiki().getSpacePreference("skin", context);
    if (skinDocName != null){
      inheritanceList.add(skinDocName);
    }
    return getFieldInheritor(className, inheritanceList, context);
  }

  public void injectPageLayoutCmd(PageLayoutCommand injectedPageLayoutCmd) {
    this.injectedPageLayoutCmd = injectedPageLayoutCmd;
  }

  private PageLayoutCommand getPageLayoutCmd() {
    if (injectedPageLayoutCmd != null) {
      return injectedPageLayoutCmd;
    }
    return new PageLayoutCommand();
  }

  IWebUtils getWebUtils() {
    if (_injectedWebUtils != null) {
      return _injectedWebUtils;
    }
    return WebUtils.getInstance();
  }

  /**
   * FOR TESTS ONLY!!!
   * @param injectedWebUtils
   */
  void inject_TEST_WebUtils(IWebUtils injectedWebUtils) {
    _injectedWebUtils = injectedWebUtils;
  }

  public FieldInheritor getPageLayoutInheritor(String fullName, XWikiContext context) {
    return getFieldInheritor("Celements2.PageType", Arrays.asList(fullName,
        getSpacePreferencesFullName(fullName), "XWiki.XWikiPreferences"), context);
  }

  String getSpacePreferencesFullName(String fullName) {
    return fullName.substring(0, fullName.indexOf('.')) + ".WebPreferences";
  }

}
