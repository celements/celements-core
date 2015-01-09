package com.celements.pagetype.xobject;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.observation.EventListener;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeCacheListenerTest extends AbstractBridgedComponentTestCase {
  
  private static final String _COMPONENT_NAME = "XObjectPageTypeCacheListener";
  private XObjectPageTypeCacheListener eventListener;

  @Before
  public void setUp_XObjectPageTypeCacheListenerTest() throws Exception {
    eventListener = getXObjectPageTypeCacheListener();
  }

  @Test
  public void testComponentSingleton() {
    assertSame(eventListener, getXObjectPageTypeCacheListener());
  }

  @Test
  public void testGetName() {
    assertEquals(_COMPONENT_NAME, eventListener.getName());
  }

  private XObjectPageTypeCacheListener getXObjectPageTypeCacheListener() {
    return (XObjectPageTypeCacheListener) Utils.getComponent(EventListener.class,
        _COMPONENT_NAME);
  }

}
