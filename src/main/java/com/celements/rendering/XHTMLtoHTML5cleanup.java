package com.celements.rendering;

public class XHTMLtoHTML5cleanup {
  
  public static String cleanAll(String xhtml) {
    String html5 = removeSelfclosingTags(xhtml);
    return html5;
  }
  
  public static String removeSelfclosingTags(String xhtml) {
    if(xhtml != null) {
      return xhtml.replaceAll("<((\".*?\"|'.*?'|[^>\"]*)*)/>", "<$1>");
    }
    return null;
  }
}