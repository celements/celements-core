package com.celements.model.context;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiUser;

@ComponentRole
public interface ModelContext {

  public static final String WEB_PREF_DOC_NAME = "WebPreferences";
  public static final String CFG_KEY_DEFAULT_LANG = "default_language";
  public static final String FALLBACK_DEFAULT_LANG = "en";

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

}
