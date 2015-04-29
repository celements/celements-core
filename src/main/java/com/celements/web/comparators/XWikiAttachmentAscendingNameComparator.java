package com.celements.web.comparators;

import java.util.Comparator;

import com.xpn.xwiki.doc.XWikiAttachment;

public class XWikiAttachmentAscendingNameComparator implements Comparator<XWikiAttachment> {

	public int compare(XWikiAttachment attachmentOne, XWikiAttachment attachmentTwo) {
      return attachmentOne.getFilename().toLowerCase().replace('_', '-'
          ).compareTo(attachmentTwo.getFilename().toLowerCase().replace('_', '-'));
	}

}
