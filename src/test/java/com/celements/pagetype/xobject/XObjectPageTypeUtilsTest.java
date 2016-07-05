package com.celements.pagetype.xobject;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.PageTypeReference;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeUtilsTest extends AbstractComponentTest {

  private XObjectPageTypeUtils pageTypeUtils;

  @Before
  public void setUp_XObjectPageTypeUtilsTest() throws Exception {
    pageTypeUtils = (XObjectPageTypeUtils) Utils.getComponent(XObjectPageTypeUtilsRole.class);
  }

  @Test
  public void testGetDocRefForPageType_String() {
    DocumentReference pageTypeDocRef = new DocumentReference(getContext().getDatabase(),
        "PageTypes", "myPageType");
    assertEquals(pageTypeDocRef, pageTypeUtils.getDocRefForPageType("myPageType"));
  }

  @Test
  public void testGetDocRefForPageType_PageTypeReference() {
    DocumentReference pageTypeDocRef = new DocumentReference(getContext().getDatabase(),
        "PageTypes", "myPageType2");
    PageTypeReference pageTypeRef = new PageTypeReference("myPageType2",
        XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER, Collections.<String>emptyList());
    assertEquals(pageTypeDocRef, pageTypeUtils.getDocRefForPageType(pageTypeRef));
  }

}
