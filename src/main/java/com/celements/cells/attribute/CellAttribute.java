package com.celements.cells.attribute;

import javax.validation.constraints.NotNull;

import com.google.common.base.Optional;

public interface CellAttribute {

  @NotNull
  public String getName();

  @NotNull
  public Optional<String> getValue();

}
