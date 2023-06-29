package se.swedenconnect.bankid.idp.authn.session;

import javax.servlet.http.HttpServletRequest;

public class SpringSessionDao implements SessionDao {
  @Override
  public void write(final String key, final Object value, final HttpServletRequest request) {
    request.getSession().setAttribute(key, value);
  }

  @Override
  public <T> T read(final String key, final Class<T> tClass, final HttpServletRequest request) {
    return tClass.cast(request.getSession().getAttribute(key));
  }

  @Override
  public void remove(final String key, final HttpServletRequest request) {
    request.getSession().setAttribute(key, null);
  }
}
