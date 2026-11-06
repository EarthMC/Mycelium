export interface Stat {
  title: string;
  icon: string;
  value: number | string;
  success: boolean;
  to: string | undefined;
}

export interface ProxyResponse {
  id: string;
  players: number;
}

export interface ServerResponse {
  name: string;
  players: number;
}

export interface ProxyListResponse {
  totalPlayers: number;
  proxies: ProxyResponse[];
}

export interface PlayerResponse {
  name: string;
  uuid: string;
  server?: string;
  proxy?: string;
}
