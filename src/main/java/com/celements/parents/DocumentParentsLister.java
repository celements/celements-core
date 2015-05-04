package com.celements.parents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.service.XDocRecursionException;

@Component
public class DocumentParentsLister implements IDocumentParentsListerRole {

  private static Logger _LOGGER = LoggerFactory.getLogger(DocumentParentsLister.class);

  @Requirement
  Map<String, IDocParentProviderRole> docParentProviderMap;

  @Override
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef,
      boolean includeDoc) {
    ArrayList<DocumentReference> docParents = new ArrayList<DocumentReference>();
    ArrayList<String> secondKeyMaster = new ArrayList<String>(
        docParentProviderMap.keySet());
    secondKeyMaster.remove(XDocParents.DOC_PROVIDER_NAME);
    ArrayList<String> secondKeys = new ArrayList<String>(secondKeyMaster);
    String firstKey = XDocParents.DOC_PROVIDER_NAME;
    try {
      if (includeDoc) {
        docParents.add(docRef);
      }
      boolean hasMore = false;
      do {
        List<DocumentReference> parentList = docParentProviderMap.get(firstKey
            ).getDocumentParentsList(docRef);
        joinParentLists(docParents, parentList);
        docRef = docParents.get(docParents.size() - 1);
        hasMore = false;
        if (secondKeys.size() > 0) {
          int secKeyIndex = -1;
          parentList = Collections.emptyList();
          while ((secKeyIndex < secondKeys.size()) && ((parentList == null)
              || parentList.isEmpty())) {
            secKeyIndex = secKeyIndex + 1;
            parentList = docParentProviderMap.get(secondKeys.get(secKeyIndex)
                ).getDocumentParentsList(docRef);
          }
          hasMore = ((parentList != null) && !parentList.isEmpty());
          if (hasMore) {
            joinParentLists(docParents, parentList);
            docRef = docParents.get(docParents.size() - 1);
            secondKeys = new ArrayList<String>(secondKeyMaster);
            secondKeys.remove(secKeyIndex);
          }
        }
      } while (hasMore);
    } catch (XDocRecursionException recExp) {
      _LOGGER.info("Recursion in document parents found [" + recExp + "].");
    }
    return docParents;
  }

  private void joinParentLists(ArrayList<DocumentReference> docParents,
      List<DocumentReference> parentList) throws XDocRecursionException {
    for (DocumentReference parentRef : parentList) {
      if (!docParents.contains(parentRef)) {
        docParents.add(parentRef);
      } else {
        throw new XDocRecursionException(parentRef);
      }
    }
  }

}
