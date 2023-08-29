package se.swedenconnect.bankid.rpapi.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;

class StatusCodePredicatesTest {

  @Test
  void statusIsBetween400And500() {
    IntPredicate userErrorPredicate = StatusCodePredicates.userError();
    Assertions.assertTrue(userErrorPredicate.test(400));
    Assertions.assertTrue(userErrorPredicate.test(499));
    Assertions.assertFalse(userErrorPredicate.test(500));
  }
}