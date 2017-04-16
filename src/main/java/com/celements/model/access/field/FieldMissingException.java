package com.celements.model.access.field;

/**
 * is thrown if a {@link FieldAccessor} detects a field that is missing or unhandled by the
 * underlying instance
 */
public class FieldMissingException extends FieldAccessException {

  private static final long serialVersionUID = 1L;

  public FieldMissingException(Throwable cause) {
    super(cause);
  }

}
