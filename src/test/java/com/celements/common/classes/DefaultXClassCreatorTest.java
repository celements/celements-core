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
package com.celements.common.classes;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.ModelMock;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.ClassPackage;
import com.celements.model.classes.TestClassDefinition;
import com.celements.model.classes.TestClassDefinitionRole;
import com.celements.model.classes.TestClassPackage;
import com.celements.model.classes.fields.ClassField;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.web.Utils;

public class DefaultXClassCreatorTest extends AbstractComponentTest {

  private XClassCreator creator;
  private ClassPackage classPackage;
  private ClassDefinition classDef;

  @Before
  public void prepareTest() throws Exception {
    ModelMock.init();
    registerComponentMock(ConfigurationSource.class);
    creator = Utils.getComponent(XClassCreator.class);
    classPackage = Utils.getComponent(ClassPackage.class, TestClassPackage.NAME);
    classDef = Utils.getComponent(TestClassDefinitionRole.class, TestClassDefinition.NAME);
  }

  @Test
  public void test_createXClasses_notActive() throws Exception {
    expect(getMock(ConfigurationSource.class).getProperty(ClassPackage.CFG_SRC_KEY)).andReturn(
        Collections.emptyList()).anyTimes();

    replayDefault();
    creator.createXClasses();
    verifyDefault();
  }

  @Test
  public void test_createXClasses_blacklisted() throws Exception {
    expect(getMock(ConfigurationSource.class).getProperty(ClassPackage.CFG_SRC_KEY)).andReturn(
        Arrays.asList(classPackage.getName())).anyTimes();
    expect(getMock(ConfigurationSource.class).getProperty(ClassDefinition.CFG_SRC_KEY)).andReturn(
        Arrays.asList(classDef.getName())).anyTimes();

    replayDefault();
    creator.createXClasses();
    verifyDefault();
  }

  @Test
  public void test_createXClasses() throws Exception {
    DocumentReference docRef = classDef.getClassRef();
    XWikiDocument doc = ModelMock.get().mockDoc(docRef).doc();

    expect(getMock(ConfigurationSource.class).getProperty(ClassPackage.CFG_SRC_KEY)).andReturn(
        Arrays.asList(classPackage.getName())).anyTimes();
    expect(getMock(ConfigurationSource.class).getProperty(ClassDefinition.CFG_SRC_KEY)).andReturn(
        null).anyTimes();

    replayDefault();
    creator.createXClasses();
    creator.createXClasses(); // save is only called once
    verifyDefault();

    assertEquals(1, ModelMock.get().mockDoc(docRef).getSavedCount());
    assertEquals(classDef.isInternalMapping(), doc.getXClass().hasInternalCustomMapping());
    @SuppressWarnings("unchecked")
    List<BaseCollection> xFields = new ArrayList<>(doc.getXClass().getFieldList());
    assertEquals(classDef.getFields().size(), xFields.size());
    int count = 0;
    for (ClassField<?> field : classDef.getFields()) {
      BaseCollection xField = xFields.get(count++);
      assertNotNull(xField);
      assertEquals(field.getName(), xField.getName());
    }
  }

}
