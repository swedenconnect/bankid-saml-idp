/*
 * Copyright 2023 Sweden Connect
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

import lombok.AllArgsConstructor;
import lombok.Data;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static se.swedenconnect.bankid.rpapi.types.CollectResponse.Status.FAILED;
import static se.swedenconnect.bankid.rpapi.types.CollectResponse.Status.PENDING;

public class StatusCodeFactory {

  private static final List<StatusCodeResolver> resolvers;

  static {
    ArrayList<StatusCodeResolver> codeResolvers = new ArrayList<>();
    codeResolvers.add(new StatusCodeResolver("rfa1", c ->  PENDING.equals(c.getCollectResponse().getStatus()) && List.of(ProgressStatus.OUTSTANDING_TRANSACTION, ProgressStatus.NO_CLIENT).contains(c.getCollectResponse().getProgressStatus())));
    codeResolvers.add(new StatusCodeResolver("rfa3", c -> ErrorCode.CANCELLED.equals(c.getCollectResponse().getErrorCode())));
    codeResolvers.add(new StatusCodeResolver("rfa4", c -> ErrorCode.ALREADY_IN_PROGRESS.equals(c.getCollectResponse().getErrorCode())));
    codeResolvers.add(new StatusCodeResolver("rfa5", c -> Objects.nonNull(c.getCollectResponse().getErrorCode()) && List.of(ErrorCode.REQUEST_TIMEOUT, ErrorCode.MAINTENANCE, ErrorCode.INTERNAL_ERROR).contains(c.getCollectResponse().getErrorCode())));
    codeResolvers.add(new StatusCodeResolver("rfa6", c -> FAILED.equals(c.getCollectResponse().getStatus()) && ProgressStatus.NO_CLIENT.equals(c.getCollectResponse().getProgressStatus())));
    codeResolvers.add(new StatusCodeResolver("rfa8", c -> FAILED.equals(c.getCollectResponse().getStatus()) && ProgressStatus.EXPIRED_TRANSACTION.equals(c.getCollectResponse().getProgressStatus())));
    codeResolvers.add(new StatusCodeResolver("rfa9-sign", c -> BankIdOperation.SIGN.equals(c.getOperation()) && PENDING.equals(c.getCollectResponse().getStatus()) && ProgressStatus.USER_SIGN.equals(c.getCollectResponse().getProgressStatus()) && BankIdOperation.SIGN.equals(c.getOperation())));
    codeResolvers.add(new StatusCodeResolver("rfa9-auth", c -> BankIdOperation.SIGN.equals(c.getOperation()) && PENDING.equals(c.getCollectResponse().getStatus()) && ProgressStatus.USER_SIGN.equals(c.getCollectResponse().getProgressStatus()) && BankIdOperation.SIGN.equals(c.getOperation())));
    codeResolvers.add(new StatusCodeResolver("rfa13", c -> PENDING.equals(c.getCollectResponse().getStatus()) && ProgressStatus.OUTSTANDING_TRANSACTION.equals(c.getCollectResponse().getProgressStatus())));
    codeResolvers.add(new StatusCodeResolver("rfa21", c -> PENDING.equals(c.getCollectResponse().getStatus()) ));
    codeResolvers.add(new StatusCodeResolver("rfa22", c -> FAILED.equals(c.getCollectResponse().getStatus())));
    codeResolvers.add(new StatusCodeResolver("rfa9", c ->  c.getShowQr() && PENDING.equals(c.getCollectResponse().getStatus()) && ProgressStatus.USER_SIGN.equals(c.getCollectResponse().getProgressStatus())));
    codeResolvers.add(new StatusCodeResolver("ext2", c -> c.getShowQr() && PENDING.equals(c.getCollectResponse().getStatus())));
    resolvers = List.copyOf(codeResolvers);
  }



  public static String statusCode(CollectResponse json, Boolean showQr, BankIdOperation operation) {
    StatusData statusData = new StatusData(json, showQr, operation);
    Optional<String> message = resolvers.stream()
        .filter(r -> r.test(statusData))
        .map(StatusCodeResolver::getStatusCode)
        .findFirst();
    return "bankid.msg." + message.orElseGet(() -> "blank");
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
  private static class StatusCodeResolver {

    private final String statusCode;
    private final Predicate<StatusData> predicate;

    public boolean test(StatusData data) {
      return this.predicate.test(data);
    }
  }
}
