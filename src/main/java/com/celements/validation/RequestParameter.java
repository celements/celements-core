package com.celements.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class RequestParameter {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      RequestParameter.class);

  private final String parameterName;
  private final String className;
  private final long objectNr;
  private final String fieldName;

  private RequestParameter(String parameterName, String className, long objectNr,
      String fieldName) {
    this.parameterName = parameterName;
    this.className = className;
    this.objectNr = objectNr;
    this.fieldName = fieldName;
  }

  /**
   * @param requestParameterName
   * @return new RequestParameter or null if requestParameterName is invalid
   */
  public static RequestParameter create(String requestParameterName) {
    if (isValidRequestParam(requestParameterName)) {
      int pos = includesDocName(requestParameterName) ? 1 : 0;
      String[] paramSplit = requestParameterName.split("_");
      String className = paramSplit[pos];
      long objectNr = Long.parseLong(paramSplit[pos + 1]);
      String fieldName = paramSplit[pos + 2];
      for (int i = pos + 3; i < paramSplit.length; i++) {
        fieldName += "_" + paramSplit[i];
      }
      return new RequestParameter(requestParameterName, className, objectNr, fieldName);
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

  public String getParameterName() {
    return parameterName;
  }

  public String getClassName() {
    return className;
  }

  public long getObjectNr() {
    return objectNr;
  }

  public String getFieldName() {
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
        + ", objectNr=" + objectNr + ", fieldName=" + fieldName + "]";
  }

}