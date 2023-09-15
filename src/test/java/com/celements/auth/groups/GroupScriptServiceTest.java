package com.celements.auth.groups;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;

public class GroupScriptServiceTest extends AbstractComponentTest {

  private GroupScriptService service;
  private DocumentReference groupDocRef = new DocumentReference("xwikidb", "XWiki", "testgroup");

  @Before
  public void prepare_Test() throws Exception {
    registerComponentMocks(GroupService.class);
    service = getBeanFactory().getBean(GroupScriptService.class);

  }

  @Test
  public void test_getGroupPrettyName() {
    Optional<String> retGroupPrettyName = Optional.of("Test Gruppe");
    expect(getMock(GroupService.class).getGroupPrettyName(groupDocRef))
        .andReturn(retGroupPrettyName);

    replayDefault();
    String groupName = service.getGroupPrettyName(groupDocRef);
    verifyDefault();

    assertEquals("Test Gruppe", groupName);
  }

  @Test
  public void test_getGroupPrettyName_emptyOptional() {
    Optional<String> retOptionalNull = Optional.ofNullable(null);
    expect(getMock(GroupService.class).getGroupPrettyName(groupDocRef))
        .andReturn(retOptionalNull);

    replayDefault();
    String groupName = service.getGroupPrettyName(groupDocRef);
    verifyDefault();

    assertEquals(groupDocRef.getName(), groupName);
  }

}
