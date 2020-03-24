/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.plugin.cmd;

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.cells.CellRenderStrategy;
import com.celements.cells.DivWriter;
import com.celements.cells.HtmlDoctype;
import com.celements.cells.ICellsClassConfig;
import com.celements.cells.IRenderStrategy;
import com.celements.cells.RenderingEngine;
import com.celements.inheritor.InheritorFactory;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.context.ModelContext;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.model.util.References;
import com.celements.web.CelConstant;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.packaging.DocumentInfo;
import com.xpn.xwiki.plugin.packaging.PackageAPI;
import com.xpn.xwiki.web.Utils;

public class PageLayoutCommand {

  public static final String SIMPLE_LAYOUT = "SimpleLayout";

  public static final String XWIKICFG_CELEMENTS_LAYOUT_DEFAULT = "celements.layout.default";

  private static final Logger LOGGER = LoggerFactory.getLogger(PageLayoutCommand.class);

  QueryManager queryManager;

  public static final String PAGE_LAYOUT_PROPERTIES_CLASS_SPACE = "Celements";
  public static final String PAGE_LAYOUT_PROPERTIES_CLASS_DOC = "PageLayoutPropertiesClass";
  public static final String PAGE_LAYOUT_PROPERTIES_CLASS = PAGE_LAYOUT_PROPERTIES_CLASS_SPACE + "."
      + PAGE_LAYOUT_PROPERTIES_CLASS_DOC;

  public static final String CEL_LAYOUT_EDITOR_PL_NAME = "CelLayoutEditor";

  private static final String CEL_RENDERING_LAYOUT_CONTEXT_PROPERTY = "celRenderingLayout";

  private static final String CEL_RENDERING_LAYOUT_STACK_PROPERTY = "celRenderingLayoutStack";

  /**
   * The name of the internal packaging plugin.
   */
  private static final String PACKAGEPLUGIN_NAME = "package";

  private InheritorFactory _injectedInheritorFactory;

  private QueryManager getQueryManager() {
    if (queryManager == null) {
      queryManager = Utils.getComponent(QueryManager.class);
    }
    return queryManager;
  }

  public Map<String, String> getAllPageLayouts() {
    return getPageLayoutMap(false);
  }

  private Map<String, String> getPageLayoutMap(boolean onlyActive) {
    Map<String, String> plMap = new HashMap<>();
    try {
      for (Object resultRowObj : getContext().getWiki().search(getPageLayoutHQL(onlyActive),
          getContext())) {
        Object[] resultRow = (Object[]) resultRowObj;
        plMap.put(resultRow[0].toString(), resultRow[1].toString());
      }
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get all page layouts", exp);
    }
    return plMap;
  }

  String getPageLayoutHQL(boolean onlyActive) {
    String hql = "select doc.space, pl.prettyname" + " from XWikiDocument as doc, BaseObject obj,"
        + " Celements.PageLayoutPropertiesClass as pl" + " where doc.fullName = obj.name"
        + " and obj.className='Celements.PageLayoutPropertiesClass'" + " and pl.id.id=obj.id";
    if (onlyActive) {
      hql += " and pl.isActive = 1";
    }
    hql += " order by pl.prettyname asc";
    return hql;
  }

  public Map<String, String> getActivePageLyouts() {
    return getPageLayoutMap(true);
  }

  public String createNew(SpaceReference layoutSpaceRef) {
    if (layoutSpaceRef != null) {
      if (!layoutExists(layoutSpaceRef)) {
        try {
          XWikiDocument propXdoc = getModelAccess().getOrCreateDocument(standardPropDocRef(
              layoutSpaceRef));
          BaseObject layoutPropObj = getModelAccess().newXObject(propXdoc,
              getPageLayoutPropertiesClassRef(propXdoc.getDocumentReference().getWikiReference()));
          layoutPropObj.setStringValue("prettyname", layoutSpaceRef.getName() + " Layout");
          layoutPropObj.setStringValue(ICellsClassConfig.LAYOUT_DOCTYPE_FIELD, getDocType());
          getModelAccess().saveDocument(propXdoc, "Creating page layout", false);
          return "cel_layout_create_successful";
        } catch (DocumentSaveException exp) {
          LOGGER.error("createNew: failed to create new page layout.", exp);
        }
      }
    }
    return "cel_layout_empty_name_msg";
  }

  private String getDocType() {
    return Utils.getComponent(ConfigurationSource.class).getProperty("celements.layout.docType",
        HtmlDoctype.HTML5.getValue());
  }

  public boolean deleteLayout(SpaceReference layoutSpaceRef) {
    final boolean moveToTrash = true;
    try {
      Query spaceDocsQuery = getQueryManager().createQuery("where doc.space = :space", Query.XWQL);
      spaceDocsQuery.bindValue("space", layoutSpaceRef.getName());
      for (String docName : spaceDocsQuery.<String>execute()) {
        DocumentReference docReference = getModelUtils().resolveRef(docName,
            DocumentReference.class);
        getModelAccess().deleteDocument(docReference, moveToTrash);
      }
      return moveToTrash;
    } catch (QueryException exp) {
      LOGGER.warn("Failed to get the list of documents while trying to delete space [{}]",
          layoutSpaceRef, exp);
    } catch (DocumentDeleteException exp) {
      LOGGER.error("deleteLayout: Failed to delete documents for space [{}].", layoutSpaceRef, exp);
    }
    return false;
  }

  DocumentReference getPageLayoutPropertiesClassRef(WikiReference wikiRef) {
    return RefBuilder.from(wikiRef)
        .space(PAGE_LAYOUT_PROPERTIES_CLASS_SPACE)
        .doc(PAGE_LAYOUT_PROPERTIES_CLASS_DOC)
        .build(DocumentReference.class);
  }

  /**
   * checks if the layout exists locally (in terms of layoutSpaceRef)
   *
   * @param layoutSpaceRef
   * @return
   */
  public boolean layoutExists(SpaceReference layoutSpaceRef) {
    return (getLayoutPropDoc(layoutSpaceRef) != null);
  }

  public boolean canRenderLayout(SpaceReference layoutSpaceRef) {
    return resolveValidLayoutSpace(layoutSpaceRef).isPresent();
  }

  public BaseObject getLayoutPropertyObj(SpaceReference layoutSpaceRef) {
    layoutSpaceRef = resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    XWikiDocument layoutPropDoc = getLayoutPropDoc(layoutSpaceRef);
    if (layoutPropDoc != null) {
      return layoutPropDoc.getXObject(getPageLayoutPropertiesClassRef(
          layoutPropDoc.getDocumentReference().getWikiReference()));
    } else {
      return null;
    }
  }

  public XWikiDocument getLayoutPropDoc() {
    SpaceReference currDocPageLayout = getPageLayoutForCurrentDoc();
    if (currDocPageLayout != null) {
      LOGGER.debug("getLayoutPropDoc: found page layout [" + currDocPageLayout + "] for page ["
          + getContext().getDoc().getDocumentReference() + "].");
      return getLayoutPropDoc(currDocPageLayout);
    } else {
      LOGGER.debug("getLayoutPropDoc: found NO page layout for page ["
          + getContext().getDoc().getDocumentReference() + "].");
    }
    return null;
  }

  /**
   * getLayoutPropDoc
   *
   * @param layoutSpaceRef
   *          may not be null (NPE if null)
   * @return property doc for layoutSpaceRef
   */
  public XWikiDocument getLayoutPropDoc(SpaceReference layoutSpaceRef) {
    XWikiDocument layoutPropDoc = null;
    DocumentReference layoutPropDocRef = standardPropDocRef(layoutSpaceRef);
    if (getModelAccess().exists(layoutPropDocRef)) {
      try {
        XWikiDocument theDoc = getModelAccess().getDocument(layoutPropDocRef);
        if (theDoc.getXObject(getPageLayoutPropertiesClassRef(
            theDoc.getDocumentReference().getWikiReference())) != null) {
          layoutPropDoc = theDoc;
        }
      } catch (DocumentNotExistsException exp) {
        LOGGER.info("getLayoutPropDoc: Failed to get layout property doc for [" + layoutSpaceRef
            + "].");
      }
    }
    return layoutPropDoc;
  }

  public DocumentReference standardPropDocRef(SpaceReference layoutSpaceRef) {
    layoutSpaceRef = Optional.ofNullable(layoutSpaceRef)
        .orElseGet(() -> getModelContext().getCurrentSpaceRefOrDefault());
    return RefBuilder.from(layoutSpaceRef).doc("WebHome").build(DocumentReference.class);
  }

  public String renderPageLayout() {
    return renderPageLayoutLocal(getPageLayoutForCurrentDoc());
  }

  public String renderPageLayout(SpaceReference layoutSpaceRef) {
    LOGGER.info("renderPageLayout: for layoutRef '{}'", layoutSpaceRef);
    layoutSpaceRef = resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    LOGGER.debug("renderPageLayout: after decideLocalOrCentral layoutRef '{}'", layoutSpaceRef);
    return renderPageLayoutLocal(layoutSpaceRef);
  }

  /**
   * renderPageLayout(SpaceReference) does NOT check any access rights. Or if the given
   * layout exists. This MUST be done before calling renderPageLayout(SpaceReference).
   *
   * @param layoutSpaceRef
   * @return
   */
  public String renderPageLayoutLocal(SpaceReference layoutSpaceRef) {
    long millisec = System.currentTimeMillis();
    LOGGER.debug("renderPageLayout for layout [" + layoutSpaceRef + "].");
    IRenderStrategy cellRenderer = new CellRenderStrategy(getContext()).setOutputWriter(
        new DivWriter());
    getRenderingLayoutStack().push(layoutSpaceRef);
    setRenderLayoutInVelocityContext(getCurrentRenderingLayout());
    RenderingEngine renderEngine = new RenderingEngine().setRenderStrategy(cellRenderer);
    renderEngine.renderPageLayout(layoutSpaceRef);
    getRenderingLayoutStack().pop();
    setRenderLayoutInVelocityContext(getCurrentRenderingLayout());
    LOGGER.info("renderPageLayout finishing. Time used in millisec: " + (System.currentTimeMillis()
        - millisec));
    return cellRenderer.getAsString();
  }

  public SpaceReference getCurrentRenderingLayout() {
    if (!getRenderingLayoutStack().isEmpty()) {
      return getRenderingLayoutStack().peek();
    }
    return null;
  }

  private void setRenderLayoutInVelocityContext(SpaceReference layoutSpaceRef) {
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    if (layoutSpaceRef != null) {
      vcontext.put(CEL_RENDERING_LAYOUT_CONTEXT_PROPERTY,
          References.cloneRef(layoutSpaceRef, SpaceReference.class));
    } else {
      vcontext.remove(CEL_RENDERING_LAYOUT_CONTEXT_PROPERTY);
    }
  }

  @SuppressWarnings("unchecked")
  private Deque<SpaceReference> getRenderingLayoutStack() {
    ExecutionContext execContext = getExecution().getContext();
    if (execContext.getProperty(CEL_RENDERING_LAYOUT_STACK_PROPERTY) == null) {
      Deque<SpaceReference> layoutStack = new LinkedList<>();
      execContext.setProperty(CEL_RENDERING_LAYOUT_STACK_PROPERTY, layoutStack);
    }
    return (Deque<SpaceReference>) execContext.getProperty(CEL_RENDERING_LAYOUT_STACK_PROPERTY);
  }

  /**
   * getPageLayoutForCurrentDoc checks that the layout returned exists and that it may be
   * used by the current context database.
   *
   * @return
   */
  public SpaceReference getPageLayoutForCurrentDoc() {
    return getPageLayoutForDoc(getContext().getDoc().getDocumentReference());
  }

  /**
   * @deprecated since 2.14.0 instead use getPageLayoutForDoc(DocumentReference)
   */
  @Deprecated
  public String getPageLayoutForDoc(String fullName, XWikiContext context) {
    DocumentReference docRef = getModelUtils().resolveRef(fullName, DocumentReference.class);
    SpaceReference spaceRef = getPageLayoutForDoc(docRef);
    if (spaceRef != null) {
      String layoutWikiName = spaceRef.getParent().getName();
      if (context.getDatabase().equals(layoutWikiName)) {
        return spaceRef.getName();
      } else {
        return layoutWikiName + ":" + spaceRef.getName();
      }
    }
    return null;
  }

  public SpaceReference getPageLayoutForDoc(DocumentReference documentReference) {
    long millisec = System.currentTimeMillis();
    LOGGER.debug("getPageLayoutForDoc: for [" + documentReference + "].");
    if (documentReference == null) {
      return null;
    }
    SpaceReference layoutSpaceRef = null;
    if (layoutExists(documentReference.getLastSpaceReference())) {
      layoutSpaceRef = getCelLayoutEditorSpaceRef();
    } else {
      String spaceName = getInheritorFactory().getPageLayoutInheritor(getFullNameForDocRef(
          documentReference), getContext()).getStringValue("page_layout", null);
      if (spaceName != null) {
        layoutSpaceRef = getModelUtils().resolveRef(spaceName, SpaceReference.class);
      }
    }
    layoutSpaceRef = resolveValidLayoutSpace(layoutSpaceRef)
        .orElseGet(this::getDefaultLayoutSpaceReference);
    LOGGER.info("getPageLayoutForDoc: for [" + documentReference + "] returning [" + layoutSpaceRef
        + "].  Time used in millisec: " + (System.currentTimeMillis() - millisec));
    return layoutSpaceRef;
  }

  /**
   * prohibit layout access in different db except central celements2web (or default
   * layout configured on disk). TODO add allowedDBs to layout properties
   *
   * @param layoutSpaceRef
   * @return
   */
  public boolean checkLayoutAccess(@NotNull SpaceReference layoutSpaceRef) {
    WikiReference layoutWikiRef = References.extractRef(checkNotNull(layoutSpaceRef),
        WikiReference.class).get();
    return getModelContext().getWikiRef().equals(layoutWikiRef)
        || getCentralWikiRef().equals(layoutWikiRef);
  }

  public SpaceReference getDefaultLayoutSpaceReference() {
    SpaceReference defaultLayoutSpaceRef = RefBuilder
        .from(getModelContext().getWikiRef())
        .space(getDefaultLayout())
        .build(SpaceReference.class);
    return resolveValidLayoutSpace(defaultLayoutSpaceRef).orElse(null);
  }

  String getDefaultLayout() {
    String defaultLayout = getContext().getWiki().Param(XWIKICFG_CELEMENTS_LAYOUT_DEFAULT,
        SIMPLE_LAYOUT);
    if ((getContext() != null) && (getContext().getAction() != null) && !"view".equals(
        getContext().getAction())) {
      defaultLayout = getContext().getWiki().Param(XWIKICFG_CELEMENTS_LAYOUT_DEFAULT + "."
          + getContext().getAction(), defaultLayout);
      LOGGER.debug("getDefaultLayout for action [" + getContext().getAction() + "] got ["
          + defaultLayout + "].");
    } else {
      LOGGER.debug("getDefaultLayout got [" + defaultLayout + "].");
    }
    return defaultLayout;
  }

  @NotNull
  public Optional<SpaceReference> resolveValidLayoutSpace(@Nullable SpaceReference layoutSpaceRef) {
    if ((layoutSpaceRef != null) && !layoutExists(layoutSpaceRef)) {
      SpaceReference centralLayoutSpaceRef = RefBuilder.from(layoutSpaceRef)
          .with(getCentralWikiRef())
          .build(SpaceReference.class);
      if (!layoutSpaceRef.equals(centralLayoutSpaceRef) && layoutExists(centralLayoutSpaceRef)) {
        layoutSpaceRef = centralLayoutSpaceRef;
      } else {
        layoutSpaceRef = null;
      }
    }
    if ((layoutSpaceRef != null) && !checkLayoutAccess(layoutSpaceRef)) {
      layoutSpaceRef = null;
    }
    return Optional.ofNullable(layoutSpaceRef);
  }

  SpaceReference getCelLayoutEditorSpaceRef() {
    return RefBuilder.from(getModelContext().getWikiRef())
        .space(CEL_LAYOUT_EDITOR_PL_NAME)
        .build(SpaceReference.class);
  }

  private String getFullNameForDocRef(DocumentReference documentReference) {
    return documentReference.getLastSpaceReference().getName() + "." + documentReference.getName();
  }

  private InheritorFactory getInheritorFactory() {
    if (_injectedInheritorFactory != null) {
      return _injectedInheritorFactory;
    }
    return new InheritorFactory();
  }

  /**
   * For TESTS ONLY!!!
   *
   * @param injectedInheritorFactory
   */
  void inject_TEST_InheritorFactory(InheritorFactory injectedInheritorFactory) {
    _injectedInheritorFactory = injectedInheritorFactory;
  }

  public boolean isActive(SpaceReference layoutSpaceRef) {
    BaseObject layoutPropertyObj = getLayoutPropertyObj(layoutSpaceRef);
    if (layoutPropertyObj != null) {
      return layoutPropertyObj.getIntValue("isActive", 0) > 0;
    }
    return false;
  }

  public String getPrettyName(SpaceReference layoutSpaceRef) {
    BaseObject layoutPropertyObj = getLayoutPropertyObj(layoutSpaceRef);
    if ((layoutPropertyObj != null) && (layoutPropertyObj.getStringValue("prettyname") != null)) {
      return layoutPropertyObj.getStringValue("prettyname");
    }
    return "";
  }

  public String getLayoutType(SpaceReference layoutSpaceRef) {
    BaseObject layoutPropertyObj = getLayoutPropertyObj(layoutSpaceRef);
    if (layoutPropertyObj != null) {
      return layoutPropertyObj.getStringValue(ICellsClassConfig.LAYOUT_TYPE_FIELD);
    }
    return ICellsClassConfig.PAGE_LAYOUT_VALUE;
  }

  @NotNull
  public HtmlDoctype getHTMLType(@NotNull SpaceReference layoutSpaceRef) {
    BaseObject layoutPropertyObj = getLayoutPropertyObj(layoutSpaceRef);
    Optional<HtmlDoctype> stringValue = Optional.empty();
    if (layoutPropertyObj != null) {
      stringValue = getHtmlDoctype(layoutPropertyObj, ICellsClassConfig.LAYOUT_DOCTYPE_FIELD);
    }
    return stringValue.orElse(HtmlDoctype.XHTML);
  }

  private Optional<HtmlDoctype> getHtmlDoctype(BaseObject layoutPropertyObj, String fieldName) {
    return HtmlDoctype.getHtmlDoctype(Strings.emptyToNull(layoutPropertyObj.getStringValue(
        fieldName))).toJavaUtil();
  }

  public String getVersion(SpaceReference layoutSpaceRef) {
    BaseObject layoutPropertyObj = getLayoutPropertyObj(layoutSpaceRef);
    if ((layoutPropertyObj != null) && (layoutPropertyObj.getStringValue("version") != null)) {
      return layoutPropertyObj.getStringValue("version");
    }
    return "";
  }

  public boolean layoutEditorAvailable() {
    SpaceReference localLayoutEditorSpaceRef = getCelLayoutEditorSpaceRef();
    return resolveValidLayoutSpace(localLayoutEditorSpaceRef).isPresent();
  }

  /**
   * Export an page layout space into XAR using Packaging plugin.
   *
   * @param layoutSpaceRef
   *          the layout space reference of the application to export.
   * @param withDocHistory
   *          indicate if history of documents is exported.
   * @param context
   *          the XWiki context.
   * @throws XWikiException
   *           error when :
   *           <ul>
   *           <li>or getting page-layouts documents to export.</li>
   *           <li>or when apply export.</li>
   *           </ul>
   * @throws IOException
   *           error when apply export.
   */
  public void exportLayoutXAR(SpaceReference layoutSpaceRef, boolean withDocHistory)
      throws XWikiException, IOException {
    PackageAPI export = ((PackageAPI) getContext().getWiki().getPluginApi(PACKAGEPLUGIN_NAME,
        getContext()));
    export.setName(getPrettyName(layoutSpaceRef) + "-" + getVersion(layoutSpaceRef));
    for (String documentName : getContext().getWiki().getSpaceDocsName(layoutSpaceRef.getName(),
        getContext())) {
      export.add(extendToFullName(layoutSpaceRef.getName(), documentName),
          DocumentInfo.ACTION_OVERWRITE);
    }
    export.setWithVersions(withDocHistory);
    export.export();
  }

  String extendToFullName(String layoutSpaceName, String documentName) {
    if (!documentName.contains(".")) {
      return layoutSpaceName + "." + documentName;
    } else {
      return documentName;
    }
  }

  final WikiReference getCentralWikiRef() {
    return RefBuilder.create().wiki(CelConstant.CENTRAL_WIKI_NAME).build(WikiReference.class);
  }

  private static final IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  private static final ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

  private static final ModelContext getModelContext() {
    return Utils.getComponent(ModelContext.class);
  }

  private static final XWikiContext getContext() {
    return getModelContext().getXWikiContext();
  }

  private static final Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

}
