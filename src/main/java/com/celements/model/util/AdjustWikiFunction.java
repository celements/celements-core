package com.celements.model.util;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.google.common.base.Function;

public class AdjustWikiFunction<T extends EntityReference> implements Function<T, T> {

  private final Class<T> type;
  private final WikiReference wikiRef;

  public AdjustWikiFunction(@NotNull Class<T> type, @NotNull WikiReference wikiRef) {
    this.type = type;
    this.wikiRef = wikiRef;
  }

  @Override
  public T apply(T ref) {
    return References.adjustRef(ref, type, wikiRef);
  }

}
