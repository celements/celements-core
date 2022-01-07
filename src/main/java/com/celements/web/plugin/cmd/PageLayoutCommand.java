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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.HtmlDoctype;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.pagelayout.LayoutServiceRole;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

/**
 * @deprecated since 5.4 instead use {@link LayoutServiceRole}
 */
@Deprecated
public class PageLayoutCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageLayoutCommand.class);

  @Deprecated
  public static final String SIMPLE_LAYOUT = "SimpleLayout";

  @Deprecated
  public static final String XWIKICFG_CELEMENTS_LAYOUT_DEFAULT = "celements.layout.default";

  @Deprecated
  public static final String PAGE_LAYOUT_PROPERTIES_CLASS_SPACE = "Celements";
  @Deprecated
  public static final String PAGE_LAYOUT_PROPERTIES_CLASS_DOC = "PageLayoutPropertiesClass";
  @Deprecated
  public static final String PAGE_LAYOUT_PROPERTIES_CLASS = PAGE_LAYOUT_PROPERTIES_CLASS_SPACE + "."
      + PAGE_LAYOUT_PROPERTIES_CLASS_DOC;

  @Deprecated
  public static final String CEL_LAYOUT_EDITOR_PL_NAME = "CelLayoutEditor";

  private LayoutServiceRole layoutService = Utils.getComponent(LayoutServiceRole.class);

  private Map<String, String> convertMap(Map<SpaceReference, String> pageLayoutMap) {
    Builder<String, String> resultMap = ImmutableMap.<String, String>builder();
    for (Map.Entry<SpaceReference, String> entry : pageLayoutMap.entrySet()) {
      resultMap.put(entry.getKey().getName(), entry.getValue());
    }
    return resultMap.build();
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#getAllPageLayouts()}
   */
  @Deprecated
  public Map<String, String> getAllPageLayouts() {
    Map<SpaceReference, String> allPageLayouts = layoutService.getAllPageLayouts();
    return convertMap(allPageLayouts);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#getActivePageLyouts()}
   */
  @Deprecated
  public Map<String, String> getActivePageLyouts() {
    return convertMap(layoutService.getActivePageLayouts());
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#createNew(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public String createNew(SpaceReference layoutSpaceRef) {
    return layoutService.createNew(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#deleteLayout(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public boolean deleteLayout(SpaceReference layoutSpaceRef) {
    return layoutService.deleteLayout(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#layoutExists(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public boolean layoutExists(SpaceReference layoutSpaceRef) {
    return layoutService.layoutExists(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#canRenderLayout(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public boolean canRenderLayout(SpaceReference layoutSpaceRef) {
    return layoutService.canRenderLayout(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#getLayoutPropertyObj(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public BaseObject getLayoutPropertyObj(SpaceReference layoutSpaceRef) {
    return layoutService.getLayoutPropertyObj(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use {@link LayoutServiceRole#getLayoutPropDocRefForCurrentDoc()}
   *             and fetch the document yourself using {@link IModelAccessFacade}.
   */
  @Deprecated
  public XWikiDocument getLayoutPropDoc() {
    @NotNull
    Optional<DocumentReference> layoutPropDocRef = layoutService.getLayoutPropDocRefForCurrentDoc();
    if (layoutPropDocRef.isPresent()) {
      try {
        return getModelAccess().getDocument(layoutPropDocRef.get());
      } catch (DocumentNotExistsException exp) {
        LOGGER.debug("getLayoutPropDoc cannot load Document [{}]", layoutPropDocRef.get());
      }
    }
    return null;
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#getLayoutPropDocRef(SpaceReference layoutSpaceRef)}
   *             and fetch the document yourself using {@link IModelAccessFacade}.
   */
  @Deprecated
  public XWikiDocument getLayoutPropDoc(SpaceReference layoutSpaceRef) {
    Optional<DocumentReference> layoutPropDocRef = layoutService
        .getLayoutPropDocRef(layoutSpaceRef);
    if (layoutPropDocRef.isPresent()) {
      try {
        XWikiDocument layoutPropDoc = getModelAccess().getDocument(layoutPropDocRef.get());
        if (layoutService.getLayoutPropertyObj(layoutSpaceRef) != null) {
          return layoutPropDoc;
        }
      } catch (DocumentNotExistsException exp) {
        LOGGER.info("getLayoutPropDoc: Failed to get layout property doc for [{}].",
            layoutSpaceRef);
      }
    }
    return null;
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#getLayoutPropDocRef(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public DocumentReference standardPropDocRef(SpaceReference layoutSpaceRef) {
    layoutSpaceRef = Optional.ofNullable(layoutSpaceRef)
        .orElseGet(() -> getModelContext().getCurrentSpaceRefOrDefault());
    return RefBuilder.from(layoutSpaceRef).doc("WebHome").build(DocumentReference.class);
  }

  /**
   * @deprecated since 5.4 instead use {@link LayoutServiceRole#renderPageLayout()}
   */
  @Deprecated
  public String renderPageLayout() {
    return layoutService.renderPageLayout();
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#renderPageLayout(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public String renderPageLayout(SpaceReference layoutSpaceRef) {
    return layoutService.renderPageLayout(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#renderPageLayoutLocal(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public String renderPageLayoutLocal(SpaceReference layoutSpaceRef) {
    return layoutService.renderPageLayoutLocal(layoutSpaceRef);
  }

  public SpaceReference getCurrentRenderingLayout() {
    return layoutService.getCurrentRenderingLayout();
  }

  /**
   * @deprecated since 5.4 instead use {@link LayoutServiceRole#getPageLayoutForCurrentDoc()}
   */
  @Deprecated
  public SpaceReference getPageLayoutForCurrentDoc() {
    return layoutService.getPageLayoutForCurrentDoc();
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

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#resolveValidLayoutSpace(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  @NotNull
  public Optional<SpaceReference> resolveValidLayoutSpace(@Nullable SpaceReference layoutSpaceRef) {
    return layoutService.resolveValidLayoutSpace(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#getPageLayoutForDoc(DocumentReference documentReference)}
   */
  @Deprecated
  public SpaceReference getPageLayoutForDoc(DocumentReference documentReference) {
    return layoutService.getPageLayoutForDoc(documentReference);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#checkLayoutAccess(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public boolean checkLayoutAccess(@NotNull SpaceReference layoutSpaceRef) {
    return layoutService.checkLayoutAccess(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#getDefaultLayoutSpaceReference()}
   */
  @Deprecated
  public SpaceReference getDefaultLayoutSpaceReference() {
    return layoutService.getDefaultLayoutSpaceReference();
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#isActive(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public boolean isActive(SpaceReference layoutSpaceRef) {
    return layoutService.isActive(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#getPrettyName(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public String getPrettyName(SpaceReference layoutSpaceRef) {
    return layoutService.getPrettyName(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#getLayoutType(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public String getLayoutType(SpaceReference layoutSpaceRef) {
    return layoutService.getLayoutType(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#getHTMLType(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  @NotNull
  public HtmlDoctype getHTMLType(@NotNull SpaceReference layoutSpaceRef) {
    return layoutService.getHTMLType(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#getVersion(SpaceReference layoutSpaceRef)}
   */
  @Deprecated
  public String getVersion(SpaceReference layoutSpaceRef) {
    return layoutService.getVersion(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.4 instead use {@link LayoutServiceRole#isLayoutEditorAvailable()}
   */
  @Deprecated
  public boolean layoutEditorAvailable() {
    return layoutService.isLayoutEditorAvailable();
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link LayoutServiceRole#exportLayoutXAR(SpaceReference layoutSpaceRef,
   *             boolean withDocHistory)}
   */
  @Deprecated
  public void exportLayoutXAR(SpaceReference layoutSpaceRef, boolean withDocHistory)
      throws XWikiException, IOException {
    layoutService.exportLayoutXAR(layoutSpaceRef, withDocHistory);
  }

  private final IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  private final ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

  private final ModelContext getModelContext() {
    return Utils.getComponent(ModelContext.class);
  }

}
