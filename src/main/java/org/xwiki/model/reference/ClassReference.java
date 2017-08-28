package org.xwiki.model.reference;

import static com.celements.model.util.References.*;
import static com.google.common.base.Preconditions.*;
import static java.text.MessageFormat.format;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.EntityType;

import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.ClassIdentity;
import com.celements.model.context.ModelContext;
import com.google.common.base.Function;
import com.xpn.xwiki.web.Utils;

@Immutable
public class ClassReference extends EntityReference implements ImmutableReference, ClassIdentity {

  private static final long serialVersionUID = -8664491352611685779L;

  private boolean initialised = false;

  public ClassReference(EntityReference reference) {
    super(reference.getName(), reference.getType(), reference.getParent());
    setChild(reference.getChild());
    initialised = true;
  }

  public ClassReference(String spaceName, String className) {
    super(className, EntityType.DOCUMENT, new EntityReference(spaceName, EntityType.SPACE));
    initialised = true;
  }

  private void checkInit() {
    if (initialised) {
      throw new IllegalStateException(format("unable to modify already initialised {0}: {1}",
          this.getClass().getSimpleName(), this));
    }
  }

  @Override
  public void setName(String name) {
    checkInit();
    super.setName(name);
  }

  @Override
  public void setParent(EntityReference parent) {
    checkInit();
    checkArgument((parent != null) && (parent.getType() == EntityType.SPACE),
        "Invalid parent reference [" + parent + "] for a class reference");
    super.setParent(new EntityReference(parent.getName(), EntityType.SPACE));
  }

  @Override
  public EntityReference getParent() {
    return super.getParent().clone();
  }

  @Override
  public void setChild(EntityReference child) {
    checkInit();
    if (child != null) {
      super.setChild(cloneRef(child));
    }
  }

  @Override
  public void setType(EntityType type) {
    checkInit();
    checkArgument(type == EntityType.DOCUMENT, "Invalid type [" + type + "] for a class reference");
    super.setType(EntityType.DOCUMENT);
  }

  @Override
  public ClassReference clone() {
    return this;
  }

  @Override
  public EntityReference getMutable() {
    return new EntityReference(getName(), getType(), getParent());
  }

  @Override
  public DocumentReference getDocRef() {
    return getDocRef(getModelContext().getWikiRef());
  }

  @Override
  public DocumentReference getDocRef(WikiReference wikiRef) {
    return new ImmutableDocumentReference(getName(), new SpaceReference(getParent().getName(),
        wikiRef));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ClassDefinition) {
      obj = ((ClassDefinition) obj).getClassReference();
    }
    return super.equals(obj);
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
