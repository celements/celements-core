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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

/**
 * Iterator class
 * @author Philipp Buser
 *
 */
public class XObjectIterator implements Iterator<BaseObject>, Iterable<BaseObject> {

  private static Log mLogger = LogFactory.getFactory().getInstance(XObjectIterator.class);

  private List<String> _docList;
  private String _xwikiClassName;
  private XWikiContext _context;
  private XWiki _xwiki;
  private Iterator<BaseObject> _objectIterator;
  private Iterator<String> _docIterator;
  private XWikiDocument _currentDoc;
  private BaseObject _nextObject;
  private String _key;
  private Object _value;

  /**
   * Constructor
   * @param XWiki context
   */
  public XObjectIterator(XWikiContext context) {
   _context = context;
   _xwiki = _context.getWiki();
  }
  
  /**
   * Checks if iterator has a next object
   */
  public boolean hasNext() {
    while ((_nextObject == null) && (getDocIterator().hasNext()
        || getObjectIterator().hasNext())) {
      if (getObjectIterator().hasNext()) {
        _nextObject = getObjectIterator().next();
        if (_key != null && _value != null){
          if (!isValidObject()){
            _nextObject = null;
          }
        }
      } else {
        moveToNextDoc();
      }
    }
    return (_nextObject != null);
  }

  boolean isValidObject() {
      return ((_nextObject != null) && (_value.equals(getValue())));
  }

  private Object getValue() {
    try {
      if (_nextObject.get(_key) != null){
        return ((BaseProperty)_nextObject.get(_key)).getValue();
      }
    } catch (XWikiException exp) {
      mLogger.warn("Failed to get a propery with key [" + _key + "]", exp);
    }
    return null;
  }

  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration.
   * @exception NoSuchElementException iteration has no more elements.
   */
  public BaseObject next() {
    if (hasNext()) {
      BaseObject theNextObject = _nextObject;
      _nextObject = null;
      return theNextObject;
    }
    throw new NoSuchElementException();
  }

  /**
   * Moves to the next document
   */
  void moveToNextDoc() {
    try {
      _currentDoc = _xwiki.getDocument(getDocIterator().next(), _context);
      _objectIterator = null;
    } catch (XWikiException exp) {
      // If getDocument failed, getDocIterator still moved on by one document in list.
      // Yet, _objectIterator is not reset and hasNext is still false.
      // Hence the while loop in 'next' moves on to the next document in list.
      mLogger.error("Failed to get next xwiki document.", exp);
    }
  }
  
  /**
   * Gets the object iterator
   * @return XObjectIterator
   */
  Iterator<BaseObject> getObjectIterator() {
    if (_objectIterator == null) {
      _objectIterator = getObjectsForCurrentDoc().iterator();
    }
    return _objectIterator;
  }

  /**
   * Gets the document iterator
   * @return XObjectIterator
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
   * Get the objects for the current document
   * @return
   */
  List<BaseObject> getObjectsForCurrentDoc() {
    if (_xwikiClassName == null) {
      throw new IllegalStateException("Classname cannot be null");
    }
    if (getCurrentDoc() == null) {
      return Collections.emptyList();
    }
    Vector<BaseObject> objs = getCurrentDoc().getObjects(_xwikiClassName);
    if (objs != null) {
      return objs;
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Gets the current document
   * @return current document
   */
  XWikiDocument getCurrentDoc() {
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
   * FOR TESTS ONLY!!!
   * @param nextObject
   */
  void inject_NextObject(BaseObject nextObject) {
    _nextObject = nextObject;
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
    _objectIterator = null;
    _docIterator = null;
    _nextObject = null;
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

  /**
   * Sets the class name
   * @param class name
   */
  public void setClassName(String className) {
    _xwikiClassName = className;
  }

  /**
   * Gets the class name
   * @return class name
   */
  public String getClassName() {
    return _xwikiClassName;
  }
  
  /**
   * Sets the filter
   * @param filter
   */
  public void setFilter(String key, Object value){
    _key = key;
    _value = value;
  }
  
  String getFilterKey(){
    return _key;
  }
  
  Object getFilterValue(){
    return _value;
  }

  public Iterator<BaseObject> iterator() {
    return this;
  }
}
