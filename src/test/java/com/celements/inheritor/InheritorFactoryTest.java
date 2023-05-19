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
package com.celements.inheritor;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.iterator.DocumentIterator;
import com.celements.iterator.XObjectIterator;
import com.celements.parents.IDocumentParentsListerRole;
import com.celements.web.plugin.cmd.PageLayoutCommand;

public class InheritorFactoryTest extends AbstractComponentTest {

  private InheritorFactory factory;
  private IDocumentParentsListerRole parentsListerMock;
  private PageLayoutCommand mockPageLayoutCmd;

  @Before
  public void setUp_InheritorFactoryTest() throws Exception {
    factory = new InheritorFactory();
    parentsListerMock = registerComponentMock(IDocumentParentsListerRole.class);
    mockPageLayoutCmd = createMockAndAddToDefault(PageLayoutCommand.class);
    factory.injectPageLayoutCmd(mockPageLayoutCmd);
  }

  @Test
  public void testGetFieldInheritor() {
    String className = "TestClassName";
    List<String> docList = new ArrayList<>();
    docList.add("my.Doc");
    docList.add("my.Doc2");
    FieldInheritor inheritor = factory.getFieldInheritor(className, docList, getContext());
    XObjectIterator iterator = inheritor.getIteratorFactory().createIterator();
    assertEquals(className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
  }

  @Test
  public void testGetContentInheritor() {
    List<String> docList = new ArrayList<>();
    docList.add("my.Doc");
    docList.add("my.Doc2");
    ContentInheritor inheritor = factory.getContentInheritor(docList, getContext());
    DocumentIterator iterator = inheritor.getIteratorFactory().createIterator();
    assertEquals(docList, iterator.getDocListCopy());
  }

  @Test
  public void testGetNavigationFieldInheritor() {
    String className = "Tools.Banner";
    String fullName = "xwikidb:mySpace.myDoc";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "mySpace",
        "myDoc");
    List<DocumentReference> docRefList = new ArrayList<>();
    docRefList.add(docRef);
    docRefList.add(new DocumentReference(getContext().getDatabase(), "myparent", "Doc"));
    docRefList.add(new DocumentReference(getContext().getDatabase(), "myparent", "Doc2"));
    expect(parentsListerMock.getDocumentParentsList(eq(docRef), eq(true))).andReturn(docRefList);
    replayDefault();
    FieldInheritor inheritor = factory.getNavigationFieldInheritor(className, fullName,
        getContext());
    XObjectIterator iterator = inheritor.getIteratorFactory().createIterator();
    assertEquals(className, iterator.getClassName());
    List<String> docFullNameList = new ArrayList<>();
    docFullNameList.add(fullName);
    docFullNameList.add("xwikidb:myparent.Doc");
    docFullNameList.add("xwikidb:myparent.Doc2");
    assertEquals(docFullNameList, iterator.getDocListCopy());
    verifyDefault();
  }

  @Test
  public void testgetSpacePreferencesFullName() {
    String fullName = "mySpace.myDoc";
    assertEquals("mySpace.WebPreferences", factory.getSpacePreferencesFullName(fullName));
  }

  @Test
  public void testGetPageLayoutInheritor() {
    String className = "Celements2.PageType";
    String fullName = "mySpace.myDoc";
    List<String> docList = new ArrayList<>();
    docList.add(fullName);
    docList.add("mySpace.WebPreferences");
    docList.add("XWiki.XWikiPreferences");
    FieldInheritor inheritor = factory.getPageLayoutInheritor(fullName, getContext());
    XObjectIterator iterator = inheritor.getIteratorFactory().createIterator();
    assertEquals(className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
  }

  @Test
  public void testGetConfigDocFieldInheritor_fullnames() throws Exception {
    String className = "mySpace.myClassName";
    String fullName = "mySpace.myDocName";
    List<String> docList = new ArrayList<>();
    docList.add("mySpace.WebPreferences");
    docList.add("XWiki.XWikiPreferences");
    DocumentReference webHomeDocRef = new DocumentReference(getContext().getDatabase(), "mySpace",
        "WebHome");
    expect(getWikiMock().exists(eq(webHomeDocRef), same(getContext()))).andReturn(false).anyTimes();
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(fullName), same(getContext()))).andReturn(null);
    expect(getWikiMock().getSpacePreference(eq("skin"), same(getContext()))).andReturn(null);
    replayDefault();
    FieldInheritor fieldInheritor = factory.getConfigDocFieldInheritor(className, fullName,
        getContext());
    XObjectIterator iterator = fieldInheritor.getIteratorFactory().createIterator();
    verifyDefault();
    assertEquals(className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
  }

  @Test
  public void testGetConfigFieldInheritor_docRef() throws Exception {
    String className = "mySpace.myClassName";
    String fullName = "mySpace.myDocName";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "mySpace",
        "myDocName");
    ClassReference classRef = new ClassReference("mySpace", "myClassName");
    List<String> docList = new ArrayList<>();
    docList.add("xwikidb:" + fullName);
    docList.add("xwikidb:mySpace.WebPreferences");
    docList.add("xwikidb:XWiki.XWikiPreferences");
    replayDefault();
    FieldInheritor fieldInheritor = factory.getConfigFieldInheritor(classRef, docRef);
    XObjectIterator iterator = fieldInheritor.getIteratorFactory().createIterator();
    verifyDefault();
    assertEquals(className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
  }

  @Test
  public void testGetConfigFieldInheritor_spaceRef() throws Exception {
    String className = "mySpace.myClassName";
    SpaceReference spaceRef = new SpaceReference("mySpace", getWikiRef());
    ClassReference classRef = new ClassReference("mySpace", "myClassName");
    List<String> docList = new ArrayList<>();
    docList.add("xwikidb:mySpace.WebPreferences");
    docList.add("xwikidb:XWiki.XWikiPreferences");
    replayDefault();
    FieldInheritor fieldInheritor = factory.getConfigFieldInheritor(classRef, spaceRef);
    XObjectIterator iterator = fieldInheritor.getIteratorFactory().createIterator();
    verifyDefault();
    assertEquals(className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
  }

  @Test
  public void testGetConfigFieldInheritor_wikiRef() throws Exception {
    String className = "mySpace.myClassName";
    WikiReference wikiRef = getWikiRef();
    ClassReference classRef = new ClassReference("mySpace", "myClassName");
    List<String> docList = new ArrayList<>();
    docList.add("xwikidb:XWiki.XWikiPreferences");
    replayDefault();
    FieldInheritor fieldInheritor = factory.getConfigFieldInheritor(classRef, wikiRef);
    XObjectIterator iterator = fieldInheritor.getIteratorFactory().createIterator();
    verifyDefault();
    assertEquals(className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
  }

  private WikiReference getWikiRef() {
    return new WikiReference(getContext().getDatabase());
  }

}
