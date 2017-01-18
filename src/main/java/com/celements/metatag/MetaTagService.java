package com.celements.metatag;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.context.ModelContext;

@Component
public class MetaTagService implements MetaTagSerivceRole {

  @Requirement
  ModelContext modelContext;

  @Override
  public void addMetaTagToCollector(MetaTag tag) {
    Object contextObj = modelContext.getXWikiContext().get(MetaTagSerivceRole.META_CONTEXT_KEY);
    List<MetaTag> metaList = getMetaTags(contextObj);
    if (metaList == null) {
      metaList = new ArrayList<>();
      modelContext.getXWikiContext().put(MetaTagSerivceRole.META_CONTEXT_KEY, metaList);
    }
    metaList.add(tag);
  }

  @Override
  public String displayCollectedMetaTags() {
    StringBuilder sb = new StringBuilder();
    Object contextObj = modelContext.getXWikiContext().remove(
        MetaTagSerivceRole.META_CONTEXT_KEY);
    List<MetaTag> metaTags = getMetaTags(contextObj);
    if (metaTags != null) {
      for (MetaTag metaTag : metaTags) {
        sb.append(metaTag.display()).append("\n");
      }
    }
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  List<MetaTag> getMetaTags(Object contextObj) {
    if (contextObj instanceof List) {
      return (List<MetaTag>) contextObj;
    }
    return null;
  }

}
