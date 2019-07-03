package com.celements.rte;

import java.util.Arrays;
import java.util.List;

import org.xwiki.component.annotation.Component;

@Component(RteTinyMce4.NAME)
public class RteTinyMce4 implements RteImplementation {

  public static final String NAME = "tinymce4";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<String> getJavaScriptFiles() {
    // TODO Auto-generated method stub
    return Arrays.asList();
  }

  @Override
  public List<String> getCssFiles() {
    // TODO Auto-generated method stub
    return Arrays.asList();
  }

}
