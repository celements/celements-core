package com.celements.web;

import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWikiConstant;

public final class CelConstant {

  public static final String CENTRAL_WIKI_NAME = "celements2web";
  public static final WikiReference CENTRAL_WIKI = new WikiReference(CENTRAL_WIKI_NAME);
  public static final String XWIKI_SPACE = XWikiConstant.XWIKI_SPACE;
  public static final String WEB_PREF_DOC_NAME = XWikiConstant.WEB_PREF_DOC_NAME;
  public static final String XWIKI_PREF_DOC_NAME = XWikiConstant.XWIKI_PREF_DOC_NAME;

  private CelConstant() {}

}
