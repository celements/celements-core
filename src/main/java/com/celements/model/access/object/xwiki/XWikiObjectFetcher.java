package com.celements.model.access.object.xwiki;

import javax.annotation.concurrent.NotThreadSafe;

import com.celements.model.access.object.DefaultObjectFetcher;
import com.celements.model.access.object.ObjectBridge;
import com.celements.model.access.object.ObjectHandler;
import com.celements.model.access.object.restriction.ObjectQuery;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@NotThreadSafe
public class XWikiObjectFetcher extends DefaultObjectFetcher<XWikiDocument, BaseObject> {

  XWikiObjectFetcher(XWikiDocument doc, ObjectQuery<BaseObject> query, boolean clone) {
    super(getXWikiObjectBridge(), doc, query, clone);
  }

  @Override
  public ObjectHandler<XWikiDocument, BaseObject> handle() {
    return XWikiObjectHandler.on(doc).with(query);
  }

  private static XWikiObjectBridge getXWikiObjectBridge() {
    return (XWikiObjectBridge) Utils.getComponent(ObjectBridge.class, XWikiObjectBridge.NAME);
  }

}
