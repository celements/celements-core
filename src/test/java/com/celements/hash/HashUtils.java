package com.celements.hash;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

import com.celements.hash.IdCalculator.IdCalcStrategy;

public class HashUtils {

  // from String.hashCode but with a long
  public static long hashCode64(String str) {
    long h = 0;
    if ((h == 0) && (str.length() > 0)) {
      for (char c : str.toCharArray()) {
        h = (31 * h) + c;
      }
    }
    return h;
  }

  public static long hashCode64(byte[] bytes) {
    long h = 0;
    if ((h == 0) && (bytes.length > 0)) {
      for (byte c : bytes) {
        h = (31 * h) + c;
      }
    }
    return h;
  }

  public static byte[] hash(String str, String algo) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(algo);
      messageDigest.update(str.getBytes("utf-8"));
      return messageDigest.digest();
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      throw new RuntimeException();
    }
  }

  public static String toHex(long nb) {
    return Hex.encodeHexString(ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(nb).array());
  }

  public static void printResult(int size, int[] collisionCount, IdCalcStrategy strategy) {
    System.out.println("Size " + size + " - " + (64 - HashingTest.BIT_OFFSET
        - HashingTest.BIT_COLL_HANDLING) + "+" + HashingTest.BIT_COLL_HANDLING + "bit " + strategy
        + " - " + Arrays.toString(collisionCount));
  }

}
