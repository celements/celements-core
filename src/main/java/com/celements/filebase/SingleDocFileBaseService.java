package com.celements.filebase;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;

import com.celements.filebase.exceptions.FileBaseLoadException;
import com.celements.filebase.exceptions.FileNotExistsException;
import com.celements.filebase.matcher.IAttachmentMatcher;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(SingleDocFileBaseService.FILEBASE_SINGLE_DOC)
public class SingleDocFileBaseService implements IFileBaseServiceRole {

  public static final String FILEBASE_SINGLE_DOC = "filebase.singleDoc";

  private static Logger _LOGGER  = LoggerFactory.getLogger(SingleDocFileBaseService.class);

  @Requirement
  IAttachmentServiceRole attService;

  @Requirement
  IModelAccessFacade modelAccess;

  @Requirement
  IWebUtilsService webUtils;

  @Requirement
  ConfigurationSource configuration;

  //TODO write unit test
  private XWikiDocument getFileBaseDoc() throws FileBaseLoadException {
    String fileBaseDocFN = configuration.getProperty(FILEBASE_CONFIG_FIELD);
    if (!Strings.isNullOrEmpty(fileBaseDocFN) && !"-".equals(fileBaseDocFN)) {
      try {
        DocumentReference fileBaseDocRef = webUtils.resolveDocumentReference(
            fileBaseDocFN);
        return modelAccess.getOrCreateDocument(fileBaseDocRef);
      } catch (DocumentLoadException exp) {
        _LOGGER.error("Failed to load FileBaseDocument.", exp);
        throw new FileBaseLoadException(fileBaseDocFN, exp);
      }
    } else {
      throw new FileBaseLoadException(fileBaseDocFN);
    }
  }

  @Override
  public boolean existsFileNameEqual(String filename) throws FileBaseLoadException {
    return attService.existsAttachmentNameEqual(getFileBaseDoc(), filename);
  }

  @Override
  public XWikiAttachment getFileNameEqual(String filename) throws FileNotExistsException,
      FileBaseLoadException {
    try {
      return attService.getAttachmentNameEqual(getFileBaseDoc(), filename);
    } catch (AttachmentNotExistsException attNotExistsExp) {
      _LOGGER.trace("failed to get file in filebase. ", attNotExistsExp);
      throw new FileNotExistsException(filename);
    }
  }

  @Override
  public List<XWikiAttachment> getFilesNameMatch(IAttachmentMatcher attMatcher
      ) throws FileBaseLoadException {
    return attService.getAttachmentsNameMatch(getFileBaseDoc(), attMatcher);
  }

}
