package com.celements.pagetype.classes;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.classes.XClassCreator;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.ClassDefinition;
import com.celements.pagetype.PageTypeClasses;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class PageTypePropertiesClassTest extends AbstractComponentTest {

  private PageTypePropertiesClass pageTypePropertiesClass;
  private PageTypeClasses pageTypeClassConfig;
  private XClassCreator xClassCreator;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void prepareTest() throws Exception {
    modelAccessMock = registerMockComponent(IModelAccessFacade.class);
    pageTypePropertiesClass = (PageTypePropertiesClass) Utils.getComponent(ClassDefinition.class,
        PageTypePropertiesClass.CLASS_DEF_HINT);
    pageTypeClassConfig = (PageTypeClasses) Utils.getComponent(IClassCollectionRole.class,
        "celements.celPageTypeClasses");
    xClassCreator = Utils.getComponent(XClassCreator.class);
  }

  @Test
  public void testGetName() {
    String expectedStr = "Celements2.PageTypePropertiesClass";
    assertEquals(expectedStr, pageTypePropertiesClass.getName());
  }

  @Test
  public void testGetClassSpaceName() {
    String expectedStr = "Celements2";
    assertEquals(expectedStr, pageTypePropertiesClass.getClassSpaceName());
  }

  @Test
  public void testGetClassDocName() {
    String expectedStr = "PageTypePropertiesClass";
    assertEquals(expectedStr, pageTypePropertiesClass.getClassDocName());
  }

  @Test
  public void test_isInternalMapping() {
    assertFalse(pageTypePropertiesClass.isInternalMapping());
  }

  @Test
  public void test_fields() throws Exception {
    DocumentReference classRef = pageTypePropertiesClass.getClassRef();
    XWikiDocument doc = new XWikiDocument(classRef);
    expect(modelAccessMock.getOrCreateDocument(eq(classRef))).andReturn(doc).once();
    modelAccessMock.saveDocument(same(doc), anyObject(String.class));
    expectLastCall().once();
    replayDefault();
    pageTypeClassConfig.getPageTypePropertiesClass();
    verifyDefault();

    assertEquals(doc.getClass(), xClassCreator.generateXClass(pageTypePropertiesClass));
  }
}
