package com.celements.model.access.object.xwiki;

import javax.validation.constraints.NotNull;

import com.celements.model.access.object.ObjectHandler;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class XWikiObjectHandler extends ObjectHandler<XWikiDocument, BaseObject> {

  public static XWikiObjectHandler onDoc(@NotNull XWikiDocument doc) {
    return new XWikiObjectHandler(doc);
  }

  private XWikiObjectHandler(@NotNull XWikiDocument doc) {
    super(doc, new XWikiObjectBridge(doc));
  }

}
