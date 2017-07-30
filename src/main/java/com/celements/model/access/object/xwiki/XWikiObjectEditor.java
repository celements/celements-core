package com.celements.model.access.object.xwiki;

import javax.annotation.concurrent.NotThreadSafe;

import com.celements.model.access.object.AbstractObjectEditor;
import com.celements.model.access.object.ObjectBridge;
import com.celements.model.access.object.ObjectFetcher;
import com.celements.model.access.object.ObjectHandler;
import com.celements.model.access.object.restriction.ObjectQuery;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@NotThreadSafe
public class XWikiObjectEditor extends AbstractObjectEditor<XWikiDocument, BaseObject> {

  XWikiObjectEditor(XWikiDocument doc, ObjectQuery<BaseObject> query) {
    super(getXWikiObjectBridge(), doc, query);
  }

  @Override
  public ObjectHandler<XWikiDocument, BaseObject> handle() {
    return XWikiObjectHandler.on(doc).with(query);
  }

  @Override
  public ObjectFetcher<XWikiDocument, BaseObject> fetch() {
    return new XWikiObjectFetcher(doc, query, false);
  }

  private static XWikiObjectBridge getXWikiObjectBridge() {
    return (XWikiObjectBridge) Utils.getComponent(ObjectBridge.class, XWikiObjectBridge.NAME);
  }

}
