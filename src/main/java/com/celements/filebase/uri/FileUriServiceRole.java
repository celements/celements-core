package com.celements.filebase.uri;

import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.filebase.references.FileReference;

@ComponentRole
public interface FileUriServiceRole {

  @NotNull
  UriBuilder createFileUri(@NotNull FileReference fileRef, @NotNull Optional<String> action,
      @NotNull Optional<String> queryString) throws FileNotExistException;

  @NotNull
  UriBuilder getFileUriPrefix(@NotNull Optional<String> action);

  @NotNull
  UriBuilder createAbsoluteFileUri(@NotNull FileReference fileRef, @NotNull Optional<String> action,
      Optional<String> queryString);

}
