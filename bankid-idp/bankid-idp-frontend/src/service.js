export async function auth() {
    const response = await fetch('/idp/auth');
    return await response.json();
}

export async function poll() {
    const response = await  fetch("/idp/poll");
    return await response.json();
}