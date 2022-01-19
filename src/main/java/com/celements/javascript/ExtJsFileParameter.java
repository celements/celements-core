package com.celements.javascript;

import static com.google.common.base.Preconditions.*;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.google.common.base.Strings;
import com.google.errorprone.annotations.Immutable;

@Immutable
public final class ExtJsFileParameter {

  public static final class Builder {

    private JsFileEntry jsFileEntry = new JsFileEntry();
    private String action;
    private String queryString;
    private boolean lazyLoad = false;

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
          + ", queryString=" + queryString + ", lazyLoad=" + lazyLoad + "]";
    }

  }

  private final String action;
  private final JsFileEntry jsFileEntry;
  private final String queryString;
  private final boolean lazyLoad;

  private ExtJsFileParameter(Builder buildParams) {
    action = buildParams.action;
    queryString = buildParams.queryString;
    lazyLoad = buildParams.lazyLoad;
    checkNotNull(buildParams.jsFileEntry);
    checkNotNull(Strings.emptyToNull(buildParams.jsFileEntry.getFilepath()));
    jsFileEntry = new JsFileEntry(buildParams.jsFileEntry);
  }

  @NotNull
  public JsFileEntry getJsFileEntry() {
    return new JsFileEntry(jsFileEntry);
  }

  @NotNull
  public String getJsFile() {
    return jsFileEntry.getFilepath();
  }

  @NotNull
  public JsLoadMode getLoadMode() {
    return jsFileEntry.getLoadMode();
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

  @Override
  public String toString() {
    return "ExtJsFileParameter [action=" + action + ", jsFileEntry=" + jsFileEntry
        + ", queryString=" + queryString + ", lazyLoad=" + lazyLoad + "]";
  }

}
