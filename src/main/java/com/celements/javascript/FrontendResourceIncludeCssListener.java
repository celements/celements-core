package com.celements.javascript;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.celements.web.plugin.cmd.CssCommand;
import com.celements.web.plugin.cmd.ExternalJavaScriptFilesCommand;
import com.celements.web.plugin.cmd.IExtJSFilesListener;

@Component
public class FrontendResourceIncludeCssListener implements IExtJSFilesListener {

  private final CssCommand cssCommand;
  private final FrontendResourceResolver resolver;

  @Inject
  public FrontendResourceIncludeCssListener(
      CssCommand cssCommand,
      FrontendResourceResolver resolver) {
    this.cssCommand = cssCommand;
    this.resolver = resolver;
  }

  @Override
  public void beforeAllExtFinish(ExternalJavaScriptFilesCommand jsCommand) {
    // Include frontend entry sources. CSSEngine treats :frontend/... values as manifest keys and
    // expands them to the CSS assets emitted for that entrypoint.
    jsCommand.streamExtJsFiles()
        .filter(resolver::isFrontendSource)
        .forEach(cssCommand::includeCSSPage);
  }
}
