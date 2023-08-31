export function getCookie(cookieName: string): string {
  const cookie: { [key: string]: string } = {};
  document.cookie.split(';').forEach(function (el) {
    const [key, value] = el.split('=');
    cookie[key.trim()] = value;
  });
  return cookie[cookieName] ? cookie[cookieName] : '';
}

export function getXSRFCookie() {
  return getCookie('XSRF-TOKEN');
}
