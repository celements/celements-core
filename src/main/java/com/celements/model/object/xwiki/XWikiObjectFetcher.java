package com.celements.model.object.xwiki;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import com.celements.model.object.AbstractObjectFetcher;
import com.celements.model.object.ObjectBridge;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@NotThreadSafe
public class XWikiObjectFetcher extends
    AbstractObjectFetcher<XWikiObjectFetcher, XWikiDocument, BaseObject> {

  public static XWikiObjectFetcher on(@NotNull XWikiDocument doc) {
    return new XWikiObjectFetcher(doc);
  }

  private XWikiObjectFetcher(XWikiDocument doc) {
    super(doc);
  }

  @Override
  protected XWikiObjectFetcher disableCloning() {
    return super.disableCloning();
  }

  @Override
  protected XWikiObjectBridge getBridge() {
    return (XWikiObjectBridge) Utils.getComponent(ObjectBridge.class, XWikiObjectBridge.NAME);
  }

  @Override
  protected XWikiObjectFetcher getThis() {
    return this;
  }

}
