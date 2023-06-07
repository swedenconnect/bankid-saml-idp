import {UAParser} from 'ua-parser-js';

export function createLink(userAgent, token, location) {
    let uap = new UAParser(userAgent);
    let ua = uap.getResult();
    switch (getType(ua)) {
        case "pc":
            return getDefaultRedirect(token);
        case "iphone-phone":
            if (ua.browser.name === "Mobile Safari" || ua.browser.name === "Safari") {
                return "bankid:///?autostarttoken=" + token + "&redirect=https://" + location;
            }
            // Unsupported browser on iphone
            return getDefaultRedirect()
        case "android-phone":
            return "bankid:///?autostarttoken=" + token + "&redirect=https://" + location;
    }
    return getDefaultRedirect(token);
}

function getDefaultRedirect(token) {
    return "bankid:///?autostarttoken=" + token + "&redirect=null";
}

export function shallSelectDeviceAutomatically(userAgent) {
    let uap = new UAParser(userAgent);
    let ua = uap.getResult();
    switch (getType(ua)) {
        case "desktop":
            return false;
        case "iphone-phone":
        case "android-phone":
            return true;
    }
    return false;
}

export function getType(ua) {
    if (ua.device.type === undefined) {
        return "desktop";
    }
    if (ua.device.type === "mobile") {
        if (ua.os.name === "Android") {
            return "android-phone";
        }
        if (ua.os.name === "iOS") {
            return "iphone-phone"
        }
    }
    return "unknown";
}