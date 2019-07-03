package com.celements.rte;

import java.util.List;

import org.xwiki.component.annotation.Component;

import com.google.common.collect.ImmutableList;

@Component(RteTinyMce4.NAME)
public class RteTinyMce4 implements RteImplementation {

  public static final String NAME = "tinymce4";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<String> getJavaScriptFiles() {
    return ImmutableList.of(
        ":celRTE/4.7.12/tinymce.min.js",
        ":celRTE/4.7.12/plugins/compat3x/plugin.min.js",
        ":structEditJS/tinyMCE4/loadTinyMCE-async.js");
  }

  @Override
  public List<String> getCssFiles() {
    return ImmutableList.of(); // TODO
  }

}
