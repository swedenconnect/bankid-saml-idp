export async function auth() {
    const response = await fetch('/idp/api/auth');
    return await response.json();
}

export async function poll() {
    const response = await  fetch("/idp/api/poll");
    return await response.json();
}

export async function cancel() {
    const response = await  fetch("/idp/api/cancel");
    return await response;
}

