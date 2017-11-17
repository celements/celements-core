package com.celements;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.google.common.primitives.Longs;

import gnu.trove.set.hash.TLongHashSet;

public class HashingTest extends AbstractComponentTest {

  private static final String FILE = "documents.txt";
  private static final String[] LANGUAGES = new String[] { "", "en", "de", "fr", "it" };
  private static final long BIT_OFFSET = 11;
  private static final int BIT_COLL_HANDLING = 2;

  private List<String> ORG_SPACES = Arrays.asList("Company.Company", "Person.Person", "Place.Place",
      "ProgonEvent.ProgonEvent");
  private List<String> EVENT_SPACES = Arrays.asList("ImpEvents.", "progonall.", "inbox.",
      "Collection29-201701161025.", "Collection2-201404281156.", "Collection14-201511271829.",
      "Collection9-201409021755.", "Collection32-201702201154.", "Collection24-201609051443.",
      "Collection33-201702201154.", "Collection13-201502261501.", "Collection31-201702201152.",
      "Collection6-201406091541.", "Collection8-201406111135.", "Collection30-201701181541.",
      "Collection11-201410161753.", "Collection15-201512161703.", "Collection26-201611291039.",
      "Collection10-201409021757.");

  private static TLongHashSet set;
  private static int[] collisionCount;

  class FullNameGenerator implements Iterator<String> {

    private final List<String> spaces;
    private final int maxCount;

    private int count = 0;
    private int spaceNb = 0;

    FullNameGenerator(List<String> spaces, int maxCount) {
      this.spaces = spaces;
      this.maxCount = maxCount;
    }

    @Override
    public boolean hasNext() {
      return count <= maxCount;
    }

    @Override
    public String next() {
      String fullName = spaces.get(spaceNb) + count;
      if ((spaceNb = ++spaceNb % spaces.size()) == 0) {
        count++;
      }
      return fullName;
    }

    @Override
    public void remove() {
      // TODO Auto-generated method stub

    }

  }

  @Test
  public void test_generated() throws Exception {
    int orgCount = 1 * 1000 * 1000;
    int eventCount = 27 * 1000 * 1000;
    int maxEventSpaces = Math.min(10, EVENT_SPACES.size());
    init(((orgCount * ORG_SPACES.size()) + (eventCount * maxEventSpaces)) * LANGUAGES.length);
    IdGenerationStrategy strategy = new IdFirst8Byte("MD5");
    generateIds(strategy, new FullNameGenerator(ORG_SPACES, orgCount));
    generateIds(strategy, new FullNameGenerator(EVENT_SPACES.subList(0, maxEventSpaces),
        eventCount));
    printResult(strategy);
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

  private void runForFile(IdGenerationStrategy strategy) throws IOException {
    init(5582460);
    File file = new File(System.getProperty("user.home") + File.separator + FILE);
    LineIterator iter = FileUtils.lineIterator(file, "UTF-8");
    try {
      generateIds(strategy, iter);
    } finally {
      printResult(strategy);
      iter.close();
    }
  }

  private void init(int initCapacity) {
    initCapacity += 1000;
    System.out.println("Init TLongHashSet with capacity " + initCapacity);
    set = new TLongHashSet(initCapacity, 1);
    collisionCount = new int[(1 << BIT_COLL_HANDLING)];
  }

  private void generateIds(IdGenerationStrategy strategy, Iterator<String> fullNameIter) {
    int count = 0;
    while (fullNameIter.hasNext()) {
      String fullName = fullNameIter.next();
      if ((++count % 1000000) == 0) {
        System.out.println(Integer.toString(count) + " - " + fullName);
      }
      for (String lang : LANGUAGES) {
        String serializedFN = serialize(fullName, lang);
        long id = generateId(serializedFN, strategy);
        set.add(id);
      }
    }
  }

  private void printResult(IdGenerationStrategy strategy) {
    System.out.println(set.size() + " - " + (64 - BIT_OFFSET - BIT_COLL_HANDLING) + "+"
        + BIT_COLL_HANDLING + "bit " + strategy + " - " + Arrays.toString(collisionCount));
  }

  // serializes string like LocalUidStringEntityReferenceSerializer from XWiki 4
  private String serialize(String fullName, String lang) {
    String spaceName = fullName.substring(0, fullName.indexOf("."));
    String docName = fullName.substring(fullName.indexOf(".") + 1, fullName.length());
    String langName = (lang.isEmpty() ? "" : (lang.length() + ":" + lang));
    String serialized = spaceName.length() + ":" + spaceName + docName.length() + ":" + docName
        + langName;
    return serialized;
  }

  private long generateId(String str, IdGenerationStrategy strategy) {
    int count = 0;
    long id = strategy.getId(str) << (BIT_COLL_HANDLING + BIT_OFFSET);
    while (set.contains(id)) {
      if (count++ < (1 << BIT_COLL_HANDLING)) {
        System.out.println("Collision '" + toHex(id) + "' : " + str);
        id = ((id >> BIT_OFFSET) + 1) << BIT_OFFSET;
        collisionCount[Math.min(count - 1, 3)]++;
      } else {
        throw new RuntimeException(str);
      }
    }
    return id;
  }

  private String toHex(long nb) {
    return Hex.encodeHexString(ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(nb).array());
  }

}

abstract class IdGenerationStrategy {

  final String algo;

  IdGenerationStrategy() {
    this("");
  }

  IdGenerationStrategy(String algo) {
    this.algo = algo;
  }

  abstract long getId(String str);

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + (algo.isEmpty() ? "" : ("[" + algo + "]"));
  }
}

class IdFirst8Byte extends IdGenerationStrategy {

  IdFirst8Byte(String algo) {
    super(algo);
  }

  @Override
  public long getId(String str) {
    return Longs.fromByteArray(Utils.hash(str, algo));
  }
}

class IdCustomHashCode extends IdGenerationStrategy {

  IdCustomHashCode(String algo) {
    super(algo);
  }

  @Override
  public long getId(String str) {
    return Utils.hashCode64(Utils.hash(str, algo));
  }
}

class IdToHexAndHashCode extends IdGenerationStrategy {

  IdToHexAndHashCode(String algo) {
    super(algo);
  }

  @Override
  public long getId(String str) {
    return Utils.hashCode64(Hex.encodeHexString(Utils.hash(str, algo)));
  }
}

// from XWiki 4.0, only uses MD5
class IdXWiki extends IdGenerationStrategy {

  IdXWiki(String algo) {
    super(algo);
  }

  @Override
  public long getId(String str) {
    long hash = 0;
    byte[] digest = Utils.hash(str, algo);
    for (int l = digest.length, i = Math.max(0, digest.length - 9); i < l; i++) {
      hash = (hash << 8) | ((long) digest[i] & 0xFF);
    }
    return hash;
  }
}

class IdHashCode32 extends IdGenerationStrategy {

  @Override
  public long getId(String str) {
    return str.hashCode();
  }
}

class IdHashCode extends IdGenerationStrategy {

  @Override
  public long getId(String str) {
    return Utils.hashCode64(str);
  }
}

class IdRandom extends IdGenerationStrategy {

  @Override
  public long getId(String str) {
    return UUID.randomUUID().getLeastSignificantBits();
  }
}

class Utils {

  // from String.hashCode but with a long
  static long hashCode64(String str) {
    long h = 0;
    if ((h == 0) && (str.length() > 0)) {
      for (char c : str.toCharArray()) {
        h = (31 * h) + c;
      }
    }
    return h;
  }

  static long hashCode64(byte[] bytes) {
    long h = 0;
    if ((h == 0) && (bytes.length > 0)) {
      for (byte c : bytes) {
        h = (31 * h) + c;
      }
    }
    return h;
  }

  static byte[] hash(String str, String algo) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(algo);
      messageDigest.update(str.getBytes("utf-8"));
      return messageDigest.digest();
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      throw new RuntimeException();
    }
  }

}
