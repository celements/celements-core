package com.celements.metatag;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface MetaTagCollectorRole {

  public static final String META_CONTEXT_KEY = "celements_meta_tags";

  public void addMetaTag(MetaTagApi tag);

  public String displayAllMetaTags();

}
