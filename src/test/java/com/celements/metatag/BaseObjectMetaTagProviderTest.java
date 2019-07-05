package com.celements.metatag;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.context.ModelContext;
import com.celements.web.classes.MetaTagClass;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class BaseObjectMetaTagProviderTest extends AbstractComponentTest {

  private ModelContext modelContext;
  private BaseObjectMetaTagProvider bomtProvider;

  @Before
  public void prepareTest() throws Exception {
    modelContext = registerComponentMock(ModelContext.class);
    expect(modelContext.getDefaultLanguage()).andReturn("de").anyTimes();
    bomtProvider = (BaseObjectMetaTagProvider) Utils.getComponent(MetaTagProviderRole.class,
        BaseObjectMetaTagProvider.COMPONENT_NAME);
  }

  @Test
  public void testGetMetaTagsForDoc_none() {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Spc", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    replayDefault();
    assertTrue(bomtProvider.getMetaTagsForDoc(doc).isEmpty());
    verifyDefault();
  }

  @Test
  public void testGetMetaTagsForDoc() {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Spc", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.addXObject(createMetaTagBaseObject(docRef, "keywords", "test,schluessel,wort", "de",
        null));
    doc.addXObject(createMetaTagBaseObject(docRef, "keywords", "test,key,word", "en", null));
    replayDefault();
    List<MetaTag> tags = bomtProvider.getMetaTagsForDoc(doc);
    verifyDefault();
    assertEquals(2, tags.size());
    assertEquals("de", tags.get(0).getLangOpt().get());
    assertEquals("en", tags.get(1).getLangOpt().get());
  }

  @Test
  public void testAddMetaTagsFromList() {
    SortedMap<String, List<MetaTag>> tags = new TreeMap<>();
    List<MetaTag> list1 = new ArrayList<>();
    list1.add(createMetaTag("description", "this is the description", "en", null));
    tags.put("description", list1);
    List<MetaTag> list2 = new ArrayList<>();
    list2.add(createMetaTag("keywords", "alle,schluessel", "de", null));
    list2.add(createMetaTag("keywords", "all,keys", "en", null));
    tags.put("keywords", list2);
    expect(modelContext.getLanguage()).andReturn(Optional.of("en")).anyTimes();
    List<MetaTag> addList = new ArrayList<>();
    addList.add(createMetaTag("keywords", "tutti,chiavi", "it", null));
    addList.add(createMetaTag("description", "tutta la informazione", "it", null));
    addList.add(createMetaTag("description", "die ganze information", "de", null));
    addList.add(createMetaTag("newkey", "new value", "en", null));
    addList.add(createMetaTag("newkey", "alles neu", "", null));
    replayDefault();
    bomtProvider.addMetaTagsFromList(addList, tags);
    verifyDefault();
    assertEquals(3, tags.size());
    List<List<MetaTag>> resultLists = ImmutableList.copyOf(tags.values());
    assertEquals(2, resultLists.get(0).size());
    assertEquals("description", resultLists.get(0).get(0).getKeyOpt().get());
    assertEquals("en", resultLists.get(0).get(0).getLangOpt().get());
    assertEquals("de", resultLists.get(0).get(1).getLangOpt().get());
    assertEquals(2, resultLists.get(1).size());
    assertEquals("keywords", resultLists.get(1).get(0).getKeyOpt().get());
    assertEquals("de", resultLists.get(1).get(0).getLangOpt().get());
    assertEquals("en", resultLists.get(1).get(1).getLangOpt().get());
    assertEquals(2, resultLists.get(2).size());
    assertEquals("newkey", resultLists.get(2).get(0).getKeyOpt().get());
    assertEquals("en", resultLists.get(2).get(0).getLangOpt().get());
    assertTrue("", resultLists.get(2).get(1).getLangOpt().isEmpty());
  }

  @Test
  public void testApplyOverride_emptyList() {
    replayDefault();
    assertFalse(Arrays.asList(new ArrayList<MetaTag>()).stream().map(bomtProvider
        .applyOverride()).filter(Objects::nonNull).findFirst().get().findFirst().isPresent());
    verifyDefault();
  }

  @Test
  public void testApplyOverride_overrideTrue() {
    String key = "description";
    String value = "die ganze information";
    List<MetaTag> tags = new ArrayList<>();
    tags.add(createMetaTag(key, "description", "en", true));
    tags.add(createMetaTag(key, "another description", "en", true));
    tags.add(createMetaTag(key, value, "de", true));
    replayDefault();
    Optional<MetaTag> tag = Arrays.asList(tags).stream().map(bomtProvider.applyOverride())
        .filter(Objects::nonNull).findFirst().get().findFirst();
    verifyDefault();
    assertTrue(tag.isPresent());
    assertEquals(key, tag.get().getKeyOpt().get());
    assertEquals(value, tag.get().getValueOpt().get());
  }

  @Test
  public void testApplyOverride_overrideFalse() {
    String key = "description";
    String value1 = "description";
    String value2 = "another description";
    String value3 = "die ganze information";
    List<MetaTag> tags = new ArrayList<>();
    tags.add(createMetaTag(key, value1, "en", false));
    tags.add(createMetaTag(key, value2, "en", null));
    tags.add(createMetaTag(key, value3, "de", false));
    replayDefault();
    Optional<MetaTag> tag = Arrays.asList(tags).stream().map(bomtProvider.applyOverride())
        .filter(Objects::nonNull).findFirst().get().findFirst();
    verifyDefault();
    assertTrue(tag.isPresent());
    assertEquals(key, tag.get().getKeyOpt().get());
    assertEquals(value1 + "," + value2 + "," + value3, tag.get().getValueOpt().get());
  }

  @Test
  public void testApplyOverride_overrideMix_endFalse() {
    String key = "description";
    String value = "die ganze information";
    List<MetaTag> tags = new ArrayList<>();
    tags.add(createMetaTag(key, "description", "en", true));
    tags.add(createMetaTag(key, "another description", "en", true));
    tags.add(createMetaTag(key, value, "de", false));
    replayDefault();
    Optional<MetaTag> tag = Arrays.asList(tags).stream().map(bomtProvider.applyOverride())
        .filter(Objects::nonNull).findFirst().get().findFirst();
    verifyDefault();
    assertTrue(tag.isPresent());
    assertEquals(key, tag.get().getKeyOpt().get());
    assertEquals(value, tag.get().getValueOpt().get());
  }

  @Test
  public void testApplyOverride_overrideMix_endTrue() {
    String key = "description";
    String value1 = "another description";
    String value2 = "die ganze information";
    List<MetaTag> tags = new ArrayList<>();
    tags.add(createMetaTag(key, "description", "en", true));
    tags.add(createMetaTag(key, value1, "en", false));
    tags.add(createMetaTag(key, value2, "de", true));
    replayDefault();
    Optional<MetaTag> tag = Arrays.asList(tags).stream().map(bomtProvider.applyOverride())
        .filter(Objects::nonNull).findFirst().get().findFirst();
    verifyDefault();
    assertTrue(tag.isPresent());
    assertEquals(key, tag.get().getKeyOpt().get());
    assertEquals(value1 + "," + value2, tag.get().getValueOpt().get());
    assertFalse(tag.get().getOverridable());
  }

  @Test
  public void testApplyOverride_overrideMultipleMix_endTrue() {
    String key = "description";
    String tag1value1 = "first false";
    String tag1value2 = "then true";
    String tag2value1 = "another description";
    String tag2value2 = "die ganze information";
    List<MetaTag> tags = new ArrayList<>();
    tags.add(createMetaTag(key, "description", "en", true));
    tags.add(createMetaTag(key, tag1value1, "en", false));
    tags.add(createMetaTag(key, tag1value2, "de", true));
    tags.add(createMetaTag(key, tag2value1, "en", false));
    tags.add(createMetaTag(key, tag2value2, "de", true));
    replayDefault();
    Optional<Stream<MetaTag>> resultTags = Arrays.asList(tags).stream()
        .map(bomtProvider.applyOverride())
        .filter(Objects::nonNull)
        .findFirst();
    verifyDefault();
    List<MetaTag> resultList = resultTags.get().collect(Collectors.toList());
    assertTrue(1 <= resultList.size());
    assertEquals(key, resultList.get(0).getKeyOpt().get());
    assertEquals(tag1value1 + "," + tag1value2 + "," + tag2value1 + "," + tag2value2, resultList
        .get(0).getValueOpt().get());
  }

  // HELPER METHODS
  BaseObject createMetaTagBaseObject(DocumentReference docRef, String key, String value,
      String language, Boolean overridable) {
    BaseObject tag = new BaseObject();
    tag.setDocumentReference(docRef);
    tag.setXClassReference(MetaTagClass.CLASS_REF);
    if (key != null) {
      tag.setStringValue(MetaTagClass.FIELD_KEY.getName(), key);
    }
    if (value != null) {
      tag.setStringValue(MetaTagClass.FIELD_VALUE.getName(), value);
    }
    if (language != null) {
      tag.setStringValue(MetaTagClass.FIELD_LANGUAGE.getName(), language);
    }
    if (overridable != null) {
      tag.setIntValue(MetaTagClass.FIELD_OVERRIDABLE.getName(), overridable ? 1 : 0);
    }
    return tag;
  }

  MetaTag createMetaTag(String key, String value, String language, Boolean overridable) {
    MetaTag tag = new MetaTag();
    tag.setKey(key);
    tag.setValue(value);
    tag.setLang(language);
    tag.setOverridable(overridable);
    return tag;
  }

}
