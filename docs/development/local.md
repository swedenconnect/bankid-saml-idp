# Running the service locally

To develop the service we recommend the use of the "local" profile.

In this mode the service will require two dependencies, a configured service provider (SP) and an instance of redis.
 

## Configure Service Provider
A preconfigured service provider for this IDP can be found at [test-my-eid](https://github.com/swedenconnect/test-my-eid)
ran with the local profile.

## Configure Redis
When running with the local profile, there is a docker-compose file for redis that works out of the box under the following path
`env/local/redis/docker-compose.yml`

