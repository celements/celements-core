package com.celements.docform;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Predicates.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.ImmutableList.*;
import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.reference.DocumentReference;

import com.google.common.collect.ImmutableList;

@Immutable
public class DocFormRequestParam implements Comparable<DocFormRequestParam> {

  private final DocFormRequestKey key;
  private final ImmutableList<String> values;

  public DocFormRequestParam(DocFormRequestKey key, List<String> values) {
    this.key = checkNotNull(key);
    this.values = values.stream()
        .map(s -> nullToEmpty(s).trim())
        .filter(not(String::isEmpty))
        .collect(toImmutableList());
  }

  DocFormRequestParam(DocFormRequestKey key, Object value) {
    this(key, toStringList(value));
  }

  private static ImmutableList<String> toStringList(Object obj) {
    return ((obj instanceof Object[]) ? Stream.of((Object[]) obj) : Stream.of(obj))
        .filter(Objects::nonNull).map(Objects::toString).collect(toImmutableList());
  }

  public DocFormRequestKey getKey() {
    return key;
  }

  public DocumentReference getDocRef() {
    return getKey().getDocRef();
  }

  public ImmutableList<String> getValues() {
    return values;
  }

  public String getValuesAsString() {
    return values.stream().collect(joining(" "));
  }

  public Object getFirstValue() {
    return values.stream().findFirst().orElse(null);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DocFormRequestParam) {
      obj = ((DocFormRequestParam) obj).getKey();
    }
    return key.equals(obj);
  }

  @Override
  public int compareTo(DocFormRequestParam that) {
    return this.key.compareTo(that.key);
  }

  @Override
  public String toString() {
    return "DocFormRequestParam [" + key + ", values=" + values + "]";
  }

}
