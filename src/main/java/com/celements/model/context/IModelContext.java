package com.celements.model.context;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWikiContext;

@ComponentRole
public interface IModelContext {

  public static final String WEB_PREF_DOC_NAME = "WebPreferences";
  public static final String CFG_KEY_DEFAULT_LANG = "default_language";
  public static final String FALLBACK_DEFAULT_LANG = "en";

  public XWikiContext getXWikiContext();

  /**
   * @return the current wiki set in context
   */
  @NotNull
  public WikiReference getWiki();

  /**
   * @param wikiRef
   *          to be set in context
   * @return the wiki which was set before
   */
  @NotNull
  public WikiReference setWiki(@NotNull WikiReference wikiRef);

  @Nullable
  public DocumentReference getDoc();

  @Nullable
  public DocumentReference setDoc(@NotNull DocumentReference docRef);

  @NotNull
  public String getDefaultLanguage();

  @NotNull
  public String getDefaultLanguage(@NotNull EntityReference ref);

}
