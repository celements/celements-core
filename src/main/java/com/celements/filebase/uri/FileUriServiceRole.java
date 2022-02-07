package com.celements.filebase.uri;

import java.util.Optional;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.filebase.references.FileReference;

@ComponentRole
public interface FileUriServiceRole {

  @NotNull
  String createFileUrl(@NotNull FileReference fileRef, @NotNull Optional<String> action,
      @NotNull Optional<String> queryString) throws FileNotExistException;

  @NotEmpty
  String createFileUrl(@NotNull FileReference fileRef, @NotNull Optional<String> action)
      throws FileNotExistException;

  @NotEmpty
  String getFileURLPrefix(@NotEmpty String action);

  @NotEmpty
  String getFileURLPrefix();

  @NotNull
  String getExternalFileURL(@NotNull FileReference fileRef, @NotNull Optional<String> action);

}
