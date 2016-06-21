package com.celements.common.classes;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.model.classes.ClassDefinition;

@ComponentRole
public interface IClassesCompositorComponent {

  /**
   * loads all {@link ClassDefinition} and {@link IClassCollectionRole} (deprecated) and creates
   * XClasses from them if they don't exist yet
   *
   * @throws XClassCreateException
   */
  public void checkClasses() throws XClassCreateException;

  /**
   * @deprecated instead use {@link #checkClasses()}
   */
  @Deprecated
  public void checkAllClassCollections();

  public boolean isActivated(String name);

}
