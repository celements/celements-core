package com.celements.filebase;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.TokenBasedUploadCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiException;

@Component("filebase")
public class FileBaseScriptService implements ScriptService {
  
  @Requirement
  IWebUtilsService webUtilsService;
  
  private static Logger _LOGGER  = LoggerFactory.getLogger(FileBaseScriptService.class);
  
  @Requirement
  private Execution execution;

  @Requirement
  IAttachmentServiceRole attachmentService;

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
}
