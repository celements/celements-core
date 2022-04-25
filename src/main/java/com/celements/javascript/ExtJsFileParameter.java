package com.celements.javascript;

import static com.google.common.base.Preconditions.*;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.google.common.base.Strings;
import com.google.errorprone.annotations.Immutable;

@Immutable
public final class ExtJsFileParameter {

  /**
   * ExtJsFileParameter.Builder is reusable.
   */
  @NotThreadSafe
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

    @NotNull
    public Builder setLazyLoad(boolean lazyLoad) {
      this.lazyLoad = lazyLoad;
      return this;
    }

    @NotNull
    public Builder setLoadMode(@Nullable JsLoadMode loadMode) {
      this.jsFileEntry.setLoadMode(loadMode);
      return this;
    }

    /**
     * Each call of {@link #build()} creates a new immutable ExtJsFileParameter instance.
     *
     * @return a new immutable ExtJsFileParameter
     */
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
    action = Strings.emptyToNull(buildParams.action);
    queryString = Strings.emptyToNull(buildParams.queryString);
    lazyLoad = buildParams.lazyLoad;
    checkNotNull(buildParams.jsFileEntry);
    jsFileEntry = new JsFileEntry(buildParams.jsFileEntry);
    checkNotNull(Strings.emptyToNull(jsFileEntry.getFilepath()));
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
    return Optional.ofNullable(action);
  }

  @NotNull
  public Optional<String> getQueryString() {
    return Optional.ofNullable(queryString);
  }

  public boolean isLazyLoad() {
    return lazyLoad;
  }

  @Override
  public int hashCode() {
    return Objects.hash(jsFileEntry, queryString, lazyLoad, action);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return (obj instanceof ExtJsFileParameter)
        && Objects.equals(((ExtJsFileParameter) obj).jsFileEntry, this.jsFileEntry)
        && Objects.equals(((ExtJsFileParameter) obj).queryString, this.queryString)
        && Objects.equals(((ExtJsFileParameter) obj).lazyLoad, this.lazyLoad)
        && Objects.equals(((ExtJsFileParameter) obj).action, this.action);
  }

  @Override
  public String toString() {
    return "ExtJsFileParameter [action=" + action + ", queryString=" + queryString + ", lazyLoad="
        + lazyLoad + ", jsFileEntry=" + jsFileEntry + "]";
  }

}
