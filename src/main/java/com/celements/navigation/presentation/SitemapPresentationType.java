package com.celements.navigation.presentation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.INavigation;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("sitemap")
public class SitemapPresentationType extends DefaultPresentationType {

  private static final Logger LOGGER = LoggerFactory.getLogger(SitemapPresentationType.class);

  private static final String _CEL_CM_SM_TREENODE_DEFAULT_CSSCLASS = "cel_cm_sitemap_treenode";

  @Override
  public String getDefaultCssClass() {
    return _CEL_CM_SM_TREENODE_DEFAULT_CSSCLASS;
  }

  @Override
  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem, INavigation navigation) {
    try {
      appendMenuItemLink(outStream, isFirstItem, isLastItem, docRef, isLeaf, numItem, navigation);
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
      outStream.append("<div class=\"docLangs\">");
      for (String lang : webUtilsService.getAllowedLanguages(spaceName)) {
        outStream.append("<a ");
        outStream.append("title=\"" + getLangName(lang) + "\" ");
        outStream.append("href=\"" + nodeDoc.getURL("edit", "language=" + lang, getContext())
            + "\" ");
        String cssClasses = "";
        if (lang.equals(defaultLanguage)) {
          cssClasses += " defaultLanguage";
        }
        if (transList.contains(lang) || lang.equals(defaultLanguage)) {
          cssClasses += " transExists";
        } else {
          cssClasses += " transNotExists";
        }
        outStream.append("target=\"_blank\" ");
        outStream.append("class=\"" + cssClasses.trim() + "\">");
        outStream.append(lang);
        outStream.append("</a>");
      }
      outStream.append("</div>");
    } catch (XWikiException exp) {
      LOGGER.error("addLanguageButtons: failed to get nodeDoc for [" + docRef + "].", exp);
    }
  }

  private String getLangName(String lang) {
    return webUtilsService.getAdminMessageTool().get("cel_" + lang);
  }

}
