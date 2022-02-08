package com.celements.filebase.uri;

import java.net.MalformedURLException;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.filebase.references.FileReference;
import com.google.common.base.Strings;

@Component(FileUriScriptService.NAME)
public class FileUriScriptService implements ScriptService {

  public static final String NAME = "fileUri";

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUriScriptService.class);

  @Requirement
  private FileUriServiceRole fileUriService;

  @NotNull
  public FileReference createFileReference(@NotEmpty String link) {
    return FileReference.of(link).build();
  }

  @NotNull
  public UriBuilder createFileUrl(@Nullable String fileRef) {
    return createFileUrl(fileRef, null);
  }

  @NotNull
  public UriBuilder createFileUrl(@Nullable String fileRef, @Nullable String action) {
    return createFileUrl(fileRef, action, null);
  }

  @NotNull
  public UriBuilder createFileUrl(@Nullable String fileRef, @Nullable String action,
      @Nullable String queryString) {
    if (!Strings.isNullOrEmpty(fileRef)) {
      return createFileUrl(createFileReference(fileRef), action, queryString);
    }
    return UriBuilder.fromPath("");
  }

  @NotNull
  public UriBuilder createFileUrl(@Nullable FileReference fileRef, @Nullable String action,
      @Nullable String queryString) {
    if (fileRef != null) {
      try {
        return fileUriService.createFileUri(fileRef, Optional.ofNullable(action),
            Optional.ofNullable(queryString));
      } catch (FileNotExistException exp) {
        LOGGER.info("createFileUrl for [{}] with action [{}] an queryString [{}] failed.", fileRef,
            action, queryString, exp);
      }
    }
    return UriBuilder.fromPath("");
  }

  @NotNull
  public UriBuilder getFileURLPrefix(@Nullable String action) {
    return fileUriService.getFileUriPrefix(Optional.ofNullable(action));
  }

  @NotNull
  public String getExternalFileURL(@Nullable String fileRef, @Nullable String action) {
    return getExternalFileURL(fileRef, action, null);
  }

  @NotNull
  public String getExternalFileURL(@Nullable String fileRef, @Nullable String action,
      @Nullable String queryString) {
    if (!Strings.isNullOrEmpty(fileRef)) {
      try {
        return fileUriService.createAbsoluteFileUri(FileReference.of(fileRef).build(),
            Optional.ofNullable(action), Optional.ofNullable(queryString))
            .build().toURL().toExternalForm();
      } catch (MalformedURLException | IllegalArgumentException | UriBuilderException exp) {
        LOGGER.info("getExternalFileURL for [{}] with action [{}] failed.", fileRef, action, exp);
      }
    }
    return "";
  }

  @NotNull
  public UriBuilder createAbsoluteFileUri(@Nullable FileReference fileRef, String action,
      @Nullable String queryString) {
    if (fileRef != null) {
      return fileUriService.createAbsoluteFileUri(fileRef, Optional.ofNullable(action),
          Optional.ofNullable(queryString));
    }
    return UriBuilder.fromPath("");
  }

}
