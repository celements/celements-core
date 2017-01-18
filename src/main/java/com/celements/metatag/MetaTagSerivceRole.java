package com.celements.metatag;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface MetaTagSerivceRole {

  public static final String META_CONTEXT_KEY = "celements_meta_tags";

  public void addMetaTagToCollector(MetaTag tag);

  public String displayCollectedMetaTags();

}
