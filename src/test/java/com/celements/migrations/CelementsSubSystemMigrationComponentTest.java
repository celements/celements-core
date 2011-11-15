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
import com.xpn.xwiki.store.XWikiHibernateStore;
import com.xpn.xwiki.store.XWikiHibernateBaseStore.HibernateCallback;
import com.xpn.xwiki.web.Utils;

public class CelementsSubSystemMigrationComponentTest
  extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private CelementsSubSystemMigrationComponent subSysMigManager;

  @Before
  public void setUp_CelementsSubSystemMigrationComponentTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    subSysMigManager = new CelementsSubSystemMigrationComponent();
  }

  @Test
  public void testComponent_sanity() {
    ISubSystemMigrationManager celSubSystemMigManager = Utils.getComponent(
        ISubSystemMigrationManager.class, "CelementsSubSystem");
    assertNotNull(celSubSystemMigManager);
    assertEquals(CelementsSubSystemMigrationComponent.class,
        celSubSystemMigManager.getClass());
  }

  @Test
  public void testGetSubSystemName() {
    assertEquals("hint and subSystemName must be identical", "CelementsSubSystem",
        subSysMigManager.getSubSystemName());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetSubSystemHibernateMigrationManager() throws Exception {
    subSysMigManager.injected_MigrationManager = null;
    XWikiConfig configMock = createMock(XWikiConfig.class);
    expect(xwiki.getConfig()).andReturn(configMock).anyTimes();
    expect(configMock.getProperty(eq("xwiki.store.migration.version"))).andReturn("2345"
        ).anyTimes();
    XWikiHibernateStore storeMock = createMock(XWikiHibernateStore.class);
    expect(xwiki.getHibernateStore()).andReturn(storeMock).anyTimes();
    expect(storeMock.executeRead(same(context), eq(true), isA(HibernateCallback.class))
        ).andReturn(null).anyTimes();
    replay(xwiki, configMock, storeMock);
    SubSystemHibernateMigrationManager hibMigManager =
      subSysMigManager.getSubSystemHibernateMigrationManager(context);
    assertNotNull(hibMigManager);
    assertNotSame(hibMigManager, subSysMigManager.getSubSystemHibernateMigrationManager(
        context));
    verify(xwiki, configMock, storeMock);
  }

  @Test
  public void testStartMigrations() throws Exception {
    subSysMigManager.injected_MigrationManager = createMock(
        SubSystemHibernateMigrationManager.class);
    subSysMigManager.injected_MigrationManager.startMigrations(same(context));
    expectLastCall().once();
    replay(xwiki, subSysMigManager.injected_MigrationManager);
    subSysMigManager.startMigrations(context);
    verify(xwiki, subSysMigManager.injected_MigrationManager);
  }

}
