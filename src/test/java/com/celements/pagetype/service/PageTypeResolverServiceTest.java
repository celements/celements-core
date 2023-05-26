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

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.pagetype.classes.PageTypeClass.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.classes.PageTypeClass;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class PageTypeResolverServiceTest extends AbstractComponentTest {

  private XWikiRequest request;
  private PageTypeResolverService pageTypeResolver;
  private IPageTypeRole pageTypeServiceMock;
  private IModelAccessFacade modelStrategyMock;
  private PageTypeReference richTextPTref;
  private DocumentReference docRef;
  private XWikiDocument doc;
  private DocumentReference webPrefDocRef;
  private DocumentReference xwikiPrefDocRef;
  private XWikiDocument webPrefDoc;
  private XWikiDocument xwikiPrefDoc;

  @Before
  public void prepareTest() throws Exception {
    request = createDefaultMock(XWikiRequest.class);
    getContext().setRequest(request);
    modelStrategyMock = registerComponentMock(IModelAccessFacade.class);
    pageTypeServiceMock = registerComponentMock(IPageTypeRole.class);
    richTextPTref = new PageTypeReference("RichText", "xObjectProvider", Arrays.asList(""));
    expect(pageTypeServiceMock.getPageTypeReference(eq("RichText"))).andReturn(Optional.of(
        richTextPTref)).anyTimes();
    expect(pageTypeServiceMock.getPageTypeReference("")).andReturn(
        Optional.<PageTypeReference>absent()).anyTimes();
    docRef = new DocumentReference(getContext().getDatabase(), "MySpace", "MyDocument");
    doc = new XWikiDocument(docRef);
    webPrefDocRef = new DocumentReference(getContext().getDatabase(),
        docRef.getLastSpaceReference().getName(), "WebPreferences");
    webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(getWikiMock().getDocument(eq(webPrefDocRef), same(getContext()))).andReturn(
        webPrefDoc).anyTimes();
    xwikiPrefDocRef = new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiPreferences");
    xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(getWikiMock().getDocument(eq(xwikiPrefDocRef), same(getContext()))).andReturn(
        xwikiPrefDoc).anyTimes();
    pageTypeResolver = (PageTypeResolverService) Utils.getComponent(IPageTypeResolverRole.class);
  }

  @Test
  public void test_getPageTypeObject_NPEs_null_doc() throws Exception {
    replayDefault();
    BaseObject resultPTObj = pageTypeResolver.getPageTypeObject(null);
    verifyDefault();
    assertNull(resultPTObj);
  }

  @Test
  public void test_getPageTypeObject_NewDocFromTemplate() throws Exception {
    doc.setNew(true);
    getContext().setDoc(doc);
    DocumentReference templDocRef = new DocumentReference(getContext().getDatabase(), "Blog",
        "ArticleTemplate");
    XWikiDocument templDoc = expectDoc(templDocRef);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef(getContext().getDatabase()));
    templDoc.addXObject(pageTypeObj);
    expect(request.get(eq("template"))).andReturn("Blog.ArticleTemplate").atLeastOnce();
    replayDefault();
    BaseObject resultPTObj = pageTypeResolver.getPageTypeObject(doc);
    verifyDefault();
    assertNotNull(resultPTObj);
    assertNotSame("clone should be returned", pageTypeObj, resultPTObj);
    assertEquals(pageTypeObj, resultPTObj);
  }

  @Test
  public void test_getPageTypeObject_cellFromCentralDB() throws Exception {
    doc.setNew(true);
    getContext().setDoc(doc);
    DocumentReference centralCellDocRef = new DocumentReference("celements2web", "SimpleLayout",
        "cell1");
    XWikiDocument centralCellDoc = new XWikiDocument(centralCellDocRef);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef("celements2web"));
    centralCellDoc.addXObject(pageTypeObj);
    replayDefault();
    BaseObject resultPTObj = pageTypeResolver.getPageTypeObject(centralCellDoc);
    verifyDefault();
    assertNotNull("no page type object found. Maybe class ref has the wrong wiki" + " reference?",
        resultPTObj);
    assertNotSame("clone should be returned", pageTypeObj, resultPTObj);
    assertEquals(pageTypeObj, resultPTObj);
  }

  @Test
  public void test_resolvePageTypeRefForCurrentDoc_noCurrentDoc() throws Exception {
    expect(request.get(eq("template"))).andReturn(null).atLeastOnce();
    replayDefault();
    PageTypeReference pageTypeRef = pageTypeResolver.resolvePageTypeRefForCurrentDoc();
    verifyDefault();
    assertEquals(richTextPTref, pageTypeRef);
  }

  @Test
  public void test_resolvePageTypeRefForCurrentDoc_Default() throws Exception {
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc);
    getContext().setDoc(doc);
    expect(request.get(eq("template"))).andReturn(null).atLeastOnce();
    replayDefault();
    // No PageType Object prepared -> Default PageType is RichText
    PageTypeReference pageTypeRef = pageTypeResolver.resolvePageTypeRefForCurrentDoc();
    verifyDefault();
    assertEquals(richTextPTref, pageTypeRef);
  }

  @Test
  public void test_resolvePageTypeRefForCurrentDoc_createNewWithTemplate() throws Exception {
    String templateSpace = "Tmpl";
    String templateName = "TheTemplate";
    DocumentReference templateDocRef = new DocumentReference(getContext().getDatabase(),
        templateSpace, templateName);
    expect(request.get(eq("template"))).andReturn(templateSpace + "." + templateName);
    XWikiDocument templateDoc = expectDoc(templateDocRef);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef(getContext().getDatabase()));
    pageTypeObj.setStringValue("page_type", "MyPageType");
    templateDoc.addXObject(pageTypeObj);
    PageTypeReference myPTref = new PageTypeReference("MyPageType", "xObjectProvider",
        Arrays.asList(""));
    expect(pageTypeServiceMock.getPageTypeReference("MyPageType")).andReturn(Optional.of(myPTref));
    replayDefault();
    PageTypeReference pageTypeRef = pageTypeResolver.resolvePageTypeRefForCurrentDoc();
    verifyDefault();
    assertEquals(myPTref, pageTypeRef);
  }

  @Test
  public void test_resolvePageTypeReferenceWithDefault_docRef() throws Exception {
    expectDoc(doc);
    doc.setNew(false);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef(getContext().getDatabase()));
    pageTypeObj.setStringValue("page_type", "MyPageType");
    doc.addXObject(pageTypeObj);
    PageTypeReference myPTref = new PageTypeReference("MyPageType", "xObjectProvider",
        Arrays.asList(""));
    expect(pageTypeServiceMock.getPageTypeReference(eq("MyPageType"))).andReturn(Optional.of(
        myPTref));
    replayDefault();
    // No PageType Object prepared -> Default PageType is RichText
    PageTypeReference pageTypeRef = pageTypeResolver.resolvePageTypeReferenceWithDefault(docRef);
    verifyDefault();
    assertEquals(myPTref, pageTypeRef);
  }

  @Test
  public void test_resolvePageTypeReferenceWithDefault_docRef_Exception() throws Exception {
    expectDoc(docRef);
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andThrow(
        new XWikiException());
    replayDefault();
    // No PageType Object prepared -> Default PageType is RichText
    PageTypeReference pageTypeRef = pageTypeResolver.resolvePageTypeReferenceWithDefault(docRef);
    verifyDefault();
    assertEquals(richTextPTref, pageTypeRef);
  }

  @Test
  public void test_resolvePageTypeReferenceWithDefault_null_doc() throws Exception {
    replayDefault();
    // No PageType Object prepared -> Default PageType is RichText
    PageTypeReference pageTypeRef = pageTypeResolver.resolvePageTypeReferenceWithDefault(
        (XWikiDocument) null);
    verifyDefault();
    assertEquals(richTextPTref, pageTypeRef);
  }

  @Test
  public void test_resolvePageTypeReferenceWithDefault_Default() throws Exception {
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc);
    replayDefault();
    // No PageType Object prepared -> Default PageType is RichText
    PageTypeReference pageTypeRef = pageTypeResolver.resolvePageTypeReferenceWithDefault(doc);
    verifyDefault();
    assertEquals(richTextPTref, pageTypeRef);
  }

  @Test
  public void test_resolvePageTypeReferenceWithDefault_MyPageType() throws Exception {
    doc.setNew(false);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef(getContext().getDatabase()));
    pageTypeObj.setStringValue("page_type", "MyPageType");
    doc.addXObject(pageTypeObj);
    PageTypeReference myPTref = new PageTypeReference("MyPageType", "xObjectProvider",
        Arrays.asList(""));
    expect(pageTypeServiceMock.getPageTypeReference("MyPageType")).andReturn(Optional.of(myPTref));
    replayDefault();
    PageTypeReference pageTypeRef = pageTypeResolver.resolvePageTypeReferenceWithDefault(doc);
    verifyDefault();
    assertNotNull(pageTypeRef);
    assertEquals(myPTref, pageTypeRef);
  }

  @Test
  public void test_resolvePageTypeReferenceWithDefault_NewDocFromTemplate() throws Exception {
    doc.setNew(true);
    getContext().setDoc(doc);
    DocumentReference templDocRef = new DocumentReference(getContext().getDatabase(), "Blog",
        "ArticleTemplate");
    XWikiDocument templDoc = expectDoc(templDocRef);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef(getContext().getDatabase()));
    pageTypeObj.setStringValue("page_type", "Article");
    templDoc.addXObject(pageTypeObj);
    expect(request.get(eq("template"))).andReturn("Blog.ArticleTemplate").atLeastOnce();
    PageTypeReference articlePTref = new PageTypeReference("Article", "xObjectProvider",
        Arrays.asList(""));
    expect(pageTypeServiceMock.getPageTypeReference("Article")).andReturn(Optional.of(
        articlePTref));
    replayDefault();
    PageTypeReference pageTypeRef = pageTypeResolver.resolvePageTypeReferenceWithDefault(doc);
    verifyDefault();
    assertEquals("Expecting 'Article' PageType.", articlePTref, pageTypeRef);
  }

  @Test
  public void test_getPageTypeRefForDocWithDefault_nullAsDefault() {
    replayDefault();
    assertNull(pageTypeResolver.getPageTypeRefForDocWithDefault(doc, null));
    verifyDefault();
  }

  @Test
  public void test_resolveDefaultPageTypeReference_default() throws Exception {
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc);
    replayDefault();
    PageTypeReference pageTypeRef = pageTypeResolver.resolveDefaultPageTypeReference(docRef);
    verifyDefault();
    assertEquals(richTextPTref, pageTypeRef);
  }

  @Test
  public void test_resolveDefaultPageTypeReference_WebPrefs() throws Exception {
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    BaseObject webPtObj = new BaseObject();
    webPtObj.setXClassReference(getPageTypeClassRef(getContext().getDatabase()));
    String webPrefPageTypeName = "myWebPrefDefPageType";
    webPtObj.setStringValue(FIELD_PAGE_TYPE.getName(), webPrefPageTypeName);
    webPrefDoc.addXObject(webPtObj);
    BaseObject xwikiPtObj = new BaseObject();
    xwikiPtObj.setXClassReference(getPageTypeClassRef(getContext().getDatabase()));
    xwikiPtObj.setStringValue(FIELD_PAGE_TYPE.getName(), "myXWikiPrefDefPageType");
    xwikiPrefDoc.addXObject(xwikiPtObj);
    expect(pageTypeServiceMock.getPageTypeReference(eq(webPrefPageTypeName))).andReturn(Optional.of(
        createPTRef(webPrefPageTypeName)));
    replayDefault();
    PageTypeReference pageTypeRef = pageTypeResolver.resolveDefaultPageTypeReference(docRef);
    verifyDefault();
    assertEquals("myWebPrefDefPageType", pageTypeRef.getConfigName());
  }

  @Test
  public void test_resolveDefaultPageTypeReference_XWikiPrefs() throws Exception {
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    BaseObject xwikiPtObj = new BaseObject();
    xwikiPtObj.setXClassReference(getPageTypeClassRef(getContext().getDatabase()));
    String xwikiPrefPageTypeName = "myXWikiPrefDefPageType";
    xwikiPtObj.setStringValue(FIELD_PAGE_TYPE.getName(), xwikiPrefPageTypeName);
    xwikiPrefDoc.addXObject(xwikiPtObj);
    expect(pageTypeServiceMock.getPageTypeReference(eq(xwikiPrefPageTypeName))).andReturn(
        Optional.of(createPTRef(xwikiPrefPageTypeName)));
    replayDefault();
    PageTypeReference pageTypeRef = pageTypeResolver.resolveDefaultPageTypeReference(docRef);
    verifyDefault();
    assertEquals("myXWikiPrefDefPageType", pageTypeRef.getConfigName());
  }

  @Test
  public void test_resolvePageTypeReference_translationDoc() throws Exception {
    doc.setNew(false);
    doc.setTranslation(1);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef(getContext().getDatabase()));
    pageTypeObj.setStringValue("page_type", "MyPageType");
    doc.addXObject(pageTypeObj);
    replayDefault();
    Optional<PageTypeReference> pageTypeRef = pageTypeResolver.resolvePageTypeReference(doc);
    verifyDefault();
    assertFalse(pageTypeRef.isPresent());
  }

  private XWikiDocument expectDoc(DocumentReference docRef) throws DocumentNotExistsException {
    XWikiDocument doc = new XWikiDocument(docRef);
    return expectDoc(doc);
  }

  private XWikiDocument expectDoc(XWikiDocument doc) throws DocumentNotExistsException {
    doc.setNew(false);
    expect(modelStrategyMock.exists(doc.getDocumentReference())).andReturn(true).anyTimes();
    expect(modelStrategyMock.getDocument(doc.getDocumentReference())).andReturn(doc);
    return doc;
  }

  private PageTypeReference createPTRef(String name) {
    return new PageTypeReference(name, null, Collections.<String>emptyList());
  }

  private ClassReference getPageTypeClassRef(String wikiName) {
    return Utils.getComponent(ClassDefinition.class,
        PageTypeClass.CLASS_DEF_HINT).getClassReference();
  }

}
