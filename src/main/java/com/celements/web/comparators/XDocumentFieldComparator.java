package com.celements.web.comparators;

import static com.google.common.base.Preconditions.*;
import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.PlainClassField;
import com.celements.model.object.ObjectFetcher.FieldFetcher;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.web.classes.oldcore.XWikiDocumentClass;
import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;
import com.xpn.xwiki.doc.XWikiDocument;

@Immutable
public class XDocumentFieldComparator implements Comparator<XWikiDocument> {

  private final Comparator<XWikiDocument> comparator;

  public XDocumentFieldComparator(@NotNull Stream<SortField> sorts) {
    this.comparator = sorts
        .map(SortField::asDocComparator)
        .reduce((c1, c2) -> c1.thenComparing(c2))
        .orElseGet(() -> Comparator.comparing(doc -> 0));
  }

  @Override
  public int compare(XWikiDocument doc1, XWikiDocument doc2) {
    return comparator.compare(doc1, doc2);
  }

  @Immutable
  public static class SortField {

    private static final String DELIM = ".";
    private static final Splitter SPLITTER = Splitter.on(DELIM).omitEmptyStrings().trimResults();
    private static final Comparator<Collection<Object>> COMPARATOR = new CollectionComparator<>(
        new ObjectComparator());

    public final ClassField<?> field;
    public final Optional<Integer> number;
    public final boolean asc;

    public SortField(ClassField<?> field, boolean asc) {
      this(field, null, asc);
    }

    public SortField(ClassField<?> field, Integer number, boolean asc) {
      this.field = checkNotNull(field);
      this.number = Optional.ofNullable(number);
      this.asc = asc;
    }

    public Comparator<XWikiDocument> asDocComparator() {
      Comparator<XWikiDocument> cmp = Comparator.comparing(doc -> fetcher(doc)
          .streamNullable().collect(toList()), COMPARATOR);
      return asc ? cmp : cmp.reversed();
    }

    public FieldFetcher<?> fetcher(XWikiDocument doc) {
      XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(doc);
      number.ifPresent(fetcher::filter);
      return fetcher.fetchField(field);
    }

    // [-ClassSpace.ClassName.3.]fieldName
    public static Optional<SortField> parse(String sort) {
      sort = sort.trim();
      boolean asc = !sort.startsWith("-");
      if (!asc) {
        sort = sort.replaceFirst("-", "");
      }
      List<String> parts = SPLITTER.splitToList(sort);
      int skip = 0;
      ClassReference classRef = tryParseClassRef(parts.stream().limit(2).collect(joining(DELIM)));
      skip += (classRef != null) ? 2 : 0;
      Integer number = Ints.tryParse(parts.stream().skip(skip).findFirst().orElse(""));
      skip += (number != null) ? 1 : 0;
      String name = parts.stream().skip(skip).collect(joining(DELIM));
      if (!name.isEmpty()) {
        ClassField<?> field = new PlainClassField<>(classRef, name, Object.class);
        return Optional.of(new SortField(field, number, asc));
      }
      return Optional.empty();
    }

    private static ClassReference tryParseClassRef(String name) {
      try {
        return new ClassReference(name);
      } catch (IllegalArgumentException iae) {
        return XWikiDocumentClass.CLASS_REF;
      }
    }
  }
}
