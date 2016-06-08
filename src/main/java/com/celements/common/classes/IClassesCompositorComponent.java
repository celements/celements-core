package com.celements.common.classes;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IClassesCompositorComponent {

  public void checkClasses();

  /**
   * @deprecated instead use {@link #checkClasses()}
   */
  @Deprecated
  public void checkAllClassCollections();

  public boolean isActivated(String name);

}
