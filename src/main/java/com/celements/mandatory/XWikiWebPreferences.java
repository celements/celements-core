package com.celements.mandatory;

import java.util.List;

import org.slf4j.Logger;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class XWikiWebPreferences extends AbstractMandatoryDocument {

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean checkDocuments(XWikiDocument arg0) throws XWikiException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected boolean checkDocumentsMain(XWikiDocument arg0) throws XWikiException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected DocumentReference getDocRef() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Logger getLogger() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean skip() {
    // TODO Auto-generated method stub
    return false;
  }

}
