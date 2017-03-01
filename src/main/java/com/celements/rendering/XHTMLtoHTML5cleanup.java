package com.celements.rendering;

import org.xwiki.component.annotation.Component;

/**
 * @author Edoardo Beutler
 * @deprecated Slef closing tags are allowed as syntactic sugar (hence not required)
 */
@Component
@Deprecated
public class XHTMLtoHTML5cleanup implements IXHTMLtoHTML5cleanup {

  @Override
  @Deprecated
  public String cleanAll(String xhtml) {
    String html5 = removeSelfclosingTags(xhtml);
    return html5;
  }

  /*
   * Slef closing tags are allowed as syntactic sugar (hence not required)
   */
  public String removeSelfclosingTags(String xhtml) {
    if (xhtml != null) {
      return xhtml.replaceAll("<((\".*?\"|'.*?'|[^>\"]*)*)/>", "<$1>");
    }
    return null;
  }
}
