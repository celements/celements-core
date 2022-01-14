package com.celements.javascript;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.google.common.base.Strings;

public final class JsFileEntry {

  private Long id;
  private DocumentReference documentReference;
  private Integer objNumber;
  private ClassReference classReference;
  private String jsFileUrl = "";
  private JsLoadMode loadMode;

  public JsFileEntry() {
    // Bean needs default constructor
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
    this.loadMode = loadMode;
  }

  @NotNull
  public String getFilepath() {
    return jsFileUrl;
  }

  @NotNull
  public JsLoadMode getLoadMode() {
    return Optional.ofNullable(loadMode).orElse(JsLoadMode.SYNC);
  }

  public @Nullable Integer getNumber() {
    return objNumber;
  }

  public void setNumber(@Nullable Integer objNumber) {
    this.objNumber = objNumber;
  }

  public @Nullable ClassReference getClassReference() {
    return classReference;
  }

  public void setClassReference(@Nullable ClassReference classReference) {
    this.classReference = classReference;
  }

  public @Nullable DocumentReference getDocumentReference() {
    return documentReference;
  }

  public void setDocumentReference(@Nullable DocumentReference documentReference) {
    this.documentReference = documentReference;
  }

  public @Nullable Long getId() {
    return id;
  }

  public void setId(@Nullable Long id) {
    this.id = id;
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
        && Objects.equals(((JsFileEntry) obj).jsFileUrl, this.jsFileUrl);
  }

  @Override
  @NotEmpty
  public String toString() {
    return "jsFileUrl [" + jsFileUrl + "], loadMode [" + loadMode + "] from docRef ["
        + documentReference + "]";
  }

}
