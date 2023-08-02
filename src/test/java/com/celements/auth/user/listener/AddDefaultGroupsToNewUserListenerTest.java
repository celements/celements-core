package com.celements.auth.user.listener;

import org.junit.Before;

import com.celements.common.test.AbstractComponentTest;

public class AddDefaultGroupsToNewUserListenerTest extends AbstractComponentTest {

  private AddDefaultGroupsToNewUserListener usrListener;

  @Before
  public void prepareTest() {
    usrListener = getBeanFactory().getBean(AddDefaultGroupsToNewUserListener.class);
  }

}
