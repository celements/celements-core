package com.celements.rendering.head;

import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.javascript.ExtJsFileParameter;

@ComponentRole
public interface LegacyLayoutInitalJavaScriptLoaderRole {

  @NotNull
  Stream<ExtJsFileParameter> getModuleInitialJavaScriptFiles();

}
