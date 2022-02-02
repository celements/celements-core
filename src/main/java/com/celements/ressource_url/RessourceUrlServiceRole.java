package com.celements.ressource_url;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface RessourceUrlServiceRole {

  @NotNull
  public String getAttachmentName(@NotNull String link);

  @NotNull
  DocumentReference getPageDocRef(@NotNull String link);

  boolean isAttachmentLink(String link);

  boolean isOnDiskLink(@Nullable String link);

  @NotNull
  String createRessourceUrl(@NotNull String jsFile, @NotNull Optional<String> action,
      @NotNull Optional<String> queryString) throws UrlRessourceNotExistException;

  @NotEmpty
  String createRessourceUrl(@NotNull String link, @NotNull Optional<String> action)
      throws UrlRessourceNotExistException;

  @NotEmpty
  String getRessourceURLPrefix(@NotEmpty String action);

  @NotEmpty
  String getRessourceURLPrefix();

  @NotNull
  String getExternalRessourceURL(@NotNull String fileName, @NotNull Optional<String> action);

}
