package com.celements.javascript;

import static com.google.common.base.Preconditions.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.google.errorprone.annotations.Immutable;

@Immutable
public final class ExtJsFileParameter {

  public static final class Builder {

    private String action;
    private String params;
    private AttachmentURLCommand attUrlCmd;
    private JsFileEntry jsFileEntry = new JsFileEntry();

    @NotNull
    public Builder setJsFileEntry(@NotNull JsFileEntry jsFileEntry) {
      checkNotNull(jsFileEntry);
      this.jsFileEntry = jsFileEntry;
      return this;
    }

    public Builder setJsFile(@NotNull String jsFile) {
      checkNotNull(jsFile);
      this.jsFileEntry.setFilepath(jsFile);
      return this;
    }

    public Builder setAction(String action) {
      this.action = action;
      return this;
    }

    public Builder setParams(String params) {
      this.params = params;
      return this;
    }

    public Builder setAttUrlCmd(AttachmentURLCommand attUrlCmd) {
      this.attUrlCmd = attUrlCmd;
      return this;
    }

    public Builder setLoadMode(JsLoadMode loadMode) {
      this.jsFileEntry.setLoadMode(loadMode);
      return this;
    }

    public ExtJsFileParameter build() {
      return new ExtJsFileParameter(this);
    }

  }

  private final String action;
  private final String params;
  private final AttachmentURLCommand attUrlCmd;
  private final JsFileEntry jsFileEntry;

  private ExtJsFileParameter(Builder buildParams) {
    action = buildParams.action;
    params = buildParams.params;
    attUrlCmd = buildParams.attUrlCmd;
    jsFileEntry = buildParams.jsFileEntry;
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

}
