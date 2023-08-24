package se.swedenconnect.bankid.idp.audit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.audit.AuditEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@AllArgsConstructor
public class AuditEventMapper {

  private final ObjectMapper mapper;

  public String write(AuditEvent event) {
    try {
      return mapper.writerFor(AuditEvent.class).writeValueAsString(event);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public AuditEvent read(String event) {
    try {
      return mapper.readerFor(BankidAuditEvent.class).readValue(event);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static class BankidAuditEvent extends AuditEvent {

    @JsonCreator
    public BankidAuditEvent(@JsonProperty("principal") String principal, @JsonProperty("type") String type, @JsonProperty("data") Map<String, Object> data) {
      super(principal, type, data);
    }
  }
}
