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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xpn.xwiki.XWikiContext;

public class DocumentCreationWorkerControl {
  private static DocumentCreationWorkerControl workerControl;
  private List<DocumentCreationWorker> workers;
  private int maxId = 0;
  private Map<Integer, Long[]> stats;
  
  private DocumentCreationWorkerControl() {
    workers = new ArrayList<DocumentCreationWorker>();
    stats = new HashMap<Integer, Long[]>();
  }
  
  /**
   * Class is Singleton to be able to add or remove workers using Velocity
   * @return
   */
  public static DocumentCreationWorkerControl getWorkerControl() {
    if(workerControl == null) {
      workerControl = new DocumentCreationWorkerControl();
    }
    return workerControl;
  }
  
  public void startThread(String space, int numberOfThreads, XWikiContext context) {
    clearFinishedWorkers(null);
    for (int i = 0; i < numberOfThreads; i++) {
      DocumentCreationWorker worker = new DocumentCreationWorker(space, maxId, context);
      worker.run();
      workers.add(worker);
      maxId++;
    }
  }
  
  public void stopAllWorkers() {
    stopWorker(workers.size());
  }
  
  public void stopWorker(int number) {
    number = Math.min(number, workers.size());
    int i = 0;
    for (DocumentCreationWorker worker : workers) {
      if(i < number) {
        worker.stopThread();
        i++;
      }
    }
    clearFinishedWorkers(null);
  }
  
  public String getStats() {
    clearFinishedWorkers(null);
    long total = 0;
    long maxTime = 0;
    String statsHTML = "<table><thead><tr><th>Thread</th><th>Created</th><th>Time</th>" +
        "<th>Average</th></tr></thead><tbody><tr><td>Stopped</td></tr>";
    for (Integer key : stats.keySet()) {
      long nrDocs = stats.get(key)[0];
      long time = stats.get(key)[1];
      maxTime = Math.max(maxTime, time);
      total += nrDocs;
      statsHTML += addStatRow(statsHTML, key, nrDocs, time);
    }
    statsHTML += "<tr><td>Running</td></tr>";
    for (DocumentCreationWorker worker : workers) {
      long nrDocs = worker.getStats()[0];
      long time = worker.getStats()[1];
      maxTime = Math.max(maxTime, time);
      total += nrDocs;
      statsHTML += addStatRow(statsHTML, worker.getCelId(), nrDocs, time);
    }
    statsHTML += "<tr><td>Total</td><td>" + total + "</td><td>" + formatTime(maxTime) + 
        "</td><td>" + ((double)total / ((double)maxTime / 1000)) + "</td></tr>" +
        "</tbody></table>";
    return statsHTML;
  }

  private String addStatRow(String statsHTML, int id, long nrDocs, long time) {
    return "<tr><td>" + id + "</td><td>" + nrDocs + "</td><td>" + formatTime(time) + 
        "</td><td>" + ((double)nrDocs / ((double)time / 1000)) + "</td></tr>";
  }
  
  private String formatTime(long time) {
    String timeString = "." + (time % 1000);
    timeString = ":" + ((time / 1000) % 60) + timeString;
    timeString = ":" + ((time / 60000) % 60) + timeString;
    timeString = (time / 3600000) + timeString;
    return timeString;
  }

  private void addStats(DocumentCreationWorker worker) {
    stats.put(worker.getCelId(), worker.getStats());
  }

  private void clearFinishedWorkers(Object object) {
    List<Integer> finishedList = new ArrayList<Integer>();
    for (DocumentCreationWorker worker : workers) {
      if(!worker.isRunning()) {
        addStats(worker);
        finishedList.add(workers.indexOf(worker));
      }
    }
    for (Integer id : finishedList) {
      workers.remove(id);
    }
  }
}
