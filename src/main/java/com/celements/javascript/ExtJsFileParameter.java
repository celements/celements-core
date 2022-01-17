package com.celements.javascript;

import static com.google.common.base.Preconditions.*;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.google.common.base.Strings;
import com.google.errorprone.annotations.Immutable;

@Immutable
public final class ExtJsFileParameter {

  public static final class Builder {

    private JsFileEntry jsFileEntry = new JsFileEntry();
    private String action;
    private String queryString;
    private boolean lazyLoad = false;
    private AttachmentURLCommand attUrlCmdMock;

    @NotNull
    public Builder setJsFileEntry(@NotNull JsFileEntry jsFileEntry) {
      checkNotNull(jsFileEntry);
      this.jsFileEntry = jsFileEntry;
      return this;
    }

    @NotNull
    public Builder setJsFile(@NotEmpty String jsFile) {
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

    public Builder setLazyLoad(boolean lazyLoad) {
      this.lazyLoad = lazyLoad;
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

    @Override
    public String toString() {
      return "ExtJsFileParameter.Builder [jsFileEntry=" + jsFileEntry + ", action=" + action
          + ", queryString=" + queryString + ", lazyLoad=" + lazyLoad + ", attUrlCmdMock="
          + attUrlCmdMock + "]";
    }

  }

  private final String action;
  private final JsFileEntry jsFileEntry;
  private final String queryString;
  private final boolean lazyLoad;
  private final AttachmentURLCommand attUrlCmdMock;

  private ExtJsFileParameter(Builder buildParams) {
    action = buildParams.action;
    queryString = buildParams.queryString;
    lazyLoad = buildParams.lazyLoad;
    attUrlCmdMock = buildParams.attUrlCmdMock;
    checkNotNull(buildParams.jsFileEntry);
    checkNotNull(Strings.emptyToNull(buildParams.jsFileEntry.getFilepath()));
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

  public boolean isLazyLoad() {
    return lazyLoad;
  }

  @NotNull
  public Optional<AttachmentURLCommand> getAttUrlCmdMock() {
    return Optional.ofNullable(attUrlCmdMock);
  }

  @NotNull
  public JsLoadMode getLoadMode() {
    return jsFileEntry.getLoadMode();
  }

  @Override
  public String toString() {
    return "ExtJsFileParameter [action=" + action + ", jsFileEntry=" + jsFileEntry
        + ", queryString=" + queryString + ", lazyLoad=" + lazyLoad + ", attUrlCmdMock="
        + attUrlCmdMock + "]";
  }

}
