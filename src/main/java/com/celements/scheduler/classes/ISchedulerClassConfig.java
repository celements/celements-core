package com.celements.scheduler.classes;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface ISchedulerClassConfig {

  public static final String XWIKI_SPACE = "XWiki";
  public static final String CLASS_SCHEDULER_JOB_NAME = "SchedulerJobClass";
  public static final String CLASS_SCHEDULER_JOB = XWIKI_SPACE + "."
      + CLASS_SCHEDULER_JOB_NAME;

  public DocumentReference getSchedulerJobClassRef();

  public DocumentReference getSchedulerJobClassRef(WikiReference wikiRef);

}
