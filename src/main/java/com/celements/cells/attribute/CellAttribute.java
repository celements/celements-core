package com.celements.cells.attribute;

import java.util.Optional;

import javax.validation.constraints.NotNull;

public interface CellAttribute {

  @NotNull
  String getName();

  @NotNull
  Optional<String> getValue();

}
