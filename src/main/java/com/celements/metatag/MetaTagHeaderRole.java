package com.celements.metatag;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface MetaTagHeaderRole {

  public @NotNull List<MetaTag> getHeaderMetaTags();
}
