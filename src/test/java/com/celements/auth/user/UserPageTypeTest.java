package com.celements.auth.user;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.IJavaPageTypeRole;
import com.xpn.xwiki.web.Utils;

public class UserPageTypeTest extends AbstractComponentTest {

  private UserPageType userPageType;

  @Before
  public void setUp_UserPageTypeTest() throws Exception {
    userPageType = (UserPageType) Utils.getComponent(IJavaPageTypeRole.class,
        UserPageType.PAGETYPE_NAME);
  }

  @Test
  public void test_getName() {
    replayDefault();
    assertEquals(UserPageType.PAGETYPE_NAME, userPageType.getName());
    verifyDefault();
  }

  @Test
  public void test_displayInFrameLayout() {
    replayDefault();
    assertTrue(userPageType.displayInFrameLayout());
    verifyDefault();
  }

  @Test
  public void test_getCategories() {
    replayDefault();
    Set<IPageTypeCategoryRole> categories = userPageType.getCategories();
    assertNotNull(categories);
    assertEquals(1, categories.size());
    assertEquals(Utils.getComponent(IPageTypeCategoryRole.class), categories.toArray()[0]);
    verifyDefault();
  }

  @Test
  public void test_hasPageTitle() {
    replayDefault();
    assertFalse(userPageType.hasPageTitle());
    verifyDefault();
  }

  @Test
  public void test_isUnconnectedParent() {
    replayDefault();
    assertFalse(userPageType.isUnconnectedParent());
    verifyDefault();
  }

  @Test
  public void test_isVisible() {
    replayDefault();
    assertTrue(userPageType.isVisible());
    verifyDefault();
  }

  @Test
  public void test_getViewTemplateName() {
    replayDefault();
    assertEquals(UserPageType.VIEW_TEMPLATE_NAME, userPageType
        .getViewTemplateName());
    verifyDefault();
  }

  @Test
  public void test_getEditTemplateName() {
    replayDefault();
    assertEquals(UserPageType.EDIT_TEMPLATE_NAME, userPageType
        .getEditTemplateName());
    verifyDefault();
  }

  @Test
  public void test_getRenderTemplateForRenderMode_view() {
    replayDefault();
    assertEquals(UserPageType.VIEW_TEMPLATE_NAME,
        userPageType.getRenderTemplateForRenderMode("view"));
    verifyDefault();
  }

  @Test
  public void test_getRenderTemplateForRenderMode_edit() {
    replayDefault();
    assertEquals(UserPageType.EDIT_TEMPLATE_NAME,
        userPageType.getRenderTemplateForRenderMode("edit"));
    verifyDefault();
  }

  @Test
  public void test_useInlineEditorMode() {
    replayDefault();
    assertTrue(userPageType.useInlineEditorMode());
    verifyDefault();
  }

}
