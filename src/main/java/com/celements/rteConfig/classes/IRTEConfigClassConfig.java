package com.celements.rteConfig.classes;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface IRTEConfigClassConfig {

  public static final String RTE_CONFIG_TYPE_PRPOP_CLASS_DOC = "RTEConfigTypePropertiesClass";
  public static final String RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE = "Classes";

  public DocumentReference getRTEConfigTypePropertiesClassRef(EntityReference inRef);

  public DocumentReference getRTEConfigTypePropertiesClassRef(WikiReference wikiRef);

}
