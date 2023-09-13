package se.swedenconnect.bankid.idp.integration.fixtures;

import org.junit.jupiter.params.provider.Arguments;
import se.swedenconnect.bankid.idp.integration.BankIdResponseFactory;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;

import java.util.stream.Stream;

public class MessageValidationArguments {
  public static Stream<Arguments> getAll() {
    return Stream.of(
        Arguments.of(
            "bankid.msg.rfa1",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ProgressStatus.NO_CLIENT.getValue())),
            false),
        Arguments.of(
            "bankid.msg.rfa3",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ErrorCode.CANCELLED.getValue()).status(CollectResponse.Status.FAILED)),
            false),
        Arguments.of(
            "bankid.msg.rfa4",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ErrorCode.ALREADY_IN_PROGRESS.getValue()).status(CollectResponse.Status.FAILED)),
            false
        ),
        Arguments.of(
            "bankid.msg.rfa5",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ErrorCode.MAINTENANCE.getValue()).status(CollectResponse.Status.FAILED)),
            false
        ),
        Arguments.of(
            "bankid.msg.rfa6",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ErrorCode.USER_CANCEL.getValue()).status(CollectResponse.Status.FAILED)),
            false
        ),
        Arguments.of(
            "bankid.msg.rfa8",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ErrorCode.EXPIRED_TRANSACTION.getValue()).status(CollectResponse.Status.FAILED)),
            false
        ),
        Arguments.of("bankid.msg.rfa9-auth",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ProgressStatus.USER_SIGN.getValue())),
            false
        ),
        Arguments.of("bankid.msg.rfa9-sign",
            true,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ProgressStatus.USER_SIGN.getValue())),
            false
        ),
        Arguments.of("bankid.msg.rfa13",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ProgressStatus.OUTSTANDING_TRANSACTION.getValue())),
            false
        ),
        Arguments.of("bankid.msg.rfa21-auth",
            false,
            BankIdResponseFactory.createCombined(c -> c),
            false
        ),
        Arguments.of("bankid.msg.rfa21-sign",
            true,
            BankIdResponseFactory.createCombined(c -> c),
            false
        ),
        Arguments.of("bankid.msg.rfa22",
            false,
            BankIdResponseFactory.createCombined(c -> c.status(CollectResponse.Status.FAILED)),
            false
        ),
        Arguments.of("bankid.msg.rfa23",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ProgressStatus.USER_MRTD.getValue())),
            false
        ),
        Arguments.of("bankid.msg.ext2",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ProgressStatus.NO_CLIENT.getValue())),
            true
        )
    );
  }
}
