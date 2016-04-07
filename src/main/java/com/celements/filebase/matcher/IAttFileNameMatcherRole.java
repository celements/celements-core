package com.celements.filebase.matcher;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IAttFileNameMatcherRole extends IAttachmentMatcher {

  public void setFileNamePattern(String fileNamePattern);

  public String getFileNamePattern();

}
