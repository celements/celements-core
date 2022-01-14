package com.celements.javascript;

import static com.google.common.base.Preconditions.*;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.google.common.base.Strings;
import com.google.errorprone.annotations.Immutable;

@Immutable
public final class ExtJsFileParameter {

  public static final class Builder {

    private String action;
    private String queryString;
    private AttachmentURLCommand attUrlCmdMock;
    private JsFileEntry jsFileEntry = new JsFileEntry();

    @NotNull
    public Builder setJsFileEntry(@NotNull JsFileEntry jsFileEntry) {
      checkNotNull(jsFileEntry);
      this.jsFileEntry = jsFileEntry;
      return this;
    }

    @NotNull
    public Builder setJsFile(@NotNull String jsFile) {
      checkNotNull(jsFile);
      this.jsFileEntry.setFilepath(jsFile);
      return this;
    }

    @NotNull
    public Builder setAction(@Nullable String action) {
      this.action = action;
      return this;
    }

    @NotNull
    public Builder setQueryString(@Nullable String params) {
      this.queryString = params;
      return this;
    }

    @NotNull
    public Builder setAttUrlCmdMock(@Nullable AttachmentURLCommand attUrlCmd) {
      this.attUrlCmdMock = attUrlCmd;
      return this;
    }

    @NotNull
    public Builder setLoadMode(@Nullable JsLoadMode loadMode) {
      this.jsFileEntry.setLoadMode(loadMode);
      return this;
    }

    @NotNull
    public ExtJsFileParameter build() {
      return new ExtJsFileParameter(this);
    }

  }

  private final String action;
  private final String queryString;
  private final AttachmentURLCommand attUrlCmdMock;
  private final JsFileEntry jsFileEntry;

  private ExtJsFileParameter(Builder buildParams) {
    action = buildParams.action;
    queryString = buildParams.queryString;
    attUrlCmdMock = buildParams.attUrlCmdMock;
    checkNotNull(buildParams.jsFileEntry);
    checkNotNull(Strings.nullToEmpty(buildParams.jsFileEntry.getFilepath()));
    jsFileEntry = buildParams.jsFileEntry;
  }

  @NotNull
  public JsFileEntry getJsFileEntry() {
    return jsFileEntry;
  }

  @NotNull
  public String getJsFile() {
    return jsFileEntry.getFilepath();
  }

  @NotNull
  public Optional<String> getAction() {
    return Optional.ofNullable(Strings.emptyToNull(action));
  }

  @NotNull
  public Optional<String> getQueryString() {
    return Optional.ofNullable(Strings.emptyToNull(queryString));
  }

  @NotNull
  public Optional<AttachmentURLCommand> getAttUrlCmdMock() {
    return Optional.ofNullable(attUrlCmdMock);
  }

  @NotNull
  public JsLoadMode getLoadMode() {
    return jsFileEntry.getLoadMode();
  }

}
