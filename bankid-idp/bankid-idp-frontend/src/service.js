export async function poll(showQr) {
    const response = await  fetch("/idp/api/poll?qr=" + showQr);
    return await response.json();
}

export async function cancel() {
    const response = await  fetch("/idp/api/cancel");
    return await response;
}

