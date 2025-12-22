---
description: >-
  Use EDN (Extensible Data Notation) format for moclojer configuration. Native Clojure syntax
  with advanced features, code evaluation, and seamless integration with Clojure projects.
---

# EDN Format Guide

EDN (Extensible Data Notation) is moclojer's native configuration format. As a Clojure-based tool, moclojer has first-class support for EDN, offering the most powerful and flexible configuration option.

## 📋 What You'll Learn

- ✅ EDN syntax basics and advantages
- ✅ How to write moclojer configs in EDN
- ✅ Advanced features (code evaluation, data structures)
- ✅ When to use EDN vs YAML/OpenAPI
- ✅ Integration with Clojure projects

## 🎯 Why Use EDN?

### Advantages

✅ **Native format** - Direct Clojure data structures
✅ **Code evaluation** - Execute Clojure expressions
✅ **Rich data types** - Keywords, symbols, sets, etc.
✅ **Comments** - Full line and inline comments
✅ **No ambiguity** - Precise type system
✅ **Clojure integration** - Use in your Clojure codebase

### When to Choose EDN

- ✅ You're using Clojure/ClojureScript
- ✅ Need advanced templating features
- ✅ Want to evaluate code in responses
- ✅ Prefer precision over readability
- ✅ Building programmatic configurations

## 🚀 Quick Start

### Basic EDN Configuration

Create `moclojer.edn`:

```clojure
[{:endpoint
  {:method :get
   :path "/hello"
   :response
   {:status 200
    :headers {"Content-Type" "application/json"}
    :body "{\"message\": \"Hello from EDN!\"}"}}}]
```

**Run:**

```bash
moclojer --config moclojer.edn
```

**Test:**

```bash
curl http://localhost:8000/hello
# {"message": "Hello from EDN!"}
```

## 📖 EDN Syntax

### Data Types

```clojure
;; Strings
"Hello World"

;; Numbers
42
3.14

;; Keywords (common for keys)
:status
:method
:get

;; Symbols
status
method

;; Booleans
true
false

;; Nil
nil

;; Vectors (ordered)
[1 2 3]
["GET" "POST" "PUT"]

;; Maps (key-value)
{:name "Alice"
 :age 30}

;; Sets (unique values)
#{:get :post :put}
```

### Complete Example

```clojure
;; EDN configuration for User API
[
 ;; Health check endpoint
 {:endpoint
  {:method :get
   :path "/health"
   :response
   {:status 200
    :headers {"Content-Type" "application/json"}
    :body "{\"status\": \"ok\", \"service\": \"user-api\"}"}}}

 ;; List users with pagination
 {:endpoint
  {:method :get
   :path "/users"
   :response
   {:status 200
    :headers {"Content-Type" "application/json"
              "X-Total-Count" "100"}
    :body (str "{"
               "\"users\": ["
               "  {\"id\": 1, \"name\": \"Alice\"},"
               "  {\"id\": 2, \"name\": \"Bob\"}"
               "],"
               "\"page\": \"{{query-params.page}}\","
               "\"total\": 100"
               "}")}}}

 ;; Get specific user
 {:endpoint
  {:method :get
   :path "/users/:id"
   :response
   {:status 200
    :headers {"Content-Type" "application/json"}
    :body "{\"id\": {{path-params.id}}, \"name\": \"User {{path-params.id}}\"}"}}}

 ;; Create user
 {:endpoint
  {:method :post
   :path "/users"
   :response
   {:status 201
    :headers {"Content-Type" "application/json"
              "Location" "/users/999"}
    :body "{\"id\": 999, \"name\": \"{{json-params.name}}\", \"email\": \"{{json-params.email}}\"}"}}}

 ;; Error response
 {:endpoint
  {:method :get
   :path "/users/999"
   :response
   {:status 404
    :headers {"Content-Type" "application/json"}
    :body "{\"error\": \"User not found\", \"code\": \"USER_NOT_FOUND\"}"}}}
]
```

## 🎓 Advanced Features

### 1. Comments

```clojure
[
 ;; This is a single-line comment
 {:endpoint
  {:method :get
   :path "/hello" ;; inline comment
   :response
   {:status 200
    :body "Hello"}}}

 ;; Comments can explain complex logic
 ;; or document why certain decisions were made
]
```

### 2. Multi-line Strings

```clojure
{:endpoint
 {:method :get
  :path "/users"
  :response
  {:status 200
   :body (str "{"
              "  \"users\": ["
              "    {\"id\": 1, \"name\": \"Alice\"},"
              "    {\"id\": 2, \"name\": \"Bob\"}"
              "  ],"
              "  \"total\": 2"
              "}")}}}
```

### 3. Code Evaluation

```clojure
;; Generate dynamic timestamps
{:endpoint
 {:method :get
  :path "/time"
  :response
  {:status 200
   :body (str "{\"timestamp\": \"" (java.time.Instant/now) "\"}")}}}

;; Calculate values
{:endpoint
 {:method :get
  :path "/calc"
  :response
  {:status 200
   :body (str "{\"result\": " (* 10 5) "}")}}}
```

### 4. Shared Data

```clojure
(def base-headers
  {"Content-Type" "application/json"
   "X-API-Version" "1.0"})

[{:endpoint
  {:method :get
   :path "/users"
   :response
   {:status 200
    :headers base-headers
    :body "{}"}}}

 {:endpoint
  {:method :get
   :path "/posts"
   :response
   {:status 200
    :headers base-headers
    :body "{}"}}}]
```

### 5. Template Variables (Same as YAML)

```clojure
{:endpoint
 {:method :get
  :path "/users/:id"
  :response
  {:status 200
   :body "{\"id\": {{path-params.id}}, \"name\": \"User {{path-params.id}}\"}"}}}

{:endpoint
 {:method :get
  :path "/search"
  :response
  {:status 200
   :body "{\"query\": \"{{query-params.q}}\", \"results\": []}"}}}

{:endpoint
 {:method :post
  :path "/users"
  :response
  {:status 201
   :body "{\"name\": \"{{json-params.name}}\", \"email\": \"{{json-params.email}}\"}"}}}
```

## 📊 EDN vs YAML Comparison

| Feature | EDN | YAML |
|---------|-----|------|
| **Readability** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Precision** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Code evaluation** | ✅ Yes | ❌ No |
| **Comments** | ✅ Yes | ✅ Yes |
| **Native to moclojer** | ✅ Yes | ❌ No |
| **Clojure integration** | ⭐⭐⭐⭐⭐ | ⭐ |
| **Learning curve** | ⭐⭐⭐ (Medium) | ⭐⭐ (Easy) |
| **Best for** | Clojure devs, advanced features | Beginners, simplicity |

## ✅ Best Practices

### Do ✅

```clojure
;; Use keywords for keys
{:method :get
 :path "/users"
 :status 200}

;; Comment your intentions
;; This endpoint handles user authentication
{:endpoint {:method :post :path "/login"}}

;; Group related endpoints
[
 ;; === USER ENDPOINTS ===
 {:endpoint {:path "/users"}}
 {:endpoint {:path "/users/:id"}}

 ;; === POST ENDPOINTS ===
 {:endpoint {:path "/posts"}}
]

;; Use def for shared values
(def common-headers {"Content-Type" "application/json"})
```

### Don't ❌

```clojure
;; Don't use strings for keys when keywords work
{"method" "get"  ; ❌ Avoid
 "path" "/users"}

;; Better:
{:method :get    ; ✅ Preferred
 :path "/users"}

;; Don't duplicate data
{:endpoint {:headers {"Content-Type" "application/json"}}}  ; ❌
{:endpoint {:headers {"Content-Type" "application/json"}}}  ; ❌

;; Better: use def
(def json-headers {"Content-Type" "application/json"})  ; ✅
{:endpoint {:headers json-headers}}
{:endpoint {:headers json-headers}}
```

## 🔧 Usage in Clojure Projects

### As a Library

```clojure
(ns myapp.mocks
  (:require [com.moclojer.adapters :as adapters]))

(def config
  [{:endpoint
    {:method :get
     :path "/api/users"
     :response
     {:status 200
      :body "{\"users\": []}"}}}])

;; Start mock server
(def server
  (adapters/create-and-start-server!
    {:endpoints config
     :port 8000}))

;; Stop server
(adapters/stop-server! server)
```

### Loading from File

```clojure
(ns myapp.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn load-mock-config [filename]
  (-> filename
      io/resource
      slurp
      edn/read-string))

(def endpoints (load-mock-config "mocks.edn"))
```

## 🔍 Troubleshooting

### Syntax Errors

**Problem:** `Invalid EDN`

**Solution:** Check for:

- Unclosed brackets/braces `[ { } ]`
- Missing commas in maps (EDN uses whitespace)
- Invalid characters in keywords

**Valid:**

```clojure
{:key "value"}  ; ✅ Correct
{:key, "value"} ; ✅ Also works (comma optional)
```

### Template Variables Not Working

**Problem:** `{{path-params.id}}` shows literally

**Solution:** Template variables work in EDN strings:

```clojure
;; ✅ Correct - in string
{:body "{\"id\": {{path-params.id}}}"}

;; ❌ Wrong - not in string
{:body {{path-params.id}}}
```

## 📚 See Also

- **[YAML Format Guide](yaml-format.md)** - Simpler alternative format
- **[OpenAPI Format](openapi-format.md)** - Industry standard specs
- **[Postman Format](postman-format.md)** - Postman Collection support
- **[Template Variables](../templates/template-variables.md)** - Dynamic responses
- **[Using as Library](../../framework/using-as-library.md)** - Clojure integration

## 🚀 Next Steps

- **[Path Parameters](../parameters/path-parameters.md)** - Dynamic URL parameters
- **[Query Parameters](../parameters/query-parameters.md)** - Filters and pagination
- **[Template System](../templates/template-system.md)** - Advanced templating

---

**💡 Tip:** EDN is perfect for Clojure developers who want the full power of Clojure's data structures and code evaluation in their mock configurations!
