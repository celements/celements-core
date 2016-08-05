package com.celements.model.context;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWikiContext;

public class DefaultModelContext implements IModelContext {

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public WikiReference getCurrentWiki() {
    return new WikiReference(getContext().getDatabase());
  }

}
