package com.celements.hash;

import java.util.List;
import java.util.concurrent.RecursiveAction;

public class IdCalcTask extends RecursiveAction {

  private static final long serialVersionUID = 1L;

  private static final int TASK_SPLIT_SIZE = 100 * 1000;

  private final String name;
  private final boolean isSubTask;
  private final IdCalculator calculator;
  private final List<String> spaces;
  private final List<String> langs;
  private final long from;
  private final long to;

  public IdCalcTask(String name, IdCalculator calc, List<String> spaces, List<String> langs,
      long from, long to) {
    this(name, false, calc, spaces, langs, from, to);
  }

  private IdCalcTask(String name, boolean isSubTask, IdCalculator calc, List<String> spaces,
      List<String> langs, long from, long to) {
    this.name = name;
    this.isSubTask = isSubTask;
    this.calculator = calc;
    this.spaces = spaces;
    this.langs = langs;
    this.from = from;
    this.to = to;
  }

  @Override
  protected void compute() {
    if ((to - from) > TASK_SPLIT_SIZE) {
      long splitAt = Math.min(from + TASK_SPLIT_SIZE, to);
      IdCalcTask left = createSubTask(from, splitAt);
      IdCalcTask right = createSubTask(splitAt, to);
      invokeAll(left, right);
    } else {
      calculator.calc(new FullNameGenerator(spaces, langs, from, to));
      System.out.println(name + ": calculated " + from + "-" + to + " | "
          + calculator.setToString());
    }
    if (!isSubTask) {
      System.out.println(name + " finished " + from + "-" + to + " | " + calculator.setToString());
    }
  }

  private IdCalcTask createSubTask(long from, long to) {
    return new IdCalcTask(name, true, calculator, spaces, langs, from, to);
  }

}
