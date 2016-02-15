package com.celements.filebase;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.filebase.exceptions.FileBaseLoadException;
import com.celements.filebase.exceptions.FileNotExistsException;
import com.celements.filebase.exceptions.NoValidFileBaseImplFound;
import com.celements.filebase.matcher.IAttachmentMatcher;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiAttachment;

@Component
public class DefaultFileBaseService implements IFileBaseServiceRole {

  private static Logger LOGGER = LoggerFactory.getLogger(DefaultFileBaseService.class);

  @Requirement
  ConfigurationSource configuration;

  @Requirement
  Map<String, IFileBaseServiceRole> fileBaseSrvMap;

  IFileBaseServiceRole getFileBaseService() throws NoValidFileBaseImplFound {
    String fileBaseImplKey = getFileBaseImplKey();
    if (!Strings.isNullOrEmpty(fileBaseImplKey) && !"default".equals(fileBaseImplKey)) {
      return fileBaseSrvMap.get(fileBaseImplKey);
    }
    throw new NoValidFileBaseImplFound(fileBaseImplKey);
  }

  private String getFileBaseImplKey() {
    return configuration.getProperty(FILEBASE_SERVICE_IMPL_CFG,
        SingleDocFileBaseService.FILEBASE_SINGLE_DOC);
  }

  @Override
  public boolean existsFileNameEqual(String filename) throws FileBaseLoadException {
    try {
      return getFileBaseService().existsFileNameEqual(filename);
    } catch (NoValidFileBaseImplFound exp) {
      LOGGER.error("existsFileNameEqual failed.", exp);
      throw new FileBaseLoadException(filename, exp);
    }
  }

  @Override
  public XWikiAttachment getFileNameEqual(String filename
      ) throws FileNotExistsException, FileBaseLoadException {
    try {
      return getFileBaseService().getFileNameEqual(filename);
    } catch (NoValidFileBaseImplFound exp) {
      LOGGER.error("getFileNameEqual failed.", exp);
      throw new FileBaseLoadException(filename, exp);
    }
  }

  @Override
  public List<XWikiAttachment> getFilesNameMatch(IAttachmentMatcher attMatcher)
      throws FileBaseLoadException {
    try {
      return getFileBaseService().getFilesNameMatch(attMatcher);
    } catch (NoValidFileBaseImplFound exp) {
      LOGGER.error("getFileNameEqual failed.", exp);
      throw new FileBaseLoadException(exp);
    }
  }

}
