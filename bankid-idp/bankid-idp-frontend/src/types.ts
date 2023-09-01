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
}

export interface SelectedDeviceInformation {
  isSign: boolean;
  device: string;
}
