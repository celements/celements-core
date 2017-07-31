package com.celements.model.access.object.xwiki;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import com.celements.model.access.object.AbstractObjectEditor;
import com.celements.model.access.object.ObjectBridge;
import com.celements.model.access.object.restriction.ObjectQuery;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@NotThreadSafe
public class XWikiObjectEditor extends AbstractObjectEditor<XWikiDocument, BaseObject> {

  public static Builder on(@NotNull XWikiDocument doc) {
    return new Builder(checkNotNull(doc));
  }

  public static class Builder extends
      AbstractObjectEditor.Builder<Builder, XWikiDocument, BaseObject> {

    public Builder(XWikiDocument doc) {
      super(getXWikiObjectBridge(), doc);
    }

    @Override
    public XWikiObjectEditor edit() {
      return new XWikiObjectEditor(doc, buildQuery());
    }

    @Override
    protected Builder getThis() {
      return this;
    }

  }

  private XWikiObjectEditor(XWikiDocument doc, ObjectQuery<BaseObject> query) {
    super(doc, query);
  }

  @Override
  public XWikiObjectFetcher fetch() {
    return XWikiObjectFetcher.on(doc).with(query).disableCloning().fetch();
  }

  @Override
  public XWikiObjectBridge getBridge() {
    return getXWikiObjectBridge();
  }

  private static XWikiObjectBridge getXWikiObjectBridge() {
    return (XWikiObjectBridge) Utils.getComponent(ObjectBridge.class, XWikiObjectBridge.NAME);
  }

}
