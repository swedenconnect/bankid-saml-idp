package se.swedenconnect.bankid.idp.authn.session;

import lombok.AllArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

@AllArgsConstructor
public class RedisSessionDao implements SessionDao {

  private final RedissonClient client;

  @Override
  public void write(final String key, final Object value, final HttpServletRequest request) {
    final RMap<Object, Object> map = getRedisHashForUser(request);
    map.fastPut(key, value);
    map.expire(Instant.now().plusSeconds(request.getSession().getMaxInactiveInterval()));
  }

  @Override
  public <T> T read(final String key, final Class<T> tClass, final HttpServletRequest request) {
    final RMap<Object, Object> map = getRedisHashForUser(request);
    return tClass.cast(map.get(key));
  }

  @Override
  public void remove(final String key, final HttpServletRequest request) {
    final RMap<Object, Object> map = getRedisHashForUser(request);
    map.remove(key);
  }

  private RMap<Object, Object> getRedisHashForUser(final HttpServletRequest request) {
    return this.client.getMap("session:%s".formatted(request.getSession().getId()));
  }
}
