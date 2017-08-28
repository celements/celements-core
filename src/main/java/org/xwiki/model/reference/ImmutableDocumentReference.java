package org.xwiki.model.reference;

import static com.celements.model.util.References.*;
import static com.google.common.base.Preconditions.*;
import static java.text.MessageFormat.format;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.EntityType;

@Immutable
public class ImmutableDocumentReference extends DocumentReference implements ImmutableReference {

  private static final long serialVersionUID = 4196990820112451663L;

  private boolean initialised = false;

  public ImmutableDocumentReference(EntityReference reference) {
    super(reference);
    setChild(reference.getChild());
    initialised = true;
  }

  public ImmutableDocumentReference(String wikiName, String spaceName, String docName) {
    super(wikiName, spaceName, docName);
    initialised = true;
  }

  public ImmutableDocumentReference(String docName, SpaceReference parent) {
    super(docName, parent);
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
    checkArgument(((parent != null) && (parent.getType() == EntityType.SPACE) && isAbsoluteRef(
        parent)), "Invalid parent reference [" + parent + "] for a document reference");
    ReferenceParentSetter.setWithoutMutabilityCheck(this, parent);
  }

  @Override
  public SpaceReference getParent() {
    return (SpaceReference) super.getParent().clone();
  }

  @Override
  public void setChild(EntityReference child) {
    checkInit();
    if (child != null) {
      super.setChild(cloneRef(child));
    }
  }

  @Override
  public EntityReference getChild() {
    return super.getChild() != null ? super.getChild().clone() : null;
  }

  @Override
  public void setType(EntityType type) {
    checkInit();
    super.setType(type);
  }

  @Override
  public void setWikiReference(WikiReference newWikiReference) {
    checkInit();
    throw new UnsupportedOperationException();
  }

  @Override
  public ImmutableDocumentReference clone() {
    return this;
  }

  @Override
  public DocumentReference getMutable() {
    DocumentReference ret = new DocumentReference(this);
    ret.setChild(getChild());
    return ret;
  }

}
