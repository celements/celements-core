package com.celements.javascript;

import static com.google.common.base.Preconditions.*;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.filebase.references.FileReference;
import com.celements.filebase.uri.FileNotExistException;
import com.celements.filebase.uri.FileUriServiceRole;
import com.celements.sajson.JsonBuilder;
import com.google.common.base.Strings;
import com.google.errorprone.annotations.Immutable;
import com.xpn.xwiki.web.Utils;

@Immutable
public final class ExtJsFileParameter {

  /**
   * ExtJsFileParameter.Builder is reusable.
   */
  @NotThreadSafe
  public static final class Builder {

    private FileReference jsFileRef;
    private JsLoadMode loadMode = JsLoadMode.SYNC;
    private String action;
    private String queryString;
    private boolean lazyLoad = false;

    @NotNull
    public Builder setJsFileEntry(@NotNull JsFileEntry jsFileEntry) {
      checkNotNull(jsFileEntry);
      this.jsFileRef = FileReference.of(jsFileEntry.getFilepath()).build();
      return this;
    }

    @NotNull
    public Builder setJsFileRef(@NotNull FileReference jsFileRef) {
      checkNotNull(jsFileRef);
      this.jsFileRef = jsFileRef;
      return this;
    }

    @NotNull
    public Builder setJsFileRef(@NotEmpty String link) {
      checkNotNull(link);
      this.jsFileRef = FileReference.of(link).build();
      return this;
    }

    @NotNull
    public Builder setAction(@Nullable String action) {
      this.action = action;
      return this;
    }

    @NotNull
    public Builder setQueryString(@Nullable String params) {
      this.queryString = Strings.emptyToNull(params);
      return this;
    }

    @NotNull
    public Builder setLazyLoad(boolean lazyLoad) {
      this.lazyLoad = lazyLoad;
      return this;
    }

    @NotNull
    public Builder setLoadMode(@Nullable JsLoadMode loadMode) {
      this.loadMode = loadMode;
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
      return "ExtJsFileParameter.Builder [jsFileEntry=" + jsFileRef + ", action=" + action
          + ", queryString=" + queryString + ", lazyLoad=" + lazyLoad + "]";
    }

  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtJsFileParameter.class);

  private final String action;
  private final FileReference jsFileRef;
  private final JsLoadMode loadMode;
  private final String queryString;
  private final boolean lazyLoad;

  private ExtJsFileParameter(Builder buildParams) {
    action = Strings.emptyToNull(buildParams.action);
    lazyLoad = buildParams.lazyLoad;
    checkNotNull(buildParams.jsFileRef);
    jsFileRef = buildParams.jsFileRef;
    queryString = Strings.emptyToNull(
        Stream.of(jsFileRef.getQueryString(), buildParams.queryString)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("&")));
    checkNotNull(jsFileRef);
    loadMode = buildParams.loadMode;
  }

  @NotNull
  public FileReference getJsFileRef() {
    return jsFileRef;
  }

  @NotNull
  public JsLoadMode getLoadMode() {
    return loadMode;
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
    return Objects.hash(jsFileRef, queryString);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return (obj instanceof ExtJsFileParameter)
        && Objects.equals(((ExtJsFileParameter) obj).jsFileRef, this.jsFileRef)
        && Objects.equals(((ExtJsFileParameter) obj).queryString, this.queryString);
  }

  @Override
  public String toString() {
    return "ExtJsFileParameter [action=" + action + ", jsFileUrl=" + jsFileRef + ", loadMode="
        + loadMode + ", queryString=" + queryString + ", lazyLoad=" + lazyLoad + "]";
  }

  @NotEmpty
  public String getLazyLoadTag() {
    final JsonBuilder jsonBuilder = new JsonBuilder();
    jsonBuilder.openDictionary();
    try {
      jsonBuilder.addProperty("fullURL",
          getAttUrlSrv().createFileUri(getJsFileRef(), getAction(), getQueryString()).toString());
    } catch (FileNotExistException exp) {
      LOGGER.info("URL ressource '{}' does not exist.", getJsFileRef(), exp);
      jsonBuilder.addProperty("errorMsg", buildNotFoundWarning());
    }
    jsonBuilder.addProperty("initLoad", true);
    jsonBuilder.closeDictionary();
    return "<span class='cel_lazyloadJS' style='display: none;'>" + jsonBuilder.getJSON()
        + "</span>";
  }

  public String getScriptTag() {
    try {
      return getScriptTagString();
    } catch (FileNotExistException exp) {
      LOGGER.info("URL ressource '{}' does not exist.", getJsFileRef(), exp);
      return buildNotFoundWarningComment();
    }
  }

  public String getScriptTagString() throws FileNotExistException {
    final UriBuilder fileUri = getAttUrlSrv().createFileUri(getJsFileRef(), getAction(),
        getQueryString());
    LOGGER.info("getScriptTagString: extJsFileParams [{}] jsFileUrl [{}]", this, fileUri);
    return "<script" + ((getLoadMode() != JsLoadMode.SYNC)
        ? " " + getLoadMode().toString().toLowerCase()
        : "") + " type=\"text/javascript\" src=\""
        + StringEscapeUtils.escapeHtml(fileUri.toString())
        + "\"></script>";
  }

  public String buildNotFoundWarningComment() {
    return "<!-- " + buildNotFoundWarning() + " -->";
  }

  private String buildNotFoundWarning() {
    return "WARNING: js-file not found: " + getJsFileRef().getFullPath();
  }

  private FileUriServiceRole getAttUrlSrv() {
    return Utils.getComponent(FileUriServiceRole.class);
  }

}
