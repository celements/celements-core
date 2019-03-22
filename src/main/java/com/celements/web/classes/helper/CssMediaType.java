package com.celements.web.classes.helper;

public enum CssMediaType {
  ALL("all"), AURAL("aural"), BRAILLE("braille"), EMBOSSED("embossed"), HANDHELD("handheld"), PRINT(
      "print"), PROJECTION("projection"), SCREEN("screen"), TTY("tty"), TV("tv");

  private final String mediaType;

  private CssMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public String getMediaType() {
    return mediaType;
  }
}
