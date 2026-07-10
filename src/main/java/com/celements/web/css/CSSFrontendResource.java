package com.celements.web.css;

import java.util.Objects;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Attachment;

final class CSSFrontendResource extends CSS {

  private final String file;

  CSSFrontendResource(String file) {
    this.file = file;
  }

  @Override
  public String getCSS(XWikiContext context) {
    return getAttachmentURLcmd().getDiskFileUrl(file);
  }

  @Override
  public boolean isAlternate() {
    return false; // vite-emitted entry CSS is a normal stylesheet
  }

  @Override
  public String getTitle() {
    return "";
  }

  @Override
  public String getMedia() {
    return "all";
  }

  @Override
  public boolean isContentCSS() {
    return false; // Frontend bundle CSS must not be reused as RTE editor content CSS.
  }

  @Override
  public boolean isAttachment() {
    return false;
  }

  @Override
  public Attachment getAttachment() {
    return null;
  }

  @Override
  public String getCssBasePath() {
    return file;
  }

  @Override
  public int hashCode() {
    return Objects.hash(file);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof CSSFrontendResource other) && Objects.equals(this.file, other.file);
  }
}
