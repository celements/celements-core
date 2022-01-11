package com.celements.javascript;

import static com.google.common.base.Preconditions.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.web.plugin.cmd.AttachmentURLCommand;

public final class ExtJsFileParameter {

  @NotNull
  private String jsFile;
  @Nullable
  private String action = null;
  @Nullable
  private String params = null;
  @Nullable
  private AttachmentURLCommand attUrlCmd = null;
  private JsLoadMode loadMode = JsLoadMode.SYNC;

  public String getJsFile() {
    return jsFile;
  }

  public String getAction() {
    return action;
  }

  public String getParams() {
    return params;
  }

  public AttachmentURLCommand getAttUrlCmd() {
    return attUrlCmd;
  }

  public JsLoadMode getLoadMode() {
    return loadMode;
  }

  public ExtJsFileParameter setJsFile(@NotNull String jsFile) {
    checkNotNull(jsFile);
    this.jsFile = jsFile;
    return this;
  }

  public ExtJsFileParameter setAction(String action) {
    this.action = action;
    return this;
  }

  public ExtJsFileParameter setParams(String params) {
    this.params = params;
    return this;
  }

  public ExtJsFileParameter setAttUrlCmd(AttachmentURLCommand attUrlCmd) {
    this.attUrlCmd = attUrlCmd;
    return this;
  }

  public ExtJsFileParameter setLoadMode(JsLoadMode loadMode) {
    this.loadMode = loadMode;
    return this;
  }

  public ExtJsFileParameter setLoadMode(String loadMode) {
    this.loadMode = JsLoadMode.convertStoreValue(loadMode);
    return this;
  }

}
