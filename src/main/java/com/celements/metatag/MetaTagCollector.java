package com.celements.metatag;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.context.ModelContext;

@Component
public class MetaTagCollector implements MetaTagCollectorRole {

  @Requirement
  ModelContext modelContext;

  @Override
  public void addMetaTag(MetaTagApi tag) {
    Object contextObj = modelContext.getXWikiContext().get(MetaTagCollectorRole.META_CONTEXT_KEY);
    List<MetaTagApi> metaList = getMetaTags(contextObj);
    if (metaList == null) {
      metaList = new ArrayList<>();
      modelContext.getXWikiContext().put(MetaTagCollectorRole.META_CONTEXT_KEY, metaList);
    }
    metaList.add(tag);
  }

  @Override
  public String displayAllMetaTags() {
    StringBuilder sb = new StringBuilder();
    Object contextObj = modelContext.getXWikiContext().remove(
        MetaTagCollectorRole.META_CONTEXT_KEY);
    List<MetaTagApi> metaTags = getMetaTags(contextObj);
    if (metaTags != null) {
      for (MetaTagApi metaTag : metaTags) {
        sb.append(metaTag.display()).append("\n");
      }
    }
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  List<MetaTagApi> getMetaTags(Object contextObj) {
    if (contextObj instanceof List) {
      return (List<MetaTagApi>) contextObj;
    }
    return null;
  }

}
