package com.celements.navigation.factories;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.NavigationConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.xobject.XObjectPageTypeProvider;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class DefaultNavigationFactoryTest extends AbstractComponentTest {

  private DefaultNavigationFactory defNavFactory;
  private DocumentReference testDocRef;
  private DocumentReference currDocRef;
  private XWikiDocument curDoc;
  private DocumentReference pageTypeDocRef;
  private IPageTypeResolverRole pageTypeResolverMock;
  private DocumentReference defaultPageTypeDocRef;
  private NavigationFactory<DocumentReference> xobjNavFactoryMock;
  private NavigationFactory<DocumentReference> pageTypeNavFactoryMock;
  private NavigationFactory<PageTypeReference> javaNavFactoryMock;
  private PageTypeReference defaultPageTypeRef;
  private PageTypeReference pageTypeRef;

  @Before
  public void setUp_DefaultNavigationFactoryTest() throws Exception {
    @SuppressWarnings("unchecked")
    NavigationFactory<DocumentReference> newNavFactoryMock = registerComponentMock(
        NavigationFactory.class, XObjectNavigationFactory.XOBJECT_NAV_FACTORY_HINT);
    xobjNavFactoryMock = newNavFactoryMock;
    @SuppressWarnings("unchecked")
    NavigationFactory<DocumentReference> newNavFactoryMock2 = registerComponentMock(
        NavigationFactory.class, PageTypeNavigationFactory.PAGETYPE_NAV_FACTORY_HINT);
    pageTypeNavFactoryMock = newNavFactoryMock2;
    @SuppressWarnings("unchecked")
    NavigationFactory<PageTypeReference> newNavFactoryMock3 = registerComponentMock(
        NavigationFactory.class, JavaNavigationFactory.JAVA_NAV_FACTORY_HINT);
    javaNavFactoryMock = newNavFactoryMock3;
    pageTypeResolverMock = registerComponentMock(IPageTypeResolverRole.class);
    defaultPageTypeDocRef = new DocumentReference(getContext().getDatabase(), "PageTypes",
        "RichText");
    defaultPageTypeRef = new PageTypeReference(defaultPageTypeDocRef.getName(),
        XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER, Collections.<String>emptyList());
    pageTypeDocRef = new DocumentReference(getContext().getDatabase(), "PageTypes", "MyPageType");
    pageTypeRef = new PageTypeReference(pageTypeDocRef.getName(),
        XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER, Collections.<String>emptyList());
    testDocRef = new DocumentReference(getContext().getDatabase(), "MySpace", "MyTestDoc");
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(testDocRef))).andReturn(
        pageTypeRef).anyTimes();
    expect(pageTypeResolverMock.getPageTypeRefForCurrentDoc()).andReturn(
        defaultPageTypeRef).anyTimes();
    defNavFactory = (DefaultNavigationFactory) Utils.getComponent(NavigationFactory.class);
    currDocRef = new DocumentReference(getContext().getDatabase(), "mySpace", "myCurDoc");
    curDoc = new XWikiDocument(currDocRef);
    getContext().setDoc(curDoc);
  }

  @Test
  public void testGetDefaultConfigReference() {
    replayDefault();
    assertEquals(currDocRef, defNavFactory.getDefaultConfigReference());
    verifyDefault();
  }

  @Test
  public void testGetNavigationConfig_curDoc_defaults() {
    expect(javaNavFactoryMock.hasNavigationConfig(eq(defaultPageTypeRef))).andReturn(false);
    expect(pageTypeNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(false);
    expect(xobjNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(false);
    replayDefault();
    assertNotNull(defNavFactory.getNavigationConfig(currDocRef));
    verifyDefault();
  }

  @Test
  public void testGetNavigationConfig_curDoc_true() {
    expect(javaNavFactoryMock.hasNavigationConfig(eq(defaultPageTypeRef))).andReturn(true);
    expect(javaNavFactoryMock.getNavigationConfig(eq(defaultPageTypeRef))).andReturn(
        NavigationConfig.DEFAULTS);
    expect(pageTypeNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(true);
    expect(pageTypeNavFactoryMock.getNavigationConfig(eq(currDocRef))).andReturn(
        NavigationConfig.DEFAULTS);
    expect(xobjNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(true);
    expect(xobjNavFactoryMock.getNavigationConfig(eq(currDocRef))).andReturn(
        NavigationConfig.DEFAULTS);
    replayDefault();
    assertNotNull(defNavFactory.getNavigationConfig(currDocRef));
    verifyDefault();
  }

  @Test
  public void testGetNavigationConfig_testDoc() {
    expect(javaNavFactoryMock.hasNavigationConfig(eq(pageTypeRef))).andReturn(true);
    expect(javaNavFactoryMock.getNavigationConfig(eq(pageTypeRef))).andReturn(
        NavigationConfig.DEFAULTS);
    expect(pageTypeNavFactoryMock.hasNavigationConfig(eq(testDocRef))).andReturn(true);
    expect(pageTypeNavFactoryMock.getNavigationConfig(eq(testDocRef))).andReturn(
        NavigationConfig.DEFAULTS);
    expect(xobjNavFactoryMock.hasNavigationConfig(eq(testDocRef))).andReturn(true);
    expect(xobjNavFactoryMock.getNavigationConfig(eq(testDocRef))).andReturn(
        NavigationConfig.DEFAULTS);
    replayDefault();
    assertNotNull(defNavFactory.getNavigationConfig(testDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_curDoc_false() {
    expect(javaNavFactoryMock.hasNavigationConfig(eq(defaultPageTypeRef))).andReturn(false);
    expect(pageTypeNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(false);
    expect(xobjNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(false);
    replayDefault();
    assertFalse(defNavFactory.hasNavigationConfig(currDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_curDoc_java() {
    expect(javaNavFactoryMock.hasNavigationConfig(eq(defaultPageTypeRef))).andReturn(true);
    expect(pageTypeNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(false).anyTimes();
    expect(xobjNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(false).anyTimes();
    replayDefault();
    assertTrue(defNavFactory.hasNavigationConfig(currDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_curDoc_pageType() {
    expect(javaNavFactoryMock.hasNavigationConfig(eq(defaultPageTypeRef))).andReturn(
        false).anyTimes();
    expect(pageTypeNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(true);
    expect(xobjNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(false).anyTimes();
    replayDefault();
    assertTrue(defNavFactory.hasNavigationConfig(currDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_curDoc_xobj() {
    expect(javaNavFactoryMock.hasNavigationConfig(eq(defaultPageTypeRef))).andReturn(
        false).anyTimes();
    expect(pageTypeNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(false).anyTimes();
    expect(xobjNavFactoryMock.hasNavigationConfig(eq(currDocRef))).andReturn(true);
    replayDefault();
    assertTrue(defNavFactory.hasNavigationConfig(currDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_testDoc_false() {
    expect(javaNavFactoryMock.hasNavigationConfig(eq(pageTypeRef))).andReturn(false);
    expect(pageTypeNavFactoryMock.hasNavigationConfig(eq(testDocRef))).andReturn(false);
    expect(xobjNavFactoryMock.hasNavigationConfig(eq(testDocRef))).andReturn(false);
    replayDefault();
    assertFalse(defNavFactory.hasNavigationConfig(testDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_testDoc_java() {
    expect(javaNavFactoryMock.hasNavigationConfig(eq(pageTypeRef))).andReturn(true);
    expect(pageTypeNavFactoryMock.hasNavigationConfig(eq(testDocRef))).andReturn(false).anyTimes();
    expect(xobjNavFactoryMock.hasNavigationConfig(eq(testDocRef))).andReturn(false).anyTimes();
    replayDefault();
    assertTrue(defNavFactory.hasNavigationConfig(testDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_testDoc_pageType() {
    expect(javaNavFactoryMock.hasNavigationConfig(eq(pageTypeRef))).andReturn(false).anyTimes();
    expect(pageTypeNavFactoryMock.hasNavigationConfig(eq(testDocRef))).andReturn(true);
    expect(xobjNavFactoryMock.hasNavigationConfig(eq(testDocRef))).andReturn(false).anyTimes();
    replayDefault();
    assertTrue(defNavFactory.hasNavigationConfig(testDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_testDoc_xobj() {
    expect(javaNavFactoryMock.hasNavigationConfig(eq(pageTypeRef))).andReturn(false).anyTimes();
    expect(pageTypeNavFactoryMock.hasNavigationConfig(eq(testDocRef))).andReturn(false).anyTimes();
    expect(xobjNavFactoryMock.hasNavigationConfig(eq(testDocRef))).andReturn(true);
    replayDefault();
    assertTrue(defNavFactory.hasNavigationConfig(testDocRef));
    verifyDefault();
  }

}
