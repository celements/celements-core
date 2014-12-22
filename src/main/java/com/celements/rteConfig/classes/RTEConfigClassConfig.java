package com.celements.rteConfig.classes;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.web.service.IWebUtilsService;

@Component
public class RTEConfigClassConfig implements IRTEConfigClassConfig {

  @Requirement
  IWebUtilsService webUtilsService;

  @Override
  public DocumentReference getRTEConfigTypePropertiesClassRef(EntityReference inRef) {
    return getRTEConfigTypePropertiesClassRef(webUtilsService.getWikiRef(inRef));
  }

  @Override
  public DocumentReference getRTEConfigTypePropertiesClassRef(WikiReference wikiRef) {
    return new DocumentReference(wikiRef.getName(), RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE,
        RTE_CONFIG_TYPE_PRPOP_CLASS_DOC);
  }

}
