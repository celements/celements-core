package com.celements.navigation.presentation;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.INavigation;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("sitemap")
public class SitemapPresentationType extends DefaultPresentationType {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      SitemapPresentationType.class);

  @Requirement
  IWebUtilsService webUtilsService;

  private static final String _CEL_CM_SM_TREENODE_DEFAULT_CSSCLASS =
    "cel_cm_sitemap_treenode";

  public String getDefaultCssClass() {
    return _CEL_CM_SM_TREENODE_DEFAULT_CSSCLASS;
  }

  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem,
      boolean isLastItem, DocumentReference docRef, boolean isLeaf,
      INavigation navigation) {
    try {
      appendMenuItemLink(outStream, isFirstItem, isLastItem, docRef, isLeaf, navigation);
      addLanguageLinks(outStream, docRef);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to writeNodeContent for docRef [" + docRef + "].", exp);
    }
  }

  void addLanguageLinks(StringBuilder outStream, DocumentReference docRef) {
    XWiki wiki = getContext().getWiki();
    try {
      XWikiDocument nodeDoc = wiki.getDocument(docRef, getContext());
      List<String> transList = nodeDoc.getTranslationList(getContext());
      String spaceName = docRef.getLastSpaceReference().getName();
      String defaultLanguage = webUtilsService.getDefaultLanguage(spaceName);
      for (String lang : webUtilsService.getAllowedLanguages(spaceName)) {
        outStream.append("<a ");
        outStream.append("title=\"" + getLangName(lang) + "\" ");
        outStream.append("href=\"" + nodeDoc.getURL("edit", "language=" + lang,
            getContext()) + "\" ");
        String cssClasses = "";
        if (lang.equals(defaultLanguage)) {
          cssClasses += " defaultLanguage";
        }
        if (transList.contains(lang)) {
          cssClasses += " transExists";
        } else {
          cssClasses += " transNotExists";
        }
        outStream.append("class=\"" + cssClasses.trim() + "\">");
        outStream.append(lang);
        outStream.append("</a>");
      }
    } catch (XWikiException exp) {
      LOGGER.error("addLanguageButtons: failed to get nodeDoc for [" + docRef + "].",
          exp);
    }
  }

  private String getLangName(String lang) {
    return webUtilsService.getAdminMessageTool().get("cel_" + lang);
  }

}
