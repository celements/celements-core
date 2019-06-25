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
package com.celements.parents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.cache.CacheLoadingException;
import com.celements.common.cache.IDocumentReferenceCache;
import com.celements.navigation.NavigationCache;
import com.celements.web.service.IWebUtilsService;

@Component(NavigationParents.NAME)
public class NavigationParents implements IDocParentProviderRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(NavigationParents.class);

  public static final String NAME = "navigation";

  @Requirement(NavigationCache.NAME)
  IDocumentReferenceCache<String> navCache;

  @Requirement
  IWebUtilsService webUtils;

  @Override
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef) {
    List<DocumentReference> ret;
    String spaceName = docRef.getLastSpaceReference().getName();
    try {
      ret = new ArrayList<>();
      ret.addAll(navCache.getCachedDocRefs(webUtils.getWikiRef(docRef), spaceName));
      ret.addAll(navCache.getCachedDocRefs(webUtils.getCentralWikiRef(), spaceName));
      // TODO get potential parents located in other wikis ?
    } catch (CacheLoadingException exp) {
      LOGGER.error("Failed loading cache for docRef '{}'", docRef, exp);
      ret = Collections.emptyList();
    }
    return ret;
  }

}
