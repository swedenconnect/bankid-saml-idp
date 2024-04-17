/*
 * Copyright 2023-2024 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.bankid.idp.integration.fixtures;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import se.swedenconnect.bankid.idp.integration.BankIdResponseFactory;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;

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
            BankIdResponseFactory
                .createCombined(c -> c.hintCode(ErrorCode.CANCELLED.getValue()).status(CollectResponse.Status.FAILED)),
            false),
        Arguments.of(
            "bankid.msg.rfa4",
            false,
            BankIdResponseFactory.createCombined(
                c -> c.hintCode(ErrorCode.ALREADY_IN_PROGRESS.getValue()).status(CollectResponse.Status.FAILED)),
            false),
        Arguments.of(
            "bankid.msg.rfa5",
            false,
            BankIdResponseFactory.createCombined(
                c -> c.hintCode(ErrorCode.MAINTENANCE.getValue()).status(CollectResponse.Status.FAILED)),
            false),
        Arguments.of(
            "bankid.msg.error.userCancel",
            false,
            BankIdResponseFactory.createCombined(
                c -> c.hintCode(ErrorCode.USER_CANCEL.getValue()).status(CollectResponse.Status.FAILED)),
            false),
        Arguments.of(
            "bankid.msg.rfa8",
            false,
            BankIdResponseFactory.createCombined(
                c -> c.hintCode(ErrorCode.EXPIRED_TRANSACTION.getValue()).status(CollectResponse.Status.FAILED)),
            false),
        Arguments.of("bankid.msg.rfa9-auth",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ProgressStatus.USER_SIGN.getValue())),
            false),
        Arguments.of("bankid.msg.rfa9-sign",
            true,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ProgressStatus.USER_SIGN.getValue())),
            false),
        Arguments.of("bankid.msg.rfa13",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ProgressStatus.OUTSTANDING_TRANSACTION.getValue())),
            false),
        Arguments.of("bankid.msg.rfa21-auth",
            false,
            BankIdResponseFactory.createCombined(c -> c),
            false),
        Arguments.of("bankid.msg.rfa21-sign",
            true,
            BankIdResponseFactory.createCombined(c -> c),
            false),
        Arguments.of("bankid.msg.rfa22",
            false,
            BankIdResponseFactory.createCombined(c -> c.status(CollectResponse.Status.FAILED)),
            false),
        Arguments.of("bankid.msg.rfa23",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ProgressStatus.USER_MRTD.getValue())),
            false),
        Arguments.of("bankid.msg.ext2",
            false,
            BankIdResponseFactory.createCombined(c -> c.hintCode(ProgressStatus.OUTSTANDING_TRANSACTION.getValue())),
            true));
  }
}
