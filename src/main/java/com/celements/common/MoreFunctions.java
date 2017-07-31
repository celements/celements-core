package com.celements.common;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Function;

public class MoreFunctions {

  private MoreFunctions() {
  }

  public static Function<Object, Integer> hashCodeFunction() {
    return HASHCODE_FUNCTION;
  }

  private static final Function<Object, Integer> HASHCODE_FUNCTION = new Function<Object, Integer>() {

    @Override
    public Integer apply(Object o) {
      return checkNotNull(o).hashCode();
    }
  };

}
