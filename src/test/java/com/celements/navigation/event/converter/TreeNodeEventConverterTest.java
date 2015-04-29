/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
