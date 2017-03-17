package org.xwiki.model.reference;

import org.xwiki.model.EntityType;

import com.celements.model.context.ModelContext;
import com.google.common.base.Function;
import com.xpn.xwiki.web.Utils;

public class ClassReference extends EntityReference {

  private static final long serialVersionUID = -8664491352611685779L;

  public ClassReference(EntityReference reference) {
    super(reference.getName(), reference.getType(), reference.getParent());
  }

  public ClassReference(String spaceName, String className) {
    super(className, EntityType.DOCUMENT, new EntityReference(spaceName, EntityType.SPACE));
  }

  @Override
  public void setParent(EntityReference parent) {
    if ((parent == null) || (parent.getType() != EntityType.SPACE)) {
      throw new IllegalArgumentException("Invalid parent reference [" + parent
          + "] for a document reference");
    }
    super.setParent(new EntityReference(parent.getName(), EntityType.SPACE));
  }

  @Override
  public void setType(EntityType type) {
    if (type != EntityType.DOCUMENT) {
      throw new IllegalArgumentException("Invalid type [" + type + "] for a document reference");
    }
    super.setType(EntityType.DOCUMENT);
  }

  public DocumentReference getDocumentReference() {
    return getDocumentReference(Utils.getComponent(ModelContext.class).getWikiRef());
  }

  public DocumentReference getDocumentReference(WikiReference wikiRef) {
    return new DocumentReference(getName(), new SpaceReference(getParent().getName(), wikiRef));
  }

  public static final Function<DocumentReference, ClassReference> FUNC_TO = new Function<DocumentReference, ClassReference>() {

    @Override
    public ClassReference apply(DocumentReference docRef) {
      return new ClassReference(docRef);
    }
  };

}
