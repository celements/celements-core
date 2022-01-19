package com.celements.javascript;

import static com.google.common.base.Preconditions.*;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import com.celements.model.object.ObjectBean;
import com.google.common.base.Strings;

@NotThreadSafe
public final class JsFileEntry extends ObjectBean {

  private static final JsLoadMode LOAD_MODE_DEFAULT = JsLoadMode.SYNC;

  private String jsFileUrl = "";
  private JsLoadMode loadMode = LOAD_MODE_DEFAULT;

  public JsFileEntry() {
    // Bean needs default constructor
  }

  public JsFileEntry(@NotNull JsFileEntry jsFileEntry) {
    checkNotNull(jsFileEntry);
    this.setDocumentReference(jsFileEntry.getDocumentReference());
    this.setNumber(jsFileEntry.getNumber());
    this.setClassReference(jsFileEntry.getClassReference());
    this.jsFileUrl = jsFileEntry.jsFileUrl;
    this.loadMode = jsFileEntry.loadMode;
  }

  @NotNull
  public JsFileEntry addFilepath(@Nullable String jsFile) {
    setFilepath(jsFile);
    return this;
  }

  @NotNull
  public JsFileEntry addLoadMode(@Nullable JsLoadMode loadMode) {
    setLoadMode(loadMode);
    return this;
  }

  public void setFilepath(@Nullable String jsFile) {
    jsFileUrl = Strings.nullToEmpty(jsFile);
  }

  public void setLoadMode(@Nullable JsLoadMode loadMode) {
    this.loadMode = Optional.ofNullable(loadMode).orElse(LOAD_MODE_DEFAULT);
  }

  @NotNull
  public String getFilepath() {
    return jsFileUrl;
  }

  @NotNull
  public JsLoadMode getLoadMode() {
    return loadMode;
  }

  public boolean isValid() {
    return !Strings.isNullOrEmpty(jsFileUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jsFileUrl);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return (obj instanceof JsFileEntry)
        && Objects.equals(((JsFileEntry) obj).jsFileUrl, this.jsFileUrl);
  }

  @Override
  public String toString() {
    return "JsFileEntry [jsFileUrl=" + jsFileUrl + ", loadMode=" + loadMode
        + ", " + super.toString() + "]";
  }

}
