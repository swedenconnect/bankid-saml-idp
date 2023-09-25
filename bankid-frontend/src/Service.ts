import { getXSRFCookie } from '@/cookies';
import type {
  ApiResponse,
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

const fetchData = async (endpoint: string): Promise<any> => (await fetch(CONTEXT_PATH + endpoint)).json();

export const status = async (): Promise<Status> => fetchData('/api/status');
export const contactInformation = async (): Promise<CustomerContactInformation> => fetchData('/api/contact');
export const cancel = async () => await fetch(CONTEXT_PATH + '/api/cancel', requestOptions);
export const uiInformation = async (): Promise<UiInformation> => fetchData('/api/ui');
export const selectedDevice = async (): Promise<SelectedDeviceInformation> => fetchData('/api/device');
export const getOverrides = async () => await fetchData('/api/overrides');
