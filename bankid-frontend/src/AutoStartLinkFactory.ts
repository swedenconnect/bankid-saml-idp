import { UAParser } from 'ua-parser-js';

export function createLink(userAgent: string, token: string, location: string) {
  const uap = new UAParser(userAgent);
  const ua = uap.getResult();
  switch (getType(ua)) {
    case 'iphone-phone':
      if (ua.browser.name === 'Mobile Safari' || ua.browser.name === 'Safari') {
        return getMobileRedirect(token, location);
      }
      if (ua.browser.name === 'Chrome') {
        return getIphoneRedirect(token, location, ua.browser.name);
      }
      // Unsupported browser on iphone
      return getDefaultRedirect(token);
    case 'android-phone':
      return getMobileRedirect(token, location);
  }
  return getDefaultRedirect(token);
}

function getDefaultRedirect(token: string) {
  return 'bankid:///?autostarttoken=' + token + '&redirect=null';
}

function getMobileRedirect(token: string, location: string) {
  return 'https://app.bankid.com/?autostarttoken=' + token + '&redirect=' + location + '#anchor';
}

export function shallSelectDeviceAutomatically(userAgent: string) {
  const uap = new UAParser(userAgent);
  const ua = uap.getResult();
  switch (getType(ua)) {
    case 'desktop':
      return false;
    case 'iphone-phone':
    case 'android-phone':
      return true;
  }
  return false;
}

export function getType(ua: UAParser.IResult) {
  if (ua.device.type === undefined) {
    return 'desktop';
  }
  if (ua.device.type === 'mobile') {
    if (ua.os.name === 'Android') {
      return 'android-phone';
    }
    if (ua.os.name === 'iOS') {
      return 'iphone-phone';
    }
  }
  return 'unknown';
}

function getIphoneRedirect(token: string, location: string, browser: string) {
  let appLink = getIphoneAppLink(browser);
  if (appLink !== 'missing') {
    return getMobileRedirect(token, appLink).replace('#anchor', '');
  }
  // Fallback to let the user switch app
  return getDefaultRedirect(token);
}

function getIphoneAppLink(browser: string) {
  if (browser === 'Chrome') {
    return 'googlechromes://';
  }
  return 'missing';
}
