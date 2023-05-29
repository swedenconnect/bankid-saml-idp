package se.swedenconnect.bankid.idp.authn.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.impl.bootstrap.HttpServer;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CollectResponseJson;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Data
public class CollectResponseEvent {
  private final HttpServletRequest request;
  private final CollectResponseJson collectResponse;
}
