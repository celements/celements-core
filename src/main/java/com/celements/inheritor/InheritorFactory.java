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
package com.celements.inheritor;

import static com.celements.model.util.References.*;
import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.iterator.DocumentIterator;
import com.celements.iterator.XObjectIterator;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.parents.IDocumentParentsListerRole;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class InheritorFactory {

  private PageLayoutCommand injectedPageLayoutCmd;

  public FieldInheritor getFieldInheritor(ClassReference classRef,
      Iterable<DocumentReference> docRefs) {
    return getFieldInheritor(getModelUtils().serializeRef(classRef),
        FluentIterable.from(docRefs).transform(getModelUtils()::serializeRef).toList(),
        getContext().getXWikiContext());
  }

  /**
   * @deprecated instead use {@link #getFieldInheritor(ClassReference, Iterable)}
   * @since 3.2
   */
  @Deprecated
  public FieldInheritor getFieldInheritor(final String className, final List<String> docList,
      final XWikiContext context) {
    FieldInheritor inheritor = new FieldInheritor();
    inheritor.setIteratorFactory(() -> {
      XObjectIterator iterator = new XObjectIterator(context);
      iterator.setClassName(className);
      iterator.setDocList(docList);
      return iterator;
    });
    return inheritor;
  }

  public ContentInheritor getContentInheritor(final List<String> docList, XWikiContext context) {
    final XWikiContext localContext = context;
    ContentInheritor inheritor = new ContentInheritor();
    inheritor.setIteratorFactory(() -> {
      DocumentIterator iterator = new DocumentIterator(localContext);
      iterator.setDocList(docList);
      return iterator;
    });
    return inheritor;
  }

  public FieldInheritor getNavigationFieldInheritor(String className, String fullName,
      XWikiContext context) {
    DocumentReference docRef = getModelUtils().resolveRef(fullName, DocumentReference.class);
    List<DocumentReference> documentParents = getIDocumentParentsListerRole()
        .getDocumentParentsList(docRef, true);
    ClassReference classRef = new ClassReference(className);
    return getFieldInheritor(classRef, documentParents);
  }

  public FieldInheritor getConfigDocFieldInheritor(String className, String fullName,
      XWikiContext context) {
    List<String> inheritanceList = new ArrayList<>();
    inheritanceList.add(fullName.split("\\.")[0] + ".WebPreferences");
    inheritanceList.add("XWiki.XWikiPreferences");
    String pageLayoutForDoc = getPageLayoutCmd().getPageLayoutForDoc(fullName, context);
    if (pageLayoutForDoc != null) {
      inheritanceList.add(pageLayoutForDoc + ".WebHome");
    }
    String skinDocName = context.getWiki().getSpacePreference("skin", context);
    if (skinDocName != null) {
      inheritanceList.add(skinDocName);
    }
    return getFieldInheritor(className, inheritanceList, context);
  }

  public void injectPageLayoutCmd(PageLayoutCommand injectedPageLayoutCmd) {
    this.injectedPageLayoutCmd = injectedPageLayoutCmd;
  }

  private PageLayoutCommand getPageLayoutCmd() {
    if (injectedPageLayoutCmd != null) {
      return injectedPageLayoutCmd;
    }
    return new PageLayoutCommand();
  }

  public FieldInheritor getPageLayoutInheritor(String fullName, XWikiContext context) {
    return getFieldInheritor("Celements2.PageType", Arrays.asList(fullName,
        getSpacePreferencesFullName(fullName), "XWiki.XWikiPreferences"), context);
  }

  /**
   * @deprecated instead use {@link #getSpacePrefDocRef(EntityReference)}
   */
  @Deprecated
  String getSpacePreferencesFullName(String fullName) {
    return fullName.substring(0, fullName.indexOf('.')) + ".WebPreferences";
  }

  public FieldInheritor getConfigFieldInheritor(DocumentReference classDocRef,
      EntityReference reference) {
    return getConfigFieldInheritor(new ClassReference(classDocRef), reference);
  }

  public FieldInheritor getConfigFieldInheritor(ClassReference classRef,
      EntityReference reference) {
    checkArgument(isAbsoluteRef(reference));
    Iterable<DocumentReference> docRefs = FluentIterable.of(extractRef(reference,
        DocumentReference.class).orNull(), getSpacePrefDocRef(reference), getXWikiPrefDocRef(
            reference))
        .filter(Predicates.notNull());
    return getFieldInheritor(classRef, docRefs);
  }

  DocumentReference getSpacePrefDocRef(EntityReference reference) {
    Optional<SpaceReference> spaceRef = extractRef(reference, SpaceReference.class);
    if (spaceRef.isPresent()) {
      return create(DocumentReference.class, "WebPreferences", spaceRef.get());
    }
    return null;
  }

  private DocumentReference getXWikiPrefDocRef(EntityReference reference) {
    Optional<WikiReference> wikiRef = extractRef(reference, WikiReference.class);
    if (wikiRef.isPresent()) {
      return create(DocumentReference.class, "XWikiPreferences", create(SpaceReference.class,
          "XWiki", wikiRef.get()));
    }
    return null;
  }

  private ModelContext getContext() {
    return Utils.getComponent(ModelContext.class);
  }

  private ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

  private IDocumentParentsListerRole getIDocumentParentsListerRole() {
    return Utils.getComponent(IDocumentParentsListerRole.class);
  }

}
