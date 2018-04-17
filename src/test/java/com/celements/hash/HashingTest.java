package com.celements.hash;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.hash.IdCalculator.IdCalcStrategy;
import com.celements.hash.IdCalculator.IdCustomHashCode;
import com.celements.hash.IdCalculator.IdFirst8Byte;
import com.celements.hash.IdCalculator.IdHashCode;
import com.celements.hash.IdCalculator.IdHashCode32;
import com.celements.hash.IdCalculator.IdToHexAndHashCode;
import com.celements.hash.IdCalculator.IdXWiki;
import com.sun.star.uno.Exception;
import com.xpn.xwiki.objects.BaseObject;

public class HashingTest extends AbstractComponentTest {

  private static final int BIT_OFFSET = 11;
  private static final int BIT_COLL_HANDLING = 2;
  private static final int ORG_COUNT = 1;
  private static final int EVENT_COUNT = 5;
  private static final boolean PRINT_COLLISIONS = true;

  private static final List<String> LANGUAGES = Arrays.asList("", "en", "de", "fr", "it");
  private static final List<String> ORG_SPACES = Arrays.asList("Company.Company", "Person.Person",
      "Place.Place", "ProgonEvent.ProgonEvent");
  private static final List<String> EVENT_SPACES = Arrays.asList("ImpEvents.", "progonall.",
      "inbox.", "Collection29-201701161025.", "Collection2-201404281156.",
      "Collection14-201511271829.", "Collection9-201409021755.", "Collection32-201702201154.",
      "Collection24-201609051443.", "Collection33-201702201154.");

  List<String> PERF_SPACES = Arrays.asList("Collection10-201409021757", "Collection11-201410161753",
      "Collection12-201410171839", "Collection13-201502261501", "Collection14-201511271829",
      "Collection15-201512161703", "Collection16-201601261616", "Collection2-201404281156",
      "Collection22-201608150938", "Collection24-201609051443", "Collection25-201610311527",
      "Collection26-201611291039", "Collection27-201612061420", "Collection28-201612061422",
      "Collection29-201701161025", "Collection3-201405131737", "Collection30-201701181541",
      "Collection31-201702201152", "Collection32-201702201154", "Collection33-201702201154",
      "Collection34-201707152302", "Collection36-201711221419", "Collection37-201711221755",
      "Collection4-201406091336", "Collection5-201406091355", "Collection6-201406091541",
      "Collection7-201406111131", "Collection8-201406111135", "Collection9-201409021755", "inbox",
      "progonall", "ImpEvents");

  @Test
  public void test_progEv() throws Exception {
    List<DocumentReference> classRefs = Arrays.asList(new DocumentReference("programmzeitung",
        "Celements2", "PageType"), new DocumentReference("programmzeitung", "Progon",
            "RealisationClass"), new DocumentReference("programmzeitung", "Progon",
                "ProgonEventPZDataClass"), new DocumentReference("programmzeitung", "Progon",
                    "ProgonEventLegacyClass"), new DocumentReference("programmzeitung", "XWiki",
                        "TagClass"), new DocumentReference("programmzeitung", "Progon",
                            "AffiliationClass"));
    BaseObject obj = new BaseObject();
    for (DocumentReference classRef : classRefs) {
      obj.setXClassReference(classRef);
      int max = classRef.getName().equals("AffiliationClass") ? 100 : 1;
      for (int nb = 0; nb < max; nb++) {
        obj.setNumber(nb);
        for (int i = 0; i <= 200000; i++) {
          obj.setDocumentReference(new DocumentReference("programmzeitung", "ProgonEvent",
              "ProgonEvent" + i));
          // ProgonEvent.ProgonEvent74798_Progon.ProgonEventLegacyClass_0 - 131788798
          if (obj.getId() == 131788798) {
            System.out.println(obj.getId() + " | " + obj.getName() + "_" + obj.getClassName() + "_"
                + obj.getNumber());
          }
        }
      }
    }
  }

  @Test
  public void test_performances() throws Exception {
    List<DocumentReference> classRefs = Arrays.asList(new DocumentReference("programmzeitung",
        "Celements2", "PageType"), new DocumentReference("programmzeitung", "Progon",
            "RealisationClass"), new DocumentReference("programmzeitung", "Classes",
                "CalendarEventClass"), new DocumentReference("programmzeitung", "Progon",
                    "AffiliationClass"));
    BaseObject obj = new BaseObject();
    obj.setNumber(0);
    for (DocumentReference classRef : classRefs) {
      obj.setXClassReference(classRef);
      for (int i = 0; i <= 376990; i++) {
        for (String space : PERF_SPACES) {
          obj.setDocumentReference(new DocumentReference("programmzeitung", space, Integer.toString(
              i)));
          // ProgonEvent.ProgonEvent74798_Progon.ProgonEventLegacyClass_0 - 131788798
          if (obj.getId() == 131788798) {
            System.out.println(obj);
            throw new Exception();
          }
        }
      }
    }
  }

  // @Test
  public void test_generated() throws Exception {
    long orgCount = ORG_COUNT * 1000 * 1000;
    long eventCount = EVENT_COUNT * 1000 * 1000;
    long initCapacity = ((orgCount * ORG_SPACES.size()) + (eventCount * EVENT_SPACES.size()))
        * LANGUAGES.size();
    HashingSet set = new HashingSet(initCapacity, BIT_OFFSET, BIT_COLL_HANDLING);
    IdCalcStrategy strategy = new IdFirst8Byte("MD5");
    ForkJoinPool pool = new ForkJoinPool(4);
    long time = System.currentTimeMillis();
    try {
      pool.invoke(createTask("OrgTask", set, strategy, ORG_SPACES, orgCount));
      pool.invoke(createTask("EventTask", set, strategy, EVENT_SPACES, eventCount));
    } finally {
      printResult(set, strategy, time);
    }
  }

  private IdCalcTask createTask(String name, HashingSet set, IdCalcStrategy strategy,
      List<String> spaces, long orgCount) {
    IdCalculator calculator = new IdCalculator(set, strategy, PRINT_COLLISIONS);
    return new IdCalcTask(name, calculator, spaces, LANGUAGES, 1, orgCount);
  }

  // @Test
  public void test_file() throws Exception {
    runForFile(new IdHashCode32());
    runForFile(new IdHashCode());
    for (String algo : Arrays.asList("MD5"/* , "SHA1", "SHA-256" */)) {
      runForFile(new IdFirst8Byte(algo));
      runForFile(new IdCustomHashCode(algo));
      runForFile(new IdToHexAndHashCode(algo));
      runForFile(new IdXWiki(algo));
    }
  }

  private void runForFile(IdCalcStrategy strategy) throws IOException {
    HashingSet set = new HashingSet(5582460, BIT_OFFSET, BIT_COLL_HANDLING);
    File file = new File(System.getProperty("user.home") + File.separator + "documents.txt");
    LineIterator iter = FileUtils.lineIterator(file, "UTF-8");
    long time = System.currentTimeMillis();
    try {
      new IdCalculator(set, strategy, PRINT_COLLISIONS).calc(iter);
    } finally {
      printResult(set, strategy, time);
      iter.close();
    }
  }

  private static void printResult(HashingSet set, IdCalcStrategy strategy, long time) {
    time = ((System.currentTimeMillis() - time) / 1000);
    System.out.println((64 - BIT_OFFSET - BIT_COLL_HANDLING) + "+" + BIT_COLL_HANDLING + "bit - "
        + strategy + " - " + set + " - " + time + "s");
  }

}
