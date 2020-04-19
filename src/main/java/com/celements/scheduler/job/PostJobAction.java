package com.celements.scheduler.job;

import java.util.function.Consumer;

import org.quartz.JobExecutionContext;
import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface PostJobAction extends Consumer<JobExecutionContext> {

}
