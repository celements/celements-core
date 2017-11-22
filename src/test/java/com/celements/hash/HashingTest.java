package com.celements.hash;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
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

  @Test
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
