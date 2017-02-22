package com.celements.metatag;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

@Component
public class MetaTagService implements MetaTagServiceRole {

  @Requirement
  private Execution execution;

  @Requirement
  private List<MetaTagHeaderRole> headerTagImpls;

  @Override
  public void addMetaTagToCollector(@NotNull MetaTag tag) {
    Object contextObj = getExecutionContext().getProperty(MetaTagServiceRole.META_CONTEXT_KEY);
    List<MetaTag> metaList = getMetaTags(contextObj);
    if (metaList == null) {
      metaList = new ArrayList<>();
      getExecutionContext().setProperty(MetaTagServiceRole.META_CONTEXT_KEY, metaList);
    }
    metaList.add(tag);
  }

  @Override
  public @NotNull String displayCollectedMetaTags() {
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

  @Override
  public void loadHeaderTags() {
    for (MetaTagHeaderRole headerTagImpl : headerTagImpls) {
      for (MetaTag tag : headerTagImpl.getHeaderMetaTags()) {
        addMetaTagToCollector(tag);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Nullable
  List<MetaTag> getMetaTags(@Nullable Object contextObj) {
    if (contextObj instanceof List) {
      return (List<MetaTag>) contextObj;
    }
    return null;
  }

  private @NotNull ExecutionContext getExecutionContext() {
    return execution.getContext();
  }

}
