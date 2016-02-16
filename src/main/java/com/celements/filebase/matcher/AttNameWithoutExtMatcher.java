package com.celements.filebase.matcher;

import java.util.regex.Pattern;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

import com.xpn.xwiki.doc.XWikiAttachment;

@Component(AttNameWithoutExtMatcher.ATT_NAME_WITHOUT_EXT_MATCHER)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class AttNameWithoutExtMatcher implements IAttFileNameMatcherRole {

  public static final String ATT_NAME_WITHOUT_EXT_MATCHER = "attNameWithoutExtMatcher";
  static final Pattern ATT_NAME_MATCH_WITHOUT_EXT = Pattern.compile("\\.[^.]+$");

  private String fileNamePattern;

  @Override
  public boolean accept(XWikiAttachment attachment) {
    //TODO
    return false;
  }

  @Override
  public void setFileNamePattern(String fileNamePattern) {
    this.fileNamePattern = fileNamePattern;
  }

}
