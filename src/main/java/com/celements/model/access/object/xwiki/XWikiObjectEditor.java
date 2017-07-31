package com.celements.model.access.object.xwiki;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import com.celements.model.access.object.AbstractObjectEditor;
import com.celements.model.access.object.ObjectBridge;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@NotThreadSafe
public class XWikiObjectEditor extends
    AbstractObjectEditor<XWikiObjectEditor, XWikiDocument, BaseObject> {

  public static XWikiObjectEditor on(@NotNull XWikiDocument doc) {
    return new XWikiObjectEditor(checkNotNull(doc));
  }

  private XWikiObjectEditor(XWikiDocument doc) {
    super(doc);
  }

  @Override
  public XWikiObjectFetcher fetch() {
    return XWikiObjectFetcher.on(doc).with(getQuery()).disableCloning();
  }

  @Override
  protected XWikiObjectBridge getBridge() {
    return (XWikiObjectBridge) Utils.getComponent(ObjectBridge.class, XWikiObjectBridge.NAME);
  }

  @Override
  protected XWikiObjectEditor getThis() {
    return this;
  }

}
