<<<<<<< HEAD
package org.xwiki.model.reference;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.EntityType;

@Immutable
public class ImmutableDocumentReference extends DocumentReference {

  private static final long serialVersionUID = 4196990820112451663L;

  private boolean initialised = false;

  public ImmutableDocumentReference(EntityReference reference) {
    super(reference);
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
    checkState(!initialised, "unable to modify already initialised instance");
  }

  @Override
  public void setName(String name) {
    checkInit();
    super.setName(name);
  }

  @Override
  public void setParent(EntityReference parent) {
    checkInit();
    super.setParent(parent);
  }

  @Override
  public SpaceReference getParent() {
    return (SpaceReference) super.getParent().clone();
  }

  @Override
  public void setChild(EntityReference child) {
    checkInit();
    super.setChild(child);
  }

  @Override
  public void setType(EntityType type) {
    checkInit();
    super.setType(type);
  }

  @Override
  public void setWikiReference(WikiReference newWikiReference) {
    checkInit();
    super.setWikiReference(newWikiReference);
  }

  @Override
  public ImmutableDocumentReference clone() {
    return this;
  }

}
=======
package org.xwiki.model.reference;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.EntityType;

@Immutable
public class ImmutableDocumentReference extends DocumentReference {

  private static final long serialVersionUID = 4196990820112451663L;

  private boolean initialised = false;

  public ImmutableDocumentReference(EntityReference reference) {
    super(reference);
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
    checkState(!initialised, "unable to modify already initialised instance");
  }

  @Override
  public void setName(String name) {
    checkInit();
    super.setName(name);
  }

  @Override
  public void setParent(EntityReference parent) {
    checkInit();
    super.setParent(parent);
  }

  @Override
  public SpaceReference getParent() {
    return (SpaceReference) super.getParent().clone();
  }

  @Override
  public void setChild(EntityReference child) {
    checkInit();
    super.setChild(child);
  }

  @Override
  public void setType(EntityType type) {
    checkInit();
    super.setType(type);
  }

  @Override
  public void setWikiReference(WikiReference newWikiReference) {
    checkInit();
    super.setWikiReference(newWikiReference);
  }

  @Override
  public ImmutableDocumentReference clone() {
    return this;
  }

}
>>>>>>> refs/remotes/origin/CELDEV-519
