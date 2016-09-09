package com.celements.model.access;

import org.xwiki.model.reference.WikiReference;

import com.celements.model.context.ModelContext;
import com.celements.model.util.References;
import com.xpn.xwiki.web.Utils;

/**
 * ContextExecutor is used to execute code within an altered {@link ModelContext} and set it back
 * after execution. This behaviour is guaranteed within {@link #call()}, for which an implementation
 * has to be provided when subclassing or instantiating.
 *
 * @param <T>
 *          return parameter of {@link #call()} and {@link #execute()}
 * @param <E>
 *          subclass of {@link Throwable} thrown by {@link #call()} and {@link #execute()}
 * @author Marc Sladek
 */
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
