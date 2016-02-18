package com.celements.filebase;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.filebase.exceptions.FileBaseLoadException;
import com.celements.filebase.exceptions.FileNotExistsException;
import com.celements.filebase.matcher.IAttachmentMatcher;
import com.celements.model.access.exception.NoAccessRightsException;
import com.celements.web.plugin.cmd.TokenBasedUploadCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;

@Component("filebase")
public class FileBaseScriptService implements ScriptService {
  
  @Requirement
  IWebUtilsService webUtilsService;
  
  private static Logger _LOGGER  = LoggerFactory.getLogger(FileBaseScriptService.class);

  @Requirement
  IAttachmentServiceRole attachmentService;

  @Requirement(SingleDocFileBaseService.FILEBASE_SINGLE_DOC)
  IFileBaseServiceRole filebaseService;

  public String clearFileName(String fileName) {
    return attachmentService.clearFileName(fileName);
  }

  public int tokenBasedUpload(DocumentReference attachToDocRef, String fieldName, 
      String userToken) {
    return tokenBasedUpload(attachToDocRef, fieldName, userToken, false);
  }
  
  public int tokenBasedUpload(DocumentReference attachToDocRef, String fieldName, 
      String userToken, Boolean createIfNotExists) {
    try {
      return new TokenBasedUploadCommand().tokenBasedUploadDocRef(attachToDocRef,
          fieldName, userToken, createIfNotExists);
    } catch (XWikiException exp) {
      _LOGGER.error("token based attachment upload failed: ", exp);
    }
    return 0;
  }
  
  public int deleteAttachmentList(List<AttachmentReference> attachmentRefList) {
    return attachmentService.deleteAttachmentList(attachmentRefList);
  }

  public boolean existsFileNameEqual(String filename) throws FileBaseLoadException {
    return filebaseService.existsFileNameEqual(filename);
  }

  public Attachment getFileNameEqual(String filename) throws FileBaseLoadException {
    try {
      XWikiAttachment xwikiAtt = filebaseService.getFileNameEqual(filename);
      return attachmentService.getApiAttachment(xwikiAtt);
    } catch (FileNotExistsException e) {
      _LOGGER.trace("Filebase could not find file [" + filename + "]");
    } catch (NoAccessRightsException nare) {
      _LOGGER.info("User {} was refused {} access on file base document {}", 
          nare.getUser(), nare.getExpectedAccessLevel(), nare.getDocumentReference());
    }
    return null;
  }

  public List<Attachment> getFilesNameMatch(IAttachmentMatcher attMatcher
      ) throws FileBaseLoadException {
    List<XWikiAttachment> xwikiAttList = filebaseService.getFilesNameMatch(attMatcher);
    List<Attachment> attList = new ArrayList<Attachment>();
    for(XWikiAttachment xwikiAtt : xwikiAttList) {
      try {
        attList.add(attachmentService.getApiAttachment(xwikiAtt));
      } catch (NoAccessRightsException nare) {
        _LOGGER.info("User {} was refused {} access on file base document {}", 
            nare.getUser(), nare.getExpectedAccessLevel(), nare.getDocumentReference());
      }
    }
    return attList;
  }
}
