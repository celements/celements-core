package com.celements.web.service;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.fileupload.FileUploadPlugin;

@ComponentRole
public interface IAttachmentServiceRole {

  public int uploadMultipleAttachments(XWikiDocument attachToDoc, String fieldNamePrefix);

  public boolean uploadAttachment(String fieldName, String filename,
      FileUploadPlugin fileupload, XWikiDocument doc) throws XWikiException;

}
