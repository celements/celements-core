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
    if (contextObj instanceof List) {
      @SuppressWarnings("unchecked")
      List<MetaTagApi> metaList = (List<MetaTagApi>) contextObj;
      metaList.add(tag);
    } else {
      List<MetaTagApi> metaList = new ArrayList<>();
      metaList.add(tag);
      modelContext.getXWikiContext().put(MetaTagCollectorRole.META_CONTEXT_KEY, metaList);
    }
  }

  @Override
  public String displayAllMetaTags() {
    StringBuilder sb = new StringBuilder();
    Object contextObj = modelContext.getXWikiContext().remove(MetaTagCollectorRole.META_CONTEXT_KEY);
    if (contextObj instanceof List) {
      @SuppressWarnings("unchecked")
      List<MetaTagApi> metaList = (List<MetaTagApi>) contextObj;
      for (MetaTagApi metaTag : metaList) {
        sb.append(metaTag.display()).append("\n");
      }
    }
    return sb.toString();
  }

}
