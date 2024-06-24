package com.celements.navigation.presentation;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.reference.RefBuilder;
import com.celements.navigation.INavigation;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.rendering.RenderCommand;
import com.celements.web.classcollections.DocumentDetailsClasses;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("renderedExtract")
public class RenderedExtractPresentationType implements IPresentationTypeRole<INavigation> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RenderedExtractPresentationType.class);

  private static final String _CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS = "cel_cm_presentation_treenode";

  RenderCommand renderCmd;

  @Requirement
  protected ModelContext context;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Requirement("celements.documentDetails")
  IClassCollectionRole docDetailsClasses;

  private DocumentDetailsClasses getDocDetailsClasses() {
    return (DocumentDetailsClasses) docDetailsClasses;
  }

  @Override
  public void writeNodeContent(ICellWriter writer, DocumentReference docRef,
      INavigation navigation) {
    writeNodeContent(writer.getAsStringBuilder(), false, false, docRef, true, 0, navigation);
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
    writer.appendContent(getRenderedExtract(docRef));
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
    outStream.append(getRenderedExtract(docRef));
    outStream.append("</div>\n");
  }

  String getRenderedExtract(DocumentReference docRef) {
    String templatePath = webUtilsService.getInheritedTemplatedPath(getTemplateRef());
    try {
      VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
      vcontext.put("extractDocRef", docRef);
      XWikiDocument contentDoc = modelAccess.getDocument(docRef);
      vcontext.put("extractDoc", contentDoc.newDocument(getContext()));
      vcontext.put("extractContent", getDocExtract(docRef));
      return getRenderCommand().renderTemplatePath(templatePath, getContext().getLanguage(), "");
    } catch (XWikiException exp) {
      LOGGER.error("Failed to render template path [{}] for [{}].",
          templatePath, docRef, exp);
    } catch (DocumentNotExistsException exp) {
      LOGGER.error("Failed to get document for [{}].", docRef, exp);
    }
    return "";
  }

  private DocumentReference getTemplateRef() {
    return RefBuilder.from(context.getWikiRef()).space(
        IPageTypeConfig.TEMPLATE_SPACE_NAME).doc("RenderedExtract").build(DocumentReference.class);
  }

  private String getDocExtract(DocumentReference docRef) throws DocumentNotExistsException {
    XWikiDocument contentDoc = modelAccess.getDocument(docRef);
    DocumentReference documentExtractClassRef = getDocDetailsClasses().getDocumentExtractClassRef(
        docRef.getLastSpaceReference().getParent().getName());
    BaseObject extractObj = contentDoc.getXObject(documentExtractClassRef,
        DocumentDetailsClasses.FIELD_DOC_EXTRACT_LANGUAGE, getContext().getLanguage(), false);
    if (extractObj == null) {
      extractObj = contentDoc.getXObject(documentExtractClassRef,
          DocumentDetailsClasses.FIELD_DOC_EXTRACT_LANGUAGE, context.getDefaultLanguage(
              docRef.getLastSpaceReference()),
          false);
    }
    if (extractObj != null) {
      return extractObj.getStringValue(DocumentDetailsClasses.FIELD_DOC_EXTRACT_CONTENT);
    } else {
      return "";
    }
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
