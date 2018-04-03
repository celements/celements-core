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

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiException;

@Component("celements.MandatoryGroups")
public class CelementsGroups extends AbstractMandatoryGroups {

  @Override
  protected String commitName() {
    return "mandatory celements";
  }

  @Override
  public void checkDocuments() throws XWikiException {
    String wikiName = modelContext.getWikiRef().getName();
    getLogger().trace("Start checkDocuments in CelementsGroups for database [{}]", wikiName);
    checkGroup(getContentEditorGroupRef(wikiName));
    checkGroup(getLayoutEditorGroupRef(wikiName));
    checkGroup(getAdminGroupRef(wikiName));
    checkGroup(getAllGroupRef(wikiName));
    getLogger().trace("end checkDocuments in CelementsGroups for database [{}]", wikiName);
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

  public DocumentReference getLayoutEditorGroupRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "LayoutEditorsGroup");
  }

}
