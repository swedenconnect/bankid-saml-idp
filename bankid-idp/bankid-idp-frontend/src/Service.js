import {getXSRFCookie} from "@/cookies";

const CONTEXT_PATH = import.meta.env.BASE_URL

export async function poll(showQr) {

  const requestOptions = {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-XSRF-TOKEN': getXSRFCookie()},
    credentials: 'include',
  };
  const response = await fetch(CONTEXT_PATH + "/api/poll?qr=" + showQr, requestOptions);
  const data = await response.json();
  if (!response.ok) {
    if (response.status === 429) {
      let retryAfter = response.headers.get("Retry-After");
      return {"retry": true, "time": retryAfter};
    }
    // TODO handle unexpected error
  }
  return data;
}

export async function cancel() {
  const requestOptions = {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-XSRF-TOKEN': getXSRFCookie()},
    credentials: 'include',
  };
  return await fetch(CONTEXT_PATH + "/api/cancel", requestOptions);
}

export async function spInformation() {
  return (await fetch(CONTEXT_PATH + "/api/sp")).json();
}

export async function selectedDecvice() {
  return (await fetch(CONTEXT_PATH + "/api/device")).json();

}

