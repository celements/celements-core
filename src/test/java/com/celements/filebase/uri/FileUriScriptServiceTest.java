package com.celements.filebase.uri;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;

import javax.ws.rs.core.UriBuilder;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractComponentTest;
import com.celements.filebase.references.FileReference;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

public class FileUriScriptServiceTest extends AbstractComponentTest {

  private FileUriScriptService fileUriSrv;
  private XWikiContext context;
  private XWikiURLFactory mockURLFactory;

  @Before
  public void setUp_FileUriScriptServiceTest() throws Exception {
    context = getContext();
    mockURLFactory = createMockAndAddToDefault(XWikiURLFactory.class);
    context.setURLFactory(mockURLFactory);
    fileUriSrv = (FileUriScriptService) Utils.getComponent(ScriptService.class,
        FileUriScriptService.NAME);
  }

  @Test
  public void test_createFileUrl_null() throws Exception {
    assertNull(fileUriSrv.createFileUrl((FileReference) null, null, null));
    assertNull(fileUriSrv.createFileUrl((String) null, null, null));
    assertNull(fileUriSrv.createFileUrl((String) null, null));
    assertNull(fileUriSrv.createFileUrl((String) null));
  }

  @Test
  public void test_createAbsoluteFileUri_null() {
    assertNotNull(fileUriSrv.createAbsoluteFileUri(null, null, null));
  }

  @Test
  public void test_createAbsoluteFileUri() throws Exception {
    String serverUrl = "http://localhost";
    URL viewURL = new URL(serverUrl);
    expect(mockURLFactory.getServerURL(same(context))).andReturn(viewURL);
    replayDefault();
    String filePath = "/a/b/c.txt";
    FileReference fileRef = fileUriSrv.createFileReference(filePath);
    UriBuilder fileUri = fileUriSrv.createAbsoluteFileUri(fileRef, null, null);
    assertNotNull(fileUri);
    assertEquals(serverUrl + filePath, fileUri.build().toURL().toExternalForm());
    verifyDefault();
  }

  @Test
  public void test_getExternalFileURL_null() {
    assertNotNull(fileUriSrv.getExternalFileURL(null, null, null));
    assertEquals("", fileUriSrv.getExternalFileURL(null, null, null));
    assertNotNull(fileUriSrv.getExternalFileURL(null, null));
    assertEquals("", fileUriSrv.getExternalFileURL(null, null));
  }

  @Test
  public void test_getExternalFileURL() throws Exception {
    URL viewURL = new URL("http://localhost");
    expect(mockURLFactory.getServerURL(same(context))).andReturn(viewURL);
    replayDefault();
    FileReference fileRef = fileUriSrv.createFileReference("/a/b/c.txt");
    assertNotNull(fileUriSrv.getExternalFileURL(fileRef, null, null));
    verifyDefault();
  }

}
