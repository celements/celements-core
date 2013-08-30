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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiException;

@Component("celements.MandatoryGroups")
public class CelementsGroups extends AbstractMandatoryGroups {

  private static Log LOGGER = LogFactory.getFactory().getInstance(CelementsGroups.class);

  @Override
  protected String commitName() {
    return "mandatory celements";
  }

  @Override
  public void checkDocuments() throws XWikiException {
    LOGGER.trace("Start checkDocuments in CelementsGroups for database ["
        + getContext().getDatabase() + "].");
    checkGroup(getContentEditorGroupRef(getContext().getDatabase()));
    checkGroup(getAdminGroupRef(getContext().getDatabase()));
    checkGroup(getAllGroupRef(getContext().getDatabase()));
    LOGGER.trace("end checkDocuments in CelementsGroups for database ["
        + getContext().getDatabase() + "].");
  }

  public DocumentReference getAdminGroupRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiAdminGroup");
  }

  public DocumentReference getAllGroupRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiAllGroup");
  }

  public DocumentReference getContentEditorGroupRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "ContentEditorsGroup");
  }

}
