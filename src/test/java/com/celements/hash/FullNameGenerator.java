package com.celements.hash;

import java.util.Iterator;
import java.util.List;

public class FullNameGenerator implements Iterator<String> {

  private final List<String> spaces;
  private final List<String> languages;
  private final long maxCount;

  private long count;
  private int spacePos = 0;
  private int langPos = 0;

  FullNameGenerator(List<String> spaces, List<String> langs, long startCount, long maxCount) {
    this.spaces = spaces;
    this.languages = langs;
    this.count = startCount;
    this.maxCount = maxCount;
  }

  @Override
  public boolean hasNext() {
    return count < maxCount;
  }

  @Override
  public String next() {
    String lang = languages.get(langPos);
    String fullName = spaces.get(spacePos) + count + (lang.isEmpty() ? "" : (":" + lang));
    if ((langPos = ++langPos % languages.size()) == 0) {
      if ((spacePos = ++spacePos % spaces.size()) == 0) {
        count++;
      }
    }
    return fullName;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
