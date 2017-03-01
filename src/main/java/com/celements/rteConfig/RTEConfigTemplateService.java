package com.celements.rteConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.rteConfig.classes.RTEConfigClasses;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class RTEConfigTemplateService implements IRTEConfigTemplateRole {

  public static final String PAGE_TYPE_CONFIG_SPACE = "PageTypes";

  @Requirement
  IPageTypeResolverRole pageTypeResolver;

  @Requirement
  protected Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Requirement("celements.rteConfigClasses")
  IClassCollectionRole rteConfigClasses;

  private RTEConfigClasses getRteConfigClasses() {
    return (RTEConfigClasses) rteConfigClasses;
  }

  @Override
  public List<BaseObject> getRTETemplateList() throws XWikiException {
    XWikiDocument doc = getContext().getDoc();
    List<BaseObject> templateList = new ArrayList<>();

    // Doc
    templateList = getTemplateListFromDoc(doc.getDocumentReference());

    // PageType
    if (templateList.isEmpty()) {
      templateList = getRTEConfigTemplateListFromPageType();
    }

    // WebPreferences
    if (templateList.isEmpty()) {
      String space = getContext().getDoc().getDocumentReference().getLastSpaceReference().getName();
      templateList = getTemplateListFromDoc(new DocumentReference(getWikiName(), space,
          "WebPreferences"));
    }

    // XWikiPreferences
    if (templateList.isEmpty()) {
      templateList = getTemplateListFromDoc(new DocumentReference(getWikiName(), "XWiki",
          "XWikiPreferences"));
    }

    return templateList;
  }

  private List<BaseObject> getRTEConfigTemplateListFromPageType() throws XWikiException {
    PageTypeReference pageTypeRef = pageTypeResolver.getPageTypeRefForCurrentDoc();
    DocumentReference pageTypeConfigDocRef = getPageTypeConfigDocRef(pageTypeRef);
    return getTemplateListFromDoc(pageTypeConfigDocRef);
  }

  private DocumentReference getPageTypeConfigDocRef(PageTypeReference pageTypeRef) {
    return new DocumentReference(getWikiName(), PAGE_TYPE_CONFIG_SPACE,
        pageTypeRef.getConfigName());
  }

  private String getWikiName() {
    return getContext().getDatabase();
  }

  private List<BaseObject> getTemplateListFromDoc(DocumentReference docRef) throws XWikiException {
    XWikiDocument tempDoc = getContext().getWiki().getDocument(docRef, getContext());
    List<BaseObject> templList = tempDoc.getXObjects(
        getRteConfigClasses().getRTEConfigTemplateClassRef(getWikiName()));
    if (templList != null) {
      return templList;
    }
    return Collections.emptyList();
  }

}
