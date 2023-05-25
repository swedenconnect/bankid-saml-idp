package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BankIdSessionData {
    private String autoStartToken;
    private String qrStartToken;
    private String qrStartSecret;
    private Instant startTime;
    private String orderReference;
    private ProgressStatus status;

    public static BankIdSessionData of(OrderResponse response) {
        return new BankIdSessionData(
                response.getAutoStartToken(),
                response.getQrStartToken(),
                response.getQrStartSecret(),
                response.getOrderTime(),
                response.getOrderReference(),
                ProgressStatus.STARTED);
    }

    public static BankIdSessionData of(BankIdSessionData previous, CollectResponse collect) {
        return new BankIdSessionData(
                previous.autoStartToken,
                previous.qrStartToken,
                previous.qrStartSecret,
                previous.getStartTime(),
                previous.getOrderReference(),
                collect.getProgressStatus()
        );
    }
}
