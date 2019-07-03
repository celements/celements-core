package com.celements.rte;

import java.util.Arrays;
import java.util.List;

import org.xwiki.component.annotation.Component;

@Component(RteTinyMce3.NAME)
public class RteTinyMce3 implements RteImplementation {

  public static final String NAME = "tinymce3";

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
