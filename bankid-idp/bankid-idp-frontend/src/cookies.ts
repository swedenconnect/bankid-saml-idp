export function getCookie(cookieName) {
  let cookie = {};
  document.cookie.split(';').forEach(function (el) {
    let [key, value] = el.split('=');
    cookie[key.trim()] = value;
  });
  return cookie[cookieName];
}

export function getXSRFCookie() {
  return getCookie('XSRF-TOKEN');
}
