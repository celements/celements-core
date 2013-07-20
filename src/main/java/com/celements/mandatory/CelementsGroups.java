package com.celements.mandatory;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiException;

@Component("celements.MandatoryGroups")
public class CelementsGroups extends AbstractMandatoryGroups {

  @Override
  protected String commitName() {
    return "mandatory celements";
  }

  @Override
  public void checkDocuments() throws XWikiException {
    checkGroup(getContentEditorGroupRef(getContext().getDatabase()));
    checkGroup(getAdminGroupRef(getContext().getDatabase()));
    checkGroup(getAllGroupRef(getContext().getDatabase()));
  }

  public DocumentReference getAdminGroupRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiAdminGroup");
  }

  public DocumentReference getAllGroupRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiAllGroup");
  }

  public DocumentReference getContentEditorGroupRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "ContentEditorsGroup");
  }

}
