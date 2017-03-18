package org.xwiki.model.reference;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.EntityType;

import com.celements.model.context.ModelContext;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.xpn.xwiki.web.Utils;

@Immutable
public class ClassReference extends EntityReference {

  private static final long serialVersionUID = -8664491352611685779L;

  private boolean initialised = false;

  public ClassReference(EntityReference reference) {
    super(reference.getName(), reference.getType(), reference.getParent());
    initialised = true;
  }

  public ClassReference(String spaceName, String className) {
    super(className, EntityType.DOCUMENT, new EntityReference(spaceName, EntityType.SPACE));
    initialised = true;
  }

  private void checkInit() {
    Preconditions.checkState(!initialised, "unable to modify already initialised instance");
  }

  @Override
  public void setName(String name) {
    checkInit();
    super.setName(name);
  }

  @Override
  public void setParent(EntityReference parent) {
    checkInit();
    if ((parent == null) || (parent.getType() != EntityType.SPACE)) {
      throw new IllegalArgumentException("Invalid parent reference [" + parent
          + "] for a document reference");
    }
    super.setParent(new EntityReference(parent.getName(), EntityType.SPACE));
  }

  @Override
  public void setChild(EntityReference child) {
    checkInit();
    super.setChild(child);
  }

  @Override
  public void setType(EntityType type) {
    checkInit();
    if (type != EntityType.DOCUMENT) {
      throw new IllegalArgumentException("Invalid type [" + type + "] for a document reference");
    }
    super.setType(EntityType.DOCUMENT);
  }

  public DocumentReference getDocumentReference() {
    return getDocumentReference(getModelContext().getWikiRef());
  }

  public DocumentReference getDocumentReference(WikiReference wikiRef) {
    return new DocumentReference(getName(), new SpaceReference(getParent().getName(), wikiRef));
  }

  private static ModelContext getModelContext() {
    return Utils.getComponent(ModelContext.class);
  }

  public static final Function<DocumentReference, ClassReference> FUNC_DOC_TO_CLASS_REF = new Function<DocumentReference, ClassReference>() {

    @Override
    public ClassReference apply(DocumentReference docRef) {
      return new ClassReference(docRef);
    }
  };

}
