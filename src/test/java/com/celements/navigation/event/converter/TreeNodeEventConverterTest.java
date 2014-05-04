package com.celements.navigation.event.converter;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.observation.remote.converter.LocalEventConverter;
import org.xwiki.observation.remote.converter.RemoteEventConverter;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class TreeNodeEventConverterTest extends AbstractBridgedComponentTestCase {
  
  private TreeNodeEventConverter localEventConverter;
  private TreeNodeEventConverter remoteEventConverter;

  @Before
  public void setUp_TreeNodeEventConverterTest() throws Exception {
    localEventConverter = getTreeNodeLocalEventConverter();
    remoteEventConverter = getTreeNodeRemoteEventConverter();
  }

  @Test
  public void testComponentSingleton() {
    assertSame(localEventConverter, getTreeNodeLocalEventConverter());
  }

  @Test
  public void testComponentDoubleRole() {
    assertNotNull(localEventConverter);
    assertNotNull(remoteEventConverter);
    assertNotSame(localEventConverter, remoteEventConverter);
  }


  private TreeNodeEventConverter getTreeNodeLocalEventConverter() {
    return (TreeNodeEventConverter) Utils.getComponent(LocalEventConverter.class,
        "TreeNode");
  }

  private TreeNodeEventConverter getTreeNodeRemoteEventConverter() {
    return (TreeNodeEventConverter) Utils.getComponent(RemoteEventConverter.class,
        "TreeNode");
  }

}
