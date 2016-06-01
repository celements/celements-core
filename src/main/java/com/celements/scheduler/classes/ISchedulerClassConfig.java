package com.celements.scheduler.classes;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface ISchedulerClassConfig {

  public static final String XWIKI_SPACE = "XWiki";
  public static final String CLASS_SCHEDULER_JOB_NAME = "SchedulerJobClass";
  public static final String CLASS_SCHEDULER_JOB = XWIKI_SPACE + "." + CLASS_SCHEDULER_JOB_NAME;

  public static final String PROP_JOB_NAME = "jobName";
  public static final String PROP_JOB_DESCRIPTION = "jobDescription";
  public static final String PROP_JOB_CLASS = "jobClass";
  public static final String PROP_STATUS = "status";
  public static final String PROP_CRON = "cron";
  public static final String PROP_SCRIPT = "script";
  public static final String PROP_CONTEXT_USER = "contextUser";
  public static final String PROP_CONTEXT_LANG = "contextLang";
  public static final String PROP_CONTEXT_DATABASE = "contextDatabase";

  public DocumentReference getSchedulerJobClassRef();

  public DocumentReference getSchedulerJobClassRef(WikiReference wikiRef);

}
