package com.celements.filebase.uri;

import java.net.URL;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.filebase.references.FileReference;

@ComponentRole
public interface FileUrlServiceRole {

  @NotNull
  URL getFileUrl(@NotNull FileReference fileRef, @Nullable String action)
      throws FileNotExistException;

  @NotNull
  URL getFileUrl(@NotNull FileReference fileRef, @Nullable String action,
      @Nullable String queryString) throws FileNotExistException;

  @NotNull
  URL getFileUrlPrefix(@NotNull String action);

}
