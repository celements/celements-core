package com.celements.collections.service;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.collections.ICollectionsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.objects.BaseObject;

@Component("collections")
public class CollectionsScriptService implements ScriptService {

  @Requirement
  ICollectionsService collectionsService;
  
  @Requirement
  Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public List<com.xpn.xwiki.api.Object> getObjectsOrdered(Document doc, 
      DocumentReference classRef,
      String orderField, boolean asc) {
    return getObjectsOrdered(doc, classRef, orderField, asc, null, false);
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
  public List<com.xpn.xwiki.api.Object> getObjectsOrdered(Document doc, 
      DocumentReference classRef, String orderField1, boolean asc1, String orderField2, 
      boolean asc2) {
    List<BaseObject> bObjs = collectionsService.getObjectsOrdered(doc.getDocument(), 
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
