package se.swedenconnect.bankid.idp.authn.session;

import lombok.AllArgsConstructor;
import org.redisson.api.MapOptions;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;

import javax.servlet.http.HttpServletRequest;

import java.time.Instant;

import static se.swedenconnect.bankid.idp.authn.session.SessionAttributeKeys.*;

@AllArgsConstructor
public class RedisBankidSessions implements BankIdSessionWriter, BankIdSessionReader {

  // TODO: 2023-06-16 Write documentation, this is meant for production using loadbalanced environment

  private final RedissonClient client;

  @Override
  public BankIdSessionState loadSessionData(HttpServletRequest request) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    return (BankIdSessionState) map.get(BANKID_STATE_ATTRIBUTE);
  }

  @Override
  public CollectResponse laodCompletionData(HttpServletRequest request) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    return (CollectResponse) map.get(BANKID_COMPLETION_DATA_ATTRIBUTE);
  }

  @Override
  public PreviousDeviceSelection loadPreviousSelectedDevice(HttpServletRequest request) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    Object attribute = map.get(PREVIOUS_DEVICE_SESSION_ATTRIBUTE);
    if (attribute == null) {
      return null;
    }
    return (PreviousDeviceSelection) attribute;
  }

  @Override
  public void save(HttpServletRequest request, BankIdSessionData data) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    BankIdSessionState state = (BankIdSessionState) map.get(BANKID_STATE_ATTRIBUTE);
    if (state == null) {
      state = new BankIdSessionState();
    } else if (state.getBankIdSessionData().getOrderReference().equals(data.getOrderReference())) {
      state.pop();
    }
    state.push(data);
    map.put(BANKID_STATE_ATTRIBUTE, state);
  }

  private RMap<Object, Object> getRedisHashForUser(HttpServletRequest request) {
    RMap<Object, Object> map = client.getMap("session:%s".formatted(request.getSession().getId()));
    map.expire(Instant.now().plusSeconds(3600)); // TODO: 2023-06-16 Make expiration configurable
    return map;
  }

  @Override
  public void save(HttpServletRequest request, CollectResponse data) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    map.put(BANKID_COMPLETION_DATA_ATTRIBUTE, data);
  }

  @Override
  public void delete(HttpServletRequest request) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    map.remove(BANKID_COMPLETION_DATA_ATTRIBUTE);
    map.remove(BANKID_STATE_ATTRIBUTE);
  }

  @Override
  public void save(HttpServletRequest request, PreviousDeviceSelection previousDeviceSelection) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    map.put(PREVIOUS_DEVICE_SESSION_ATTRIBUTE, previousDeviceSelection);
  }
}
