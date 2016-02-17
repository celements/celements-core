package com.celements.filebase;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.filebase.exceptions.FileBaseLoadException;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class SingleDocFileBaseServiceTest extends AbstractBridgedComponentTestCase {
  
  private SingleDocFileBaseService fbService;
  
  @Before
  public void setUp_SingleDocFileBaseService() throws Exception {
    fbService = (SingleDocFileBaseService) Utils.getComponent(IFileBaseServiceRole.class, 
        SingleDocFileBaseService.FILEBASE_SINGLE_DOC);
  }

  @Test
  public void test_getFileBaseDoc_noConfig() {
    replayDefault();
    try {
      fbService.getFileBaseDoc();
      fail("Expected FileBaseLoadException");
    } catch(FileBaseLoadException fble) {
      //expected outcome
    }
    verifyDefault();
  }

  @Test
  public void test_getFileBaseDoc_configMinus() {
    fbService.configuration = createMockAndAddToDefault(ConfigurationSource.class);
    expect(fbService.configuration.getProperty(
        eq(SingleDocFileBaseService.FILEBASE_CONFIG_FIELD))).andReturn("-");
    replayDefault();
    try {
      fbService.getFileBaseDoc();
      fail("Expected FileBaseLoadException");
    } catch(FileBaseLoadException fble) {
      //expected outcome
    }
    verifyDefault();
  }

  @Test
  public void test_getFileBaseDoc_configDocLoadFail() throws DocumentLoadException {
    String spcName = "FBSpace";
    String docName = "FBDoc";
    DocumentReference fileBaseDocRef = new DocumentReference(getContext().getDatabase(),
        spcName, docName);
    fbService.configuration = createMockAndAddToDefault(ConfigurationSource.class);
    expect(fbService.configuration.getProperty(
        eq(SingleDocFileBaseService.FILEBASE_CONFIG_FIELD))).andReturn(spcName + "." + 
            docName);
    fbService.modelAccess = createMockAndAddToDefault(IModelAccessFacade.class);
    expect(fbService.modelAccess.getOrCreateDocument(eq(fileBaseDocRef))).andThrow(
        new DocumentLoadException(fileBaseDocRef));
    replayDefault();
    try {
      fbService.getFileBaseDoc();
      fail("Expected FileBaseLoadException");
    } catch(FileBaseLoadException fble) {
      //expected outcome
    }
    verifyDefault();
  }

  @Test
  public void test_getFileBaseDoc_configDoc() throws Exception {
    String spcName = "FBSpace";
    String docName = "FBDoc";
    DocumentReference fileBaseDocRef = new DocumentReference(getContext().getDatabase(),
        spcName, docName);
    XWikiDocument doc = new XWikiDocument(fileBaseDocRef);
    fbService.configuration = createMockAndAddToDefault(ConfigurationSource.class);
    expect(fbService.configuration.getProperty(
        eq(SingleDocFileBaseService.FILEBASE_CONFIG_FIELD))).andReturn(spcName + "." + 
            docName);
    fbService.modelAccess = createMockAndAddToDefault(IModelAccessFacade.class);
    expect(fbService.modelAccess.getOrCreateDocument(eq(fileBaseDocRef))).andReturn(doc);
    replayDefault();
    XWikiDocument resultDoc = fbService.getFileBaseDoc();
    verifyDefault();
    assertEquals(doc.getDocumentReference(), resultDoc.getDocumentReference());
  }

}
