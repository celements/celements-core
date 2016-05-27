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
package com.celements.pagetype.xobject.listener;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.ObservationManager;

import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;

public abstract class AbstractXObjectPageTypeDocumentListener {

  @Requirement
  private IPageTypeClassConfig pageTypeClassConfig;

  @Requirement
  private IWebUtilsService webUtilsService;

  /**
   * The observation manager that will be use to fire user creation events. Note: We can't
   * have the OM as a requirement, since it would create an infinite initialization loop,
   * causing a stack overflow error (this event listener would require an initialized OM
   * and the OM requires a list of initialized event listeners)
   */
  private ObservationManager observationManager;

  protected ObservationManager getObservationManager() {
    if (this.observationManager == null) {
      try {
        this.observationManager = webUtilsService.lookup(ObservationManager.class);
      } catch (ComponentLookupException e) {
        throw new RuntimeException(
            "Cound not retrieve an Observation Manager against the component manager");
      }
    }
    return this.observationManager;
  }

  protected XWikiDocument getOrginialDocument(Object source) {
    if (source != null) {
      return ((XWikiDocument) source).getOriginalDocument();
    }
    return null;
  }

  protected DocumentReference getPageTypePropertiesClassRef(XWikiDocument doc) {
    return pageTypeClassConfig.getPageTypePropertiesClassRef(webUtilsService.getWikiRef(doc));
  }

  final protected IWebUtilsService getWebUtilsService() {
    return webUtilsService;
  };

  abstract protected Logger getLogger();

}
