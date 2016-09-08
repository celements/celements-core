package com.celements.model.access;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.WikiReference;

import com.celements.model.context.ModelContext;
import com.celements.model.util.References;
import com.xpn.xwiki.web.Utils;

public abstract class ContextExecutor<T, E extends Throwable> {

  public T execute(@NotNull WikiReference wiki) throws E {
    wiki = References.cloneRef(wiki, WikiReference.class);
    WikiReference currWiki = getContext().getWikiRef();
    try {
      getContext().setWikiRef(wiki);
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
