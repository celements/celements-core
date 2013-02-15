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

  private static final String _CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS =
    "cel_cm_presentation_treenode";

  RenderCommand renderCmd;

  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem,
      boolean isLastItem, DocumentReference docRef, boolean isLeaf, int numItem,
      INavigation nav) {
    LOGGER.debug("writeNodeContent for [" + docRef + "].");
    outStream.append("<div ");
    outStream.append(nav.addCssClasses(docRef, true, isFirstItem, isLastItem, isLeaf,
        numItem) + " ");
    outStream.append(nav.addUniqueElementId(docRef) + ">\n");
    try {
      outStream.append(getRenderCommand().renderCelementsDocument(docRef, "view"));
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get document for [" + docRef + "].", exp);
    }
    outStream.append("</div>\n");
  }

  RenderCommand getRenderCommand() {
    if (renderCmd == null) {
      renderCmd = new RenderCommand();
    }
    return renderCmd;
  }

  public String getDefaultCssClass() {
    return _CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS;
  }

  public String getEmptyDictionaryKey() {
    return "cel_nav_empty_presentation";
  }

}
