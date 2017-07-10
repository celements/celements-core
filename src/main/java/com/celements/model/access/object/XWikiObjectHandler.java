package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.text.MessageFormat;

import javax.validation.constraints.NotNull;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class XWikiObjectHandler extends AbstractObjectHandler<XWikiDocument, BaseObject> {

  public static @NotNull XWikiObjectHandler onDoc(@NotNull XWikiDocument doc) {
    return new XWikiObjectHandler(doc);
  }

  private XWikiObjectHandler(XWikiDocument doc) {
    super(doc);
    checkState(doc.getTranslation() == 0, MessageFormat.format("ObjectHandler cannot be used"
        + " on translation ''{0}'' of doc ''{1}''", doc.getLanguage(), doc.getDocumentReference()));
  }

  @Override
  public XWikiObjectFetcher fetch() {
    return new XWikiObjectFetcher(getDoc(), getFilter());
  }

  @Override
  public XWikiObjectEditor edit() {
    return new XWikiObjectEditor(getDoc(), getFilter());
  }

}
