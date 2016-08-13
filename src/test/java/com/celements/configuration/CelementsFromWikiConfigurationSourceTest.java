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

public class CelementsFromWikiConfigurationSourceTest extends AbstractComponentTest {

  private CelementsFromWikiConfigurationSource cfgSrc;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    String hint = CelementsFromWikiConfigurationSource.NAME;
    Utils.getComponentManager().unregisterComponent(ConfigurationSource.class, hint);
    CelementsFromWikiConfigurationSource instance = new CelementsFromWikiConfigurationSource();
    instance.wikiPreferencesSource = createMockAndAddToDefault(ConfigurationSource.class);
    instance.celementsPropertiesSource = createMockAndAddToDefault(ConfigurationSource.class);
    instance.xwikiPropertiesSource = createMockAndAddToDefault(ConfigurationSource.class);
    instance.initialize();
    DefaultComponentDescriptor<ConfigurationSource> descriptor = new DefaultComponentDescriptor<>();
    descriptor.setRole(ConfigurationSource.class);
    descriptor.setRoleHint(hint);
    Utils.getComponentManager().registerComponent(descriptor, instance);

    cfgSrc = (CelementsFromWikiConfigurationSource) Utils.getComponent(ConfigurationSource.class,
        hint);
  }

  @Test
  public void test_getProperty_none() throws Exception {
    String key = "key";

    expect(cfgSrc.wikiPreferencesSource.containsKey(eq(key))).andReturn(false).once();
    expect(cfgSrc.celementsPropertiesSource.containsKey(eq(key))).andReturn(false).once();
    expect(cfgSrc.xwikiPropertiesSource.containsKey(eq(key))).andReturn(false).once();

    replayDefault();
    assertNull(cfgSrc.getProperty(key));
    verifyDefault();
  }

  @Test
  public void test_getProperty_fromCel() throws Exception {
    String key = "key";
    String val = "val";

    expect(cfgSrc.wikiPreferencesSource.containsKey(eq(key))).andReturn(false).once();
    expect(cfgSrc.celementsPropertiesSource.containsKey(eq(key))).andReturn(true).once();
    expect(cfgSrc.celementsPropertiesSource.getProperty(eq(key))).andReturn(val).once();

    replayDefault();
    assertEquals(val, cfgSrc.getProperty(key));
    verifyDefault();
  }

}
