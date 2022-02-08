package com.celements.filebase.uri;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.UriBuilder;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractComponentTest;
import com.celements.filebase.references.FileReference;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

public class FileUriScriptServiceTest extends AbstractComponentTest {

  private FileUriScriptService fileUriSrv;
  private XWikiContext context;
  private XWikiURLFactory mockURLFactory;
  private XWiki wiki;

  @Before
  public void setUp_FileUriScriptServiceTest() throws Exception {
    context = getContext();
    wiki = getWikiMock();
    mockURLFactory = createMockAndAddToDefault(XWikiURLFactory.class);
    context.setURLFactory(mockURLFactory);
    fileUriSrv = (FileUriScriptService) Utils.getComponent(ScriptService.class,
        FileUriScriptService.NAME);
  }

  @Test
  public void test_createFileUrl_null() throws Exception {
    assertEquals("", fileUriSrv.createFileUrl((FileReference) null, null, null).toString());
    assertEquals("", fileUriSrv.createFileUrl((String) null, null, null).toString());
    assertEquals("", fileUriSrv.createFileUrl((String) null, null).toString());
    assertEquals("", fileUriSrv.createFileUrl((String) null).toString());
  }

  @Test
  public void test_createFileUrl_file_action() throws Exception {
    String resultURL = "/skin/resources/celJS/prototype.js";
    expect(wiki.getSkinFile(eq("celJS/prototype.js"), eq(true), same(context)))
        .andReturn(resultURL);
    Date lastModificationDate = new SimpleDateFormat("YYYYmmddHHMMss").parse("20201123101535");
    expect(wiki.getResourceLastModificationDate(eq("resources/celJS/prototype.js"))).andReturn(
        lastModificationDate);
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("download");
    replayDefault();
    assertEquals("/file/resources/celJS/prototype.js?version=20191230101135",
        fileUriSrv.createFileUrl(":celJS/prototype.js", "file").toString());
    verifyDefault();
  }

  @Test
  public void test_createFileUrl_file_action_query() throws Exception {
    String resultURL = "/skin/resources/celJS/prototype.js";
    expect(wiki.getSkinFile(eq("celJS/prototype.js"), eq(true), same(context)))
        .andReturn(resultURL);
    Date lastModificationDate = new SimpleDateFormat("YYYYmmddHHMMss").parse("20201123101535");
    expect(wiki.getResourceLastModificationDate(eq("resources/celJS/prototype.js"))).andReturn(
        lastModificationDate);
    expect(wiki.getXWikiPreference(eq("celdefaultAttAction"), eq(
        "celements.attachmenturl.defaultaction"), eq("file"), same(context))).andReturn("download");
    replayDefault();
    assertEquals("/file/resources/celJS/prototype.js?version=20191230101135&bla=asfd",
        fileUriSrv.createFileUrl(":celJS/prototype.js", "file", "bla=asfd").toString());
    verifyDefault();
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
    assertNotNull(fileUriSrv.getExternalFileURL("/a/b/c.txt", null, null));
    verifyDefault();
  }

}
