package com.celements.filebase.uri;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.configuration.CelementsFromWikiConfigurationSource;
import com.celements.filebase.IAttachmentServiceRole;
import com.celements.filebase.references.FileReference;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.web.service.LastStartupTimeStampRole;
import com.google.common.base.Strings;
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

  @Requirement(CelementsFromWikiConfigurationSource.NAME)
  private ConfigurationSource configSrc;

  private String getDefaultAction() {
    return Optional.ofNullable(
        Strings.emptyToNull(configSrc.getProperty("celements.fileuri.defaultaction")))
        .orElse(context.getXWikiContext().getWiki().getXWikiPreference("celdefaultAttAction",
            "celements.attachmenturl.defaultaction", "file", context.getXWikiContext()));
  }

  @Override
  public @NotNull UriBuilder getFileUriPrefix(@NotNull Optional<String> action) {
    URL baseUrl = getUrlFactory().createResourceURL("", false, context.getXWikiContext());
    try {
      return UriBuilder.fromUri(baseUrl.toURI())
          .replacePath("/" + action.orElse(getDefaultAction()) + "/")
          .path(baseUrl.getPath());
    } catch (URISyntaxException exp) {
      LOGGER.error("Failed to get file url prefix.", exp);
      return UriBuilder.fromPath(baseUrl.toString());
    }
  }

  @Override
  @NotNull
  public UriBuilder createAbsoluteFileUri(@NotNull FileReference fileRef,
      @NotNull Optional<String> action, Optional<String> queryString) {
    try {
      return UriBuilder.fromUri(getUrlFactory().getServerURL(context.getXWikiContext()).toURI())
          .uri(createFileUri(fileRef, action, queryString).build());
    } catch (MalformedURLException | FileNotExistException | URISyntaxException exp) {
      LOGGER.error("Failed to getServerURL for [{}].", fileRef, exp);
      return UriBuilder.fromPath("");
    }
  }

  private XWikiURLFactory getUrlFactory() {
    return context.getXWikiContext().getURLFactory();
  }

  @Override
  @NotNull
  public UriBuilder createFileUri(@NotNull FileReference fileRef, @NotNull Optional<String> action,
      @NotNull Optional<String> queryString) throws FileNotExistException {
    final UriBuilder baseUrl = createFileUri(fileRef, action);
    if (queryString.isPresent()) {
      return baseUrl.replaceQuery(Optional.ofNullable(baseUrl.build().getQuery())
          .map(qS -> qS + "&" + queryString.get())
          .orElse(queryString.get()));
    }
    return baseUrl;
  }

  @NotNull
  UriBuilder createFileUri(@NotNull FileReference fileRef,
      @NotNull Optional<String> action)
      throws FileNotExistException {
    UriBuilder uriBuilder;
    if (fileRef.isAttachmentReference()) {
      uriBuilder = createAttachmentUri(fileRef, action);
    } else if (fileRef.isOnDiskReference()) {
      uriBuilder = createOnDiskUri(fileRef, action);
    } else {
      uriBuilder = fileRef.getUri();
    }
    return addContextUri(uriBuilder);
  }

  UriBuilder addContextUri(UriBuilder uriBuilder) {
    Optional<XWikiDocument> currentDoc = context.getCurrentDoc().toJavaUtil();
    if (currentDoc.isPresent() && uriBuilder.toString().startsWith("?")) {
      uriBuilder.replacePath(currentDoc.get().getURL("view", context.getXWikiContext()));
    }
    return uriBuilder;
  }

  private UriBuilder createAttachmentUri(@NotNull FileReference fileRef, Optional<String> action)
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

  UriBuilder createOnDiskUri(@NotNull FileReference fileRef, Optional<String> action) {
    String path = fileRef.getFullPath().trim().substring(1);
    UriBuilder uri = UriBuilder.fromPath(
        context.getXWikiContext().getWiki().getSkinFile(path, true, context.getXWikiContext())
            .replace("/skin/",
                "/" + action.orElse(getDefaultAction()) + "/"));
    uri.queryParam("version", lastStartupTimeStamp.getFileModificationDate(path));
    return uri;
  }

}
