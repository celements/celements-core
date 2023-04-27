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
package com.celements.web.plugin.api;

import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.HtmlDoctype;
import com.celements.cells.classes.PageLayoutPropertiesClass;
import com.celements.model.context.ModelContext;
import com.celements.pagelayout.LayoutServiceRole;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.web.Utils;

public class PageLayoutApi extends Api {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageLayoutApi.class);

  private SpaceReference spaceRef;

  public PageLayoutApi(SpaceReference layoutSpaceRef) {
    super(getContext().getXWikiContext());
    this.spaceRef = layoutSpaceRef;
  }

  public SpaceReference getSpaceRef() {
    return spaceRef;
  }

  public SpaceReference getLayoutSpaceRef() {
    return getSpaceRef();
  }

  public DocumentReference getLayoutConfigDocRef() {
    return getLayoutSrv().getLayoutPropDocRef(spaceRef).orElse(null);
  }

  public boolean isActive() {
    return getLayoutSrv().isActive(spaceRef);
  }

  public String getPrettyName() {
    return getLayoutSrv().getPrettyName(spaceRef).orElse("Untitled Layout");
  }

  public String getVersion() {
    return getLayoutSrv().getVersion(spaceRef);
  }

  public String getLayoutType() {
    return getLayoutSrv().getLayoutType(spaceRef);
  }

  /**
   * @return 'HTML 5' or 'XHTML 1.1' see ICellsClassConfig for DOCTYPE_HTML_5_VALUE and
   *         DOCTYPE_HTML_5_VALUE
   */
  @NotNull
  public HtmlDoctype getHTMLType() {
    return getLayoutSrv().getHTMLType(spaceRef);
  }

  public boolean isPageLayoutType() {
    return PageLayoutPropertiesClass.PAGE_LAYOUT_VALUE.equals(getLayoutType());
  }

  public boolean isEditorLayoutType() {
    return PageLayoutPropertiesClass.EDITOR_LAYOUT_VALUE.equals(getLayoutType());
  }

  /**
   * @param withDocHistory
   * @return successful
   */
  public boolean exportLayoutXAR(boolean withDocHistory) {
    try {
      return getLayoutSrv().exportLayoutXAR(spaceRef, withDocHistory);
    } catch (XWikiException | IOException exp) {
      LOGGER.error("Failed to export page layout [{}]", spaceRef, exp);
    }
    return false;
  }

  @Override
  public String toString() {
    return getSpaceRef().toString();
  }

  private static final ModelContext getContext() {
    return Utils.getComponent(ModelContext.class);
  }

  private static final LayoutServiceRole getLayoutSrv() {
    return Utils.getComponent(LayoutServiceRole.class);
  }

}
