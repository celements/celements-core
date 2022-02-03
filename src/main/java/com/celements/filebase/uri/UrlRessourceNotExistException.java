package com.celements.filebase.uri;

import javax.validation.constraints.NotNull;

public class UrlRessourceNotExistException extends Exception {

  private static final long serialVersionUID = 1L;
  private final String ressource;

  public UrlRessourceNotExistException(@NotNull String ressource) {
    super("Url ressource [" + ressource + "] does not exist.");
    this.ressource = ressource;
  }

  public UrlRessourceNotExistException(@NotNull String message, @NotNull String ressource) {
    super(message);
    this.ressource = ressource;
  }

  public UrlRessourceNotExistException(@NotNull String message, @NotNull String ressource,
      Exception wrapExp) {
    super(message, wrapExp);
    this.ressource = ressource;
  }

  public String getRessource() {
    return ressource;
  }
}
