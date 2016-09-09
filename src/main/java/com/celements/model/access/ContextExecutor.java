package com.celements.model.access;

import org.xwiki.model.reference.WikiReference;

import com.celements.model.context.ModelContext;
import com.celements.model.util.References;
import com.xpn.xwiki.web.Utils;

public abstract class ContextExecutor<T, E extends Throwable> {

  private WikiReference wiki;

  public ContextExecutor<T, E> inWiki(WikiReference wiki) {
    wiki = References.cloneRef(wiki, WikiReference.class);
    return this;
  }

  public T execute() throws E {
    WikiReference currWiki = getContext().getWikiRef();
    try {
      if (wiki != null) {
        getContext().setWikiRef(wiki);
      }
      return call();
    } finally {
      getContext().setWikiRef(currWiki);
    }
  }

  protected abstract T call() throws E;

  private ModelContext getContext() {
    return Utils.getComponent(ModelContext.class);
  }

}
