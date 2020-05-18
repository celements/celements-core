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
package com.celements.docform;

import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.celements.common.test.CelementsTestUtils.*;
import static com.google.common.collect.ImmutableSet.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.docform.IDocForm.ResponseState;
import com.celements.model.access.ModelAccessStrategy;
import com.celements.model.access.XWikiDocumentCreator;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.web.plugin.cmd.AddTranslationCommand;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class DocFormCommandTest extends AbstractComponentTest {

  private DocFormRequestKeyParser parser;
  private WikiReference wiki = new WikiReference("db");
  private DocumentReference docRef;
  private XWikiDocument xdoc;
  private XWikiDocument tdoc;

  private DocFormCommand docFormCmd;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(ModelAccessStrategy.class);
    getContext().setDatabase(wiki.getName());
    docRef = new DocumentReference(wiki.getName(), "Space", "Doc");
    parser = new DocFormRequestKeyParser(docRef);
    docFormCmd = (DocFormCommand) Utils.getComponent(IDocForm.class)
        .initialize(docRef, true);

    xdoc = create(docRef);
    tdoc = create(docRef, "it");
  }

  @Test
  public void test_updateDoc_docField() throws Exception {
    DocFormRequestParam param = parseParam("title", "val");

    replayDefault();
    assertSame(tdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, param));
    verifyDefault();

    assertEquals(param.getValuesAsString(), tdoc.getTitle());
  }

  @Test
  public void test_updateDoc_docField_alreadySet() throws Exception {
    DocFormRequestParam param = parseParam("title", "val");
    tdoc.setTitle(param.getValuesAsString());

    replayDefault();
    assertNull(docFormCmd.updateDocFromParam(xdoc, tdoc, param));
    verifyDefault();

    assertEquals(param.getValuesAsString(), tdoc.getTitle());
  }

  @Test
  public void test_updateDoc_objField_get() throws Exception {
    DocFormRequestParam param = parseParam("A.B_3_hi", "val");
    BaseObject obj = addXObject(xdoc, param.getKey());

    replayDefault();
    assertSame(xdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, param));
    verifyDefault();

    assertSame(obj, assertObj(param, xdoc));
    assertEquals(1, XWikiObjectFetcher.on(xdoc).count());
  }

  @Test
  public void test_updateDoc_objField_alreadySet() throws Exception {
    DocFormRequestParam param = parseParam("A.B_3_hi", "val");
    BaseObject obj = addXObject(xdoc, param.getKey());
    obj.setStringValue(param.getKey().getFieldName(), param.getValuesAsString());

    replayDefault();
    assertNull(docFormCmd.updateDocFromParam(xdoc, tdoc, param));
    verifyDefault();

    assertSame(obj, assertObj(param, xdoc));
    assertEquals(1, XWikiObjectFetcher.on(xdoc).count());
  }

  @Test
  public void test_updateDoc_objField_multipleFields() throws Exception {
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of(
        "A.B_0_hi", "val1",
        "A.B_0_content", "val2"));
    BaseObject obj = addXObject(xdoc, params.get(0).getKey());

    replayDefault();
    assertSame(xdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, params.get(0)));
    assertSame(xdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, params.get(1)));
    verifyDefault();

    assertSame(obj, assertObj(params.get(0), xdoc));
    assertEquals(1, XWikiObjectFetcher.on(xdoc).count());
    params.forEach(param -> assertEquals(param.getValuesAsString(),
        obj.getStringValue(param.getKey().getFieldName())));
  }

  @Test
  public void test_updateDoc_objField_create_positiveNb() throws Exception {
    DocFormRequestParam param = parseParam("A.B_3_hi", "val");

    replayDefault();
    assertSame(xdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, param));
    verifyDefault();

    assertEquals(0, assertObj(param, xdoc).getNumber());
    assertEquals(1, XWikiObjectFetcher.on(xdoc).count());
  }

  @Test
  public void test_updateDoc_objField_create_negativeNb() throws Exception {
    DocFormRequestParam param = parseParam("A.B_-3_hi", "val");

    replayDefault();
    assertSame(xdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, param));
    verifyDefault();

    assertEquals(0, assertObj(param, xdoc).getNumber());
    assertEquals(1, XWikiObjectFetcher.on(xdoc).count());
  }

  @Test
  public void test_updateDoc_objField_create_multiple_sameClass() throws Exception {
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of(
        "A.B_0_hi", "val1",
        "A.B_-1_hi", "val2"));

    replayDefault();
    assertSame(xdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, params.get(0)));
    assertSame(xdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, params.get(1)));
    verifyDefault();

    assertEquals(0, assertObj(params.get(0), xdoc).getNumber());
    assertEquals(1, assertObj(params.get(1), xdoc).getNumber());
    assertEquals(2, XWikiObjectFetcher.on(xdoc).count());
  }

  @Test
  public void test_updateDoc_objField_create_multiple_otherClasses() throws Exception {
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of(
        "A.B_-1_hi", "val1",
        "X.Y_-1_hi", "val2"));

    replayDefault();
    assertSame(xdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, params.get(0)));
    assertSame(xdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, params.get(1)));
    verifyDefault();

    assertObj(params.get(0), xdoc);
    assertObj(params.get(1), xdoc);
    assertEquals(2, XWikiObjectFetcher.on(xdoc).count());
  }

  @Test
  public void test_updateDoc_objRemove_none() throws Exception {
    DocFormRequestParam param = parseParam("A.B_^3", "");

    replayDefault();
    assertNull(docFormCmd.updateDocFromParam(xdoc, tdoc, param));
    verifyDefault();

    assertEquals(0, XWikiObjectFetcher.on(xdoc).count());
  }

  @Test
  public void test_updateDoc_objRemove() throws Exception {
    DocFormRequestParam param = parseParam("A.B_^3", "");
    addXObject(xdoc, param.getKey());

    replayDefault();
    assertSame(xdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, param));
    verifyDefault();

    assertEquals(0, XWikiObjectFetcher.on(xdoc).count());
  }

  @Test
  public void test_updateDoc_objRemove_withField() throws Exception {
    DocFormRequestParam param = parseParam("A.B_^3_hi", "val");
    addXObject(xdoc, param.getKey());

    replayDefault();
    assertSame(xdoc, docFormCmd.updateDocFromParam(xdoc, tdoc, param));
    verifyDefault();

    assertEquals(0, XWikiObjectFetcher.on(xdoc).count());
  }

  @Test
  public void test_updateDocs_none() throws Exception {
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of());
    expectDoc(xdoc);

    replayDefault();
    docFormCmd.updateDocs(params);
    verifyDefault();

    assertEquals(0, XWikiObjectFetcher.on(xdoc).count());
    assertResponse(params, null, null, ImmutableSet.of(xdoc));
  }

  @Test
  public void test_updateDocs() throws Exception {
    DocFormRequestParam param = parseParam("A.B_0_hi", "val");
    expectDocWithSave(xdoc);

    replayDefault();
    docFormCmd.updateDocs(ImmutableList.of(param));
    verifyDefault();

    assertEquals(0, assertObj(param, xdoc).getNumber());
    assertEquals(1, XWikiObjectFetcher.on(xdoc).count());
    assertResponse(ImmutableList.of(param), ImmutableSet.of(xdoc), null, null);
  }

  @Test
  public void test_updateDocs_alreadySet() throws Exception {
    DocFormRequestParam param = parseParam("A.B_0_hi", "val");
    expectDoc(xdoc);
    BaseObject obj = addXObject(xdoc, param.getKey());
    obj.setStringValue(param.getKey().getFieldName(), param.getValuesAsString());

    replayDefault();
    docFormCmd.updateDocs(ImmutableList.of(param));
    verifyDefault();

    assertSame(obj, assertObj(param, xdoc));
    assertEquals(1, XWikiObjectFetcher.on(xdoc).count());
    assertResponse(ImmutableList.of(param), null, null, ImmutableSet.of(xdoc));
  }

  @Test
  public void test_updateDocs_multipleDocs() throws Exception {
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of(
        "This.Doc_A.B_0_foo", "val1",
        "That.Doc_A.B_0_foo", "val1"));
    XWikiDocument docThis = expectDocWithSave(create(
        new DocumentReference(wiki.getName(), "This", "Doc")));
    XWikiDocument docThat = expectDocWithSave(create(
        new DocumentReference(wiki.getName(), "That", "Doc")));
    expectDoc(xdoc);

    replayDefault();
    docFormCmd.updateDocs(params);
    verifyDefault();

    assertEquals(1, XWikiObjectFetcher.on(docThis).count());
    assertEquals(1, XWikiObjectFetcher.on(docThat).count());
    assertResponse(params, ImmutableSet.of(docThis, docThat), null, ImmutableSet.of(xdoc));
  }

  @Test
  public void test_updateDocs_newFromTemplate() throws Exception {
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of());
    expectDocWithSave(xdoc);

    getContext().setRequest(createMockAndAddToDefault(XWikiRequest.class));
    expect(getContext().getRequest().get(eq("template"))).andReturn("Tmpl.MyTmpl");
    XWikiDocument templateDoc = create(new DocumentReference(wiki.getName(), "Tmpl", "MyTmpl"));
    templateDoc.setNew(false);
    templateDoc.setContent("Content");
    expect(getWikiMock().getDocument(eq(templateDoc.getDocumentReference()), same(getContext())))
        .andReturn(templateDoc);

    replayDefault();
    docFormCmd.updateDocs(params);
    verifyDefault();

    assertEquals("Content", xdoc.getContent());
    assertResponse(params, ImmutableSet.of(xdoc), null, null);
  }

  @Test
  public void test_updateDocs_docFields() throws Exception {
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of(
        "Space.Doc_title", "Title",
        "content", "Content"));
    expectDocWithSave(xdoc);

    replayDefault();
    docFormCmd.updateDocs(params);
    verifyDefault();

    assertEquals("Title", xdoc.getTitle());
    assertEquals("Content", xdoc.getContent());
    assertResponse(params, ImmutableSet.of(xdoc), null, null);
  }

  @Test
  public void test_updateDocs_docFields_translation() throws Exception {
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of(
        "Space.Doc_title", "Title",
        "content", "Content"));
    expectDoc(xdoc);
    getContext().setLanguage(tdoc.getLanguage());
    docFormCmd.addTranslationCmd = createMockAndAddToDefault(AddTranslationCommand.class);
    expect(docFormCmd.addTranslationCmd.getTranslatedDoc(same(xdoc), eq(tdoc.getLanguage())))
        .andReturn(tdoc);
    expectDocWithSave(tdoc);

    replayDefault();
    docFormCmd.updateDocs(params);
    verifyDefault();

    assertEquals("", xdoc.getTitle());
    assertEquals("", xdoc.getContent());
    assertEquals("Title", tdoc.getTitle());
    assertEquals("Content", tdoc.getContent());
    assertResponse(params, ImmutableSet.of(xdoc), null, null);
  }

  @Test
  public void test_updateDocs_objFields_deleteAndSet() throws Exception {
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of(
        "A.B_^0", "",
        "A.B_1_foo", "val1",
        "A.B_0_foo", "val2"));
    addXObject(xdoc, params.get(0).getKey());
    expectDocWithSave(xdoc);

    replayDefault();
    docFormCmd.updateDocs(params);
    verifyDefault();

    assertEquals(1, XWikiObjectFetcher.on(xdoc).count());
    assertResponse(params, ImmutableSet.of(xdoc), null, null);
  }

  @Test
  public void test_updateDocs_createNotAllowed() throws Exception {
    docFormCmd = (DocFormCommand) Utils.getComponent(IDocForm.class)
        .initialize(docRef, false);
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of("A.B_0_foo", "val"));
    xdoc.setNew(true);
    expectDoc(xdoc);

    replayDefault();
    docFormCmd.updateDocs(params);
    verifyDefault();

    assertResponse(params, null, ImmutableSet.of(xdoc), null);
  }

  @Test
  public void test_updateDocs_createNotAllowed_notNew() throws Exception {
    docFormCmd = (DocFormCommand) Utils.getComponent(IDocForm.class)
        .initialize(docRef, false);
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of("A.B_0_foo", "val"));
    xdoc.setNew(false);
    expectDocWithSave(xdoc);

    replayDefault();
    docFormCmd.updateDocs(params);
    verifyDefault();

    assertResponse(params, ImmutableSet.of(xdoc), null, null);
  }

  @Test
  public void test_updateDocs_saveFail() throws Exception {
    List<DocFormRequestParam> params = parseParams(ImmutableMap.of("A.B_0_foo", "val"));
    xdoc.setNew(false);
    expectDocWithSave(xdoc);
    expectLastCall().andThrow(new DocumentSaveException(docRef));

    replayDefault();
    docFormCmd.updateDocs(params);
    verifyDefault();

    assertResponse(params, null, ImmutableSet.of(xdoc), null);
  }

  private void assertResponse(List<DocFormRequestParam> params,
      Set<XWikiDocument> successful,
      Set<XWikiDocument> failed,
      Set<XWikiDocument> unchanged) {
    Map<ResponseState, Set<DocumentReference>> responseMap = docFormCmd.getResponseMap(params);
    assertEquals(3, responseMap.size());
    assertEquals("successful", convert(successful), responseMap.get(ResponseState.successful));
    assertEquals("failed", convert(failed), responseMap.get(ResponseState.failed));
    assertEquals("unchanged", convert(unchanged), responseMap.get(ResponseState.unchanged));
  }

  private Set<DocumentReference> convert(Set<XWikiDocument> docs) {
    return Optional.ofNullable(docs).map(Set::stream).orElse(Stream.empty())
        .map(XWikiDocument::getDocumentReference).collect(toImmutableSet());
  }

  private BaseObject addXObject(XWikiDocument doc, DocFormRequestKey key) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(key.getClassRef());
    doc.setXObject(key.getObjNb(), obj);
    return obj;
  }

  private XWikiDocument expectDoc(XWikiDocument doc) {
    expect(getMock(ModelAccessStrategy.class).exists(doc.getDocumentReference(), ""))
        .andReturn(true).anyTimes();
    expect(getMock(ModelAccessStrategy.class).getDocument(doc.getDocumentReference(), ""))
        .andReturn(doc).anyTimes();
    return doc;
  }

  private XWikiDocument expectDocWithSave(XWikiDocument doc) throws DocumentSaveException {
    expectDoc(doc);
    getMock(ModelAccessStrategy.class).saveDocument(doc, "updateAndSaveDocFormRequest", false);
    return doc;
  }

  private DocFormRequestParam parseParam(String key, String value) throws Exception {
    return parseParams(ImmutableMap.of(key, value)).get(0);
  }

  private List<DocFormRequestParam> parseParams(Map<String, String> map) throws Exception {
    List<DocFormRequestParam> requestParams = parser.parseParameterMap(map);
    requestParams.stream().map(DocFormRequestParam::getKey)
        .filter(key -> key.getClassRef() != null)
        .collect(Collectors.groupingBy(key -> key.getClassRef()))
        .forEach(rethrowBiConsumer((classRef, keys) -> {
          final BaseClass bClass = expectNewBaseObject(classRef.getDocRef(wiki));
          keys.stream().map(DocFormRequestKey::getFieldName)
              .forEach(field -> expect(bClass.get(field)).andReturn(new StringClass()).anyTimes());
        }));
    return requestParams;
  }

  private BaseObject assertObj(DocFormRequestParam param, XWikiDocument xdoc) {
    DocFormRequestKey key = param.getKey();
    int actualObjNb = docFormCmd.getChangedObjects().get(key.getObjHash());
    BaseObject obj = XWikiObjectEditor.on(xdoc)
        .filter(param.getKey().getClassRef())
        .filter(actualObjNb)
        .fetch().unique();
    assertEquals(param.getValuesAsString(), obj.getStringValue(key.getFieldName()));
    assertEquals(obj.getNumber(), actualObjNb);
    return obj;
  }

  static XWikiDocument create(DocumentReference docRef) {
    XWikiDocument doc = Utils.getComponent(XWikiDocumentCreator.class)
        .createWithoutDefaults(docRef);
    doc.setDefaultLanguage("de");
    return doc;
  }

  static XWikiDocument create(DocumentReference docRef, String lang) {
    return Utils.getComponent(XWikiDocumentCreator.class).createWithoutDefaults(docRef, lang);
  }

}
