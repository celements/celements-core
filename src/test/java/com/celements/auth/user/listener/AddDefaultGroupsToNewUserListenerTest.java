package com.celements.auth.user.listener;

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.model.reference.DocumentReference;

import com.celements.auth.user.User;
import com.celements.auth.user.UserInstantiationException;
import com.celements.auth.user.UserService;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.doc.XWikiDocument;

public class AddDefaultGroupsToNewUserListenerTest extends AbstractComponentTest {

  private AddDefaultGroupsToNewUserListener usrListener;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(UserService.class);
    usrListener = getBeanFactory().getBean(AddDefaultGroupsToNewUserListener.class);
  }

  @Test
  public void testOnEventInternal() throws Exception {
    XWikiDocument userDoc = new XWikiDocument(
        new DocumentReference("xwikidb", "XWiki", "cpichler"));
    User user = createDefaultMock(User.class);
    expect(getMock(UserService.class).getUser(userDoc.getDocRef())).andReturn(user);
    expect(getMock(UserService.class).addUserToDefaultGroups(user)).andReturn(true);

    replayDefault();
    usrListener.onEventInternal(new DocumentCreatedEvent(), userDoc, new Object());
    verifyDefault();

  }

  @Test
  public void testOnEventInternal_sourceIsNotUserDoc() throws Exception {
    XWikiDocument anyDoc = new XWikiDocument(
        new DocumentReference("xwikidb", "anySpace", "testDocument"));
    Exception uie = new UserInstantiationException("anyDoc is no UserDoc");
    expect(getMock(UserService.class).getUser(anyDoc.getDocRef()))
        .andThrow(uie);

    replayDefault();
    usrListener.onEventInternal(new DocumentCreatedEvent(), anyDoc, new Object());
    verifyDefault();

  }

}
