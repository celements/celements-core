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
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.store.migration.hibernate.XWikiHibernateMigrationManager;
import com.xpn.xwiki.web.Utils;

public class XWikiSubSystemMigrationComponentTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private XWikiSubSystemMigrationComponent subSysMigManager;

  @Before
  public void setUp_XWikiSubSystemMigrationManagerTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    subSysMigManager = new XWikiSubSystemMigrationComponent();
  }

  @Test
  public void testComponent_sanity() {
    ISubSystemMigrationManager xwikiSubSystemMigManager = Utils.getComponent(
        ISubSystemMigrationManager.class, "XWikiSubSystem");
    assertNotNull(xwikiSubSystemMigManager);
    assertEquals(XWikiSubSystemMigrationComponent.class,
        xwikiSubSystemMigManager.getClass());
  }

  @Test
  public void testGetSubSystemName() {
    assertEquals("hint and subSystemName must be identical", "XWikiSubSystem",
        subSysMigManager.getSubSystemName());
  }

  @Test
  public void testGetXWikiHibernateMigrationManager() throws Exception {
    subSysMigManager.injected_MigrationManager = null;
    XWikiConfig configMock = createMock(XWikiConfig.class);
    expect(xwiki.getConfig()).andReturn(configMock).anyTimes();
    expect(configMock.getProperty(eq("xwiki.store.migration.version"))).andReturn("2345"
        ).anyTimes();
    replay(xwiki, configMock);
    XWikiHibernateMigrationManager hibMigManager =
      subSysMigManager.getXWikiHibernateMigrationManager(context);
    assertNotNull(hibMigManager);
    assertNotSame(hibMigManager, subSysMigManager.getXWikiHibernateMigrationManager(
        context));
    verify(xwiki, configMock);
  }

  @Test
  public void testStartMigrations() throws Exception {
    subSysMigManager.injected_MigrationManager = createMock(
        XWikiHibernateMigrationManager.class);
    subSysMigManager.injected_MigrationManager.startMigrations(same(context));
    expectLastCall().once();
    replay(xwiki, subSysMigManager.injected_MigrationManager);
    subSysMigManager.startMigrations(context);
    verify(xwiki, subSysMigManager.injected_MigrationManager);
  }

}
