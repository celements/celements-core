package com.celements.hash;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.math.LongRange;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.hash.IdCalculator.IdCalcStrategy;
import com.celements.hash.IdCalculator.IdCustomHashCode;
import com.celements.hash.IdCalculator.IdFirst8Byte;
import com.celements.hash.IdCalculator.IdHashCode;
import com.celements.hash.IdCalculator.IdHashCode32;
import com.celements.hash.IdCalculator.IdToHexAndHashCode;
import com.celements.hash.IdCalculator.IdXWiki;

public class HashingTest extends AbstractComponentTest {

  static final long BIT_OFFSET = 24;
  static final int BIT_COLL_HANDLING = 2;
  static final int ORG_COUNT = 1;
  static final int EVENT_COUNT = 20;
  static final int SPLIT_SIZE = 100 * 1000;

  private static final List<String> LANGUAGES = Arrays.asList("", "en", "de", "fr", "it");
  private List<String> ORG_SPACES = Arrays.asList("Company.Company", "Person.Person", "Place.Place",
      "ProgonEvent.ProgonEvent");
  private List<String> EVENT_SPACES = Arrays.asList("ImpEvents.", "progonall.", "inbox.",
      "Collection29-201701161025.", "Collection2-201404281156.", "Collection14-201511271829.",
      "Collection9-201409021755.", "Collection32-201702201154.", "Collection24-201609051443.",
      "Collection33-201702201154.");

  @Test
  public void test_generated() throws Exception {
    long orgCount = ORG_COUNT * 1000 * 1000;
    long eventCount = EVENT_COUNT * 1000 * 1000;
    HashingSet set = new HashingSet(((orgCount * ORG_SPACES.size()) + (eventCount
        * EVENT_SPACES.size())) * LANGUAGES.size());
    IdCalculator calculator = new IdCalculator(set, new IdFirst8Byte("MD5"));
    ForkJoinPool fjp = new ForkJoinPool(4);
    try {
      long time = System.currentTimeMillis();
      fjp.invoke(new IdCalcTask("Org", calculator, ORG_SPACES, LANGUAGES, new LongRange(1,
          orgCount)));
      fjp.invoke(new IdCalcTask("Event", calculator, EVENT_SPACES, LANGUAGES, new LongRange(1,
          eventCount)));
      time = System.currentTimeMillis() - time;
      System.out.println("Took " + (time / 1000) + "s");
    } finally {
      HashUtils.printResult(set.size(), set.getCollisionCount(), calculator.strategy);
    }
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
    HashingSet set = new HashingSet(5582460);
    File file = new File(System.getProperty("user.home") + File.separator + "documents.txt");
    LineIterator iter = FileUtils.lineIterator(file, "UTF-8");
    try {
      new IdCalculator(set, strategy).calc(iter);
    } finally {
      HashUtils.printResult(set.size(), set.getCollisionCount(), strategy);
      iter.close();
    }
  }

}
