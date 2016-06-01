package com.celements.filebase;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.filebase.exceptions.FileBaseLoadException;
import com.celements.filebase.exceptions.FileNotExistsException;
import com.celements.filebase.matcher.IAttachmentMatcher;
import com.xpn.xwiki.doc.XWikiAttachment;

@ComponentRole
public interface IFileBaseServiceRole {

  public static final String FILEBASE_CONFIG_FIELD = "cel_centralfilebase";

  public boolean existsFileNameEqual(String filename) throws FileBaseLoadException;

  public XWikiAttachment getFileNameEqual(String filename) throws FileNotExistsException,
      FileBaseLoadException;

  public List<XWikiAttachment> getFilesNameMatch(IAttachmentMatcher attMatcher)
      throws FileBaseLoadException;

}
