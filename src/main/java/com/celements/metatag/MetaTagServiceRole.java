package com.celements.metatag;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface MetaTagServiceRole {

  public static final String META_CONTEXT_KEY = "celements_meta_tags";

  public void addMetaTagToCollector(@NotNull MetaTag tag);

  public @NotNull String displayCollectedMetaTags();

  public void collectHeaderTags();

  public void collectBodyTags();

}
