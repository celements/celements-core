package com.celements.cells.json;

import javax.validation.constraints.NotNull;

import com.celements.cells.AbstractRenderStrategy;
import com.celements.navigation.TreeNode;

public class JsonRenderStrategy extends AbstractRenderStrategy {

  public JsonRenderStrategy() {
    super(new JsonWriter());
  }

  @Override
  public void renderEmptyChildren(@NotNull TreeNode node) {
    cellWriter.appendContent(modelUtils.serializeRef(node.getDocumentReference()));
  }

}
