package com.celements.pagelayout;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.HtmlDoctype;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface LayoutServiceRole {

  public static final String PAGE_LAYOUT_PROPERTIES_CLASS_SPACE = "Celements";
  public static final String PAGE_LAYOUT_PROPERTIES_CLASS_DOC = "PageLayoutPropertiesClass";
  public static final String PAGE_LAYOUT_PROPERTIES_CLASS = PAGE_LAYOUT_PROPERTIES_CLASS_SPACE + "."
      + PAGE_LAYOUT_PROPERTIES_CLASS_DOC;

  public static final String CEL_LAYOUT_EDITOR_PL_NAME = "CelLayoutEditor";

  boolean createLayout(@NotNull SpaceReference layoutSpaceRef);

  boolean deleteLayout(@NotNull SpaceReference layoutSpaceRef);

  @NotNull
  String renderPageLayout();

  @NotNull
  String renderPageLayout(@Nullable SpaceReference layoutSpaceRef);

  /**
   * getPageLayoutForCurrentDoc checks that the layout returned exists and that it may be
   * used by the current context database.
   *
   * @return
   */
  @Nullable
  SpaceReference getPageLayoutForCurrentDoc();

  /**
   * renderPageLayout(SpaceReference) does NOT check any access rights. Or if the given
   * layout exists. This MUST be done before calling renderPageLayout(SpaceReference).
   *
   * @param layoutSpaceRef
   * @return
   */
  @NotNull
  String renderPageLayoutLocal(@Nullable SpaceReference layoutSpaceRef);

  @Nullable
  SpaceReference getPageLayoutForDoc(@Nullable DocumentReference documentReference);

  @Nullable
  SpaceReference getCurrentRenderingLayout();

  /**
   * checks if the layout exists locally (in terms of layoutSpaceRef)
   *
   * @param layoutSpaceRef
   * @return true if layout exists
   */
  boolean existsLayout(@Nullable SpaceReference layoutSpaceRef);

  @NotNull
  Optional<DocumentReference> getLayoutPropDocRefForCurrentDoc();

  /**
   * getLayoutPropDocRef
   *
   * @param layoutSpaceRef
   * @return optional DocumentReference for Layout property document or Optional.empty for null
   */
  @NotNull
  Optional<DocumentReference> getLayoutPropDocRef(@Nullable SpaceReference layoutSpaceRef);

  boolean isLayoutEditorAvailable();

  /**
   * prohibit layout access in different db except central celements2web (or default
   * layout configured on disk).
   *
   * @param layoutSpaceRef
   * @return boolean for access allowed
   */
  boolean checkLayoutAccess(@NotNull SpaceReference layoutSpaceRef);

  @Nullable
  SpaceReference getDefaultLayoutSpaceReference();

  boolean canRenderLayout(@Nullable SpaceReference layoutSpaceRef);

  @NotNull
  Optional<BaseObject> getLayoutPropertyObj(@Nullable SpaceReference layoutSpaceRef);

  @NotNull
  Map<SpaceReference, String> getActivePageLayouts();

  @NotNull
  Map<SpaceReference, String> getAllPageLayouts();

  @NotNull
  Optional<SpaceReference> resolveValidLayoutSpace(@Nullable SpaceReference layoutSpaceRef);

  boolean isActive(@Nullable SpaceReference layoutSpaceRef);

  @NotNull
  Optional<String> getPrettyName(@Nullable SpaceReference layoutSpaceRef);

  @NotEmpty
  String getLayoutType(@Nullable SpaceReference layoutSpaceRef);

  @NotNull
  HtmlDoctype getHTMLType(@NotNull SpaceReference layoutSpaceRef);

  @NotNull
  String getVersion(@Nullable SpaceReference layoutSpaceRef);

  /**
   * Export an page layout space into XAR using Packaging plugin.
   *
   * @param layoutSpaceRef
   *          the layout space reference of the application to export.
   * @param withDocHistory
   *          indicate if history of documents is exported.
   * @param context
   *          the XWiki context.
   * @return
   * @throws XWikiException
   *           error when :
   *           <ul>
   *           <li>or getting page-layouts documents to export.</li>
   *           <li>or when apply export.</li>
   *           </ul>
   * @throws IOException
   *           error when apply export.
   */
  boolean exportLayoutXAR(@NotNull SpaceReference layoutSpaceRef, boolean withDocHistory)
      throws XWikiException, IOException;

  @NotNull
  String renderCelementsDocumentWithLayout(@NotNull DocumentReference docRef,
      @Nullable SpaceReference layoutSpaceRef);

}
