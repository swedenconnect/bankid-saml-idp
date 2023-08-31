export enum ApiResponseStatus {
  NOT_STARTED,
  IN_PROGRESS,
  ERROR,
  COMPLETE,
  CANCEL,
}
export interface ApiResponse {
  status: ApiResponseStatus;
  qrCode: string;
  autoStartToken: string;
  messageCode: string;
}

export enum StatusDescription {
  OK,
  ISSUES,
}
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
