package com.celements.auth.user;

import java.util.Objects;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiUser;

@Component(CelementsUser.NAME)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class CelementsUser implements User {

  public static final String NAME = "CelementsUser";

  @Requirement(XWikiUsersClass.CLASS_DEF_HINT)
  private ClassDefinition usersClass;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  private XWikiDocument userDoc;

  @Override
  public void initialize(DocumentReference userDocRef) throws UserInstantiationException {
    try {
      userDoc = modelAccess.getDocument(userDocRef);
      if (!getUserObjectFetcher().exists()) {
        throw new UserInstantiationException("No user object on doc: " + userDocRef);
      }
    } catch (DocumentNotExistsException exc) {
      throw new UserInstantiationException(exc);
    }
  }

  @Override
  public DocumentReference getDocRef() {
    return userDoc.getDocumentReference();
  }

  @Override
  public XWikiDocument getDocument() {
    return userDoc;
  }

  @Override
  public XWikiUser asXWikiUser() {
    return new XWikiUser(modelUtils.serializeRefLocal(getDocRef()));
  }

  @Override
  public Optional<String> getEmail() {
    return getUserFieldValue(XWikiUsersClass.FIELD_EMAIL);
  }

  @Override
  public boolean isActive() {
    return getUserFieldValue(XWikiUsersClass.FIELD_ACTIVE).or(false);
  }

  @Override
  public Optional<String> getAdminLanguage() {
    return getUserFieldValue(XWikiUsersClass.FIELD_ADMIN_LANG);
  }

  private <T> Optional<T> getUserFieldValue(ClassField<T> field) {
    return modelAccess.getFieldValue(getUserObject(), field);
  }

  private BaseObject getUserObject() {
    return getUserObjectFetcher().first().get();
  }

  private XWikiObjectFetcher getUserObjectFetcher() {
    return XWikiObjectFetcher.on(userDoc).filter(usersClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDocRef());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof User) {
      User other = (User) obj;
      return Objects.equals(this.getDocRef(), other.getDocRef());
    }
    return false;
  }

  @Override
  public String toString() {
    return "CelementsUser [userDocRef=" + getDocRef() + "]";
  }

}
