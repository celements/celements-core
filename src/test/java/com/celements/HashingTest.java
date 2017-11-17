package com.celements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.google.common.primitives.Longs;

public class HashingTest extends AbstractComponentTest {

  private static final String FILE = "documents-generated.txt";
  private static final String[] LANGUAGES = new String[] { "", "en", "de", "fr", "it", "es", "zh" };
  private static final long BIT_OFFSET = 30;
  private static final int BIT_COLL_HANDLING = 2;

  private static Set<Long> map;
  private static int[] collisionCount;

  // @Test
  public void generateDocuments() throws Exception {
    generateDocumentListFile("", 10, (100 * 1000), (200 * 1000));
  }

  private void generateDocumentListFile(String path, int max, int orgDocCount, int eventDocCount)
      throws Exception {
    PrintWriter writer = new PrintWriter(path + "/documents-generated.txt", "UTF-8");
    try {
      List<String> orgDocs = Arrays.asList("Company.Company", "Person.Person", "Place.Place",
          "ProgonEvent.ProgonEvent");
      write(writer, orgDocs, max, orgDocCount);
      List<String> eventDocs = Arrays.asList("ImpEvents", "progonall", "Collection29-201701161025",
          "Collection2-201404281156", "Collection14-201511271829", "Collection9-201409021755",
          "Collection32-201702201154", "Collection24-201609051443", "Collection33-201702201154",
          "Collection13-201502261501", "Collection31-201702201152", "Collection6-201406091541",
          "Collection8-201406111135", "Collection30-201701181541", "Collection11-201410161753",
          "Collection15-201512161703", "Collection26-201611291039", "Collection10-201409021757");
      write(writer, eventDocs, max, eventDocCount);
    } finally {
      writer.close();
    }
  }

  private void write(PrintWriter writer, List<String> docs, int max, int count) {
    for (int i = 0; i < Math.min(docs.size(), max); i++) {
      for (int j = 1; j <= count; j++) {
        writer.println(docs.get(i) + "." + j);
      }
    }
  }

  @Test
  public void checkForCollisions() throws Exception {
    run(new IdHashCode32());
    run(new IdHashCode());
    for (String algo : Arrays.asList("MD5", /* "SHA1", */ "SHA-256")) {
      run(new IdFirst8Byte(algo));
      run(new IdCustomHashCode(algo));
      run(new IdToHexAndHashCode(algo));
      run(new IdXWiki(algo));
    }
  }

  private void run(IdGenerationStrategy strategy) throws Exception {
    map = new HashSet<>(20 * 1000 * 1000);
    collisionCount = new int[(1 << BIT_COLL_HANDLING)];
    BufferedReader reader = new BufferedReader(new InputStreamReader(
        getClass().getClassLoader().getResourceAsStream(FILE)));
    try {
      String fullName = reader.readLine();
      while (fullName != null) {
        for (String lang : LANGUAGES) {
          String serializedFN = serialize(fullName, lang);
          long id = generateId(serializedFN, strategy);
          map.add(id);
        }
        fullName = reader.readLine();
      }
    } finally {
      reader.close();
      printResult(strategy);
    }
  }

  private void printResult(IdGenerationStrategy strategy) {
    System.out.println(map.size() + " - " + (64 - BIT_OFFSET - BIT_COLL_HANDLING) + "+"
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
    while (map.contains(id)) {
      if (count++ < (1 << BIT_COLL_HANDLING)) {
        // System.out.println("Collision '" + toHex(id) + "': " + str + " - " + map.get(id));
        id = ((id >> BIT_OFFSET) + 1) << BIT_OFFSET;
        collisionCount[Math.min(count - 1, 3)]++;
      } else {
        throw new RuntimeException();
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
