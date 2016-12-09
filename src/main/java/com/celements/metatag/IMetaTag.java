package com.celements.metatag;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public abstract class IMetaTag {

  public static MetaTagApi createMetaTag() {
    return new MetaTagApi();
  };

  public abstract void addMetaTag(MetaTagApi tag);

  public abstract String displayAllMetaTags();

}
