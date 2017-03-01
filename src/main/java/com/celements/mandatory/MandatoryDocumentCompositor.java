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
package com.celements.mandatory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;

@Component
public class MandatoryDocumentCompositor implements IMandatoryDocumentCompositorRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      MandatoryDocumentCompositor.class);

  @Requirement
  Map<String, IMandatoryDocumentRole> mandatoryDocumentsMap;

  @Requirement
  Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public void checkAllMandatoryDocuments() {
    LOGGER.info("checkAllMandatoryDocuments for wiki [" + getContext().getDatabase() + "].");
    for (String mandatoryDocKey : getMandatoryDocumentsList()) {
      IMandatoryDocumentRole mandatoryDoc = mandatoryDocumentsMap.get(mandatoryDocKey);
      try {
        LOGGER.trace("checkDocuments with [" + mandatoryDoc.getClass() + "].");
        mandatoryDoc.checkDocuments();
        LOGGER.trace("end checkDocuments with [" + mandatoryDoc.getClass() + "].");
      } catch (Exception exp) {
        LOGGER.error("Exception checking mandatory documents for component "
            + mandatoryDoc.getClass(), exp);
      }
    }
  }

  List<String> getMandatoryDocumentsList() {
    Collection<String> mandatoryDocElemKeys = new ArrayList<>(mandatoryDocumentsMap.keySet());
    List<String> mandatoryDocExecList = new Vector<>();
    do {
      for (String mandatoryDocElemKey : mandatoryDocElemKeys) {
        if (mandatoryDocExecList.containsAll(mandatoryDocumentsMap.get(
            mandatoryDocElemKey).dependsOnMandatoryDocuments())) {
          mandatoryDocExecList.add(mandatoryDocElemKey);
        }
      }
    } while (mandatoryDocElemKeys.removeAll(mandatoryDocExecList)
        && !mandatoryDocElemKeys.isEmpty());
    for (String skippedDocElemKey : mandatoryDocElemKeys) {
      LOGGER.error("Cannot order all mandatory document roles. Thus skipping: "
          + skippedDocElemKey);
    }
    LOGGER.debug("getMandatoryDocumentsList returning [" + mandatoryDocExecList + "].");
    return mandatoryDocExecList;
  }

}
