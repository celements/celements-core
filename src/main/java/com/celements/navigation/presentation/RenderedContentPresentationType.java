package com.celements.navigation.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.ICellWriter;
import com.celements.navigation.INavigation;
import com.celements.rendering.RenderCommand;
import com.xpn.xwiki.XWikiException;

@Component("renderedContent")
public class RenderedContentPresentationType implements IPresentationTypeRole<INavigation> {

  private static Logger LOGGER = LoggerFactory.getLogger(RenderedContentPresentationType.class);

  private static final String _CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS = "cel_cm_presentation_treenode";

  RenderCommand renderCmd;

  @Override
  public void writeNodeContent(ICellWriter writer, DocumentReference docRef,
      INavigation navigation) {
    writeNodeContent(writer.getAsStringBuilder(), false, false, docRef, true, 0, navigation);
  }

  @Override
  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem, INavigation nav) {
    LOGGER.debug("writeNodeContent for [" + docRef + "].");
    outStream.append("<div ");
    outStream.append(nav.addCssClasses(docRef, true, isFirstItem, isLastItem, isLeaf, numItem)
        + " ");
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

  @Override
  public String getDefaultCssClass() {
    return _CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS;
  }

  @Override
  public String getEmptyDictionaryKey() {
    return "cel_nav_empty_presentation";
  }

  @Override
  public SpaceReference getPageLayoutForDoc(DocumentReference docRef) {
    return null;
  }

}
