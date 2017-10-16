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

public class DefaultXClassCreatorTest {
  // public class DefaultXClassCreatorTest extends AbstractComponentTest {

  // private XClassCreator creator;
  // private ClassPackage classPackage;
  // private ClassDefinition classDef;
  // private ModelMock modelMock;
  // private ConfigurationSource configSrcMock;
  //
  // @Before
  // public void prepareTest() throws Exception {
  // modelMock = ModelMock.init();
  // configSrcMock = registerComponentMock(ConfigurationSource.class);
  // creator = Utils.getComponent(XClassCreator.class);
  // assertEquals(DefaultXClassCreator.class, creator.getClass());
  // classPackage = Utils.getComponent(ClassPackage.class, TestClassPackage.NAME);
  // classDef = Utils.getComponent(TestClassDefinitionRole.class, TestClassDefinition.NAME);
  // }
  //
  // @Test
  // public void test_createXClasses_notActive() throws Exception {
  // DocRecord record = modelMock.registerDoc(classDef.getClassReference().getDocRef());
  // expectActive(Collections.<String>emptyList(), "");
  //
  // replayDefault();
  // creator.createXClasses();
  // verifyDefault();
  //
  // assertEquals(0, record.getSavedCount());
  // }
  //
  // @Test
  // public void test_createXClasses_blacklisted() throws Exception {
  // DocRecord record = modelMock.registerDoc(classDef.getClassReference().getDocRef());
  // expectActive(Arrays.asList(classPackage.getName()), "");
  // expectBlacklist(Arrays.asList(classDef.getName()));
  //
  // replayDefault();
  // creator.createXClasses();
  // verifyDefault();
  //
  // assertEquals(0, record.getSavedCount());
  // }
  //
  // @Test
  // public void test_createXClasses() throws Exception {
  // ClassReference classRef = classDef.getClassReference();
  // XWikiDocument doc = modelMock.registerDoc(classRef.getDocRef()).doc();
  //
  // expectActive(Arrays.asList(classPackage.getName()), "");
  // expectBlacklist(Collections.<String>emptyList());
  //
  // replayDefault();
  // creator.createXClasses();
  // creator.createXClasses(); // save is only called once
  // verifyDefault();
  //
  // assertEquals(1, modelMock.getDocRecord(classRef.getDocRef()).getSavedCount());
  // assertEquals(classDef.isInternalMapping(), doc.getXClass().hasInternalCustomMapping());
  // @SuppressWarnings("unchecked")
  // List<BaseCollection> xFields = new ArrayList<>(doc.getXClass().getFieldList());
  // assertEquals(classDef.getFields().size(), xFields.size());
  // int count = 0;
  // for (ClassField<?> field : classDef.getFields()) {
  // BaseCollection xField = xFields.get(count++);
  // assertNotNull(xField);
  // assertEquals(field.getName(), xField.getName());
  // }
  // }
  //
  // @Test
  // public void test_createXClasses_legacy() throws Exception {
  // TestClassPackageLegacy classPackage = (TestClassPackageLegacy) Utils.getComponent(
  // ClassPackage.class, TestClassPackageLegacy.NAME);
  // DocRecord record = modelMock.registerDoc(classDef.getClassReference().getDocRef());
  //
  // expectActive(Collections.<String>emptyList(), classPackage.getLegacyName());
  // expectBlacklist(Collections.<String>emptyList());
  //
  // replayDefault();
  // creator.createXClasses();
  // verifyDefault();
  //
  // assertEquals(1, record.getSavedCount());
  // }
  //
  // @Test
  // public void test_createXClasses_legacy_illegalName() throws Exception {
  // TestClassPackageLegacy classPackage = (TestClassPackageLegacy) Utils.getComponent(
  // ClassPackage.class, TestClassPackageLegacy.NAME);
  // classPackage.setLegacyName("asdf");
  // DocRecord record = modelMock.registerDoc(classDef.getClassReference().getDocRef());
  //
  // expectActive(Collections.<String>emptyList(), classPackage.getLegacyName());
  // expectBlacklist(Collections.<String>emptyList());
  //
  // replayDefault();
  // new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {
  //
  // @Override
  // protected void execute() throws IllegalStateException {
  // creator.createXClasses();
  // }
  // }.evaluate();
  // verifyDefault();
  //
  // assertEquals(0, record.getSavedCount());
  // }
  //
  // private void expectActive(List<String> ret, String legacy) {
  // expect(configSrcMock.getProperty(ClassPackage.CFG_SRC_KEY)).andReturn(ret).anyTimes();
  // expect(getWikiMock().getXWikiPreference(IClassCollectionRole.ACTIVATED_XWIKIPREF,
  // getContext())).andReturn(legacy).anyTimes();
  // expect(getWikiMock().Param(IClassCollectionRole.ACTIVATED_PARAM)).andReturn("").anyTimes();
  // }
  //
  // private void expectBlacklist(List<String> ret) {
  // expect(configSrcMock.getProperty(ClassDefinition.CFG_SRC_KEY)).andReturn(ret).anyTimes();
  // }

}
