## Redis Implementation

{env} refers to the environment, i.e. prod
{client} is a randomly generated string

### Shared network-wide channels

#### `m:{env}:player:{uuid}`
Hash containing all the needed player data
- name
- proxy
- server

#### `m:{env}:players`
Set of player uuids on the whole network

#### `m:{env}:name2uuid:{name}`
Mapping for each player online that corresponds to their uuid

### Proxy Channels

#### `m:{env}:proxy:{proxy}:players`
Set of player uuids on this specific proxy

#### `m:{env}:proxies`
Set of proxy ids.

### Server Channels

#### `m:{env}:server:{server}:players`
Set of player uuids on this specific server

#### `m:{env}:servers`
Set of servers.

### PubSub Channel Formats

#### `m:{env}:clients:{client}:callback`
Registered by each client, allowing for receivers of messages from this client to send messages back, which allows for more complex communication flows

#### `m:{env}:{proxy|server}:{id}:channels:{channel}`
Format for channels registered through the api.

#### `m:{env}:channels:{id}`
Custom channels may be registered through the API

### KV Key Format
Mycelium has a KV API for consumers to use, values stored using this API are formatted the following way:
`m:{env}:store:{key}`
