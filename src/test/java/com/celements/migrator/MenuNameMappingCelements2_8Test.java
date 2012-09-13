package com.celements.migrator;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

public class MenuNameMappingCelements2_8Test extends AbstractBridgedComponentTestCase {

  private MenuNameMappingCelements2_8 migrator;
  private XWikiContext context;
  private XWiki xwiki;

  @Before
  public void setUp_MenuNameMappingCelements2_8Test() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    migrator = new MenuNameMappingCelements2_8();
  }

  @Test
  public void testGetNavigationClasses() {
    replayAll();
    assertNotNull(migrator.getNavigationClasses());
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki);
    verify(mocks);
  }

}
