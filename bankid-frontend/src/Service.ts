import type { Ref } from 'vue';
import { getXSRFCookie } from '@/cookies';
import { PATHS } from './Redirects';
import type {
  ApiResponse,
  ApiResponseStatus,
  CustomerContactInformation,
  RetryResponse,
  SelectedDeviceInformation, SessionExpiredResponse, UserErrorResponse,
  Status,
  UiInformation,
} from './types';

const CONTEXT_PATH = import.meta.env.BASE_URL;
const requestOptions: RequestInit = {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getXSRFCookie() },
  credentials: 'include',
};

export async function poll(showQr: boolean) {
  const response = await fetch(CONTEXT_PATH + '/api/poll?qr=' + showQr, requestOptions);
  const data = await response.json();
  if (!response.ok) {
    if (response.status === 429) {
      const retryAfter = response.headers.get('Retry-After');
      return { retry: true, time: retryAfter } as RetryResponse;
    }
    if (response.status === 403) {
      return {"sessionExpired": true } as SessionExpiredResponse;
    }
    if (response.status === 400) {
      return data as UserErrorResponse;
    }
  }
  return data as ApiResponse;
}

function isApiResponse(obj: any): obj is ApiResponse {
  return obj && 'status' in obj;
}

function isRetryResponse(obj: any): obj is RetryResponse {
  return obj && 'retry' in obj;
}

function isSessionExpiredResponse(obj: any): obj is SessionExpiredResponse {
  return obj && 'sessionExpired' in obj;
}

export function isUserErrorResponse(obj: any): obj is UserErrorResponse {
  return obj && 'errorMessage' in obj;
}

export const pollingQr = (
  qrImage: Ref<string>,
  messageCode: Ref<string>,
  responseStatus: Ref<ApiResponseStatus | undefined>,
  cancelRetry?: Ref<boolean>,
) => {
  const pollFunction = () => poll(true);
  pollFunction().then((response) => {
    handleResponse(response, pollFunction, qrImage, null, null, messageCode, responseStatus, cancelRetry);
  });
};

export const pollingAutoStart = (
  hideAutoStart: Ref<boolean>,
  token: Ref<string>,
  messageCode: Ref<string>,
  responseStatus: Ref<ApiResponseStatus | undefined>,
  cancelRetry?: Ref<boolean>,
) => {
  const pollFunction = () => poll(false);
  pollFunction().then((response) => {
    handleResponse(response, pollFunction, null, hideAutoStart, token, messageCode, responseStatus, cancelRetry);
  });
};

export function handleApiError(response : UserErrorResponse) {
  console.log("User error!");
  let location = import.meta.env.BASE_URL + "/bankid#/error/" + response.errorMessage;
  if (response.traceId !== "") {
    location = location + "/" + response.traceId;
  }
  window.location.href = location;
}

const handleResponse = (
  response: ApiResponse | RetryResponse | SessionExpiredResponse | UserErrorResponse,
  pollFunction: () => Promise<ApiResponse | RetryResponse | SessionExpiredResponse | UserErrorResponse>,
  qrImage: Ref<string> | null,
  hideAutoStart: Ref<boolean> | null,
  token: Ref<string> | null,
  messageCode: Ref<string>,
  responseStatus: Ref<ApiResponseStatus | undefined>,
  cancelRetry?: Ref<boolean>,
) => {
  if (isSessionExpiredResponse(response)) {
      window.location.href = PATHS.ERROR;
  }

  if (isUserErrorResponse(response)) {
    handleApiError(response);
  }

  if (isApiResponse(response)) {
    responseStatus.value = response.status;

    if (qrImage && response.qrCode !== '') {
      qrImage.value = response.qrCode;
    }

    if (response.status !== 'NOT_STARTED') {
      if (qrImage) {
        qrImage.value = '';
      }
      if (hideAutoStart) {
        hideAutoStart.value = true;
      }
    }

    if (token) {
      token.value = response.autoStartToken;
    }

    messageCode.value = response.messageCode;

    if (response.status === 'COMPLETE') {
      window.location.href = PATHS.COMPLETE;
    } else if (response.status === 'CANCEL') {
      window.location.href = PATHS.CANCEL;
    }
  }

  if (!cancelRetry?.value) {
    let timeout = 0;
    if (isRetryResponse(response) && response.retry === true) {
      /* Time is defined in seconds and setTimeout is in milliseconds */
      timeout = parseInt(response.time) * 1000;
    } else if (
      isRetryResponse(response) ||
      (isApiResponse(response) && (response.status === 'NOT_STARTED' || response.status === 'IN_PROGRESS'))
    ) {
      timeout = 500;
    }
    if (timeout > 0) {
      window.setTimeout(
        () =>
          pollFunction().then((response) =>
            handleResponse(
              response,
              pollFunction,
              qrImage,
              hideAutoStart,
              token,
              messageCode,
              responseStatus,
              cancelRetry,
            ),
          ),
        timeout,
      );
    }
  }
};

const fetchData = async (endpoint: string): Promise<any> => (await fetch(CONTEXT_PATH + endpoint)).json();

export const status = async (): Promise<Status> => fetchData('/api/status');
export const contactInformation = async (): Promise<CustomerContactInformation> => fetchData('/api/contact');
export const cancel = async () => await fetch(CONTEXT_PATH + '/api/cancel', requestOptions);
export const uiInformation = async (): Promise<UiInformation|UserErrorResponse> => fetchData('/api/ui');
export const selectedDevice = async (): Promise<SelectedDeviceInformation> => fetchData('/api/device');
export const getOverrides = async () => await fetchData('/api/overrides');
