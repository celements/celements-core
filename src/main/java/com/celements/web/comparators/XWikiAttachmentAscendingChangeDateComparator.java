package com.celements.web.comparators;

import java.util.Comparator;

import com.xpn.xwiki.doc.XWikiAttachment;

public class XWikiAttachmentAscendingChangeDateComparator implements Comparator<XWikiAttachment> {

  @Override
  public int compare(XWikiAttachment attachmentOne, XWikiAttachment attachmentTwo) {
    return attachmentOne.getDate().compareTo(attachmentTwo.getDate());
  }

}
