package com.celements.scheduler.classes;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.web.service.IWebUtilsService;

@Component
public class SchedulerClassConfig implements ISchedulerClassConfig {

  @Requirement
  private IWebUtilsService webUtilsService;

  @Override
  public DocumentReference getSchedulerJobClassRef() {
    return getSchedulerJobClassRef(webUtilsService.getWikiRef());
  }

  @Override
  public DocumentReference getSchedulerJobClassRef(WikiReference wikiRef) {
    return new DocumentReference(CLASS_SCHEDULER_JOB_NAME, new SpaceReference(XWIKI_SPACE,
        wikiRef));
  }

}
