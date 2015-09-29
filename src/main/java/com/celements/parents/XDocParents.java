package com.celements.parents;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component(XDocParents.DOC_PROVIDER_NAME)
public class XDocParents implements IDocParentProviderRole {
  
  private static Logger _LOGGER = LoggerFactory.getLogger(XDocParents.class);

  public static final String DOC_PROVIDER_NAME = "xwiki";

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef) {
    ArrayList<DocumentReference> docParents = new ArrayList<DocumentReference>();
    try {
      DocumentReference nextParent = getParentRef(docRef);
      while ((nextParent != null) && getContext().getWiki().exists(nextParent,
          getContext()) && !docParents.contains(nextParent)) {
        docParents.add(nextParent);
        nextParent = getParentRef(nextParent);
      }
    } catch (XWikiException exp) {
      _LOGGER.error("Failed to get parent reference. ", exp);
    }
    return docParents;
  }

  private DocumentReference getParentRef(DocumentReference docRef) throws XWikiException {
      return getContext().getWiki().getDocument(docRef, getContext()
          ).getParentReference();
  }

}
