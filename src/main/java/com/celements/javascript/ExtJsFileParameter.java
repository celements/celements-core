package com.celements.javascript;

import static com.google.common.base.Preconditions.*;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.ressource_url.RessourceUrlServiceRole;
import com.celements.ressource_url.UrlRessourceNotExistException;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtJsFileParameter.class);

  private final String action;
  private final String jsFileUrl;
  private final JsLoadMode loadMode;
  private final String queryString;
  private final boolean lazyLoad;

  private ExtJsFileParameter(Builder buildParams) {
    action = Strings.emptyToNull(buildParams.action);
    queryString = Strings.emptyToNull(buildParams.queryString);
    lazyLoad = buildParams.lazyLoad;
    checkNotNull(buildParams.jsFileEntry);
    jsFileUrl = Strings.emptyToNull(buildParams.jsFileEntry.getFilepath());
    checkNotNull(jsFileUrl);
    loadMode = buildParams.jsFileEntry.getLoadMode();
  }

  @NotNull
  public String getJsFile() {
    return jsFileUrl;
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
    return Objects.hash(jsFileUrl, loadMode, queryString, lazyLoad);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return (obj instanceof ExtJsFileParameter)
        && Objects.equals(((ExtJsFileParameter) obj).jsFileUrl, this.jsFileUrl)
        && Objects.equals(((ExtJsFileParameter) obj).queryString, this.queryString));
  }

  @Override
  public String toString() {
    return "ExtJsFileParameter [action=" + action + ", jsFileUrl=" + jsFileUrl + ", loadMode="
        + loadMode + ", queryString=" + queryString + ", lazyLoad=" + lazyLoad + "]";
  }

  @NotEmpty
  public String getLazyLoadTag() {
    final JsonBuilder jsonBuilder = new JsonBuilder();
    jsonBuilder.openDictionary();
    try {
      jsonBuilder.addProperty("fullURL",
          getAttUrlSrv().createRessourceUrl(getJsFile(), getAction(), getQueryString()));
    } catch (UrlRessourceNotExistException exp) {
      LOGGER.info("URL ressource '{}' does not exist.", getJsFile(), exp);
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
    } catch (UrlRessourceNotExistException exp) {
      LOGGER.info("URL ressource '{}' does not exist.", getJsFile(), exp);
      return buildNotFoundWarningComment();
    }
  }

  public String getScriptTagString()
      throws UrlRessourceNotExistException {
    final String fileUrl = getAttUrlSrv().createRessourceUrl(getJsFile(), getAction(),
        getQueryString());
    LOGGER.info("getScriptTagString: extJsFileParams [{}] jsFileUrl [{}]", this, fileUrl);
    return "<script" + ((getLoadMode() != JsLoadMode.SYNC)
        ? " " + getLoadMode().toString().toLowerCase()
        : "") + " type=\"text/javascript\" src=\"" + StringEscapeUtils.escapeHtml(fileUrl)
        + "\"></script>";
  }

  public String buildNotFoundWarningComment() {
    return "<!-- " + buildNotFoundWarning() + " -->";
  }

  private String buildNotFoundWarning() {
    return "WARNING: js-file not found: " + getJsFile();
  }

  private RessourceUrlServiceRole getAttUrlSrv() {
    return Utils.getComponent(RessourceUrlServiceRole.class);
  }

}
