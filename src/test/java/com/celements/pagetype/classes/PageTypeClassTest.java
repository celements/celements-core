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

public class PageTypeClassTest extends AbstractComponentTest {

  private PageTypeClass pageTypeClass;
  private PageTypeClasses pageTypeClassConfig;
  private XClassCreator xClassCreator;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void prepareTest() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    pageTypeClass = (PageTypeClass) Utils.getComponent(ClassDefinition.class,
        PageTypeClass.CLASS_DEF_HINT);
    pageTypeClassConfig = (PageTypeClasses) Utils.getComponent(IClassCollectionRole.class,
        "celements.celPageTypeClasses");
    xClassCreator = Utils.getComponent(XClassCreator.class);
  }

  @Test
  public void testGetName() {
    String expectedStr = "Celements2.PageType";
    assertEquals(expectedStr, pageTypeClass.getName());
  }

  @Test
  public void testGetClassSpaceName() {
    String expectedStr = "Celements2";
    assertEquals(expectedStr, pageTypeClass.getClassSpaceName());
  }

  @Test
  public void testGetClassDocName() {
    String expectedStr = "PageType";
    assertEquals(expectedStr, pageTypeClass.getClassDocName());
  }

  @Test
  public void test_isInternalMapping() {
    assertFalse(pageTypeClass.isInternalMapping());
  }

  @Test
  public void test_fields() throws Exception {
    DocumentReference pageTypeClassRef = pageTypeClass.getClassRef();

    XWikiDocument doc = new XWikiDocument(pageTypeClassRef);
    expect(modelAccessMock.getOrCreateDocument(eq(pageTypeClassRef))).andReturn(doc).once();
    modelAccessMock.saveDocument(same(doc), anyObject(String.class));
    expectLastCall().once();
    replayDefault();
    pageTypeClassConfig.getPageTypeClass();
    verifyDefault();

    assertEquals(doc.getXClass(), xClassCreator.generateXClass(pageTypeClass));
  }
}
