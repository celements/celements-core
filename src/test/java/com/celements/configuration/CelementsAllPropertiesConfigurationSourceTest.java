package com.celements.configuration;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class CelementsAllPropertiesConfigurationSourceTest extends AbstractComponentTest {

  private CelementsAllPropertiesConfigurationSource cfgSrc;

  @Before
  public void prepareTest() throws Exception {
    String hint = CelementsAllPropertiesConfigurationSource.NAME;
    Utils.getComponentManager().unregisterComponent(ConfigurationSource.class, hint);
    CelementsAllPropertiesConfigurationSource instance = new CelementsAllPropertiesConfigurationSource();
    instance.celementsPropertiesSource = createMockAndAddToDefault(ConfigurationSource.class);
    instance.xwikiPropertiesSource = createMockAndAddToDefault(ConfigurationSource.class);
    instance.initialize();
    DefaultComponentDescriptor<ConfigurationSource> descriptor = new DefaultComponentDescriptor<>();
    descriptor.setRole(ConfigurationSource.class);
    descriptor.setRoleHint(hint);
    Utils.getComponentManager().registerComponent(descriptor, instance);

    cfgSrc = (CelementsAllPropertiesConfigurationSource) Utils.getComponent(
        ConfigurationSource.class, hint);
  }

  @Test
  public void test_getProperty_none() throws Exception {
    String key = "key";

    expect(cfgSrc.celementsPropertiesSource.containsKey(eq(key))).andReturn(false).once();
    expect(cfgSrc.xwikiPropertiesSource.containsKey(eq(key))).andReturn(false).once();

    replayDefault();
    assertNull(cfgSrc.getProperty(key));
    verifyDefault();
  }

  @Test
  public void test_getProperty_fromCelProp() throws Exception {
    String key = "key";
    String val = "val";

    expect(cfgSrc.celementsPropertiesSource.containsKey(eq(key))).andReturn(true).once();
    expect(cfgSrc.celementsPropertiesSource.getProperty(eq(key))).andReturn(val).once();

    replayDefault();
    assertEquals(val, cfgSrc.getProperty(key));
    verifyDefault();
  }

  @Test
  public void test_getProperty_fromXWikiProp() throws Exception {
    String key = "key";
    String val = "val";

    expect(cfgSrc.celementsPropertiesSource.containsKey(eq(key))).andReturn(false).once();
    expect(cfgSrc.xwikiPropertiesSource.containsKey(eq(key))).andReturn(true).once();
    expect(cfgSrc.xwikiPropertiesSource.getProperty(eq(key))).andReturn(val).once();

    replayDefault();
    assertEquals(val, cfgSrc.getProperty(key));
    verifyDefault();
  }

}
