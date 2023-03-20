package com.celements.validation;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.ImmutableList.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.docform.DocFormRequestParam;

@Immutable
public class ValidationResult {

  private final ValidationType type;
  private final String name;
  private final String message;
  private final List<DocFormRequestParam> params;

  public ValidationResult(@NotNull ValidationType type, @Nullable String name,
      @Nullable String message, @Nullable DocFormRequestParam... params) {
    this.type = checkNotNull(type);
    this.name = nullToEmpty(name);
    this.message = nullToEmpty(message);
    this.params = Stream.of(params).filter(Objects::nonNull).collect(toImmutableList());
  }

  @NotNull
  public ValidationType getType() {
    return type;
  }

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public String getMessage() {
    return message;
  }

  @NotNull
  public List<DocFormRequestParam> getParams() {
    return params;
  }

  @Override
  public String toString() {
    return "ValidationResult [type=" + type + ", name=" + name + ", message=" + message + "]";
  }

}
