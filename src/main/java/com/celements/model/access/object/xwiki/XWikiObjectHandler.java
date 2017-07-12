package com.celements.model.access.object.xwiki;

import javax.validation.constraints.NotNull;

import com.celements.model.access.object.DefaultObjectHandler;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class XWikiObjectHandler extends DefaultObjectHandler<XWikiDocument, BaseObject> {

  public static XWikiObjectHandler on(@NotNull XWikiDocument doc) {
    return new XWikiObjectHandler(doc);
  }

  public static XWikiObjectHandler from(@NotNull XWikiObjectHandler handler) {
    return new XWikiObjectHandler(handler);
  }

  private XWikiObjectHandler(XWikiDocument doc) {
    super(doc, new XWikiObjectBridge(doc));
  }

  private XWikiObjectHandler(XWikiObjectHandler handler) {
    super(handler.getDoc(), handler.getBridge(), handler.getFilter());
  }

}
