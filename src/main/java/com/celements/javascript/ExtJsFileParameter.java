package com.celements.javascript;

import static com.google.common.base.Preconditions.*;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import com.celements.web.plugin.cmd.AttachmentURLCommand;

@NotThreadSafe
public final class ExtJsFileParameter {

  private String action;
  private String params;
  private AttachmentURLCommand attUrlCmd;
  private JsFileEntry jsFileEntry = new JsFileEntry();

  @NotNull
  public ExtJsFileParameter setJsFileEntry(@NotNull JsFileEntry jsFileEntry) {
    checkNotNull(jsFileEntry);
    this.jsFileEntry = jsFileEntry;
    return this;
  }

  @NotNull
  public JsFileEntry getJsFileEntry() {
    return jsFileEntry;
  }

  @Nullable
  public String getJsFile() {
    return jsFileEntry.getFilepath();
  }

  @Nullable
  public String getAction() {
    return action;
  }

  @Nullable
  public String getParams() {
    return params;
  }

  @Nullable
  public AttachmentURLCommand getAttUrlCmd() {
    return attUrlCmd;
  }

  @Nullable
  public JsLoadMode getLoadMode() {
    return jsFileEntry.getLoadMode();
  }

  public ExtJsFileParameter setJsFile(@NotNull String jsFile) {
    checkNotNull(jsFile);
    this.jsFileEntry.setFilepath(jsFile);
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
    this.jsFileEntry.setLoadMode(loadMode);
    return this;
  }

}
