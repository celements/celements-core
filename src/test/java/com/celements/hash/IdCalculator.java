package com.celements.hash;

import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

import com.google.common.primitives.Longs;

public class IdCalculator {

  public final IdCalcStrategy strategy;
  private final HashingSet set;

  public IdCalculator(HashingSet set, IdCalcStrategy strategy) {
    this.set = set;
    this.strategy = strategy;
  }

  public void calc(Iterator<String> fullNameIter) {
    // BiMap<String, Long> origIdMap = HashBiMap.create();
    while (fullNameIter.hasNext()) {
      String fullName = fullNameIter.next();
      long originalId = strategy.getId(serialize(fullName)) << (HashingTest.BIT_COLL_HANDLING
          + HashingTest.BIT_OFFSET);
      long correctedId = set.addWithCollisionHandling(originalId);
      if (originalId != correctedId) {
        System.out.println("Collision on '" + fullName + "': " + HashUtils.toHex(originalId)
            + " -> " + HashUtils.toHex(correctedId));
      }
    }
    // List<Long> origIds = new ArrayList<>(origIdMap.values());
    // List<Long> corrIds = set.addWithCollisionHandling(origIds);
    // for (int i = 0; i < origIds.size(); i++) {
    // if (origIds.get(i) != corrIds.get(i)) {
    // System.out.println("Collision on '" + origIdMap.inverse().get(origIds.get(i)) + "': "
    // + HashUtils.toHex(origIds.get(i)) + " -> " + HashUtils.toHex(corrIds.get(i)));
    // }
    // }
    HashUtils.printResult(set.size(), set.getCollisionCount(), strategy);
  }

  // serializes string like LocalUidStringEntityReferenceSerializer from XWiki 4
  private String serialize(String fullName) {
    int docIdx = fullName.indexOf(".");
    int langIdx = fullName.indexOf(":");
    String space = fullName.substring(0, docIdx);
    String doc = fullName.substring(docIdx + 1, langIdx > 0 ? langIdx : fullName.length());
    String lang = langIdx > 0 ? fullName.substring(langIdx + 1, fullName.length()) : "";
    return space.length() + ":" + space + doc.length() + ":" + doc + (lang.isEmpty() ? ""
        : lang.length() + ":" + lang);
  }

  static abstract class IdCalcStrategy {

    final String algo;

    IdCalcStrategy() {
      this("");
    }

    IdCalcStrategy(String algo) {
      this.algo = algo;
    }

    abstract long getId(String str);

    @Override
    public String toString() {
      return this.getClass().getSimpleName() + (algo.isEmpty() ? "" : ("[" + algo + "]"));
    }

  }

  static class IdFirst8Byte extends IdCalcStrategy {

    IdFirst8Byte(String algo) {
      super(algo);
    }

    @Override
    public long getId(String str) {
      return Longs.fromByteArray(HashUtils.hash(str, algo));
    }
  }

  static class IdCustomHashCode extends IdCalcStrategy {

    IdCustomHashCode(String algo) {
      super(algo);
    }

    @Override
    public long getId(String str) {
      return HashUtils.hashCode64(HashUtils.hash(str, algo));
    }
  }

  static class IdToHexAndHashCode extends IdCalcStrategy {

    IdToHexAndHashCode(String algo) {
      super(algo);
    }

    @Override
    public long getId(String str) {
      return HashUtils.hashCode64(Hex.encodeHexString(HashUtils.hash(str, algo)));
    }
  }

  // from XWiki 4.0, only uses MD5
  static class IdXWiki extends IdCalcStrategy {

    IdXWiki(String algo) {
      super(algo);
    }

    @Override
    public long getId(String str) {
      long hash = 0;
      byte[] digest = HashUtils.hash(str, algo);
      for (int l = digest.length, i = Math.max(0, digest.length - 9); i < l; i++) {
        hash = (hash << 8) | ((long) digest[i] & 0xFF);
      }
      return hash;
    }
  }

  static class IdHashCode32 extends IdCalcStrategy {

    @Override
    public long getId(String str) {
      return str.hashCode();
    }
  }

  static class IdHashCode extends IdCalcStrategy {

    @Override
    public long getId(String str) {
      return HashUtils.hashCode64(str);
    }
  }

  static class IdRandom extends IdCalcStrategy {

    @Override
    public long getId(String str) {
      return UUID.randomUUID().getLeastSignificantBits();
    }
  }

}
