package com.celements.filebase;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.filebase.exceptions.NoValidFileBaseImplFound;
import com.google.common.base.Strings;

@Component
public class FileBaseAccessService implements IFileBaseAccessRole {

  private static Logger LOGGER = LoggerFactory.getLogger(FileBaseAccessService.class);

  @Requirement
  ConfigurationSource configuration;

  @Requirement
  Map<String, IFileBaseServiceRole> fileBaseSrvMap;

  @Override
  public IFileBaseServiceRole getInstance() throws NoValidFileBaseImplFound {
    String fileBaseImplKey = getFileBaseImplKey();
    if (!Strings.isNullOrEmpty(fileBaseImplKey) && !"default".equals(fileBaseImplKey)) {
      return fileBaseSrvMap.get(fileBaseImplKey);
    }
    LOGGER.error("Failed to get valid FileBase implementation instance.");
    throw new NoValidFileBaseImplFound(fileBaseImplKey);
  }

  private String getFileBaseImplKey() {
    return configuration.getProperty(FILEBASE_SERVICE_IMPL_CFG,
        SingleDocFileBaseService.FILEBASE_SINGLE_DOC);
  }

}
