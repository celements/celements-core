package com.celements.javascript;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Strings;

public final class JsFileEntry {

  private int objNumber;

  private String jsFileUrl;
  private JsLoadMode loadMode;

  public JsFileEntry addFilepath(String jsFile) {
    setFilepath(jsFile);
    return this;
  }

  public JsFileEntry addLoadMode(JsLoadMode loadMode) {
    setLoadMode(loadMode);
    return this;
  }

  public void setFilepath(String jsFile) {
    jsFileUrl = jsFile;
  }

  public void setLoadMode(JsLoadMode loadMode) {
    this.loadMode = loadMode;
  }

  public String getFilepath() {
    return jsFileUrl;
  }

  public JsLoadMode getLoadMode() {
    return Optional.ofNullable(loadMode).orElse(JsLoadMode.SYNC);
  }

  public int getNumber() {
    return objNumber;
  }

  public void setNumber(int objNumber) {
    this.objNumber = objNumber;
  }

  public boolean isValid() {
    return !Strings.isNullOrEmpty(jsFileUrl);
  }

  @Override
  public int hashCode() {
    return jsFileUrl.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof JsFileEntry)
        && Objects.equals(((JsFileEntry) obj).jsFileUrl, jsFileUrl);
  }

}
