package com.celements.filebase.uri;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.filebase.references.FileReference;

@Component("fileUri")
public class FileUriScriptService implements ScriptService {

  @Requirement
  private FileUriServiceRole fileUriService;

  public FileReference createFileReference(@NotEmpty String link) {
    return FileReference.of(link).build();
  }

  @NotNull
  public UriBuilder createFileUrl(@NotNull FileReference fileRef) {
    return createFileUrl(fileRef, null);
  }

  @NotNull
  public UriBuilder createFileUrl(@NotNull FileReference fileRef, @Nullable String action) {
    return createFileUrl(fileRef, action, null);
  }

  @NotNull
  public UriBuilder createFileUrl(@NotNull FileReference fileRef, @Nullable String action,
      @Nullable String queryString) {
    try {
      return fileUriService.createFileUrl(fileRef, Optional.ofNullable(action),
          Optional.ofNullable(queryString));
    } catch (FileNotExistException exp) {
      return UriBuilder.fromPath("");
    }
  }

  @NotNull
  public UriBuilder getFileURLPrefix(@Nullable String action) {
    return fileUriService.getFileURLPrefix(Optional.ofNullable(action));
  }

  @NotNull
  public String getExternalFileURL(@NotNull FileReference fileRef, String action) {
    return fileUriService.getExternalFileURL(fileRef, Optional.ofNullable(action));
  }

}
