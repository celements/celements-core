package com.celements.pagetype.service;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.classcollection.PageTypeClassCollection;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class PageTypeResolverServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private XWikiRequest request;
  private PageTypeResolverService pageTypeResolver;
  private IPageTypeRole pageTypeServiceMock;
  private PageTypeReference richTextPTref;
  private DocumentReference docRef;
  private XWikiDocument doc;

  @Before
  public void setUp_PageTypeResolverServiceTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    request = createMock(XWikiRequest.class);
    context.setRequest(request);
    pageTypeResolver = new PageTypeResolverService();
    pageTypeServiceMock = createMock(IPageTypeRole.class);
    pageTypeResolver.pageTypeService = pageTypeServiceMock;
    pageTypeResolver.webUtilsService = Utils.getComponent(IWebUtilsService.class);
    pageTypeResolver.execution = Utils.getComponent(Execution.class);
    pageTypeResolver.pageTypeClasses = Utils.getComponent(IClassCollectionRole.class,
        "com.celements.pagetype.classcollection");
    richTextPTref = new PageTypeReference("RichText", "xObjectProvider",
        Arrays.asList(""));
    expect(pageTypeServiceMock.getPageTypeRefByConfigName(eq("RichText"))).andReturn(
        richTextPTref).anyTimes();
    docRef = new DocumentReference(context.getDatabase(), "MySpace", "MyDocument");
    doc = new XWikiDocument(docRef);
  }

  @Test
  public void testGetPageTypeObject_NewDocFromTemplate() throws XWikiException {
    doc.setNew(true);
    DocumentReference templDocRef = new DocumentReference(context.getDatabase(), "Blog",
        "ArticleTemplate");
    XWikiDocument templDoc = new XWikiDocument(templDocRef);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef(context.getDatabase()));
    templDoc.addXObject(pageTypeObj);
    expect(xwiki.exists(eq(templDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(templDocRef), same(context))).andReturn(templDoc
        ).atLeastOnce();
    expect(request.get(eq("template"))).andReturn("Blog.ArticleTemplate").anyTimes();
    replayAll();
    BaseObject resultPTObj = pageTypeResolver.getPageTypeObject(doc);
    verifyAll();
    assertNotNull(resultPTObj);
    assertSame(pageTypeObj, resultPTObj);
  }

  @Test
  public void testGetPageTypeObject_cellFromCentralDB() throws XWikiException {
    doc.setNew(true);
    DocumentReference centralCellDocRef = new DocumentReference("celements2web",
        "SimpleLayout", "cell1");
    XWikiDocument centralCellDoc = new XWikiDocument(centralCellDocRef);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef("celements2web"));
    centralCellDoc.addXObject(pageTypeObj);
    expect(request.get(eq("template"))).andReturn(null).anyTimes();
    replayAll();
    BaseObject resultPTObj = pageTypeResolver.getPageTypeObject(centralCellDoc);
    verifyAll();
    assertNotNull("no page type object found. Maybe class ref has the wrong wiki"
        + " reference?", resultPTObj);
    assertSame(pageTypeObj, resultPTObj);
  }

  @Test
  public void testGetPageTypeRefForCurrentDoc_Default() throws Exception {
    context.setDoc(doc);
    expect(request.get(eq("template"))).andReturn(null).anyTimes();
    replayAll();
    //No PageType Object prepared -> Default PageType is RichText
    PageTypeReference pageTypeRef = pageTypeResolver.getPageTypeRefForCurrentDoc();
    assertEquals(richTextPTref, pageTypeRef);
    verifyAll();
  }

  @Test
  public void testGetPageTypeRefForDocWithDefault_docRef() throws Exception {
    expect(request.get(eq("template"))).andReturn(null).anyTimes();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).anyTimes();
    doc.setNew(false);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef(context.getDatabase()));
    pageTypeObj.setStringValue("page_type", "MyPageType");
    doc.addXObject(pageTypeObj);
    PageTypeReference myPTref = new PageTypeReference("MyPageType", "xObjectProvider",
        Arrays.asList(""));;
    expect(pageTypeServiceMock.getPageTypeRefByConfigName(eq("MyPageType"))).andReturn(
        myPTref).anyTimes();
    replayAll();
    //No PageType Object prepared -> Default PageType is RichText
    PageTypeReference pageTypeRef = pageTypeResolver.getPageTypeRefForDocWithDefault(
        docRef);
    assertEquals(myPTref, pageTypeRef);
    verifyAll();
  }

  @Test
  public void testGetPageTypeRefForDocWithDefault_docRef_Exception() throws Exception {
    expect(request.get(eq("template"))).andReturn(null).anyTimes();
    expect(xwiki.getDocument(eq(docRef), same(context))).andThrow(
        new XWikiException());
    replayAll();
    //No PageType Object prepared -> Default PageType is RichText
    PageTypeReference pageTypeRef = pageTypeResolver.getPageTypeRefForDocWithDefault(
        docRef);
    assertEquals(richTextPTref, pageTypeRef);
    verifyAll();
  }

  @Test
  public void testGetPageTypeRefForDocWithDefault_Default() throws Exception {
    expect(request.get(eq("template"))).andReturn(null).anyTimes();
    replayAll();
    //No PageType Object prepared -> Default PageType is RichText
    PageTypeReference pageTypeRef = pageTypeResolver.getPageTypeRefForDocWithDefault(doc);
    assertEquals(richTextPTref, pageTypeRef);
    verifyAll();
  }

  @Test
  public void testGetPageTypeRefForDocWithDefault_MyPageType() throws Exception {
    doc.setNew(false);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef(context.getDatabase()));
    pageTypeObj.setStringValue("page_type", "MyPageType");
    doc.addXObject(pageTypeObj);
    PageTypeReference myPTref = new PageTypeReference("MyPageType", "xObjectProvider",
        Arrays.asList(""));;
    expect(pageTypeServiceMock.getPageTypeRefByConfigName(eq("MyPageType"))).andReturn(
        myPTref).anyTimes();
    replayAll();
    PageTypeReference pageTypeRef = pageTypeResolver.getPageTypeRefForDocWithDefault(doc);
    assertNotNull(pageTypeRef);
    assertEquals(myPTref, pageTypeRef);
    verifyAll();
  }

  private DocumentReference getPageTypeClassRef(String wikiName) {
    return new DocumentReference(wikiName, PageTypeClassCollection.PAGE_TYPE_CLASS_SPACE,
        PageTypeClassCollection.PAGE_TYPE_CLASS_DOC);
  }

  @Test
  public void testGetPageTypeRefForDocWithDefault_NewDocFromTemplate(
      ) throws XWikiException {
    doc.setNew(true);
    DocumentReference templDocRef = new DocumentReference(context.getDatabase(), "Blog",
        "ArticleTemplate");
    XWikiDocument templDoc = new XWikiDocument(templDocRef);
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setXClassReference(getPageTypeClassRef(context.getDatabase()));
    pageTypeObj.setStringValue("page_type", "Article");
    templDoc.addXObject(pageTypeObj);
    expect(xwiki.exists(eq(templDocRef), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(templDocRef), same(context))).andReturn(templDoc
        ).anyTimes();
    expect(request.get(eq("template"))).andReturn("Blog.ArticleTemplate").anyTimes();
    PageTypeReference articlePTref = new PageTypeReference("Article", "xObjectProvider",
        Arrays.asList(""));
    expect(pageTypeServiceMock.getPageTypeRefByConfigName(eq("Article"))).andReturn(
        articlePTref).anyTimes();
    replayAll();
    PageTypeReference pageTypeRef = pageTypeResolver.getPageTypeRefForDocWithDefault(doc);
    assertEquals("Expecting 'Article' PageType.", articlePTref, pageTypeRef);
    verifyAll();
  }

  @Test
  public void testGetPageTypeWithDefault_nullAsDefault() {
    assertNull(pageTypeResolver.getPageTypeRefForDocWithDefault(doc, null));
  }



  private void replayAll(Object ... mocks) {
    replay(xwiki, request, pageTypeServiceMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, request, pageTypeServiceMock);
    verify(mocks);
  }
}
