package com.celements.model.access.object.xwiki;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import com.celements.model.access.object.AbstractObjectFetcher;
import com.celements.model.access.object.ObjectBridge;
import com.celements.model.access.object.restriction.ObjectQuery;
import com.celements.model.access.object.restriction.ObjectQueryBuilder;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@NotThreadSafe
public class XWikiObjectFetcher extends AbstractObjectFetcher<XWikiDocument, BaseObject> {

  public static Builder on(@NotNull XWikiDocument doc) {
    return new Builder(checkNotNull(doc));
  }

  public static class Builder extends ObjectQueryBuilder<Builder, BaseObject> {

    private final XWikiDocument doc;
    private boolean clone = true;

    public Builder(XWikiDocument doc) {
      super(getXWikiObjectBridge());
      this.doc = doc;
    }

    Builder disableCloning() {
      clone = false;
      return getThis();
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public XWikiObjectFetcher fetch() {
      return new XWikiObjectFetcher(doc, buildQuery(), clone);
    }

  }

  XWikiObjectFetcher(XWikiDocument doc, ObjectQuery<BaseObject> query, boolean clone) {
    super(doc, query, clone);
  }

  @Override
  public XWikiObjectBridge getBridge() {
    return getXWikiObjectBridge();
  }

  private static XWikiObjectBridge getXWikiObjectBridge() {
    return (XWikiObjectBridge) Utils.getComponent(ObjectBridge.class, XWikiObjectBridge.NAME);
  }

}
