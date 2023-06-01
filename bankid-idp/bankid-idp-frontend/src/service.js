import {getXSRFCookie} from "@/cookies";

export async function poll(showQr) {

    const requestOptions = {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getXSRFCookie() },
        credentials: 'include',
    };
    const response = await fetch("/idp/api/poll?qr=" + showQr, requestOptions);
    return await response.json();
}

export async function cancel() {
    const requestOptions = {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getXSRFCookie() },
        credentials: 'include',
    };
    const response = await fetch("/idp/api/cancel", requestOptions);
    return await response;
}

