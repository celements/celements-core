package com.celements.navigation.presentation;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.ICellWriter;
import com.celements.navigation.INavigation;
import com.celements.rendering.RenderCommand;
import com.xpn.xwiki.XWikiException;

@Component(value = PresentationContentRenderer.RENDERED_CONTENT_HINT, roles = {
    IPresentationTypeRole.class, PresentationContentRenderer.class })
public class RenderedContentPresentationType
    implements IPresentationTypeRole<INavigation>, PresentationContentRenderer {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RenderedContentPresentationType.class);

  private static final String CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS = "cel_cm_presentation_treenode";

  protected RenderCommand renderCmd;

  @Override
  public void writeNodeContent(ICellWriter writer, DocumentReference docRef,
      INavigation navigation) {
    writeNodeContent(writer.getAsStringBuilder(), false, false, docRef, true, 0, navigation);
  }

  @Override
  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem, INavigation nav) {
    LOGGER.debug("writeNodeContent for [{}].", docRef);
    outStream.append("<div ");
    outStream.append(nav.addCssClasses(docRef, true, isFirstItem, isLastItem, isLeaf, numItem)
        + " ");
    outStream.append(nav.addUniqueElementId(docRef) + ">\n");
    addRenderedContent(outStream, docRef);
    outStream.append("</div>\n");
  }

  protected void addRenderedContent(@NotNull StringBuilder outStream,
      @NotNull DocumentReference docRef) {
    try {
      outStream.append(renderInnerContent(docRef));
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get document for [" + docRef + "].", exp);
    }
  }

  @Override
  public String renderInnerContent(@NotNull DocumentReference docRef) throws XWikiException {
    return getRenderCommand().renderCelementsDocument(docRef, "view");
  }

  RenderCommand getRenderCommand() {
    if (renderCmd == null) {
      renderCmd = new RenderCommand();
    }
    return renderCmd;
  }

  @Override
  public String getDefaultCssClass() {
    return CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS;
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
