package com.celements.filebase;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.filebase.exceptions.NoValidFileBaseImplFound;

@ComponentRole
public interface IFileBaseAccessRole {

  public static final String FILEBASE_SERVICE_IMPL_CFG = "filebase_service_impl";

  public IFileBaseServiceRole getInstance() throws NoValidFileBaseImplFound;

}
