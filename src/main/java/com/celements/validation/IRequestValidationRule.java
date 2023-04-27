package com.celements.validation;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.docform.DocFormRequestParam;

@ComponentRole
public interface IRequestValidationRule {

  @NotNull
  List<ValidationResult> validate(@NotNull List<DocFormRequestParam> params);

}
