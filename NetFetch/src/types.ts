export interface Tab {
  id: string;
  name: string;
  method: string;
  url: string;
  headers: KeyVal[];
  params: KeyVal[];
  body: BodyData;
  auth: AuthData;
  settings: Settings;
  preRequestScript?: string;
  testScript?: string;
  response?: ResponseData;
}

export interface KeyVal {
  id: string;
  key: string;
  value: string;
  enabled: boolean;
}

export interface BodyData {
  mode: 'none' | 'raw' | 'urlencoded' | 'formdata' | 'binary';
  raw?: string;
  urlencoded?: KeyVal[];
}

export interface AuthData {
  type: 'none' | 'bearer' | 'basic' | 'apikey' | 'oauth2';
  token?: string;
  username?: string;
  password?: string;
  key?: string;
  value?: string;
  in?: 'header' | 'query';
}

export interface Settings {
  ssl_verify: boolean;
  follow_redirects: boolean;
  timeout: number;
}

export interface ResponseData {
  status: number;
  statusText: string;
  headers: Record<string, string>;
  body: string;
  time: number;
  size: number;
}

export type CollectionItemType = 'collection' | 'folder' | 'request';

export interface CollectionItem {
  id: string;
  name: string;
  type: CollectionItemType;
  method?: string; // for requests
  tabData?: Tab; // for requests
  children?: CollectionItem[];
  expanded?: boolean;
}

export interface HistoryEntry {
  id: number;
  method: string;
  url: string;
  status: number;
  duration: number;
  timestamp: number;
}
