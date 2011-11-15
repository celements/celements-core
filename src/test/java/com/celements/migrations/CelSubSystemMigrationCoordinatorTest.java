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
package com.celements.migrations;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.ComponentDescriptor;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class CelSubSystemMigrationCoordinatorTest
  extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private CelSubSystemMigrationCoordinator celSubSysMigCoordinator;
  private ISubSystemMigrationManager componentMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp_CelSubSystemMigrationCoordinatorTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    componentMock = createMock(ISubSystemMigrationManager.class);
    ComponentDescriptor<ISubSystemMigrationManager> descMock = createMock(ComponentDescriptor.class);
    expect(descMock.getRole()).andReturn(ISubSystemMigrationManager.class);
    expect(descMock.getRoleHint()).andReturn("testSubSystem");
    replay(descMock);
    Utils.getComponentManager().registerComponent(descMock, componentMock);
    // get Coordinator only AFTER registering mock component. Otherwise injection fails
    // adding the mockComponent.
    celSubSysMigCoordinator = (CelSubSystemMigrationCoordinator) Utils.getComponent(
        ISubSystemMigrationCoordinator.class);
  }

  @Test
  public void testStartSubSystemMigrations_skip() throws Exception {
    XWikiConfig configMock = createMock(XWikiConfig.class);
    expect(xwiki.getConfig()).andReturn(configMock).anyTimes();
    expect(configMock.getPropertyAsList(eq("celements.subsystems.migration.manager.order")
      )).andReturn(new String[]{"testSubSystem"});
    expect(xwiki.Param(eq("celements.subsystems.testSubSystem.migration"), eq("0")
      )).andReturn("0");
    replay(xwiki, configMock, componentMock);
    celSubSysMigCoordinator.startSubSystemMigrations(context);
    verify(xwiki, configMock, componentMock);
  }

  @Test
  public void testStartSubSystemMigrations_execute() throws Exception {
    XWikiConfig configMock = createMock(XWikiConfig.class);
    expect(xwiki.getConfig()).andReturn(configMock).anyTimes();
    expect(configMock.getPropertyAsList(eq("celements.subsystems.migration.manager.order")
      )).andReturn(new String[]{"testSubSystem"});
    expect(xwiki.Param(eq("celements.subsystems.testSubSystem.migration"), eq("0")
      )).andReturn("1");
    expect(componentMock.getSubSystemName()).andReturn("testSubSystem").anyTimes();
    componentMock.startMigrations(same(context));
    expectLastCall().once();
    replay(xwiki, configMock, componentMock);
    celSubSysMigCoordinator.startSubSystemMigrations(context);
    verify(xwiki, configMock, componentMock);
  }

}
