package com.celements.web.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;

public class CelementsWebServiceTest extends AbstractComponentTest {

  private CelementsWebService service;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(IModelAccessFacade.class);
    service = (CelementsWebService) getComponentManager().lookup(ICelementsWebServiceRole.class);
  }

  @Test
  public void testEncodeUrlToUtf8() throws Exception {
    String url = "http://www.üparties.ch";
    replayDefault();
    String ret = service.encodeUrlToUtf8(url);
    verifyDefault();
    assertEquals("http://www.%C3%BCparties.ch", ret);
  }

  @Test
  public void testEncodeUrlToUtf8_withSlashes() throws Exception {
    String url = "http://www.üparties.ch/test1/test2";
    replayDefault();
    String ret = service.encodeUrlToUtf8(url);
    verifyDefault();
    assertEquals("http://www.%C3%BCparties.ch/test1/test2", ret);
  }

}
