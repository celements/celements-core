package com.celements.model.access.object.xwiki;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import com.celements.model.access.object.DefaultObjectHandler;
import com.celements.model.access.object.ObjectBridge;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@NotThreadSafe
public class XWikiObjectHandler extends DefaultObjectHandler<XWikiDocument, BaseObject> {

  public static XWikiObjectHandler on(@NotNull XWikiDocument doc) {
    return new XWikiObjectHandler(doc);
  }

  private XWikiObjectHandler(XWikiDocument doc) {
    super(getXWikiObjectBridge(), doc);
  }

  private static XWikiObjectBridge getXWikiObjectBridge() {
    return (XWikiObjectBridge) Utils.getComponent(ObjectBridge.class, XWikiObjectBridge.NAME);
  }

}
