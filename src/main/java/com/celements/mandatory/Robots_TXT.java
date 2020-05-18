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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.pagetype.PageTypeClasses;
import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("celements.mandatory.robots_txt")
public class Robots_TXT implements IMandatoryDocumentRole {

  private static final String _ROBOTS_TXT_PAGE_TYPE = "Code";

  private static final Logger LOGGER = LoggerFactory.getLogger(Robots_TXT.class);

  @Requirement("celements.celPageTypeClasses")
  IClassCollectionRole pageTypeClasses;

  @Requirement
  Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  private PageTypeClasses getPageTypeClasses() {
    return (PageTypeClasses) pageTypeClasses;
  }

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return Collections.emptyList();
  }

  @Override
  public void checkDocuments() throws XWikiException {
    LOGGER.trace("Start checkDocuments in robots_txt for database [" + getContext().getDatabase()
        + "].");
    if (!isSkipCelementsRobots_txt()) {
      LOGGER.trace("before checkRobots_txt for database [" + getContext().getDatabase() + "].");
      checkRobots_txtDocument();
    } else {
      LOGGER.info("skip mandatory checkRobots_txt for database [" + getContext().getDatabase()
          + "], skipCelementsParam [" + isSkipCelementsRobots_txt() + "].");
    }
    LOGGER.trace("end checkDocuments in Robots_txt for database [" + getContext().getDatabase()
        + "].");
  }

  boolean isSkipCelementsRobots_txt() {
    boolean isSkip = getContext().getWiki().ParamAsLong("celements.mandatory.skipRobots_txt",
        0) == 1L;
    LOGGER.trace("skipCelementsRobots_txt for database [" + getContext().getDatabase()
        + "] returning [" + isSkip + "].");
    return isSkip;
  }

  void checkRobots_txtDocument() throws XWikiException {
    DocumentReference robotsTxtDocRef = getRobotsTxtDocRef(getContext().getDatabase());
    XWikiDocument robotsTxtDoc;
    if (!getContext().getWiki().exists(robotsTxtDocRef, getContext())) {
      LOGGER.debug("Robots_txtDocument is missing that we create it. [" + getContext().getDatabase()
          + "]");
      robotsTxtDoc = new CreateDocumentCommand().createDocument(robotsTxtDocRef,
          _ROBOTS_TXT_PAGE_TYPE);
    } else {
      robotsTxtDoc = getContext().getWiki().getDocument(robotsTxtDocRef, getContext());
      LOGGER.trace("Robots_txtDocument already exists. [" + getContext().getDatabase() + "]");
    }
    if (robotsTxtDoc != null) {
      boolean dirty = checkPageType(robotsTxtDoc);
      dirty |= checkRobots_txt(robotsTxtDoc);
      if (dirty) {
        LOGGER.info("Robots_txtDocument updated for [" + getContext().getDatabase() + "].");
        getContext().getWiki().saveDocument(robotsTxtDoc, "autocreate" + " HTML.robots_txt.",
            getContext());
      } else {
        LOGGER.debug("Robots_txtDocument not saved. Everything uptodate. ["
            + getContext().getDatabase() + "].");
      }
    } else {
      LOGGER.trace("skip checkRobots_txt because robotsTxtDoc is null! ["
          + getContext().getDatabase() + "]");
    }
  }

  boolean checkRobots_txt(XWikiDocument robotsTxtDoc) throws XWikiException {
    if (StringUtils.isEmpty(robotsTxtDoc.getContent())) {
      robotsTxtDoc.setContent("User-agent: *\n\nCrawl-delay: 120\n"
          + "# Angabe der Sitemap ist Agent-unabhaengig\n" + "Sitemap: $doc.getExternalURL('view',"
          + " 'ajax=1&xpage=celements_ajax&ajax_mode=sitemapxml')");
      LOGGER.debug("Robots_txt missing content fixed for database [" + getContext().getDatabase()
          + "].");
      return true;
    }
    return false;
  }

  boolean checkPageType(XWikiDocument robotsTxtDoc) throws XWikiException {
    DocumentReference pageTypeClassRef = getPageTypeClasses().getPageTypeClassRef(
        getContext().getDatabase());
    BaseObject pageTypeObj = robotsTxtDoc.getXObject(pageTypeClassRef, false, getContext());
    if (pageTypeObj == null) {
      pageTypeObj = robotsTxtDoc.newXObject(pageTypeClassRef, getContext());
      pageTypeObj.setStringValue("page_type", _ROBOTS_TXT_PAGE_TYPE);
      LOGGER.debug("HTML.robots_txt missing page type object fixed for database ["
          + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  private DocumentReference getRobotsTxtDocRef(String wikiName) {
    return new DocumentReference(wikiName, "HTML", "robots_txt");
  }

}
