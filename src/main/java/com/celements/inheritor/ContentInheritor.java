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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.iterator.DocumentIterator;
import com.celements.iterator.IIteratorFactory;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public class ContentInheritor {

  private IIteratorFactory<DocumentIterator> _iteratorFactory;
  private IEmptyDocumentChecker _emptyDocumentChecker;
  private static Log mLogger = LogFactory.getFactory().getInstance(FieldInheritor.class);
  private String _language;
  
  public ContentInheritor(){
  }
  
  public void setIteratorFactory(IIteratorFactory<DocumentIterator> iteratorFactory) {
    _iteratorFactory = iteratorFactory;
  }

  protected IIteratorFactory<DocumentIterator> getIteratorFactory() {
    return _iteratorFactory;
  }
  
  public void setLanguage(String language){
    _language = language;
  }
  protected String getLanguage(){
    return _language;
  }
  
  public String getTitle(){
    return getTitle("");
  }
  
  public String getTitle(String defaultValue){
    if (getDocument() != null)
      return getDocument().getTitle();
    else
      return defaultValue;
  }
  
  public String getTranslatedTitle(XWikiContext context){
    return getTranslatedTitle(context, "");
  }
  
  public String getTranslatedTitle(XWikiContext context, String defaultValue){
    if (getTranslatedDocument(context) != null)
      return getTranslatedDocument(context).getTitle();
    else 
      return defaultValue;
  }
  
  public String getContent(){
    return getContent("");
  }
  
  public String getContent(String defaultValue){
    if (getDocument() != null)
      return getDocument().getContent(); 
    else
      return defaultValue;
  }
  
  public String getTranslatedContent(XWikiContext context){
    return getTranslatedContent(context, "");
  }
  
  public String getTranslatedContent(XWikiContext context, String defaultValue){
    if (getTranslatedDocument(context) != null)
      return getTranslatedDocument(context).getContent();
    else
      return defaultValue;
  }
  
  XWikiDocument getDocument(){
    return getDoc(null);
  }
  
  XWikiDocument getTranslatedDocument(XWikiContext context){
    if (_language == null){
      throw new IllegalStateException("No language given.");
    }
    return getDoc(context);
  }

  private XWikiDocument getDoc(XWikiContext context) {
    if (getIteratorFactory() == null) {
      throw new IllegalStateException("No IteratorFactory given.");
    }
    DocumentIterator iterator = getIteratorFactory().createIterator();
    while(iterator.hasNext()){
      try {
        XWikiDocument doc = iterator.next();
        if (context != null && _language != doc.getDefaultLanguage()){
          doc = doc.getTranslatedDocument(_language, context);
        }
        if (!getEmptyDocumentChecker().isEmpty(doc)) {
          return doc;
        }
      } catch (Exception exp) {
        mLogger.warn("Failed to get translated document.", exp);
      }
    }
    return null;
  }
  
  public void setEmptyDocumentChecker(IEmptyDocumentChecker emptyDocumentChecker){
    _emptyDocumentChecker = emptyDocumentChecker;
  }
  
  IEmptyDocumentChecker getEmptyDocumentChecker(){
    if (_emptyDocumentChecker == null){
      _emptyDocumentChecker = new DefaultEmptyDocumentChecker();
    }
    return _emptyDocumentChecker;
  }
}
