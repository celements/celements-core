package com.celements.model.context;

import java.net.URL;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.XWikiRequest;
import com.xpn.xwiki.web.XWikiResponse;

@ComponentRole
public interface ModelContext {

  public static final String WEB_PREF_DOC_NAME = "WebPreferences";
  public static final String CFG_KEY_DEFAULT_LANG = "default_language";
  public static final String FALLBACK_DEFAULT_LANG = "en";

  /**
   * WARNING: This call is discouraged, use other methods of this service. It will be deprecated
   * once we'll have a replacement for all of them.
   *
   * @return the old, discouraged {@link XWikiContext}
   */
  @NotNull
  public XWikiContext getXWikiContext();

  /**
   * @return the current wiki set in context
   */
  @NotNull
  public WikiReference getWikiRef();

  /**
   * @param wikiRef
   *          to be set in context
   * @return the wiki which was set before
   */
  @NotNull
  public WikiReference setWikiRef(@NotNull WikiReference wikiRef);

  @NotNull
  public WikiReference getMainWikiRef();

  /**
   * @return the current doc set in context
   */
  @Nullable
  public XWikiDocument getDoc();

  /**
   * @param doc
   *          to be set in context
   * @return the doc which was set before
   */
  @Nullable
  public XWikiDocument setDoc(@Nullable XWikiDocument doc);

  @Nullable
  public XWikiUser getUser();

  @Nullable
  public XWikiUser setUser(@Nullable XWikiUser user);

  @NotNull
  public String getUserName();

  @NotNull
  public Optional<XWikiRequest> getRequest();

  @NotNull
  public Optional<String> getRequestParameter(String key);

  @NotNull
  public Optional<XWikiResponse> getResponse();

  /**
   * @return the default language for the current wiki
   */
  @NotNull
  public String getDefaultLanguage();

  /**
   * @param ref
   *          from which the default language is extracted (document, space, or wiki)
   * @return the default language for the given reference
   */
  @NotNull
  public String getDefaultLanguage(@NotNull EntityReference ref);

  /**
   * @return the current url set in context
   */
  @NotNull
  public Optional<URL> getUrl();

  /**
   * @param url
   *          to be set in context
   * @return the url which was set before
   */
  @NotNull
  public Optional<URL> setUrl(@Nullable URL url);

}
