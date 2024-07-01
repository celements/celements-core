package com.celements.cells.json;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.NotImplementedException;

import com.celements.cells.AbstractWriter;
import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.CellAttribute;
import com.celements.sajson.JsonBuilder;

@NotThreadSafe
public class JsonWriter extends AbstractWriter {

  private final JsonBuilder jsonBuilder;

  public JsonWriter() {
    this(new JsonBuilder());
    jsonBuilder.openDictionary();
  }

  public JsonWriter(JsonBuilder jsonBuilder) {
    this.jsonBuilder = jsonBuilder;
  }

  @Override
  public void closeLevel() {
    jsonBuilder.closeDictionary();
  }

  @Override
  public void clear() {
    super.clear();
    jsonBuilder.clear();
    jsonBuilder.openDictionary();
  }

  @Override
  public @NotNull ICellWriter appendContent(@Nullable String content) {
    jsonBuilder.addPropertyNonEmpty("content", content);
    return this;
  }

  @Override
  public void openLevel(@Nullable String tagName, @NotNull List<CellAttribute> attributes) {
    jsonBuilder.openDictionary("cell");
    jsonBuilder.addPropertyNonEmpty("tagName", tagName);
    jsonBuilder.openProperty("attributes");
    jsonBuilder.openDictionary();
    attributes.stream()
        .filter(attribute -> attribute.getValue().isPresent())
        .forEach(
            attribute -> jsonBuilder.addPropertyNonEmpty(attribute.getName(),
                attribute.getValue().get()));
    jsonBuilder.closeDictionary();
  }

  @Override
  public boolean hasLevelContent() {
    // TODO solve correctly with a boolean inside JsonWriter
    return hasLevelContentOptional().orElse(!jsonBuilder.isOnFirstElement());
  }

  @Override
  public @NotNull String getAsString() {
    jsonBuilder.closeDictionary();
    return jsonBuilder.getJSON();
  }

  @Override
  public @NotNull StringBuilder getAsStringBuilder() {
    throw new NotImplementedException();
  }

}
