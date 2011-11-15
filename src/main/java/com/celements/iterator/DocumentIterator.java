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
package com.celements.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class DocumentIterator implements Iterator<XWikiDocument>, Iterable<XWikiDocument> {

  private static Log mLogger = LogFactory.getFactory().getInstance(XObjectIterator.class);

  private List<String> _docList;
  private XWikiContext _context;
  private XWiki _xwiki;
  private Iterator<String> _docIterator;
  private XWikiDocument _currentDoc;

  /**
   * Constructor
   * @param XWiki context
   */
  public DocumentIterator(XWikiContext context) {
   _context = context;
   _xwiki = _context.getWiki();
  }
  
  /**
   * Checks if iterator has a next object
   */
  public boolean hasNext() {
    while ((_currentDoc == null) && (getDocIterator().hasNext())) {
      moveToNextDoc();
    }
    return (_currentDoc != null);
  }
  
  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration.
   * @exception NoSuchElementException iteration has no more elements.
   */
  public XWikiDocument next() {
    if (hasNext()) {
      XWikiDocument theCurrentDoc = _currentDoc;
      _currentDoc = null;
      return theCurrentDoc;
    }
    throw new NoSuchElementException();
  }

  /**
   * Moves to the next document
   */
  void moveToNextDoc() {
    try {
      String fullname;
      do{
        fullname = getDocIterator().next();
      } while (!_xwiki.exists(fullname, _context) && getDocIterator().hasNext());
      if (_xwiki.exists(fullname, _context)){
        _currentDoc = _xwiki.getDocument(fullname, _context);
      }
      else{
        _currentDoc = null;
      }
    } catch (XWikiException exp) {
      // If getDocument failed, getDocIterator still moved on by one document in list.
      // Hence the while loop in 'next' moves on to the next document in list.
      mLogger.error("Failed to get next xwiki document.", exp);
    }
  }
  
  /**
   * 
   * @return
   */
  Iterator<String> getDocIterator() {
    if (getDocList() == null) {
      throw new IllegalStateException("No doc list set.");
    }
    if (_docIterator == null) {
      _docIterator = getDocList().iterator();
    }
    return _docIterator;
  }

  /**
   * Gets the current document
   * @return current document
   */
  XWikiDocument getCurrentDoc() {
    if ((_currentDoc == null) && (getDocIterator().hasNext())) {
      moveToNextDoc();
    }
    return _currentDoc;
  }

  /**
   * FOR TESTS ONLY!!!
   * @param testCurrentDoc
   */
  void inject_CurrentDoc(XWikiDocument testCurrentDoc) {
    _currentDoc = testCurrentDoc;
  }
  

  /**
   * Remove is not supported
   * @throws UnsupportedOperationException
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }

  /**
   * Set the document list
   * @param document list
   */
  public void setDocList(List<String> docList) {
    _docIterator = null;
    _docList = docList;
  }

  /**
   * Gets the document list
   * @return document list
   */
  List<String> getDocList() {
    return _docList;
  }

  /**
   * Get a copy of the document list
   * @return document list
   */
  public List<String> getDocListCopy() {
    return new ArrayList<String>(_docList);
  }

  public Iterator<XWikiDocument> iterator() {
    return this;
  }
}
