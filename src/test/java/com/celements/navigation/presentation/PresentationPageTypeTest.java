package com.celements.navigation.presentation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.IJavaPageTypeRole;
import com.xpn.xwiki.web.Utils;

public class PresentationPageTypeTest extends AbstractComponentTest {

  private PresentationPageType presentationPageType;

  @Before
  public void setUp_PresentationPageTypeTest() throws Exception {
    presentationPageType = (PresentationPageType) Utils.getComponent(IJavaPageTypeRole.class,
        PresentationPageType.PAGETYPE_NAME);
  }

  @Test
  public void test_getName() {
    replayDefault();
    assertEquals(PresentationPageType.PAGETYPE_NAME, presentationPageType.getName());
    verifyDefault();
  }

  @Test
  public void test_displayInFrameLayout() {
    replayDefault();
    assertTrue(presentationPageType.displayInFrameLayout());
    verifyDefault();
  }

  @Test
  public void test_getCategories() {
    replayDefault();
    Set<IPageTypeCategoryRole> categories = presentationPageType.getCategories();
    assertNotNull(categories);
    assertEquals(1, categories.size());
    assertEquals(Utils.getComponent(IPageTypeCategoryRole.class), categories.toArray()[0]);
    verifyDefault();
  }

  @Test
  public void test_hasPageTitle() {
    replayDefault();
    assertFalse(presentationPageType.hasPageTitle());
    verifyDefault();
  }

  @Test
  public void test_isUnconnectedParent() {
    replayDefault();
    assertFalse(presentationPageType.isUnconnectedParent());
    verifyDefault();
  }

  @Test
  public void test_isVisible() {
    replayDefault();
    assertTrue(presentationPageType.isVisible());
    verifyDefault();
  }

  @Test
  public void test_getViewTemplateName() {
    replayDefault();
    assertEquals(PresentationPageType.VIEW_TEMPLATE_NAME, presentationPageType
        .getViewTemplateName());
    verifyDefault();
  }

  @Test
  public void test_getEditTemplateName() {
    replayDefault();
    assertEquals(PresentationPageType.EDIT_TEMPLATE_NAME, presentationPageType
        .getEditTemplateName());
    verifyDefault();
  }

  @Test
  public void test_getRenderTemplateForRenderMode_view() {
    replayDefault();
    assertEquals(PresentationPageType.VIEW_TEMPLATE_NAME,
        presentationPageType.getRenderTemplateForRenderMode("view"));
    verifyDefault();
  }

  @Test
  public void test_getRenderTemplateForRenderMode_edit() {
    replayDefault();
    assertEquals(PresentationPageType.EDIT_TEMPLATE_NAME,
        presentationPageType.getRenderTemplateForRenderMode("edit"));
    verifyDefault();
  }

}
