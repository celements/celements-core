package com.celements.parents;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.pagetype.IPageTypeConfig;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * This service collects all parents, connected and unconnected, for the provided document
 * and returns them as a list from bottom up.<br>
 * <br>
 * Primarily, the connected ({@link XWikiDocument}) parents are added to the result.<br>
 * <br>
 * Secondarily, implementations of {@link IDocParentProviderRole} are asked to provide
 * further parents for the top connected parent. However, these unconnected parents are
 * only added to the result if the corresponding
 * {@link IPageTypeConfig#isUnconnectedParent()} returns true.<br>
 * <br>
 * Found top parents are repeatedly checked for it's own connected and unconnected parents
 * until no parent whatsoever is to be found.
 * 
 * @author Marc Sladek
 */
@ComponentRole
public interface IDocumentParentsListerRole {

  /**
   * @param docRef
   *          the docRef to get parents for
   * @param includeDoc
   *          if true, includes docRef as first element in list
   * @return parents of docRef from bottom up
   */
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef,
      boolean includeDoc);

}
