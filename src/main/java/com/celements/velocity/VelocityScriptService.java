package com.celements.velocity;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.IModelAccessFacade;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;

@Component("velocity")
public class VelocityScriptService implements ScriptService {

  @Requirement
  private VelocityService service;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @NotNull
  public String evaluate(@Nullable String text) {
    return service.evaluate(text).orElse("");
  }

  @NotNull
  public String evaluate(@Nullable DocumentReference docRef) {
    if ((docRef != null) && rightsAccess.hasAccessLevel(docRef, EAccessLevel.VIEW)) {
      return service.evaluate(modelAccess.getOrCreateDocument(docRef)).orElse("");
    }
    return "";
  }

}
