package se.swedenconnect.bankid.rpapi.service.impl;

import java.util.function.IntPredicate;

public class StatusCodePredicates {
  public static IntPredicate compareBetween(int from, int to) {
    return (toCompare) -> {
      return toCompare >= from && toCompare < to;
    };
  }

  public static IntPredicate userError() {
    return compareBetween(400, 500);
  }

  public static IntPredicate serverError() {
    return compareBetween(500, 600);
  }
}
