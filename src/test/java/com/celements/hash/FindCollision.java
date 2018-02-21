package com.celements.hash;

import static com.google.common.base.MoreObjects.*;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;

public class FindCollision {

  private static final int THREADS = Runtime.getRuntime().availableProcessors();
  private static final XWikiIdCalculator CALCULATOR = new XWikiIdCalculator();
  private static final FullNameGenerator FN_GENERATOR = new FullNameGenerator(Arrays.asList("XWiki",
      "Content", "Test", "asdf", "Page"), Arrays.asList("", "en", "fr", "de", "es", "no"), 0,
      Long.MAX_VALUE);

  private long time;

  @Test
  public void test() throws Exception {
    findCollision(40, "hash.list");
  }

  public static void main(String[] args) throws Exception {
    int blockSize = firstNonNull(Ints.tryParse(get(args, 0)), 1);
    String file = get(args, 1).isEmpty() ? "hash.list" : get(args, 1);
    new FindCollision().findCollision(blockSize, file);
  }

  private static String get(String[] args, int i) {
    return i < args.length ? args[i] : "";
  }

  private void findCollision(int blockSize, String file) throws Exception {
    time = System.currentTimeMillis();
    new File(file).delete();
    println("storing hashes to file: " + file);
    blockSize *= 1000000;
    println("block size: " + blockSize);
    println("thread count: " + THREADS);
    println("trying to find a collision ...");
    System.out.println();
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(file));
      int nb = 1;
      while (true) {
        SyncHashSet set = new SyncHashSet(blockSize);
        generateBlock(set, nb);
        set.checkContainsOnWrittenData(file);
        set.write(out);
        nb++;
        println(FN_GENERATOR.getCount() + " hashes generated taking " + (new File(file).length()
            / (1024 * 1024)) + "MB");
      }
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  private void generateBlock(final SyncHashSet set, int nb) throws Exception {
    print("generating block " + nb + "... ");
    long time = System.currentTimeMillis();
    execute(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        while (set.hasSpace()) {
          String fullName = FN_GENERATOR.next();
          set.add(CALCULATOR.calculateId(fullName));
        }
        return null;
      }

    });
    System.out.println("took " + ((System.currentTimeMillis() - time) / 1000) + "s");
  }

  private class SyncHashSet {

    private final int maxSize;
    private final TLongHashSet set;

    public SyncHashSet(int maxSize) {
      this.maxSize = maxSize;
      set = new TLongHashSet(maxSize, 1);
    }

    public boolean hasSpace() {
      return set.size() < maxSize;
    }

    public synchronized void add(long id) throws CollisionException {
      if (!set.add(id)) {
        throw new CollisionException(Long.toString(id));
      }
    }

    public synchronized void write(ObjectOutputStream out) throws Exception {
      TLongIterator iter = set.iterator();
      while (iter.hasNext()) {
        out.writeLong(iter.next());
      }
      out.flush();
    }

    public void checkContainsOnWrittenData(String file) throws Exception {
      print("checking hashes... ");
      long time = System.currentTimeMillis();
      try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
        List<Long> result = execute(new Callable<Long>() {

          @Override
          public Long call() throws Exception {
            try {
              while (true) {
                Long id = null;
                synchronized (in) {
                  id = in.readLong();
                }
                if (set.contains(id)) {
                  return id;
                }
              }
            } catch (EOFException exc) {
            }
            return null;
          }
        });
        if (!result.isEmpty()) {
          throw new CollisionException(Long.toString(result.get(0)));
        }
      }
      System.out.println("took " + ((System.currentTimeMillis() - time) / 1000) + "s");
    }

  }

  private static <V> List<V> execute(Callable<V> callable) throws Exception {
    ExecutorService ex = Executors.newFixedThreadPool(THREADS);
    List<FutureTask<V>> tasks = new ArrayList<>(THREADS);
    for (int i = 0; i < THREADS; i++) {
      tasks.add(new FutureTask<>(callable));
      ex.execute(tasks.get(i));
    }
    List<V> ret = new ArrayList<>();
    for (FutureTask<V> t : tasks) {
      ret.add(t.get());
    }
    ret.removeAll(Collections.singleton(null));
    return ret;
  }

  private void println(String msg) {
    System.out.println("[" + ((System.currentTimeMillis() - time) / 1000) + "] " + msg);
  }

  private void print(String msg) {
    System.out.print("[" + ((System.currentTimeMillis() - time) / 1000) + "] " + msg);
  }

}

class CollisionException extends Exception {

  private static final long serialVersionUID = 1L;

  public CollisionException(String msg) {
    super(msg);
  }

}

class XWikiIdCalculator {

  public long calculateId(String fullName) {
    long hash = 0;
    String serialized = serializeLocalUid(fullName);
    byte[] digest = HashUtils.hash(serialized, "MD5");
    for (int l = digest.length, i = Math.max(0, digest.length - 9); i < l; i++) {
      hash = (hash << 8) | ((long) digest[i] & 0xFF);
    }
    return hash;
  }

  private String serializeLocalUid(String fullName) {
    StringBuilder key = new StringBuilder();
    for (String name : Splitter.on('.').split(fullName)) {
      if (!name.isEmpty()) {
        key.append(name.length()).append(':').append(name);
      }
    }
    return key.toString();
  }

}
