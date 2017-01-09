package classes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassDefinition;
import com.xpn.xwiki.web.Utils;

public class PageTypeClassTest extends AbstractComponentTest {

  private ClassDefinition pageTypeClass;

  @Before
  public void prepareTest() throws Exception {
    pageTypeClass = Utils.getComponent(ClassDefinition.class, "PageTypeClass.CLASS_DEF_HINT");
  }

  @Test
  public void testGetName() {
    String expectedStr = "Celements2.PageType";
    assertEquals(expectedStr, pageTypeClass.getName());
  }

}
