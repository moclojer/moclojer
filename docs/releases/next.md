# v0.3.3

> **[🎯 milestone](https://github.com/moclojer/moclojer/milestone/6?closed=1)**
> **[🔖 full Changelog](https://github.com/moclojer/moclojer/commits/v0.3.3)**

## What’s Changed

* Added JSON logging format support. [PR #246](https://github.com/moclojer/moclojer/issues/246)
* Updated dependencies `@actions/artifact` to v2.1.5 and `@actions/core` to v1.0.1. [PR #562](https://github.com/actions/upload-artifact/pull/562)
* Added deprecation notice for versions v3/v2/v1 in the README. [PR #561](https://github.com/actions/upload-artifact/pull/561)
* Minor fix to the migration README. [PR #523](https://github.com/actions/upload-artifact/pull/523)
* HTTP `header` parameters as variables. [PR #251](https://github.com/moclojer/moclojer/issues/251)
* Added `interceptor-error-handler` function for formatting exceptions as JSON for Internal Server Errors. Integrated error handling into `get-interceptors`. Imported `io.pedestal.interceptor.error` and `clojure.data.json` [PR #254](https://github.com/moclojer/moclojer/pull/254)
* We should be able to create a dev and prod config map when start moclojer [PR #199](https://github.com/moclojer/moclojer/issues/199)
* Updated dependencies `@actions/artifact` to v2.1.6. This update includes improvements to the artifact upload process. [PR #257](https://github.com/moclojer/moclojer/pull/257)

## Contributors

* @j0suetm
* @eggyhead
* @robherley
* @andrewakim
* @avelino
