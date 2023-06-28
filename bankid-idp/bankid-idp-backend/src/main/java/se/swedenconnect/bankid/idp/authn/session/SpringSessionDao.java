package se.swedenconnect.bankid.idp.authn.session;

import javax.servlet.http.HttpServletRequest;

public class SpringSessionDao implements SessionDao {
  @Override
  public void write(String key, Object value, HttpServletRequest request) {
    request.getSession().setAttribute(key, value);
  }

  @Override
  public <T> T read(String key, Class<T> tClass, HttpServletRequest request) {
    return tClass.cast(request.getSession().getAttribute(key));
  }

  @Override
  public void remove(String key, HttpServletRequest request) {
    request.getSession().setAttribute(key, null);
  }
}
