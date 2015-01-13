package com.celements.copydoc;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface ICopyDocumentRole {

  /**
   * 
   * @param doc1 may not be null
   * @param doc2 may not be null
   * @return true if the given docs are different
   * @throws XWikiException
   */
  boolean checkChanges(XWikiDocument doc1, XWikiDocument doc2) throws XWikiException;

  /**
   * Copies given source doc to given target doc and saves target if it has changed
   * 
   * @param srcDoc source doc, may not be null
   * @param trgDoc target doc, may not be null
   * @return whether target doc has changed or not
   * @throws XWikiException
   */
  public boolean copyAndSave(XWikiDocument srcDoc, XWikiDocument trgDoc
      ) throws XWikiException;
  
  /**
   * Copies given source doc to given target doc
   * 
   * @param srcDoc source doc, may not be null
   * @param trgDoc target doc, may not be null
   * @return whether target doc has changed or not
   * @throws XWikiException
   */
  public boolean copy(XWikiDocument srcDoc, XWikiDocument trgDoc) throws XWikiException;

  /**
   * Reads out the value for the given BaseObject and name
   * @param bObj
   * @param name
   * @return
   * @throws XWikiException
   */
  public Object getValue(BaseObject bObj, String name) throws XWikiException;

}
