package com.celements.filebase.matcher;

import com.xpn.xwiki.doc.XWikiAttachment;

public interface IAttachmentMatcher {

  public boolean accept(XWikiAttachment attachment);

}
