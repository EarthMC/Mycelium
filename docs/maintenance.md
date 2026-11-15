# Maintaining Mycelium

Mycelium is designed to not require any manual maintenance during regular operation, with at most some minimal intervention when larger changes are made.
In more exceptional cases which Mycelium is not equipped to handle (yet), desyncs may occur which aren't able to be fixed automatically.

## Using the web interface
The easiest way to perform basic operations like viewing, deleting, creating, or deleting data is to use the web interface for Mycelium. Using the
interface, all player/server/proxy data is laid out in a simple dashboard.

## Sidenote: manually inspecting and modifying the database
Since all runtime data lives in Redis, it's possible to manually make small changes to the database by referencing the [key format](redis-key-format.md) and
the Redis commands for [sets](https://redis.io/docs/latest/commands/?group=set) or [hashes](https://redis.io/docs/latest/commands/?group=hash).
Though this quickly becomes unpractical when needing to make changes on a larger scale.

## Possible error cases and how to deal with them

### Unexpected proxy crash
When a proxy suddenly crashes, the usual cleanup logic is not able to run, which causes stale player and proxy data to remain. There are several ways of dealing with this.

#### 1. Starting a proxy with the same id
During startup, the proxy plugin has logic for cleaning up stale user data for players who are still considered to be on that proxy. This can be used as
a mostly automatic way of getting the database back in sync.

Starting a new proxy depends on the exact setup, at EarthMC the start-proxy script accepts an optional proxy id argument, i.e. `./start-proxy.sh <proxy id>`

#### 2. Deleting the proxy using the web interface
With the web interface, deleting the proxy in the proxies tab will also clear the proxy field for all players considered to be on the proxy. After that, you
can go to the players tab and filter by all players on the "Unknown" proxy to bulk delete them.
