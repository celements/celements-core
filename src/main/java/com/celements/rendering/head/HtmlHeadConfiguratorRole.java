package com.celements.rendering.head;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.javascript.ExtJsFileParameter;

@ComponentRole
public interface HtmlHeadConfiguratorRole {

  @NotNull
  List<ExtJsFileParameter> getAllInitialJavaScriptFiles();

}
