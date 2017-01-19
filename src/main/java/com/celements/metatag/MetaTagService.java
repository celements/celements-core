package com.celements.metatag;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

@Component
public class MetaTagService implements MetaTagServiceRole {

  @Requirement
  private Execution execution;

  @Override
  public void addMetaTagToCollector(MetaTag tag) {
    Object contextObj = getExecutionContext().getProperty(MetaTagServiceRole.META_CONTEXT_KEY);
    List<MetaTag> metaList = getMetaTags(contextObj);
    if (metaList == null) {
      metaList = new ArrayList<>();
      getExecutionContext().setProperty(MetaTagServiceRole.META_CONTEXT_KEY, metaList);
    }
    metaList.add(tag);
  }

  @Override
  public String displayCollectedMetaTags() {
    StringBuilder sb = new StringBuilder();
    Object contextObj = getExecutionContext().getProperty(MetaTagServiceRole.META_CONTEXT_KEY);
    getExecutionContext().removeProperty(MetaTagServiceRole.META_CONTEXT_KEY);
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

  private ExecutionContext getExecutionContext() {
    return execution.getContext();
  }

}
