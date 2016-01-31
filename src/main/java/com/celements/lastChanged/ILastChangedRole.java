package com.celements.lastChanged;

import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.SpaceReference;

@ComponentRole
public interface ILastChangedRole {

  public Date getLastUpdatedDate();

  public Date getLastUpdatedDate(SpaceReference spaceRef);

  public List<String[]> getLastChangedDocuments(int numEntries);

  public List<String[]> getLastChangedDocuments(int numEntries, SpaceReference spaceRef);

  /**
   * @deprecated instead use List<String[]> getLastChangedDocuments(int, SpaceReference)
   */
  @Deprecated
  public List<String[]> getLastChangedDocuments(int numEntries, String space);

}
