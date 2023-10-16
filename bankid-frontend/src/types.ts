export type LangObject = {
  [key: string]: string | LangObject;
};

export type Messages = {
  [lang: string]: LangObject;
};

export type MessageOverride = {
  code: string;
  text: LangObject;
};

const enum ApiResponseStatusEnum {
  NOT_STARTED,
  IN_PROGRESS,
  ERROR,
  COMPLETE,
  CANCEL,
}
export type ApiResponseStatus = keyof typeof ApiResponseStatusEnum;
export interface ApiResponse {
  status: ApiResponseStatus;
  qrCode: string;
  autoStartToken: string;
  messageCode: string;
}

export interface RetryResponse {
  retry: boolean;
  time: string;
}

export interface SessionExpiredResponse {
  sessionExpired: boolean;
  redirect: string;
}

export interface UserErrorResponse {
  errorMessage: string;
  traceId: string;
}

const enum StatusDescriptionEnum {
  OK,
  ISSUES,
}
export type StatusDescription = keyof typeof StatusDescriptionEnum;
export interface Status {
  status: StatusDescription;
}

export interface CustomerContactInformation {
  email: string;
  displayInformation: boolean;
}

export interface SpInformation {
  displayNames: { [language: string]: string };
  imageUrl: string;
  showSpMessage: boolean;
}

export interface UiInformation {
  sp: SpInformation;
  displayQrHelp: boolean;
  qrSize: string;
  accessibilityReportLink: string | null;
  providerName: LangObject;
  qrDisplayInMinutes: bigint;
}

export interface SelectedDeviceInformation {
  isSign: boolean;
  device: string;
}
