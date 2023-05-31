package se.swedenconnect.bankid.idp.authn;

import se.swedenconnect.bankid.rpapi.types.CollectResponseJson;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static se.swedenconnect.bankid.rpapi.types.CollectResponseJson.Status.FAILED;
import static se.swedenconnect.bankid.rpapi.types.CollectResponseJson.Status.PENDING;

public class StatusCodeFactory {

  private static final Map<Predicate<CollectResponseJson>, String> MESSAGE_CONDITIONS = Map.of(
      c -> PENDING.equals(c.getStatus()) && List.of(ProgressStatus.OUTSTANDING_TRANSACTION, ProgressStatus.NO_CLIENT).contains(c.getProgressStatus()), "rfa1",
      c -> ErrorCode.CANCELLED.equals(c.getErrorCode()), "rfa3",
      c -> ErrorCode.ALREADY_IN_PROGRESS.equals(c.getErrorCode()), "rfa4",
      c -> List.of(ErrorCode.REQUEST_TIMEOUT, ErrorCode.MAINTENANCE, ErrorCode.INTERNAL_ERROR).contains(c), "rfa5",
      c -> FAILED.equals(c.getStatus()) && ProgressStatus.NO_CLIENT.equals(c.getProgressStatus()), "rfa6",
      c -> FAILED.equals(c.getStatus()) && ProgressStatus.EXPIRED_TRANSACTION.equals(c.getProgressStatus()), "rfa8",
      c -> PENDING.equals(c.getStatus()) && ProgressStatus.USER_SIGN.equals(c.getProgressStatus()), "rfa9",
      c -> PENDING.equals(c.getStatus()) && ProgressStatus.OUTSTANDING_TRANSACTION.equals(c.getProgressStatus()), "rfa13"
  );

  public static String statusCode(CollectResponseJson json) {
    Optional<String> message = MESSAGE_CONDITIONS.entrySet().stream()
        .filter(kv -> kv.getKey().test(json))
        .map(Map.Entry::getValue)
        .findFirst();
    return "bankid.msg." + message.orElseGet(() -> "rfa22");
  }
}
