package com.celements.filebase;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class AttachmentServiceTest extends AbstractBridgedComponentTestCase {

  private AttachmentService attService;
  
  @Before
  public void setUp_AttachmentServiceTest() throws ComponentLookupException, Exception {
    attService = (AttachmentService) getComponentManager().lookup(
        IAttachmentServiceRole.class);
  }
  
  @Test
  public void testClearFileName_empty() {
    String name = "";
    assertEquals(name, attService.clearFileName(name));
  }
  
  @Test
  public void testClearFileName_clean() {
    String name = "abc.jpg";
    assertEquals(name, attService.clearFileName(name));
  }
  
  @Test
  public void testClearFileName_minus() {
    String name = "abc-123.jpg";
    assertEquals(name, attService.clearFileName(name));
  }
  
  @Test
  public void testClearFileName_space() {
    String name = "abc 123.jpg";
    String target = "abc-123.jpg";
    assertEquals(target, attService.clearFileName(name));
  }
  
  @Test
  public void testClearFileName_underscore() {
    String name = "abc_123.jpg";
    assertEquals(name, attService.clearFileName(name));
  }

}
