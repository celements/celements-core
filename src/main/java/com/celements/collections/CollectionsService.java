package com.celements.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.comparators.BaseObjectComparator;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class CollectionsService implements ICollectionsService {
  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
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
  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField1, boolean asc1, String orderField2, boolean asc2) {
    List<BaseObject> resultList = new ArrayList<BaseObject>();
    if(doc != null) {
      List<BaseObject> allObjects = doc.getXObjects(classRef);
      if(allObjects != null) {
        for (BaseObject obj : allObjects) {
          if(obj != null) {
            resultList.add(obj);
          }
        }
      }
      Collections.sort(resultList, new BaseObjectComparator(orderField1, asc1, 
          orderField2, asc2));
    }
    return resultList;
  }
}
