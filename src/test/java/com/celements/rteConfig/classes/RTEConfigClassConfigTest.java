package com.celements.rteConfig.classes;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class RTEConfigClassConfigTest extends AbstractBridgedComponentTestCase {

  private RTEConfigClassConfig rteConfigClassCfg;

  @Before
  public void setUp_RTEConfigClassConfigTest() throws Exception {
    rteConfigClassCfg = (RTEConfigClassConfig) Utils.getComponent(
        IRTEConfigClassConfig.class);
  }

  @Test
  public void testGetRTEConfigTypePropertiesClassRefEntityReference() {
    EntityReference inRef = new EntityReference("xwikiDb", EntityType.WIKI);
    replayDefault();
    DocumentReference classRef = rteConfigClassCfg.getRTEConfigTypePropertiesClassRef(
        inRef);
    assertEquals("xwikiDb", classRef.extractReference(EntityType.WIKI).getName());
    assertEquals(IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE,
        classRef.extractReference(EntityType.SPACE).getName());
    assertEquals(IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_DOC,
        classRef.getName());
    verifyDefault();
  }

  @Test
  public void testGetRTEConfigTypePropertiesClassRefWikiReference() {
    WikiReference inRef = new WikiReference("xwikiDb");
    replayDefault();
    DocumentReference classRef = rteConfigClassCfg.getRTEConfigTypePropertiesClassRef(
        inRef);
    assertEquals("xwikiDb", classRef.extractReference(EntityType.WIKI).getName());
    assertEquals(IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE,
        classRef.extractReference(EntityType.SPACE).getName());
    assertEquals(IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_DOC,
        classRef.getName());
    verifyDefault();
  }

}
