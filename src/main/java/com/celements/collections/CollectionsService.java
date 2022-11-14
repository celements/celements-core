package com.celements.collections;

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.web.comparators.BaseObjectComparator;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
@Deprecated
public class CollectionsService implements ICollectionsService {

  @Deprecated
  @Override
  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField, boolean asc) {
    return getObjectsOrdered(doc, classRef, orderField, asc, null, false);
  }

  @Deprecated
  @Override
  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField1, boolean asc1, String orderField2, boolean asc2) {
    if (doc == null) {
      return new ArrayList<>();
    }
    return XWikiObjectFetcher.on(doc)
        .filter(new ClassReference(classRef))
        .stream()
        .sorted(BaseObjectComparator.create(orderField1, asc1)
            .thenComparing(BaseObjectComparator.create(orderField2, asc2)))
        .collect(toList());
  }
}
