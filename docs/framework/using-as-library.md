---
description: Moclojer supports use as a library (package), making it possible to import and extend standard behavior
---

# Using moclojer as a framework

Do you know when you want to use (binary) software, but you miss a feature that doesn't yet exist in its core (or the maintainers believe it doesn't make sense to implement)?
With this in mind, moclojer makes it possible to extend its default behavior by using it as a library, importing the packages you need and changing the default behavior.

## Where to start?

moclojer consists of two main features:

- **specification** for writing http configuration
- http server _(we use pedestal)_

The specification is the main feature of moclojer, and it is the one that allows you to write the configuration of the http server in a simple and intuitive way.

## Distribution

We distribute the library via [Clojars](https://clojars.org/com.moclojer/moclojer).

### Clojure CLI/deps.edn

```clojure
com.moclojer/moclojer {:mvn/version "0.3.1"}
```

### Leiningen/Boot

```clojure
[com.moclojer/moclojer "0.3.1"]
```

### [`git`](https://clojure.org/guides/deps_and_cli#_using_git_libraries) in `deps.edn`

```edn
{:deps
 {com.moclojer/moclojer {:git/url "https://github.com/moclojer/moclojer.git"
                         :git/tag "v0.3.1"
                         :git/sha "c4ca0f2cfcfbe47de6eb0c601b26106190e20793"}}}
```

## Example

```clj
(ns my-app.core
  (:require [com.moclojer.adapters :as adapters]
            [com.moclojer.server :as server]))

(def *router
  "create a router from a config map"
  (adapters/generate-routes
   [{:endpoint
     {:method "GET"
      :path "/example"
      :response {:status 200
                 :headers {:Content-Type "application/json"}
                 :body {:id 123}}}}]))

(defn -main
  "start the server"
  [& args]
  (server/start-server! *router))
```
