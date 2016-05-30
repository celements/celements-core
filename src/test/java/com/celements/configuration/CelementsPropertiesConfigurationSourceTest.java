package com.celements.configuration;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.container.ApplicationContext;
import org.xwiki.container.Container;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

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
