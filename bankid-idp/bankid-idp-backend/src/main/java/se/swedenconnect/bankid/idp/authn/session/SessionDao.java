package se.swedenconnect.bankid.idp.authn.session;

import javax.servlet.http.HttpServletRequest;

public interface SessionDao {
    void write(String key, Object value, HttpServletRequest request);

    <T> T read(String key, Class<T> tClass, HttpServletRequest request);

    void remove(String key, HttpServletRequest request);
}
