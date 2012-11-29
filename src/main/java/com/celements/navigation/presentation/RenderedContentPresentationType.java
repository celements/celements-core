package com.celements.navigation.presentation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.INavigation;
import com.celements.rendering.RenderCommand;
import com.xpn.xwiki.XWikiException;

@Component("renderedContent")
public class RenderedContentPresentationType implements IPresentationTypeRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      RenderedContentPresentationType.class);

  RenderCommand renderCmd;

  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem,
      boolean isLastItem, DocumentReference docRef, boolean isLeaf, INavigation nav) {
    LOGGER.debug("writeNodeContent for [" + docRef + "].");
    try {
      outStream.append(getRenderCommand().renderCelementsDocument(docRef, "view"));
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get document for [" + docRef + "].", exp);
    }
  }

  RenderCommand getRenderCommand() {
    if (renderCmd == null) {
      renderCmd = new RenderCommand();
    }
    return renderCmd;
  }

}
