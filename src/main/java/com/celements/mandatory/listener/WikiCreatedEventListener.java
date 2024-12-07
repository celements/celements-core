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
package com.celements.mandatory.listener;

import static com.celements.execution.XWikiExecutionProp.*;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.xwiki.context.Execution;

import com.celements.mandatory.IMandatoryDocumentCompositorRole;
import com.celements.wiki.event.WikiCreatedEvent;
import com.xpn.xwiki.XWikiContext;

@Component
public class WikiCreatedEventListener implements ApplicationListener<WikiCreatedEvent>, Ordered {

  private static final Logger LOGGER = LoggerFactory.getLogger(WikiCreatedEventListener.class);

  private IMandatoryDocumentCompositorRole mandatoryDocCmp;
  private Execution execution;

  @Inject
  public WikiCreatedEventListener(
      IMandatoryDocumentCompositorRole mandatoryDocCmp,
      Execution execution) {
    this.mandatoryDocCmp = mandatoryDocCmp;
    this.execution = execution;
  }

  private XWikiContext getContext() {
    return execution.getContext().get(XWIKI_CONTEXT).orElseThrow();
  }

  @Override
  public int getOrder() {
    return -100;
  }

  @Override
  public void onApplicationEvent(WikiCreatedEvent event) {
    String database = event.getWiki().getName();
    LOGGER.debug("received WikiCreatedEvent for database '{}'", database);
    String dbBackup = getContext().getDatabase();
    try {
      LOGGER.info("checking all mandatory documents for db '{}'", database);
      getContext().setDatabase(database);
      mandatoryDocCmp.checkAllMandatoryDocuments();
    } finally {
      getContext().setDatabase(dbBackup);
    }
  }

}
