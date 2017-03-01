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
package com.celements.web.utils;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;

/**
 * TODO change to ScriptService using an ExecuterService implementation
 * e.g. ExecutorService executor = Executors.newFixedThreadPool(2);
 * Create the DocumentCreationWorker and wrap in a FutureTask. After that add the
 * futureTask to the executor: executor.execute(futureTask1);
 * The FutureTask allows to cancel the Worker and can check if it isDone.
 *
 * @author pichlerf
 */
@Deprecated
public class DocumentCreationWorkerControlApi extends Api {

  DocumentCreationWorkerControl workerControl;

  public DocumentCreationWorkerControlApi(XWikiContext context) {
    super(context);
    workerControl = DocumentCreationWorkerControl.getWorkerControl();
  }

  public void startThread(String space, int numberOfThreads) {
    workerControl.startThread(space, numberOfThreads, context);
  }

  public void stopAllWorkers() {
    workerControl.stopAllWorkers();
  }

  public void stopWorker(int number) {
    workerControl.stopWorker(number);
  }

  public String getStats() {
    return workerControl.getStats();
  }
}
