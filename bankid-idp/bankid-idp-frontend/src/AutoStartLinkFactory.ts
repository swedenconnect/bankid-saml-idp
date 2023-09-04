import { UAParser } from 'ua-parser-js';

export function createLink(userAgent: string, token: string, location: string) {
  const uap = new UAParser(userAgent);
  const ua = uap.getResult();

  switch (getType(ua)) {
    case 'iphone-phone':
      if (ua.browser.name === 'Mobile Safari' || ua.browser.name === 'Safari') {
        getMobileRedirect(token, location);
      }
      // Unsupported browser on iphone
      return getDefaultRedirect(token);
    case 'android-phone':
      getMobileRedirect(token, location);
  }
  return getDefaultRedirect(token);
}

function getDefaultRedirect(token: string) {
  return 'bankid:///?autostarttoken=' + token + '&redirect=null';
}

function getMobileRedirect(token: string, location: string) {
  return 'https://app.bankid.com/?autostarttoken=' + token + '&redirect=' + location;
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
