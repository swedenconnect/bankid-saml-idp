package se.swedenconnect.bankid.idp.authn.session;

import lombok.AllArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

import static se.swedenconnect.bankid.idp.authn.session.SessionAttributeKeys.*;


/**
 * Redis Implementation of Session Storage for Bankid Data
 * Uses direct save/load in contrast to spring sessions load once, write once strategy
 */
@AllArgsConstructor
public class RedisBankidSessions implements BankIdSessionWriter, BankIdSessionReader {

  private final RedissonClient client;

  /**
   * {@inheritDoc}
   */
  @Override
  public BankIdSessionState loadSessionData(HttpServletRequest request) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    return (BankIdSessionState) map.get(BANKID_STATE_ATTRIBUTE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectResponse laodCompletionData(HttpServletRequest request) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    return (CollectResponse) map.get(BANKID_COMPLETION_DATA_ATTRIBUTE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PreviousDeviceSelection loadPreviousSelectedDevice(HttpServletRequest request) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    Object attribute = map.get(PREVIOUS_DEVICE_SESSION_ATTRIBUTE);
    if (attribute == null) {
      return null;
    }
    return (PreviousDeviceSelection) attribute;
  }

  private RMap<Object, Object> getRedisHashForUser(HttpServletRequest request) {
    return this.client.getMap("session:%s".formatted(request.getSession().getId()));
  }

  /**
   * {@inheritDoc}
   */
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
    map.fastPut(BANKID_STATE_ATTRIBUTE, state);
    map.expire(Instant.now().plusSeconds(request.getSession().getMaxInactiveInterval()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void save(HttpServletRequest request, CollectResponse data) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    map.fastPut(BANKID_COMPLETION_DATA_ATTRIBUTE, data);
    map.expire(Instant.now().plusSeconds(request.getSession().getMaxInactiveInterval()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void save(HttpServletRequest request, PreviousDeviceSelection previousDeviceSelection) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    map.fastPut(PREVIOUS_DEVICE_SESSION_ATTRIBUTE, previousDeviceSelection);
    map.expire(Instant.now().plusSeconds(request.getSession().getMaxInactiveInterval()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(HttpServletRequest request) {
    RMap<Object, Object> map = getRedisHashForUser(request);
    map.fastRemove(BANKID_COMPLETION_DATA_ATTRIBUTE);
    map.fastRemove(BANKID_STATE_ATTRIBUTE);
  }
}
