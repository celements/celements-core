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
package com.celements.pagelayout;

import java.util.Map;
import java.util.Optional;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.rights.access.DefaultRightsAccessFacade;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.plugin.api.PageLayoutApi;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component("layout")
public class LayoutScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LayoutScriptService.class);

  public static final String CELEMENTS_PAGE_LAYOUT_COMMAND = "com.celements.web.PageLayoutCommand";

  @Requirement
  private LayoutServiceRole layoutService;

  @Requirement
  private ModelContext modelContext;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private DefaultRightsAccessFacade rightsAccess;

  private XWikiContext getContext() {
    return modelContext.getXWikiContext();
  }

  public String renderPageLayout() {
    return layoutService.renderPageLayout();
  }

  public String renderPageLayout(SpaceReference spaceRef) {
    return layoutService.renderPageLayout(spaceRef);
  }

  public Map<SpaceReference, String> getActivePageLayouts() {
    return layoutService.getActivePageLayouts();
  }

  public Map<SpaceReference, String> getAllPageLayouts() {
    return layoutService.getAllPageLayouts();
  }

  /**
   * @deprecated since 5.3 instead use {@link #createNewLayout(SpaceReference)}
   */
  @Deprecated
  public String createNewLayout(String layoutSpaceName) {
    return createNewLayout(modelUtils.resolveRef(layoutSpaceName, SpaceReference.class));
  }

  public String createNewLayout(SpaceReference layoutSpaceRef) {
    return layoutService.createNew(layoutSpaceRef);
  }

  /**
   * @deprecated since 5.3 instead use {@link #deleteLayout(SpaceReference)}
   */
  @Deprecated
  public boolean deleteLayout(String layoutSpaceName) {
    SpaceReference layoutSpaceRef = modelUtils.resolveRef(layoutSpaceName, SpaceReference.class);
    return deleteLayout(layoutSpaceRef);
  }

  public boolean deleteLayout(SpaceReference layoutSpaceRef) {
    Optional<DocumentReference> layoutPropDocRef = layoutService
        .getLayoutPropDocRef(layoutSpaceRef);
    if (layoutPropDocRef.isPresent()
        && rightsAccess.hasAccessLevel(layoutPropDocRef.get(), EAccessLevel.DELETE)) {
      return layoutService.deleteLayout(layoutSpaceRef);
    } else {
      LOGGER.warn("NO delete rights on [{}] for user [{}].", layoutPropDocRef.orElse(null),
          getContext().getUser());
    }
    return false;
  }

  public PageLayoutApi getPageLayoutApiForRef(SpaceReference layoutSpaceRef) {
    return layoutService.resolveValidLayoutSpace(layoutSpaceRef)
        .map(PageLayoutApi::new)
        .orElse(null);
  }

  /**
   * getPageLayouApiForDocRef computes the layoutSpaceRef rendered for the given docRef
   *
   * @return PageLayoutApi for the layoutSpaceRef computed
   */
  public PageLayoutApi getPageLayoutApiForDocRef(DocumentReference docRef) {
    SpaceReference pageLayoutForDoc = layoutService.getPageLayoutForDoc(docRef);
    if (pageLayoutForDoc != null) {
      return new PageLayoutApi(pageLayoutForDoc, getContext());
    }
    return null;
  }

  /**
   * @deprecated since 2.82 instead use {@link #getPageLayoutApiForRef(SpaceReference)}
   *             since 2.86 : or {@link #getPageLayoutApiForDocRef(DocumentReference)}
   */
  @Deprecated
  public PageLayoutApi getPageLayoutApiForName(String layoutSpaceName) {
    return new PageLayoutApi(getWebUtilsService().resolveSpaceReference(layoutSpaceName),
        getContext());
  }

  public String getPageLayoutForDoc(DocumentReference docRef) {
    SpaceReference pageLayoutForDoc = layoutService.getPageLayoutForDoc(docRef);
    if (pageLayoutForDoc != null) {
      return pageLayoutForDoc.getName();
    }
    return "";
  }

  public boolean layoutExists(SpaceReference layoutSpaceRef) {
    return layoutService.layoutExists(layoutSpaceRef);
  }

  public boolean canRenderLayout(SpaceReference spaceRef) {
    return layoutService.canRenderLayout(spaceRef);
  }

  public boolean useXWikiLoginLayout() {
    return "1".equals(getContext().getWiki().getSpacePreference("xwikiLoginLayout",
        "celements.xwikiLoginLayout", "1", getContext()));
  }

  public boolean layoutEditorAvailable() {
    return layoutService.isLayoutEditorAvailable();
  }

  public String renderCelementsDocumentWithLayout(DocumentReference docRef,
      SpaceReference layoutSpaceRef) {
    XWikiDocument oldContextDoc = getContext().getDoc();
    LOGGER.debug(
        "renderCelementsDocumentWithLayout for docRef [{}] and layoutSpaceRef [{}] overwrite "
            + "oldContextDoc [{}].",
        docRef, layoutSpaceRef, oldContextDoc.getDocumentReference());
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    try {
      XWikiDocument newContextDoc = getContext().getWiki().getDocument(docRef, getContext());
      getContext().setDoc(newContextDoc);
      vcontext.put("doc", newContextDoc.newDocument(getContext()));
      return layoutService.renderPageLayout(layoutSpaceRef);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get docRef document to renderCelementsDocumentWithLayout.", exp);
    } finally {
      getContext().setDoc(oldContextDoc);
      vcontext.put("doc", oldContextDoc.newDocument(getContext()));
    }
    return "";
  }

  public SpaceReference getCurrentRenderingLayout() {
    return layoutService.getCurrentRenderingLayout();
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }
}
