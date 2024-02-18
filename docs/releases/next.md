# v0.3.1

> **[🎯 milestone](https://github.com/moclojer/moclojer/milestone/4?closed=1)**
> **[🔖 full Changelog](https://github.com/moclojer/moclojer/commits/v0.3.1)**

## What’s Changed

* Swagger in docs by @avelino <https://github.com/moclojer/moclojer/issues/196>
* Running moclojer as framework it has an error when hit the `localhost:8000`  by @matheusfrancisco <https://github.com/moclojer/moclojer/issues/201>
* Watcher is not restarting the moclojer when I change the spec file by @matheusfrancisco <https://github.com/moclojer/moclojer/issues/208>
* `BUG`: docker no main manifest attribute, in /app/moclojer.jar by @avelino <https://github.com/moclojer/moclojer/issues/210>
* Review structural organization the moclojer version data is stored by @avelino <https://github.com/moclojer/moclojer/issues/212>
* improve request logs, add more fields by @avelino <https://github.com/moclojer/moclojer/issues/214>
  * Updated `io.pedestal` library to version 0.6.3 for better stability and performance
  * Ensured proper handling of not-found HTTP responses 
* sentry support, if set `SENTRY_DSN` ([sentry doc](https://docs.sentry.io/platforms/node/guides/azure-functions/configuration/options/#dsn)) envvar send except to <sentry.io> by @avelino <https://github.com/moclojer/moclojer/issues/195>

## Contributors

* @avelino
* @matheusfrancisco
