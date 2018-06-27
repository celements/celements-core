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
package com.celements.pagetype.service;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.pagetype.PageTypeReference;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface IPageTypeResolverRole {

  public PageTypeReference getPageTypeRefForCurrentDoc();

  /**
   * getPageTypeObject returns the page-type object attached to the given doc. If the
   * doc isNew and thus has no objects it falls back to the wikiTemplateDoc in case
   * one is defined in the request.
   *
   * @deprecated since 3.1
   * @param docRef
   * @return page-type object or null if non is present
   */
  @Deprecated
  public BaseObject getPageTypeObject(XWikiDocument docRef);

  /**
   * @deprecated since 3.1, instead use {@link #resolvePageTypeReference(XWikiDocument)}
   */
  @Nullable
  @Deprecated
  public PageTypeReference getPageTypeRefForDoc(@Nullable XWikiDocument doc);

  /**
   * gets the {@link PageTypeReference} defined by the PageType-xobject attached to
   * the given document.
   *
   * @param doc
   * @return
   */
  @NotNull
  public Optional<PageTypeReference> resolvePageTypeReference(@NotNull XWikiDocument doc);

  /**
   * getDefaultPageTypeRefForDoc returns the PageTypeReference which is defined as default
   * PageType in the current location (docRef).
   *
   * @param docRef
   * @return
   */
  public PageTypeReference getDefaultPageTypeRefForDoc(DocumentReference docRef);

  /**
   * getPageTypeRefForDocWithDefault returns the PageTypeReference defined for the given
   * document reference. If no explicit page-type is defined it returns the default
   * PageTypeReference defined for the given document location.
   *
   * @param docRef
   * @return
   */
  @Nullable
  public PageTypeReference getPageTypeRefForDocWithDefault(@NotNull DocumentReference docRef);

  /**
   * getPageTypeRefForDocWithDefault returns the PageTypeReference defined for the given
   * document. If no explicit page-type is defined it returns the default
   * PageTypeReference defined for the given document location.
   *
   * @param doc
   * @return
   */
  @Nullable
  public PageTypeReference getPageTypeRefForDocWithDefault(@Nullable XWikiDocument doc);

  /**
   * getPageTypeRefForDocWithDefault returns the PageTypeReference defined for the given
   * document. If no explicit page-type is defined it returns the default
   * PageTypeReference given by the caller.
   *
   * @param doc
   * @param defaultPTRef
   * @return
   */
  public PageTypeReference getPageTypeRefForDocWithDefault(XWikiDocument doc,
      PageTypeReference defaultPTRef);

}
