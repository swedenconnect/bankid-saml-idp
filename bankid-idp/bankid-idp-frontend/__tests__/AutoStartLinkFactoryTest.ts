import {expect, test} from "vitest";
import {UAParser} from "ua-parser-js";
import {createLink, shallSelectDeviceAutomatically, getType} from "../src/AutoStartLinkFactory";

const testArguments = [
    {
        "name": "mac-os-computer-chrome",
        "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36",
        "device": "desktop",
        "link": "bankid:///?autostarttoken=token&redirect=null",
        "automaticDeviceSelect": false
    },
    {
        "name": "android-phone-chrome",
        "userAgent": "Mozilla/5.0 (Linux; Android 12; SM-S906N Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/80.0.3987.119 Mobile Safari/537.36",
        "device": "android-phone",
        "link": "bankid:///?autostarttoken=token&redirect=https://location.se",
        "automaticDeviceSelect": true
    },
    {
        "name": "iphone-mobile-safari",
        "userAgent": "Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/604.1",
        "device": "iphone-phone",
        "link": "bankid:///?autostarttoken=token&redirect=https://location.se",
        "automaticDeviceSelect": true
    }
];


test("Parameterized Test : Device Detection : Link Creation", () => {
    testArguments.forEach(arg => {
        let appLink = createLink(arg["userAgent"], "token", "location.se");
        expect(appLink, "Link was not same as expected actual:" + appLink + " expected:" + arg["link"])
            .toBe(arg["link"]);
    })
});
test("Parameterized Test : Device Detection : Start Automatically", () => {
    testArguments.forEach(arg => {
        let automaticDeviceSelected = shallSelectDeviceAutomatically(arg["userAgent"]);
        expect(automaticDeviceSelected, "Automatic start was not same as expected actual:" + automaticDeviceSelected + " expected:" + arg["automaticDeviceSelect"])
            .toBe(arg["automaticDeviceSelect"]);
    })
});

test("Parameterized Test : Device Detection : Device Type", () => {
    testArguments.forEach(arg => {
        let uap = new UAParser(arg["userAgent"]);
        let ua = uap.getResult();
        let device = getType(ua);
        console.log(ua);
        expect(device, "Device was not same as expected actual:" + device + " expected:" + arg["device"])
            .toBe(arg["device"]);
    })
});