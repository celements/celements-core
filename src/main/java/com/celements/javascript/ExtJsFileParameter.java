package com.celements.javascript;

import static com.google.common.base.Preconditions.*;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.web.plugin.cmd.AttachmentURLCommand;

@NotThreadSafe
public final class ExtJsFileParameter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtJsFileParameter.class);

  private String jsFile;
  private String action;
  private String params;
  private AttachmentURLCommand attUrlCmd;
  private JsLoadMode loadMode = JsLoadMode.SYNC;

  @Nullable
  public String getJsFile() {
    return jsFile;
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
    LOGGER.debug("setLoadMode: [{}] for js file [{}].", loadMode, jsFile);
    this.loadMode = loadMode;
    return this;
  }

  public ExtJsFileParameter setLoadMode(String loadMode) {
    this.loadMode = JsLoadMode.convertStoreValue(loadMode);
    return this;
  }

}
