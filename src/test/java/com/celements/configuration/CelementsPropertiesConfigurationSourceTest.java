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
package com.celements.configuration;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.container.ApplicationContext;
import org.xwiki.container.Container;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ComponentList;
import com.celements.common.test.HintedComponent;
import com.xpn.xwiki.web.Utils;

@ComponentList({ @HintedComponent(clazz = ConfigurationSource.class,
    hint = "celementsproperties") })
public class CelementsPropertiesConfigurationSourceTest extends AbstractComponentTest {

  @Test
  public void test_initialize() throws Exception {
    registerComponentMock(Container.class);
    ApplicationContext contextMock = createMockAndAddToDefault(ApplicationContext.class);
    expect(getMock(Container.class).getApplicationContext()).andReturn(contextMock).once();
    String name = CelementsPropertiesConfigurationSource.CELEMENTS_PROPERTIES_FILE;
    expect(contextMock.getResource(eq(name))).andReturn(null).once();
    replayDefault();
    assertSame(CelementsPropertiesConfigurationSource.class, Utils.getComponent(
        ConfigurationSource.class, "celementsproperties").getClass());
    verifyDefault();
  }

}
