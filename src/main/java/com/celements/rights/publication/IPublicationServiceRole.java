package com.celements.rights.publication;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.rights.CelementsRightServiceImpl.PubUnpub;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IPublicationServiceRole {

  public boolean isPubUnpubOverride();

  public boolean isPubOverride();

  public boolean isUnpubOverride();

  public boolean isRestrictedRightsAction(String accessLevel);

  public boolean isPublishActive();

  public boolean isPublishActive(DocumentReference docRef);

  public boolean isPublished(XWikiDocument doc);

  public void overridePubUnpub(PubUnpub value);

}
