package com.celements.hash;

import java.util.List;
import java.util.concurrent.RecursiveAction;

import org.apache.commons.lang.math.LongRange;

public class IdCalcTask extends RecursiveAction {

  private static final long serialVersionUID = 1L;

  private String name;
  private IdCalculator calculator;
  private List<String> spaces;
  private List<String> langs;
  private LongRange range;

  IdCalcTask(String name, IdCalculator calc, List<String> spaces, List<String> langs,
      LongRange range) {
    this.name = name;
    this.calculator = calc;
    this.spaces = spaces;
    this.langs = langs;
    this.range = range;
  }

  @Override
  protected void compute() {
    if ((range.getMaximumLong() - range.getMinimumLong()) > HashingTest.SPLIT_SIZE) {
      long nextMax = Math.min(range.getMinimumLong() + HashingTest.SPLIT_SIZE,
          range.getMaximumLong());
      IdCalcTask left = new IdCalcTask(name, calculator, spaces, langs, new LongRange(
          range.getMinimumLong(), nextMax));
      IdCalcTask right = new IdCalcTask(name, calculator, spaces, langs, new LongRange(nextMax,
          range.getMaximumLong()));
      invokeAll(left, right);
    } else {
      calculator.calc(new FullNameGenerator(spaces, langs, range.getMinimumLong(),
          range.getMaximumLong()));
      System.out.println(name + " " + range + " finished");
    }
  }
}
