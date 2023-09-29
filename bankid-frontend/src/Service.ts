import type { Ref } from 'vue';
import { getXSRFCookie } from '@/cookies';
import { PATHS } from './Redirects';
import type {
  ApiResponse,
  ApiResponseStatus,
  CustomerContactInformation,
  RetryResponse,
  SelectedDeviceInformation,
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
  const data: ApiResponse = await response.json();
  if (!response.ok) {
    if (response.status === 429) {
      const retryAfter = response.headers.get('Retry-After');
      return { retry: true, time: retryAfter } as RetryResponse;
    }
    // TODO handle unexpected error
  }
  return data;
}

function isApiResponse(obj: any): obj is ApiResponse {
  return obj && 'status' in obj;
}

function isRetryResponse(obj: any): obj is RetryResponse {
  return obj && 'retry' in obj;
}

export const polling = (
  otherDevice: boolean,
  qrImage: Ref<string>,
  hideAutoStart: Ref<boolean>,
  token: Ref<string>,
  messageCode: Ref<string>,
  responseStatus: Ref<ApiResponseStatus | null>,
) => {
  poll(otherDevice).then((response) => {
    if (isApiResponse(response)) {
      responseStatus.value = response.status;
      if (response.qrCode !== '') {
        qrImage.value = response.qrCode;
      }
      if (response.status !== 'NOT_STARTED') {
        qrImage.value = '';
      }
      hideAutoStart.value = response.status !== 'NOT_STARTED';
      token.value = response.autoStartToken;
      messageCode.value = response.messageCode;

      if (response.status === 'COMPLETE') {
        window.location.href = PATHS.COMPLETE;
      } else if (response.status === 'CANCEL') {
        window.location.href = PATHS.CANCEL;
      }
    }
    if (isRetryResponse(response) && response.retry === true) {
      /* Time is defined in seconds and setTimeout is in milliseconds */
      window.setTimeout(
        () => polling(otherDevice, qrImage, hideAutoStart, token, messageCode, responseStatus),
        parseInt(response.time) * 1000,
      );
    } else if (
      isRetryResponse(response) ||
      (isApiResponse(response) && (response.status === 'NOT_STARTED' || response.status === 'IN_PROGRESS'))
    ) {
      window.setTimeout(() => polling(otherDevice, qrImage, hideAutoStart, token, messageCode, responseStatus), 500);
    }
  });
};

const fetchData = async (endpoint: string): Promise<any> => (await fetch(CONTEXT_PATH + endpoint)).json();

export const status = async (): Promise<Status> => fetchData('/api/status');
export const contactInformation = async (): Promise<CustomerContactInformation> => fetchData('/api/contact');
export const cancel = async () => await fetch(CONTEXT_PATH + '/api/cancel', requestOptions);
export const uiInformation = async (): Promise<UiInformation> => fetchData('/api/ui');
export const selectedDevice = async (): Promise<SelectedDeviceInformation> => fetchData('/api/device');
export const getOverrides = async () => await fetchData('/api/overrides');
