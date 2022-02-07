package com.celements.filebase.uri;

import java.net.MalformedURLException;
import java.util.Optional;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;

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
  public UriBuilder createFileUrl(@NotNull FileReference fileRef, @NotNull Optional<String> action,
      @NotNull Optional<String> queryString) throws FileNotExistException {
    final UriBuilder baseUrl = createFileUrl(fileRef, action);
    if (queryString.isPresent()) {
      String[] baseQueryParts = baseUrl.toString().split("\\?", 2);
      if (baseQueryParts.length > 1) {
        return baseUrl.replaceQuery(baseQueryParts[1] + "&" + queryString.get());
      } else {
        return baseUrl.replaceQuery(queryString.get());
      }
    }
    return baseUrl;
  }

  @Override
  public @NotNull UriBuilder createFileUrl(@NotNull FileReference fileRef,
      @NotNull Optional<String> action)
      throws FileNotExistException {
    UriBuilder uriBuilder;
    if (fileRef.isAttachmentReference()) {
      uriBuilder = createAttachmentUrl(fileRef, action);
    } else if (fileRef.isOnDiskReference()) {
      uriBuilder = createOnDiskUrl(fileRef, action);
    } else {
      uriBuilder = fileRef.getUri();
    }
    return addContextUrl(uriBuilder);
  }

  UriBuilder addContextUrl(UriBuilder uriBuilder) {
    Optional<XWikiDocument> currentDoc = context.getCurrentDoc().toJavaUtil();
    if (currentDoc.isPresent() && uriBuilder.toString().startsWith("?")) {
      uriBuilder.replacePath(currentDoc.get().getURL("view", context.getXWikiContext()));
    }
    return uriBuilder;
  }

  private UriBuilder createAttachmentUrl(@NotNull FileReference fileRef, Optional<String> action)
      throws FileNotExistException {
    String attName = fileRef.getName();
    try {
      XWikiDocument doc = modelAccess.getDocument(fileRef.getDocRef());
      XWikiAttachment att = attachmentSrv.getAttachmentNameEqual(doc, attName);

      return UriBuilder.fromPath(doc.getAttachmentURL(attName, action.orElse(getDefaultAction()),
          context.getXWikiContext()))
          .replaceQuery("version=" + lastStartupTimeStamp.getLastChangedTimeStamp(att.getDate()));
    } catch (DocumentNotExistsException exp) {
      LOGGER.error("Error getting attachment URL for doc '{}' and file {}", fileRef.getDocRef(),
          attName, exp);
      throw new FileNotExistException(fileRef);
    } catch (AttachmentNotExistsException anee) {
      LOGGER.info("Attachment not found for link [{}] and action [{}]", fileRef, action, anee);
      throw new FileNotExistException(fileRef);
    }
  }

  UriBuilder createOnDiskUrl(@NotNull FileReference fileRef, Optional<String> action) {
    String path = fileRef.getFullPath().trim().substring(1);
    UriBuilder uri = UriBuilder.fromPath(
        context.getXWikiContext().getWiki().getSkinFile(path, true, context.getXWikiContext())
            .replace("/skin/",
                "/" + action.orElse(getDefaultAction()) + "/"));
    uri.queryParam("version", lastStartupTimeStamp.getFileModificationDate(path));
    return uri;
  }

}
