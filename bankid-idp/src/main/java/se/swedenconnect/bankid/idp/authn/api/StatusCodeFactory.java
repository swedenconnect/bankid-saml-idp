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
package se.swedenconnect.bankid.idp.authn.api;

import static se.swedenconnect.bankid.rpapi.types.CollectResponse.Status.FAILED;
import static se.swedenconnect.bankid.rpapi.types.CollectResponse.Status.PENDING;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.swedenconnect.bankid.idp.ApplicationVersion;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;

public class StatusCodeFactory {

  private static final List<StatusResolver> RESOLVES = new ArrayList<>() {

    @Serial
    private static final long serialVersionUID = ApplicationVersion.SERIAL_VERSION_UID;

    {
      this.add(new StatusResolver("rfa1", c -> PENDING == c.getCollectResponse().getStatus() && ProgressStatus.NO_CLIENT
          == c.getCollectResponse().getProgressStatus() && !c.getShowQr()));
      this.add(new StatusResolver("rfa3", c -> FAILED == c.getCollectResponse().getStatus() && ErrorCode.CANCELLED
          == c.getCollectResponse().getErrorCode()));
      this.add(new StatusResolver("rfa4", c -> FAILED == c.getCollectResponse().getStatus() &&
          ErrorCode.ALREADY_IN_PROGRESS == c.getCollectResponse().getErrorCode()));
      this.add(new StatusResolver("rfa5",
          c -> FAILED == c.getCollectResponse().getStatus() && Objects.nonNull(c.getCollectResponse().getErrorCode())
              && List.of(ErrorCode.REQUEST_TIMEOUT, ErrorCode.MAINTENANCE, ErrorCode.INTERNAL_ERROR)
              .contains(c.getCollectResponse().getErrorCode())));
      this.add(new StatusResolver("rfa6", c -> FAILED == c.getCollectResponse().getStatus() && ErrorCode.USER_CANCEL
          == c.getCollectResponse().getErrorCode()));
      this.add(new StatusResolver("rfa8", c -> FAILED == c.getCollectResponse().getStatus() &&
          ErrorCode.EXPIRED_TRANSACTION == c.getCollectResponse().getErrorCode()));
      this.add(new StatusResolver("rfa9-auth", c -> PENDING == c.getCollectResponse().getStatus() &&
          ProgressStatus.USER_SIGN == c.getCollectResponse().getProgressStatus() &&
          BankIdOperation.AUTH == c.getOperation()));
      this.add(new StatusResolver("rfa9-sign", c -> PENDING == c.getCollectResponse().getStatus() &&
          ProgressStatus.USER_SIGN == c.getCollectResponse().getProgressStatus() &&
          BankIdOperation.SIGN == c.getOperation()));
      this.add(new StatusResolver("rfa13", c -> PENDING == c.getCollectResponse().getStatus() &&
          ProgressStatus.OUTSTANDING_TRANSACTION == c.getCollectResponse().getProgressStatus() && !c.getShowQr()));
      this.add(new StatusResolver("rfa21-auth", c -> PENDING == c.getCollectResponse().getStatus() && c.getOperation()
          == BankIdOperation.AUTH && Objects.isNull(c.getCollectResponse().getHintCode())));
      this.add(new StatusResolver("rfa21-sign", c -> PENDING == c.getCollectResponse().getStatus() && c.getOperation()
          == BankIdOperation.SIGN && Objects.isNull(c.getCollectResponse().getHintCode())));
      this.add(new StatusResolver("rfa22", c -> FAILED == c.getCollectResponse().getStatus()));
      this.add(new StatusResolver("rfa23", c -> PENDING == c.getCollectResponse().getStatus() &&
          ProgressStatus.USER_MRTD == c.getCollectResponse().getProgressStatus()));
      this.add(new StatusResolver("ext2", c -> PENDING == c.getCollectResponse().getStatus() &&
          ProgressStatus.OUTSTANDING_TRANSACTION == c.getCollectResponse().getProgressStatus() && c.getShowQr()));
    }
  };

  public static String statusCode(final CollectResponse json, final Boolean showQr, final BankIdOperation operation) {
    final StatusData statusData = new StatusData(json, showQr, operation);
    final Optional<String> message = RESOLVES.stream()
        .filter(r -> r.getPredicate().test(statusData))
        .map(StatusResolver::getResult)
        .findFirst();
    return "bankid.msg." + message.orElse("blank");
  }

  @AllArgsConstructor
  @Data
  private static class StatusData {
    CollectResponse collectResponse;
    Boolean showQr;
    BankIdOperation operation;
  }

  @AllArgsConstructor
  @Data
  private static class StatusResolver {
    String result;
    Predicate<StatusData> predicate;
  }
}
