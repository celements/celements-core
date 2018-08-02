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
import org.xwiki.model.reference.EntityReference;

import com.celements.pagetype.PageTypeReference;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface IPageTypeResolverRole {

  /**
   * @deprecated use {@link #resolvePageTypeRefForCurrentDoc()}
   * @since 3.2
   */
  @Deprecated
  PageTypeReference getPageTypeRefForCurrentDoc();

  @NotNull
  PageTypeReference resolvePageTypeRefForCurrentDoc();

  /**
   * getPageTypeObject returns the page-type object attached to the given doc. If the
   * doc isNew and thus has no objects it falls back to the wikiTemplateDoc in case
   * one is defined in the request.
   *
   * @deprecated
   * @since 3.1
   * @param docRef
   * @return page-type object or null if non is present
   */
  @Deprecated
  BaseObject getPageTypeObject(XWikiDocument docRef);

  /**
   * @deprecated instead use {@link #resolvePageTypeReference(XWikiDocument)}
   * @since 3.1
   */
  @Nullable
  @Deprecated
  PageTypeReference getPageTypeRefForDoc(@Nullable XWikiDocument doc);

  /**
   * gets the {@link PageTypeReference} defined by the PageType-xobject attached to
   * the given document.
   *
   * @param doc
   * @return
   */
  @NotNull
  Optional<PageTypeReference> resolvePageTypeReference(@NotNull XWikiDocument doc);

  /**
   * getDefaultPageTypeRefForDoc returns the PageTypeReference which is defined as default
   * PageType in the current location (docRef).
   *
   * @deprecated instead use {@link #resolveDefaultPageTypeReference(EntityReference)}
   * @since 3.2
   * @param docRef
   * @return
   */
  @Deprecated
  @NotNull
  PageTypeReference getDefaultPageTypeRefForDoc(@NotNull DocumentReference docRef);

  /**
   * returns the default PageType for the provided reference (doc, space or wiki)
   *
   * @param reference
   * @return
   */
  @NotNull
  PageTypeReference resolveDefaultPageTypeReference(@Nullable EntityReference reference);

  /**
   * getPageTypeRefForDocWithDefault returns the PageTypeReference defined for the given
   * document reference. If no explicit page-type is defined it returns the default
   * PageTypeReference defined for the given document location.
   *
   * @deprecated instead use {@link #resolvePageTypeReferenceWithDefault(DocumentReference)}
   * @since 3.2
   * @param docRef
   * @return
   */
  @NotNull
  @Deprecated
  PageTypeReference getPageTypeRefForDocWithDefault(@NotNull DocumentReference docRef);

  /**
   * like {@link #resolvePageTypeReferenceWithDefault(XWikiDocument)}, but for a document reference.
   */
  @NotNull
  PageTypeReference resolvePageTypeReferenceWithDefault(@Nullable DocumentReference docRef);

  /**
   * getPageTypeRefForDocWithDefault returns the PageTypeReference defined for the given
   * document. If no explicit page-type is defined it returns the default
   * PageTypeReference defined for the given document location.
   *
   * @deprecated instead use {@link #resolvePageTypeReferenceWithDefault(XWikiDocument)}
   * @since 3.2
   * @param doc
   * @return
   */
  @Nullable
  @Deprecated
  PageTypeReference getPageTypeRefForDocWithDefault(@Nullable XWikiDocument doc);

  /**
   * getPageTypeRefForDocWithDefault returns the PageTypeReference defined for the given
   * document. If no explicit page-type is defined it returns the default
   * PageTypeReference given by the caller.
   *
   * @deprecated instead use {@link #resolvePageTypeReference(XWikiDocument)} with
   *             {@link Optional#or(Object)}
   * @since 3.2
   */
  @Nullable
  @Deprecated
  PageTypeReference getPageTypeRefForDocWithDefault(@Nullable XWikiDocument doc,
      @Nullable PageTypeReference defaultPTRef);

  /**
   * like {@link #resolvePageTypeReference(XWikiDocument)}, but returns the default
   * PageTypeReference defined for the given document location if no explicit PageType is defined
   */
  @NotNull
  PageTypeReference resolvePageTypeReferenceWithDefault(@Nullable XWikiDocument doc);

}
