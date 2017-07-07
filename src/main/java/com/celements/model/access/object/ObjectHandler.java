package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;

import java.text.MessageFormat;
import java.util.Collection;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.fields.ClassField;
import com.xpn.xwiki.doc.XWikiDocument;

@NotThreadSafe
public class ObjectHandler {

  private final XWikiDocument doc;
  private final ObjectFilter filter = new ObjectFilter();

  public static @NotNull ObjectHandler onDoc(@NotNull XWikiDocument doc) {
    return new ObjectHandler(doc);
  }

  private ObjectHandler(XWikiDocument doc) {
    checkNotNull(doc);
    checkState(doc.getTranslation() == 0, MessageFormat.format("ObjectHandler cannot be used"
        + " on translation ''{0}'' of doc ''{1}''", doc.getLanguage(), doc.getDocumentReference()));
    this.doc = doc;
  }

  public ObjectHandler filter(ClassReference classRef) {
    filter.add(checkNotNull(classRef));
    return this;
  }

  public <T> ObjectHandler filter(ClassField<T> field, T value) {
    filter.add(checkNotNull(field), checkNotNull(value));
    return this;
  }

  public <T> ObjectHandler filter(ClassField<T> field, Collection<T> values) {
    checkNotNull(field);
    checkArgument(!checkNotNull(values).isEmpty(), "cannot filter for empty value list");
    for (T value : values) {
      filter.add(field, value);
    }
    return this;
  }

  public ObjectHandler filterAbsent(ClassField<?> field) {
    filter.addAbsent(checkNotNull(field));
    return this;
  }

  public ObjectFetcher fetch() {
    return new ObjectFetcher(doc, filter.createView());
  }

  public ObjectEditor edit() {
    return new ObjectEditor(doc, filter.createView());
  }

}
