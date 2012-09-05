package com.celements.collections.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.collections.ICollectionsService;
import com.celements.iterator.XObjectIterator;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("collections")
public class CollectionsScriptService implements ScriptService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CollectionsScriptService.class);
  
  @Requirement
  ICollectionsService collectionsService;
  
  @Requirement
  Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public List<com.xpn.xwiki.api.Object> getObjectsOrdered(DocumentReference doc, 
      DocumentReference classRef, String orderField, boolean asc) {
    return getObjectsOrdered(doc, classRef, orderField, asc, null, false);
  }

    public List<com.xpn.xwiki.api.Object> getObjectsOrdered(Document doc, 
        DocumentReference classRef, String orderField, boolean asc) {
    return getObjectsOrdered(doc.getDocumentReference(), classRef, orderField, asc, null,
        false);
  }

  public List<com.xpn.xwiki.api.Object> getObjectsOrdered(Document doc, 
      DocumentReference classRef, String orderField1, boolean asc1, String orderField2, 
      boolean asc2) {
    return getObjectsOrdered(doc.getDocumentReference(), classRef, orderField1, asc1, 
        orderField2, asc2);
  }
  
  /**
   * Get a list of Objects for a Document sorted by one or two fields.
   * 
   * @param doc The Document where the Objects are attached.
   * @param classRef The reference to the class of the Objects to return
   * @param orderField1 Field to order the objects by. First priority.
   * @param asc1 Order first priority ascending or descending.
   * @param orderField2 Field to order the objects by. Second priority.
   * @param asc2 Order second priority ascending or descending.
   * @return List of objects ordered as specified
   */
  public List<com.xpn.xwiki.api.Object> getObjectsOrdered(DocumentReference doc, 
      DocumentReference classRef, String orderField1, boolean asc1, String orderField2, 
      boolean asc2) {
    XWikiDocument xdoc = null;
    try {
      xdoc = getContext().getWiki().getDocument(doc, getContext());
    } catch (XWikiException e) {
      LOGGER.error("Could not get Document with reference " + doc, e);
    }
    List<BaseObject> bObjs = collectionsService.getObjectsOrdered(xdoc, 
        classRef, orderField1, asc1, orderField2, asc2);
    List<com.xpn.xwiki.api.Object> objs = new ArrayList<com.xpn.xwiki.api.Object>();
    for (BaseObject bObj : bObjs) {
      com.xpn.xwiki.api.Object obj = new com.xpn.xwiki.api.Object(bObj, getContext());
      if(obj != null) {
        objs.add(obj);
      }
    }
    return objs;
  }
  
}
