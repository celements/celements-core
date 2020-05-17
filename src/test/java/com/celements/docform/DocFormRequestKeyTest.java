package com.celements.docform;

import static com.celements.docform.DocFormRequestKey.*;
import static com.google.common.collect.ImmutableList.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ImmutableDocumentReference;

import com.google.common.collect.ImmutableList;

public class DocFormRequestKeyTest {

  @Test
  public void test_hashCode() {
    List<DocFormRequestKey> all = getAllKeyCombinations();
    Set<DocFormRequestKey> set = new HashSet<>(all);
    assertEquals(all.size(), set.size());
    set.addAll(getAllKeyCombinations());
    assertEquals(all.size(), set.size());
  }

  @Test
  public void test_equals() {
    List<DocFormRequestKey> all1 = getAllKeyCombinations();
    List<DocFormRequestKey> all2 = getAllKeyCombinations();
    for (int i = 0; i < all1.size(); i++) {
      for (int j = 0; j < all2.size(); j++) {
        DocFormRequestKey key1 = all1.get(i), key2 = all2.get(j);
        if (i == j) {
          assertEquals(key1 + " =/= " + key2, key1, key2);
          assertEquals(key2 + " =/= " + key1, key2, key1);
        } else {
          assertNotEquals(key1 + " =/= " + key2, key1, key2);
          assertNotEquals(key2 + " =/= " + key1, key2, key1);
        }
      }
    }
  }

  private List<DocFormRequestKey> getAllKeyCombinations() {
    return ImmutableList.of(
        createDocFieldKey("key", getDocRef("A"), "A"),
        createDocFieldKey("key", getDocRef("A"), "B"),
        createDocFieldKey("key", getDocRef("B"), "A"),
        createDocFieldKey("key", getDocRef("B"), "B"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("A"), 0, "A"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("A"), 0, "B"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("A"), 1, "A"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("A"), 1, "B"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("B"), 0, "A"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("B"), 0, "B"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("B"), 1, "A"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("B"), 1, "B"),
        createObjFieldKey("key", getDocRef("B"), getClassRef("A"), 0, "A"),
        createObjFieldKey("key", getDocRef("B"), getClassRef("A"), 0, "B"),
        createObjFieldKey("key", getDocRef("B"), getClassRef("A"), 1, "A"),
        createObjFieldKey("key", getDocRef("B"), getClassRef("A"), 1, "B"),
        createObjFieldKey("key", getDocRef("B"), getClassRef("B"), 0, "A"),
        createObjFieldKey("key", getDocRef("B"), getClassRef("B"), 0, "B"),
        createObjFieldKey("key", getDocRef("B"), getClassRef("B"), 1, "A"),
        createObjFieldKey("key", getDocRef("B"), getClassRef("B"), 1, "B"),
        createObjRemoveKey("ky", getDocRef("A"), getClassRef("A"), 0),
        createObjRemoveKey("ky", getDocRef("A"), getClassRef("A"), 1),
        createObjRemoveKey("ky", getDocRef("A"), getClassRef("B"), 0),
        createObjRemoveKey("ky", getDocRef("A"), getClassRef("B"), 1),
        createObjRemoveKey("ky", getDocRef("B"), getClassRef("A"), 0),
        createObjRemoveKey("ky", getDocRef("B"), getClassRef("A"), 1),
        createObjRemoveKey("ky", getDocRef("B"), getClassRef("B"), 0),
        createObjRemoveKey("ky", getDocRef("B"), getClassRef("B"), 1));
  }

  @Test
  public void test_compareTo() {
    List<DocFormRequestKey> sorted = getSortedKeys();
    List<DocFormRequestKey> shuffled = new ArrayList<>(sorted);
    Collections.shuffle(shuffled);
    assertEquals(sorted, shuffled.stream().sorted().collect(toImmutableList()));
  }

  private List<DocFormRequestKey> getSortedKeys() {
    return ImmutableList.of(
        createObjFieldKey("key", getDocRef("A"), getClassRef("A"), 0, "A"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("A"), 0, "B"),
        createObjRemoveKey("ky", getDocRef("A"), getClassRef("A"), 0),
        createObjFieldKey("key", getDocRef("A"), getClassRef("A"), 1, "A"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("A"), 3, "A"),
        createObjRemoveKey("ky", getDocRef("A"), getClassRef("A"), 3),
        createObjFieldKey("key", getDocRef("A"), getClassRef("A"), -1, "A"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("A"), -3, "A"),
        createObjFieldKey("key", getDocRef("A"), getClassRef("B"), 0, "A"),
        createDocFieldKey("key", getDocRef("A"), "A"),
        createDocFieldKey("key", getDocRef("A"), "B"),
        createObjFieldKey("key", getDocRef("B"), getClassRef("A"), 0, "A"),
        createDocFieldKey("key", getDocRef("B"), "A"));
  }

  private DocumentReference getDocRef(String name) {
    return new ImmutableDocumentReference("db", "space", name);
  }

  private ClassReference getClassRef(String name) {
    return new ClassReference("class", name);
  }

}
