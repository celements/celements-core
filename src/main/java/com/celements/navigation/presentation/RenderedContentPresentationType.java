package com.celements.navigation.presentation;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.navigation.INavigation;
import com.celements.rendering.RenderCommand;
import com.xpn.xwiki.XWikiException;

@Component("renderedContent")
public class RenderedContentPresentationType implements IPresentationTypeRole<INavigation> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RenderedContentPresentationType.class);

  private static final String CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS = "cel_cm_presentation_treenode";

  protected RenderCommand renderCmd;

  @Override
  public void writeNodeContent(ICellWriter writer, DocumentReference docRef,
      INavigation navigation) {
    writeNodeContent(writer, false, false, docRef, true, 0, navigation);
  }

  @Override
  public void writeNodeContent(ICellWriter writer, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem, INavigation nav) {
    LOGGER.debug("writeNodeContent for [{}].", docRef);
    AttributeBuilder attributes = new DefaultAttributeBuilder();
    attributes.addId(nav.getUniqueId(docRef));
    attributes.addCssClasses(
        nav.getCssClassList(docRef, isLeaf, isFirstItem, isLastItem, isLeaf, numItem));
    writer.openLevel("div", attributes.build());
    writer.appendContent(addRenderedContent(docRef));
    writer.closeLevel();
  }

  /**
   * @deprecated instead use {@link #writeNodeContent(ICellWriter, boolean, boolean,
   *             DocumentReference, boolean, int, INavigation)}
   */
  @Deprecated(since = "6.7", forRemoval = true)
  @Override
  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem, INavigation nav) {
    LOGGER.debug("writeNodeContent for [{}].", docRef);
    outStream.append("<div ");
    outStream.append(nav.addCssClasses(docRef, true, isFirstItem, isLastItem, isLeaf, numItem)
        + " ");
    outStream.append(nav.addUniqueElementId(docRef) + ">\n");
    outStream.append(addRenderedContent(docRef));
    outStream.append("</div>\n");
  }

  protected String addRenderedContent(@NotNull DocumentReference docRef) {
    try {
      return getRenderCommand().renderCelementsDocument(docRef, "view");
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get document for [{}].", docRef, exp);
    }
    return "";
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
