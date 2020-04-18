package com.celements.collections.service;

import static com.google.common.base.Strings.*;
import static org.glassfish.jersey.internal.guava.Predicates.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;

@Component("stream")
public class StreamScriptService implements ScriptService {

  public Stream<?> of(Object... values) {
    return (values != null) ? Stream.of(values).filter(Objects::nonNull) : Stream.of();
  }

  public <T> List<T> toList(Stream<T> stream) {
    return nullGuard(stream).collect(Collectors.toList());
  }

  public <T> Set<T> toSet(Stream<T> stream) {
    return nullGuard(stream).collect(Collectors.toSet());
  }

  public <T> Collection<T> toCollection(Stream<T> stream, Collection<T> collection) {
    return nullGuard(stream).collect(Collectors.toCollection(() -> collection));
  }

  public String join(Stream<?> stream, String separator) {
    return filterNonBlank(stream).collect(Collectors.joining(nullToEmpty(separator)));
  }

  public Stream<String> filterNonBlank(Stream<?> stream) {
    return nullGuard(stream).map(Object::toString).map(String::trim).filter(not(String::isEmpty));
  }

  public <T> Stream<T> filterNull(Stream<T> stream) {
    return nullGuard(stream);
  }

  private <T> Stream<T> nullGuard(Stream<T> stream) {
    return Optional.ofNullable(stream).orElseGet(Stream::empty).filter(Objects::nonNull);
  }

}
