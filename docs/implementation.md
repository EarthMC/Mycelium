## Redis Implementation

{env} refers to the environment, i.e. prod

`m:{env}:player:{uuid}`: Hash containing all the needed player data
- name
- proxy
- server

`m:{env}:players`: Set of player uuids on the whole network

`m:{env}:server:{server}:players`: Set of player uuids on this specific server

`m:{env}:proxy:{proxy}:players`: Set of player uuids on this specific proxy

`m:{env}:name2uuid:{name}`: Mapping for each player online that corresponds to their uuid

`m:{env}:proxies`: Set of proxy ids.

`m:{env}:servers`: Set of servers.

### PubSub channels

`m:{env}:clients:{client}:callback`: Callback channel, registered by each client.

## Proxy implementation

