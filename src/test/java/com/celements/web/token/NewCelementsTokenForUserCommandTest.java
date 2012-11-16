/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.token;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

public class NewCelementsTokenForUserCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private NewCelementsTokenForUserCommand celTokenForUserCmd;

  @Before
  public void setUp_NewCelementsTokenForUserCommandTest() throws Exception {
    context = getContext();
    celTokenForUserCmd = new NewCelementsTokenForUserCommand();
    celTokenForUserCmd.refLocalSerializer = Utils.getComponent(
        EntityReferenceSerializer.class);
    celTokenForUserCmd.stringRefResolver = Utils.getComponent(
        EntityReferenceResolver.class);
  }

  @Test
  public void testGetNewCelementsTokenForUser_XWikiGuest() throws XWikiException {
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    expect(xwiki.exists(eq("XWiki.XWikiGuest"), same(context))).andReturn(false).once();
    expect(xwiki.getDocument(eq("XWiki.XWikiGuest"), same(context))).andReturn(
        new XWikiDocument()).once();
    replay(xwiki);
    assertNull(celTokenForUserCmd.getNewCelementsTokenForUser("XWiki.XWikiGuest", false,
        context));
    verify(xwiki);
  }
  
  @Test
  public void testRemoveOutdatedTokens_noTokens() throws QueryException {
    DocumentReference dRef = new DocumentReference(getContext().getDatabase(), "X", "Y");
    XWikiDocument doc = new XWikiDocument(dRef);
    QueryManager queryManager = createMock(QueryManager.class);
    celTokenForUserCmd.queryManager = queryManager;
    Query query = createMock(Query.class);
    expect(queryManager.createQuery((String)anyObject(), eq(Query.XWQL))).andReturn(query
        ).anyTimes();
    expect(query.bindValue((String)anyObject(), (String)anyObject())).andReturn(query
        ).anyTimes();
    expect(query.execute()).andReturn(Collections.emptyList());
    replay(query, queryManager);
    celTokenForUserCmd.removeOutdatedTokens(doc);
    verify(query, queryManager);
    assertNull(doc.getXObjects(celTokenForUserCmd.getTokenClassDocRef()));
  }
  
  @Test
  public void testRemoveOutdatedTokens_1new() throws QueryException {
    DocumentReference dRef = new DocumentReference(getContext().getDatabase(), "X", "Y");
    XWikiDocument doc = new XWikiDocument(dRef);
    BaseObject obj = new BaseObject();
    obj.setXClassReference(celTokenForUserCmd.getTokenClassDocRef());
    Date afterNow = new Date();
    afterNow.setTime(afterNow.getTime() + 1000000l);
    obj.setDateValue("validuntil", afterNow);
    doc.addXObject(obj);
    QueryManager queryManager = createMock(QueryManager.class);
    celTokenForUserCmd.queryManager = queryManager;
    Query query = createMock(Query.class);
    expect(queryManager.createQuery((String)anyObject(), eq(Query.XWQL))).andReturn(query
        ).anyTimes();
    expect(query.bindValue((String)anyObject(), (String)anyObject())).andReturn(query
        ).anyTimes();
    expect(query.execute()).andReturn(Collections.emptyList());
    replay(query, queryManager);
    celTokenForUserCmd.removeOutdatedTokens(doc);
    verify(query, queryManager);
    assertEquals(1, doc.getXObjects(celTokenForUserCmd.getTokenClassDocRef()).size());
  }
  
  @Test
  public void testRemoveOutdatedTokens_1outdated() throws QueryException {
    DocumentReference dRef = new DocumentReference(getContext().getDatabase(), "X", "Y");
    XWikiDocument doc = new XWikiDocument(dRef);
    BaseObject obj = new BaseObject();
    obj.setXClassReference(celTokenForUserCmd.getTokenClassDocRef());
    Date beforeNow = new Date();
    beforeNow.setTime(beforeNow.getTime() - 1000000l);
    obj.setDateValue("validuntil", beforeNow);
    doc.addXObject(obj);
    QueryManager queryManager = createMock(QueryManager.class);
    celTokenForUserCmd.queryManager = queryManager;
    Query query = createMock(Query.class);
    expect(queryManager.createQuery((String)anyObject(), eq(Query.XWQL))).andReturn(query
        ).anyTimes();
    expect(query.bindValue((String)anyObject(), (String)anyObject())).andReturn(query
        ).anyTimes();
    List<Object> list = new ArrayList<Object>();
    list.add("0");
    expect(query.execute()).andReturn(list);
    replay(query, queryManager);
    celTokenForUserCmd.removeOutdatedTokens(doc);
    verify(query, queryManager);
    assertNull(doc.getXObjects(celTokenForUserCmd.getTokenClassDocRef()).get(0));
  }
  
  @Test
  public void testRemoveOutdatedTokens_multiple() throws QueryException {
    DocumentReference dRef = new DocumentReference(getContext().getDatabase(), "X", "Y");
    XWikiDocument doc = new XWikiDocument(dRef);
    BaseObject obj1 = new BaseObject();
    obj1.setXClassReference(celTokenForUserCmd.getTokenClassDocRef());
    Date afterNow = new Date();
    afterNow.setTime(afterNow.getTime() + 1000000l);
    obj1.setDateValue("validuntil", afterNow);
    doc.addXObject(obj1);
    BaseObject obj2 = new BaseObject();
    obj2.setXClassReference(celTokenForUserCmd.getTokenClassDocRef());
    Date beforeNow = new Date();
    beforeNow.setTime(beforeNow.getTime() - 1000000l);
    obj2.setDateValue("validuntil", beforeNow);
    doc.addXObject(obj2);
    BaseObject obj3 = new BaseObject();
    obj3.setXClassReference(celTokenForUserCmd.getTokenClassDocRef());
    beforeNow.setTime(beforeNow.getTime() - 1000000l);
    obj3.setDateValue("validuntil", beforeNow);
    doc.addXObject(obj3);
    QueryManager queryManager = createMock(QueryManager.class);
    celTokenForUserCmd.queryManager = queryManager;
    Query query = createMock(Query.class);
    expect(queryManager.createQuery((String)anyObject(), eq(Query.XWQL))).andReturn(query
        ).anyTimes();
    expect(query.bindValue((String)anyObject(), (String)anyObject())).andReturn(query
        ).anyTimes();
    List<Object> list = new ArrayList<Object>();
    list.add("1");
    list.add(2);
    expect(query.execute()).andReturn(list);
    replay(query, queryManager);
    celTokenForUserCmd.removeOutdatedTokens(doc);
    verify(query, queryManager);
    List<BaseObject> objs = doc.getXObjects(celTokenForUserCmd.getTokenClassDocRef());
    assertEquals(3, objs.size());
    assertEquals(afterNow, objs.get(0).getDateValue("validuntil"));
    assertNull(objs.get(1));
    assertNull(objs.get(2));
  }
  
  @Test
  public void testGetNewCelementsTokenForUser_XWikiGuest_withGuestPlus(
      ) throws XWikiException, QueryException {
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    XWikiStoreInterface store = createMock(XWikiStoreInterface.class);
    BaseObject baseObject = createMock(BaseObject.class);
    XWikiDocument doc = createMock(XWikiDocument.class);
    String randomString = "123456789012345678901234";
    expect(doc.getObject(eq("XWiki.XWikiUsers"))).andReturn(new BaseObject()).once();
    expect(doc.newObject(eq("Classes.TokenClass"), same(context))).andReturn(
        baseObject).once();
    baseObject.set(eq("tokenvalue"), eq(randomString), same(context));
    expectLastCall().once();
    Capture<Date> captDate = new Capture<Date>();
    baseObject.set(eq("validuntil"), capture(captDate), same(context));
    expectLastCall().once();
    Vector<BaseObject> userObjects = new Vector<BaseObject>();
    userObjects.add(baseObject);
    String hql = "select str.value from BaseObject as obj, StringProperty as str ";
    hql += "where obj.className='XWiki.XWikiUsers' ";
    hql += "and obj.id=str.id.id ";
    hql += "and str.id.name='validkey' ";
    hql += "and str.value<>''";
    expect(store.search(eq(hql), eq(0), eq(0), same(context))).
        andReturn(new ArrayList<Object>()).once();
    expect(xwiki.generateRandomString(eq(24))).andReturn(randomString).once();
    expect(xwiki.getStore()).andReturn(store).once();
    expect(xwiki.exists(eq("XWiki.XWikiGuestPlus"), same(context))).andReturn(true
        ).once();
    expect(xwiki.getDocument(eq("XWiki.XWikiGuestPlus"), same(context))).andReturn(doc
        ).once();
    xwiki.saveDocument(eq(doc), same(context));
    expectLastCall().once();
    QueryManager queryManager = createMock(QueryManager.class);
    celTokenForUserCmd.queryManager = queryManager;
    Query query = createMock(Query.class);
    expect(queryManager.createQuery((String)anyObject(), eq(Query.XWQL))).andReturn(query
        ).anyTimes();
    expect(query.bindValue((String)anyObject(), (String)anyObject())).andReturn(query
        ).anyTimes();
    expect(query.execute()).andReturn(Collections.emptyList());
    expect(doc.getDocumentReference()).andReturn(new DocumentReference("X", "Y", "Z"));
    replay(xwiki, store, baseObject, doc, query, queryManager);
    assertEquals(randomString, celTokenForUserCmd.getNewCelementsTokenForUser(
        "XWiki.XWikiGuest",  true, context));
    Calendar expectedDateCal = Calendar.getInstance();
    expectedDateCal.add(Calendar.DAY_OF_YEAR, 1);
    // Depending on time used to run this test, there might be a small difference in
    // the computed time values.
    assertFalse("The token must not be valid for more than one day",
        expectedDateCal.getTime().before(captDate.getValue()));
    verify(xwiki, store, baseObject, doc, query, queryManager);
  }
  
}
