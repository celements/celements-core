package com.celements.filebase.uri;

import java.net.MalformedURLException;
import java.util.Optional;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.filebase.IAttachmentServiceRole;
import com.celements.filebase.references.FileReference;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.web.service.LastStartupTimeStampRole;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.XWikiURLFactory;

@Component
public class FileUriService implements FileUriServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUriService.class);

  @Requirement
  private ModelContext context;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private IAttachmentServiceRole attachmentSrv;

  @Requirement
  private LastStartupTimeStampRole lastStartupTimeStamp;

  private String getDefaultAction() {
    return context.getXWikiContext().getWiki().getXWikiPreference("celdefaultAttAction",
        "celements.attachmenturl.defaultaction", "file", context.getXWikiContext());
  }

  @Override
  @NotEmpty
  public String getFileURLPrefix() {
    return getFileURLPrefix(getDefaultAction());
  }

  @Override
  @NotEmpty
  public String getFileURLPrefix(@NotEmpty String action) {
    XWikiURLFactory urlf = context.getXWikiContext().getURLFactory();
    return urlf.createResourceURL("", true, context.getXWikiContext()).toString().replace("/skin/",
        "/" + action + "/");
  }

  @Override
  @NotNull
  public String getExternalFileURL(@NotNull FileReference fileRef,
      @NotNull Optional<String> action) {
    try {
      return context.getXWikiContext().getURLFactory().getServerURL(context.getXWikiContext())
          .toExternalForm() + createFileUrl(fileRef, action);
    } catch (MalformedURLException | FileNotExistException exp) {
      LOGGER.error("Failed to getServerURL.", exp);
    }
    return "";
  }

  @Override
  @NotNull
  public String createFileUrl(@NotNull FileReference fileRef, @NotNull Optional<String> action,
      @NotNull Optional<String> queryString) throws FileNotExistException {
    final String baseUrl = createFileUrl(fileRef, action);
    if (queryString.isPresent()) {
      if (baseUrl.indexOf("?") > -1) {
        return baseUrl + "&" + queryString.get();
      } else {
        return baseUrl + "?" + queryString.get();
      }
    }
    return baseUrl;
  }

  @Override
  public @NotEmpty String createFileUrl(@NotNull FileReference fileRef,
      @NotNull Optional<String> action)
      throws FileNotExistException {
    String url;
    if (fileRef.isAttachmentReference()) {
      url = createAttachmentUrl(fileRef, action);
    } else if (fileRef.isOnDiskReference()) {
      url = createOnDiskUrl(fileRef, action);
    } else {
      url = fileRef.getFullPath();
    }
    return addContextUrl(url);
  }

  private String addContextUrl(String url) {
    Optional<XWikiDocument> currentDoc = context.getCurrentDoc().toJavaUtil();
    if (currentDoc.isPresent() && url.startsWith("?")) {
      url = currentDoc.get().getURL("view", context.getXWikiContext()) + url;
    }
    return url;
  }

  private String createAttachmentUrl(@NotNull FileReference fileRef, Optional<String> action)
      throws FileNotExistException {
    String attName = fileRef.getName();
    try {
      XWikiDocument doc = modelAccess.getDocument(fileRef.getDocRef());
      XWikiAttachment att = attachmentSrv.getAttachmentNameEqual(doc, attName);
      return doc.getAttachmentURL(attName, action.orElse(getDefaultAction()),
          context.getXWikiContext())
          + "?version=" + lastStartupTimeStamp.getLastChangedTimeStamp(att.getDate());
    } catch (DocumentNotExistsException exp) {
      LOGGER.error("Error getting attachment URL for doc '{}' and file {}", fileRef.getDocRef(),
          attName, exp);
      throw new FileNotExistException(fileRef);
    } catch (AttachmentNotExistsException anee) {
      LOGGER.info("Attachment not found for link [{}] and action [{}]", fileRef, action, anee);
      throw new FileNotExistException(fileRef);
    }
  }

  private String createOnDiskUrl(@NotNull FileReference fileRef, Optional<String> action) {
    String url;
    String path = fileRef.getFullPath().trim().substring(1);
    url = context.getXWikiContext().getWiki().getSkinFile(path, true, context.getXWikiContext())
        .replace("/skin/",
            "/" + action.orElse(getDefaultAction()) + "/");
    url += "?version=" + lastStartupTimeStamp.getFileModificationDate(path);
    return url;
  }

}
