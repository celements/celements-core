/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.job;

import java.net.URL;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.scheduler.XWikiServletRequestStub;
import com.xpn.xwiki.plugin.scheduler.XWikiServletResponseStub;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiResponse;
import com.xpn.xwiki.web.XWikiServletRequest;

/**
 * Base class for any XWiki Quartz Job. This class take care of initializing ExecutionContext
 * properly.
 * <p>
 * A class extending {@link AbstractJob} should implements {@link #executeJob(JobExecutionContext)}.
 *
 * @since 2.90
 * @author fabian pichler
 */
public abstract class AbstractJob implements Job {

  /**
   * {@inheritDoc}
   *
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public final void execute(JobExecutionContext jobContext) throws JobExecutionException {
    JobDataMap data = jobContext.getJobDetail().getJobDataMap();

    // The XWiki context was saved in the Job execution data map. Get it as we'll retrieve
    // the script to execute from it.
    XWikiContext xwikiContext = (XWikiContext) data.get("context");

    Execution execution;
    try {
      execution = initExecutionContext(xwikiContext);
    } catch (ExecutionContextException | DocumentNotExistsException e) {
      throw new JobExecutionException("Fail to initialize execution context", e);
    }

    try {
      // Execute the job
      executeJob(jobContext);
    } catch (Throwable t) {
      getLogger().error("Exception thrown during job execution", t);
    } finally {
      // We must ensure we clean the ThreadLocal variables located in the Execution
      // component as otherwise we will have a potential memory leak.
      execution.removeContext();
    }
  }

  private Execution initExecutionContext(XWikiContext xwikiContext)
      throws DocumentNotExistsException, ExecutionContextException {
    // Init execution context
    ExecutionContextManager ecim = Utils.getComponent(ExecutionContextManager.class);
    Execution execution = Utils.getComponent(Execution.class);

    ExecutionContext ec = new ExecutionContext();
    // Bridge with old XWiki Context, required for old code.
    ec.setProperty("xwikicontext", createJobContext(xwikiContext));

    ecim.initialize(ec);
    execution.setContext(ec);
    return execution;
  }

  /**
   * create a new copy of the xwiki context. Job Executions always use a different thread.
   * The xwiki context is NOT thread safe and may not be shared.
   *
   * @throws DocumentNotExistsException
   */
  private XWikiContext createJobContext(XWikiContext xwikiContext)
      throws DocumentNotExistsException {
    // lets now build the stub context
    XWikiContext scontext = (XWikiContext) xwikiContext.clone();
    scontext.setWiki(xwikiContext.getWiki());
    scontext.getWiki().getStore().cleanUp(scontext);

    // We are sure the context request is a real servlet request
    // So we force the dummy request with the current host
    XWikiServletRequestStub dummy = new XWikiServletRequestStub();
    dummy.setHost(xwikiContext.getRequest().getHeader("x-forwarded-host"));
    dummy.setScheme(xwikiContext.getRequest().getScheme());
    XWikiServletRequest request = new XWikiServletRequest(dummy);
    scontext.setRequest(request);

    // Force forged context response to a stub response, since the current context response
    // will not mean anything anymore when running in the scheduler's thread, and can cause
    // errors.
    XWikiResponse stub = new XWikiServletResponseStub();
    scontext.setResponse(stub);

    // feed the dummy context
    scontext.setUser(xwikiContext.getUser());
    scontext.setLanguage(xwikiContext.getLanguage());
    scontext.setDatabase(xwikiContext.getDatabase());
    scontext.setMainXWiki(xwikiContext.getMainXWiki());
    scontext.setMode(XWikiContext.MODE_SERVLET);
    if (scontext.getURL() == null) {
      try {
        scontext.setURL(new URL("http://www.mystuburl.com/"));
      } catch (Exception e) {
        // the URL is well formed, I promise
      }
    }

    com.xpn.xwiki.web.XWikiURLFactory xurf = xwikiContext.getURLFactory();
    if (xurf == null) {
      xurf = scontext.getWiki().getURLFactoryService().createURLFactory(scontext.getMode(),
          scontext);
    }
    scontext.setURLFactory(xurf);

    scontext.setDoc(getModelAccess().getDocument(xwikiContext.getDoc().getDocumentReference()));
    scontext.flushClassCache();
    scontext.flushArchiveCache();
    return scontext;
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  protected abstract Logger getLogger();

  protected abstract void executeJob(JobExecutionContext jobContext) throws JobExecutionException;
}
