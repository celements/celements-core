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
package com.celements.cells.div;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.cells.AbstractRenderStrategy;
import com.celements.cells.ICellWriter;
import com.celements.navigation.TreeNode;
import com.celements.rendering.RenderCommand;
import com.xpn.xwiki.XWikiException;

@Immutable
public class CellRenderStrategy extends AbstractRenderStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(CellRenderStrategy.class);

  private final RenderCommand rendererCmd;

  public CellRenderStrategy() {
    this(new DivWriter(), new RenderCommand());
  }

  public CellRenderStrategy(ICellWriter cellWriter, RenderCommand rendererCmd) {
    super(cellWriter);
    this.rendererCmd = checkNotNull(rendererCmd);
  }

  @Override
  public void renderEmptyChildren(@NotNull TreeNode node) {
    String cellContent = "";
    try {
      LOGGER.debug("renderEmptyChildren: parent [{}].", node);
      long millisec = System.currentTimeMillis();
      cellContent = rendererCmd.renderCelementsCell(node.getDocumentReference());
      LOGGER.info("renderEmptyChildren: rendered parent [{}]. Time used in millisec: {}", node,
          (System.currentTimeMillis() - millisec));
    } catch (XWikiException exp) {
      LOGGER.error("failed to get cell [{}] document to render cell content.", node, exp);
    }
    cellWriter.appendContent(cellContent);
  }

}
