package com.celements.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

final class RequestParameter {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      RequestParameter.class);

  private final String parameterName;
  private final String className;
  private final String fieldName;

  private RequestParameter(String parameterName, String className, String fieldName) {
    this.parameterName = parameterName;
    this.className = className;
    this.fieldName = fieldName;
  }

  static RequestParameter create(String requestParameterName) {
    if (isValidRequestParam(requestParameterName)) {
      int pos = includesDocName(requestParameterName) ? 1 : 0;
      String[] paramSplit = requestParameterName.split("_");
      String className = paramSplit[pos];
      String fieldName = paramSplit[pos + 2];
      for (int i = pos + 3; i < paramSplit.length; i++) {
        fieldName += "_" + paramSplit[i];
      }
      return new RequestParameter(requestParameterName, className, fieldName);
    }
    LOGGER.debug("request parameter is not valid '" + requestParameterName + "'");
    return null;
  }

  static boolean isValidRequestParam(String paramName) {
    return paramName.matches("([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){1,2}-?(\\d)*_(.*)");
  }

  static boolean includesDocName(String paramName) {
    return paramName.matches("([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){2}-?(\\d)*_(.*)");
  }

  String getParameterName() {
    return parameterName;
  }

  String getClassName() {
    return className;
  }

  String getFieldName() {
    return fieldName;
  }

  @Override
  public int hashCode() {
    return parameterName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RequestParameter) {
      return parameterName.equals(((RequestParameter) obj).parameterName);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "FieldName [parameterName=" + parameterName + ", className=" + className
        + ", fieldName=" + fieldName + "]";
  }

}