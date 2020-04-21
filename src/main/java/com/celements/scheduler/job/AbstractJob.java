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
package com.celements.scheduler.job;

import java.net.MalformedURLException;
import java.net.URL;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.velocity.VelocityManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.XWiki;
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

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

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
    } catch (ExecutionContextException | DocumentNotExistsException | MalformedURLException exp) {
      throw new JobExecutionException("Failed to initialize execution context", exp);
    }

    try {
      // Execute the job
      executeJob(jobContext);
    } catch (Throwable exp) {
      getLogger().error("Exception thrown during job '{}' execution",
          jobContext.getJobDetail().getFullName(), exp);
      throw new JobExecutionException("Failed to execute job '"
          + jobContext.getJobDetail().getFullName() + "'", exp);
    } finally {
      // We must ensure we clean the ThreadLocal variables located in the Execution
      // component as otherwise we will have a potential memory leak.
      execution.removeContext();
      Utils.getComponentList(PostJobAction.class).forEach(runnable -> {
        try {
          runnable.accept(jobContext);
        } catch (Exception exc) {
          getLogger().error("failed to execute [{}]", runnable, exc);
        }
      });
    }
  }

  Execution initExecutionContext(XWikiContext xwikiContext) throws ExecutionContextException,
      DocumentNotExistsException, MalformedURLException {
    // Init execution context
    ExecutionContextManager ecim = Utils.getComponent(ExecutionContextManager.class);
    Execution execution = Utils.getComponent(Execution.class);

    ExecutionContext ec = new ExecutionContext();
    XWikiContext scontext = createJobContext(xwikiContext);
    // Bridge with old XWiki Context, required for old code.
    ec.setProperty("xwikicontext", scontext);

    ecim.initialize(ec);
    execution.setContext(ec);

    setupServerUrlAndFactory(scontext, xwikiContext);
    VelocityManager velocityManager = Utils.getComponent(VelocityManager.class);
    velocityManager.getVelocityContext();
    return execution;
  }

  /**
   * create a new copy of the xwiki context. Job Executions always use a different thread.
   * The xwiki context is NOT thread safe and may not be shared.
   * TODO CELDEV-534
   *
   * @throws DocumentNotExistsException
   * @throws MalformedURLException
   */
  XWikiContext createJobContext(XWikiContext xwikiContext) throws DocumentNotExistsException,
      MalformedURLException {
    final XWiki xwiki = xwikiContext.getWiki();
    final String database = xwikiContext.getDatabase();

    // We are sure the context request is a real servlet request
    // So we force the dummy request with the current host
    XWikiServletRequestStub dummy = new XWikiServletRequestStub();
    dummy.setHost(xwikiContext.getRequest().getHeader("x-forwarded-host"));
    dummy.setScheme(xwikiContext.getRequest().getScheme());
    XWikiServletRequest request = new XWikiServletRequest(dummy);

    // Force forged context response to a stub response, since the current context response
    // will not mean anything anymore when running in the scheduler's thread, and can cause
    // errors.
    XWikiResponse response = new XWikiServletResponseStub();

    // IMPORTANT: do NOT clone xwikiContext. You would need to ensure that no reference or
    // unwanted value leaks in the new context.
    // IMPORTANT: following lines base on Utils.prepareContext
    XWikiContext scontext = new XWikiContext();
    scontext.setEngineContext(xwikiContext.getEngineContext());
    scontext.setRequest(request);
    scontext.setResponse(response);
    scontext.setAction("view");
    scontext.setDatabase(database);

    // feed the job context
    scontext.setUser(xwikiContext.getUser());
    scontext.setLanguage(xwikiContext.getLanguage());
    scontext.setMainXWiki(xwikiContext.getMainXWiki());
    scontext.setMode(XWikiContext.MODE_SERVLET);

    scontext.setWiki(xwiki);
    scontext.getWiki().getStore().cleanUp(scontext);

    scontext.flushClassCache();
    scontext.flushArchiveCache();
    return scontext;
  }

  void setupServerUrlAndFactory(XWikiContext scontext, XWikiContext xwikiContext)
      throws MalformedURLException, DocumentNotExistsException {
    scontext.setDoc(getModelAccess().getDocument(xwikiContext.getDoc().getDocumentReference()));

    final URL url = xwikiContext.getWiki().getServerURL(xwikiContext.getDatabase(), scontext);
    // Push the URL into the slf4j MDC context so that we can display it in the generated logs
    // using the %X{url} syntax.
    MDC.put("url", url.toString());
    scontext.setURL(url);

    com.xpn.xwiki.web.XWikiURLFactory xurf = xwikiContext.getURLFactory();
    if (xurf == null) {
      xurf = scontext.getWiki().getURLFactoryService().createURLFactory(scontext.getMode(),
          scontext);
    }
    scontext.setURLFactory(xurf);
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  protected Logger getLogger() {
    return logger;
  }

  protected abstract void executeJob(JobExecutionContext jobContext) throws JobExecutionException;
}
