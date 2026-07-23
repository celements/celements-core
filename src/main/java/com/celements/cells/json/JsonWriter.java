package com.celements.cells.json;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import com.celements.cells.AbstractWriter;
import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.CellAttribute;
import com.celements.sajson.JsonBuilder;

@NotThreadSafe
public class JsonWriter extends AbstractWriter {

  private final JsonBuilder jsonBuilder;
  private final StringBuilder contentBuilder;

  public JsonWriter() {
    this(new JsonBuilder());
    jsonBuilder.openArray();
  }

  public JsonWriter(JsonBuilder jsonBuilder) {
    this.jsonBuilder = jsonBuilder;
    this.contentBuilder = new StringBuilder();
  }

  @Override
  public void closeLevel() {
    jsonBuilder.closeArray();
    jsonBuilder.addPropertyNonEmpty("content", contentBuilder.toString());
    contentBuilder.setLength(0);
    jsonBuilder.closeDictionary();
  }

  @Override
  public void clear() {
    super.clear();
    contentBuilder.setLength(0);
    jsonBuilder.clear();
    jsonBuilder.openArray();
  }

  @Override
  public @NotNull ICellWriter appendContent(@Nullable String content) {
    contentBuilder.append(content);
    return this;
  }

  @Override
  public void openLevel(@Nullable String tagName, @NotNull List<CellAttribute> attributes) {
    jsonBuilder.openDictionary();
    jsonBuilder.addPropertyNonEmpty("tagName", tagName);
    jsonBuilder.openDictionary("attributes");
    attributes.stream().filter(attribute -> attribute.getValue().isPresent())
        .forEach(attribute -> jsonBuilder.addPropertyNonEmpty(attribute.getName(),
            attribute.getValue().get()));
    jsonBuilder.closeDictionary();
    jsonBuilder.openArray("subnodes");
  }

  @Override
  public boolean hasLevelContent() {
    throw new UnsupportedOperationException("hasLevelContent has no meaning in json");
  }

  @Override
  public @NotNull String getAsString() {
    jsonBuilder.closeArray();
    return jsonBuilder.getJSON();
  }

  @Override
  public @NotNull StringBuilder getAsStringBuilder() {
    throw new UnsupportedOperationException("getAsStringBuilder has no meaning in json");
  }

}
