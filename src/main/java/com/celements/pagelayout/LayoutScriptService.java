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

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.internal.reference.DefaultStringEntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.api.PageLayoutApi;
import com.celements.web.plugin.cmd.PageLayoutCommand;
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
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public String renderPageLayout() {
    return getPageLayoutCmd().renderPageLayout();
  }

  public String renderPageLayout(SpaceReference spaceRef) {
    return getPageLayoutCmd().renderPageLayout(spaceRef);
  }

  public Map<String, String> getActivePageLayouts() {
    return getPageLayoutCmd().getActivePageLyouts();
  }

  public Map<String, String> getAllPageLayouts() {
    return getPageLayoutCmd().getAllPageLayouts();
  }

  public String createNewLayout(String layoutSpaceName) {
    return getPageLayoutCmd().createNew(getWebUtilsService().resolveSpaceReference(
        layoutSpaceName));
  }

  public boolean deleteLayout(String layoutSpaceName) {
    SpaceReference layoutSpaceRef = getWebUtilsService().resolveSpaceReference(layoutSpaceName);
    String layoutPropDocName = getEntitySerializer().serialize(
        getPageLayoutCmd().standardPropDocRef(layoutSpaceRef));
    try {
      if (getContext().getWiki().getRightService().hasAccessLevel("delete", getContext().getUser(),
          layoutPropDocName, getContext())) {
        return getPageLayoutCmd().deleteLayout(layoutSpaceRef);
      } else {
        LOGGER.warn("NO delete rights on [" + layoutPropDocName + "] for user ["
            + getContext().getUser() + "].");
      }
    } catch (XWikiException exp) {
      LOGGER.error("Failed to check delete rights on [" + layoutSpaceName + "] for user ["
          + getContext().getUser() + "].");
    }
    return false;
  }

  public PageLayoutApi getPageLayoutApiForRef(SpaceReference layoutSpaceRef) {
    return getPageLayoutCmd().resolveValidLayoutSpace(layoutSpaceRef)
        .map(PageLayoutApi::new)
        .orElse(null);
  }

  /**
   * getPageLayouApiForDocRef computes the layoutSpaceRef rendered for the given docRef
   *
   * @return PageLayoutApi for the layoutSpaceRef computed
   */
  public PageLayoutApi getPageLayoutApiForDocRef(DocumentReference docRef) {
    SpaceReference pageLayoutForDoc = getPageLayoutCmd().getPageLayoutForDoc(docRef);
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
    SpaceReference pageLayoutForDoc = getPageLayoutCmd().getPageLayoutForDoc(docRef);
    if (pageLayoutForDoc != null) {
      return pageLayoutForDoc.getName();
    }
    return "";
  }

  public boolean layoutExists(SpaceReference layoutSpaceRef) {
    return getPageLayoutCmd().layoutExists(layoutSpaceRef);
  }

  public boolean canRenderLayout(SpaceReference spaceRef) {
    return getPageLayoutCmd().canRenderLayout(spaceRef);
  }

  public boolean useXWikiLoginLayout() {
    return "1".equals(getContext().getWiki().getSpacePreference("xwikiLoginLayout",
        "celements.xwikiLoginLayout", "1", getContext()));
  }

  public boolean layoutEditorAvailable() {
    return getPageLayoutCmd().layoutEditorAvailable();
  }

  public String renderCelementsDocumentWithLayout(DocumentReference docRef,
      SpaceReference layoutSpaceRef) {
    XWikiDocument oldContextDoc = getContext().getDoc();
    LOGGER.debug("renderCelementsDocumentWithLayout for docRef [" + docRef
        + "] and layoutSpaceRef [" + layoutSpaceRef + "] overwrite oldContextDoc ["
        + oldContextDoc.getDocumentReference() + "].");
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    try {
      XWikiDocument newContextDoc = getContext().getWiki().getDocument(docRef, getContext());
      getContext().setDoc(newContextDoc);
      vcontext.put("doc", newContextDoc.newDocument(getContext()));
      return getPageLayoutCmd().renderPageLayout(layoutSpaceRef);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get docRef document to renderCelementsDocumentWithLayout.", exp);
    } finally {
      getContext().setDoc(oldContextDoc);
      vcontext.put("doc", oldContextDoc.newDocument(getContext()));
    }
    return "";
  }

  public SpaceReference getCurrentRenderingLayout() {
    return getPageLayoutCmd().getCurrentRenderingLayout();
  }

  /**
   * TODO: Probably use TreeNodeService directly
   */
  private PageLayoutCommand getPageLayoutCmd() {
    if (!getContext().containsKey(CELEMENTS_PAGE_LAYOUT_COMMAND)) {
      getContext().put(CELEMENTS_PAGE_LAYOUT_COMMAND, new PageLayoutCommand());
    }
    return (PageLayoutCommand) getContext().get(CELEMENTS_PAGE_LAYOUT_COMMAND);
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private DefaultStringEntityReferenceSerializer getEntitySerializer() {
    return ((DefaultStringEntityReferenceSerializer) Utils.getComponent(
        EntityReferenceSerializer.class));
  }
}
