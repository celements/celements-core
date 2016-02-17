package com.celements.filebase.matcher;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

import com.xpn.xwiki.doc.XWikiAttachment;

@Component(AttNameWithoutExtMatcher.ATT_NAME_WITHOUT_EXT_MATCHER)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class AttNameWithoutExtMatcher implements IAttFileNameMatcherRole {

  private static Logger LOGGER  = LoggerFactory.getLogger(AttNameWithoutExtMatcher.class);

  public static final String ATT_NAME_WITHOUT_EXT_MATCHER = "attNameWithoutExtMatcher";
  static final Pattern ATT_NAME_MATCH_WITHOUT_EXT = Pattern.compile("\\.[^.]+$");

  private String fileNamePattern;

  @Override
  public boolean accept(XWikiAttachment attachment) {
    if((attachment == null) || (fileNamePattern == null)) {
      LOGGER.debug("accept failed with Attachment [" + attachment + "] and " +
          "fileNamePattern [" + fileNamePattern + "]");
      return false;
    }
    String[] split = ATT_NAME_MATCH_WITHOUT_EXT.split(attachment.getFilename());
    return (split.length == 1) && split[0].equals(fileNamePattern);
  }

  @Override
  public void setFileNamePattern(String fileNamePattern) {
    this.fileNamePattern = fileNamePattern;
  }

}
