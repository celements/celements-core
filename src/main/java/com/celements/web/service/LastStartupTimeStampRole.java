package com.celements.web.service;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface LastStartupTimeStampRole {

  /**
   * getLastChangedTimeStamp the formated date is e.g. used as url versioning in attachment urls
   *
   * @return changedDate formated as yyyyMMddHHmmss
   */
  public String getLastChangedTimeStamp(Date changedDate);

  /**
   * getLastStartupTimeStamp
   *
   * @return formatted yyyyMMddHHmmss "startup" timestamp
   */
  @NotNull
  public String getLastStartupTimeStamp();

  /**
   * resetLastStartupTimeStamp reset "startup" timestamp to current date object
   */
  public void resetLastStartupTimeStamp();

  /**
   * getFileModificationDate the formated file modification date is e.g. used as url versioning in
   * resource file urls
   *
   * @param path
   *          relative path of file in 'resources' directory
   * @return formatted yyyyMMddHHmmss modification date of given resource
   */
  @NotNull
  public String getFileModificationDate(@NotNull String path);

}
