package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class UserNameForUserDataCommandTest extends AbstractBridgedComponentTestCase{
  UserNameForUserDataCommand cmd;
  
  XWiki xwiki;
  XWikiStoreInterface store;
  
  @Before
  public void setUp_UserNameForUserDataCommandTest() throws Exception {
    cmd = new UserNameForUserDataCommand();
    xwiki = createMock(XWiki.class);
    getContext().setWiki(xwiki);
    store = createMock(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(store).anyTimes();
  }

  @Test
  public void testGetUsernameForUserData_null() throws XWikiException {
    replay(store, xwiki);
    assertEquals("", cmd.getUsernameForUserData(null, "loginname,email", getContext()));
    verify(store, xwiki);
  }

  @Test
  public void testGetUsernameForUserData_empty() throws XWikiException {
    replay(store, xwiki);
    assertEquals("", cmd.getUsernameForUserData(" \t", "loginname,email", getContext()));
    verify(store, xwiki);
  }

  @Test
  public void testGetUsernameForUserData_loginname_notExists() throws XWikiException {
    String login = "testLogin";
    expect(xwiki.exists(eq("XWiki." + login), same(getContext()))).andReturn(false).once();
    List<String> emptyList = Collections.emptyList();
    expect(store.searchDocumentsNames((String)anyObject(), eq(0), eq(0), same(getContext(
        )))).andReturn(emptyList).once();
    List<XWikiDocument> emptyDocList = Collections.emptyList();
    expect(store.searchDocuments(eq("where lower(doc.name)=?"), (List<String>)anyObject(), 
        same(getContext()))).andReturn(emptyDocList).once();
    replay(store, xwiki);
    assertEquals("", cmd.getUsernameForUserData(login, "loginname,,", getContext()));
    verify(store, xwiki);
  }

  @Test
  public void testGetUsernameForUserData_loginname_correctCase() throws XWikiException {
    String login = "testLogin";
    expect(xwiki.exists(eq("XWiki." + login), same(getContext()))).andReturn(true).once();
    List<String> emptyList = Collections.emptyList();
    expect(store.searchDocumentsNames((String)anyObject(), eq(0), eq(0), same(getContext(
        )))).andReturn(emptyList).once();
    replay(store, xwiki);
    assertEquals("XWiki." + login, cmd.getUsernameForUserData(login, ",,loginname", 
        getContext()));
    verify(store, xwiki);
  }

  @Test
  public void testGetUsernameForUserData_loginname_mixedCase() throws XWikiException {
    String login = "testLogin";
    String mixedLogin = "TeStLoGin";
    expect(xwiki.exists(eq("XWiki." + mixedLogin), same(getContext()))).andReturn(false
        ).once();
    List<String> emptyList = Collections.emptyList();
    expect(store.searchDocumentsNames((String)anyObject(), eq(0), eq(0), same(getContext(
        )))).andReturn(emptyList).once();
    List<XWikiDocument> list = new ArrayList<XWikiDocument>();
    list.add(new XWikiDocument(new DocumentReference(getContext().getDatabase(), "XWiki", 
        login)));
    expect(store.searchDocuments(eq("where lower(doc.name)=?"), (List<String>)anyObject(), 
        same(getContext()))).andReturn(list).once();
    replay(store, xwiki);
    assertEquals("XWiki." + login, cmd.getUsernameForUserData(mixedLogin, ",loginname,", 
        getContext()));
    verify(store, xwiki);
  }

  @Test
  public void testGetUsernameForUserData_email_notExists() throws XWikiException {
    List<String> emptyList = Collections.emptyList();
    expect(store.searchDocumentsNames((String)anyObject(), eq(0), eq(0), same(getContext(
        )))).andReturn(emptyList).once();
    replay(store, xwiki);
    assertEquals("", cmd.getUsernameForUserData("abc@ucme.com", "email", getContext()));
    verify(store, xwiki);
  }

  @Test
  public void testGetUsernameForUserData_email_correctCase() throws XWikiException {
    String login = "abc@ucme.com";
    String docName = "XWiki.abc";
    List<String> list = new ArrayList<String>();
    list.add(docName);
    expect(store.searchDocumentsNames(eq(getDBQuery(login)), eq(0), eq(0), 
        same(getContext()))).andReturn(list).once();
    replay(store, xwiki);
    assertEquals(docName, cmd.getUsernameForUserData(login, "email,loginname", 
        getContext()));
    verify(store, xwiki);
  }

  @Test
  public void testGetUsernameForUserData_email_mixedCase() throws XWikiException {
    String login = "abc@ucme.com";
    String loginCased = "ABC@ucme.com";
    String docName = "XWiki.abc";
    List<String> list = new ArrayList<String>();
    list.add(docName);
    expect(store.searchDocumentsNames(eq(getDBQuery(login)), eq(0), eq(0), 
        same(getContext()))).andReturn(list).once();
    replay(store, xwiki);
    assertEquals(docName, cmd.getUsernameForUserData(loginCased, "email,loginname", 
        getContext()));
    verify(store, xwiki);
  }

  @Test
  public void testGetUsernameForUserData_email_multiple() throws XWikiException {
    String login = "abc@ucme.com";
    String docName = "XWiki.abc";
    List<String> list = new ArrayList<String>();
    list.add(docName);
    list.add(docName);
    expect(store.searchDocumentsNames(eq(getDBQuery(login)), eq(0), eq(0), 
        same(getContext()))).andReturn(list).once();
    replay(store, xwiki);
    assertNull(cmd.getUsernameForUserData(login, "email,loginname", getContext()));
    verify(store, xwiki);
  }
  
  private String getDBQuery(String login) {
    return ", BaseObject as obj, StringProperty as str where doc.space='XWiki' and " +
        "obj.name=doc.fullName and obj.className='XWiki.XWikiUsers' and " +
        "obj.id=str.id.id and (str.id.name='email' or str.id.name='loginname' ) and " +
        "lower(str.value)='" + login.toLowerCase() + "'";
  }
}