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
  public void test_encodeUrlForRedirect() throws Exception {
    String url = "https://www.üparties.ch:8080/täst/töst?köy=välüe#änchör";
    assertEquals("https://www.%C3%BCparties.ch:8080/t%C3%A4st/t%C3%B6st?köy=välüe#änchör",
        service.encodeUrlForRedirect(url));
  }

}
