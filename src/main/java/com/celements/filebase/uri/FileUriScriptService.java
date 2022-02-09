package com.celements.filebase.uri;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.filebase.references.FileReference;
import com.google.common.base.Strings;

@Component(FileUriScriptService.NAME)
@Singleton
public class FileUriScriptService implements ScriptService {

  public static final String NAME = "fileUri";

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUriScriptService.class);

  @Requirement
  private FileUriServiceRole fileUriService;

  @NotNull
  public FileReference createFileReference(@NotEmpty String link) {
    return FileReference.of(link);
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
  public UriBuilder getFileURLPrefix() {
    return fileUriService.getFileUriPrefix(Optional.empty());
  }

  @NotNull
  public UriBuilder getFileURLPrefix(@Nullable String action) {
    return fileUriService.getFileUriPrefix(Optional.ofNullable(action));
  }

  @NotNull
  public UriBuilder createAbsoluteFileUri(@Nullable String fileRef, @Nullable String action) {
    return createAbsoluteFileUri(fileRef, action, null);
  }

  @NotNull
  public UriBuilder createAbsoluteFileUri(@Nullable String fileRef, String action,
      @Nullable String queryString) {
    if (!Strings.isNullOrEmpty(fileRef)) {
      return createAbsoluteFileUri(createFileReference(fileRef), action, queryString);
    }
    return UriBuilder.fromPath("");
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
