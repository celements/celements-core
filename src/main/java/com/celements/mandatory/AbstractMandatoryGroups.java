package com.celements.mandatory;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public abstract class AbstractMandatoryGroups implements IMandatoryDocumentRole {

  @Requirement
  Execution execution;

  public abstract void checkDocuments() throws XWikiException;

  public AbstractMandatoryGroups() {
    super();
  }

  protected XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  protected abstract String commitName();

  protected void checkGroup(DocumentReference groupRef) throws XWikiException {
    if (!getContext().getWiki().exists(groupRef, getContext())) {
      XWikiDocument editorGroupDoc = new CreateDocumentCommand().createDocument(groupRef,
          "UserGroup");
      if (editorGroupDoc != null) {
        editorGroupDoc.newXObject(getGroupClassRef(getContext().getDatabase()),
            getContext());
        getContext().getWiki().saveDocument(editorGroupDoc, "autocreate " + commitName()
            + " group.", getContext());
      }
    }
  }

  protected DocumentReference getGroupClassRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiGroups");
  }

}