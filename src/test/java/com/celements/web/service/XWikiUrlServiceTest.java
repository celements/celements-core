package com.celements.web.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

public class XWikiUrlServiceTest extends AbstractComponentTest {

  private UrlService service;
  private XWikiURLFactory urlFactoryMock;

  private DocumentReference docRef = new DocumentReference("wiki", "space", "page");

  @Before
  public void prepareTest() {
    service = Utils.getComponent(UrlService.class);
    getContext().setURLFactory(urlFactoryMock = createMockAndAddToDefault(XWikiURLFactory.class));
    docRef = new DocumentReference("wiki", "space", "page");
  }

  @Test
  public void test_getURL() throws Exception {
    String action = "edit";
    String queryString = "key=value1&key=value2";
    String file = "/" + action + "/space/page?" + queryString;
    URL url = new URL("http", "wiki.celements.com", 8080, file);
    expect(urlFactoryMock.createURL("space", "page", action, queryString, null, "wiki",
        getContext())).andReturn(url);

    replayDefault();
    assertEquals(file, service.getURL(docRef, action, queryString));
    verifyDefault();
  }

  @Test
  public void test_getURL_defaults() throws Exception {
    String file = "/space/page";
    URL url = new URL("http", "wiki.celements.com", 8080, file);
    expect(urlFactoryMock.createURL("space", "page", "view", null, null, "wiki",
        getContext())).andReturn(url);

    replayDefault();
    assertEquals(file, service.getURL(docRef));
    verifyDefault();
  }

  @Test
  public void test_getURL_attachment() throws Exception {
    AttachmentReference attRef = new AttachmentReference("file", docRef);
    String file = "/download/space/page/file";
    URL url = new URL("http", "wiki.celements.com", 8080, file);
    expect(urlFactoryMock.createAttachmentURL("file", "space", "page", "download", null, "wiki",
        getContext())).andReturn(url);

    replayDefault();
    assertEquals(file, service.getURL(attRef));
    verifyDefault();
  }

  @Test
  public void test_getURL_null() throws Exception {
    replayDefault();
    new ExceptionAsserter<IllegalArgumentException>(IllegalArgumentException.class) {

      @Override
      protected void execute() throws IllegalArgumentException {
        service.getURL(null);
      }
    }.evaluate();
    verifyDefault();
  }

  @Test
  public void test_getExternalURL() throws Exception {
    String action = "edit";
    String queryString = "key=value1&key=value2";
    String file = "/" + action + "/space/page?" + queryString;
    URL url = new URL("http", "wiki.celements.com", 8080, file);
    expect(urlFactoryMock.createURL("space", "page", action, queryString, null, "wiki",
        getContext())).andReturn(url);

    replayDefault();
    assertEquals(url.toString(), service.getExternalURL(docRef, action, queryString));
    verifyDefault();
  }

  @Test
  public void test_getExternalURL_null() throws Exception {
    replayDefault();
    new ExceptionAsserter<IllegalArgumentException>(IllegalArgumentException.class) {

      @Override
      protected void execute() throws IllegalArgumentException {
        service.getExternalURL(null);
      }
    }.evaluate();
    verifyDefault();
  }

  @Test
  public void test_createURIBuilder() throws Exception {
    String url = "http://wiki.celements.com/space/page";
    expect(urlFactoryMock.createURL("space", "page", "view", null, null, "wiki",
        getContext())).andReturn(new URL(url));

    replayDefault();
    assertEquals(url, service.createURIBuilder(docRef).build().toString());
    verifyDefault();
  }

  @Test
  public void test_createURIBuilder_null() throws Exception {
    replayDefault();
    new ExceptionAsserter<IllegalArgumentException>(IllegalArgumentException.class) {

      @Override
      protected void execute() throws IllegalArgumentException {
        service.createURIBuilder(null);
      }
    }.evaluate();
    verifyDefault();
  }

}
